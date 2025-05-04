package org.cloudbus.cloudsim.controller;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.cost.*;
import org.cloudbus.cloudsim.metrics.*;
import org.cloudbus.cloudsim.models.*;
import org.cloudbus.cloudsim.policies.*;
import org.cloudbus.cloudsim.policies.VmAllocationPolicy;
import org.cloudbus.cloudsim.utils.*;

import java.util.*;

public class SimulationManager {
    private DatacenterBroker broker;
    private final List<CustomDatacenter> datacenters = new ArrayList<>();
    private final List<Vm> vmList = new ArrayList<>();
    private final Map<String, List<Cloudlet>> tierResults = new HashMap<>();

    private TierSelectionPolicy tierPolicy;
    private Map<String,VmAllocationPolicy> vmAllocMap;

    // Maximum allowed round-trip latency in seconds
    private static final double L_MAX = 0.1; // #TODO: decrease

    public void initializeSimulation() throws Exception {
        int num_user = 1;
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false;

        CloudSim.init(num_user, calendar, trace_flag);

        broker = new DatacenterBroker("Broker");
        int brokerId = broker.getId();
        System.out.println("Broker ID: " + brokerId);

        datacenters.add(CreateDatacenter.createDeviceDatacenter());
        datacenters.add(CreateDatacenter.createEdgeDatacenter());
        datacenters.add(CreateDatacenter.createCloudDatacenter());
        System.out.println("Created " + datacenters.size() + (datacenters.size() == 1 ? " Datacenter" : " Datacenters"));

        vmList.addAll(CreateVm.createDeviceVms(brokerId, 5));
        vmList.addAll(CreateVm.createEdgeVms(brokerId, 5));
        vmList.addAll(CreateVm.createCloudVms(brokerId, 5));
        System.out.println("Created " + vmList.size() + (vmList.size() == 1 ? " VM" : " VMs"));

        broker.submitGuestList(vmList);
        System.out.println("Submitted " + vmList.size() + " VMs to Broker ID: " + broker.getId());

        CostModel heuristic = new HeuristicCostModel();
        tierPolicy = new ConstrainedCostOptimizer(L_MAX, heuristic);
        tierPolicy.initialize(vmList);

        vmAllocMap = new HashMap<>();
        for (String tier : List.of("device","edge","cloud")) {
            List<Vm> tierVms = tierPolicy.getVmsForTier(tier);
            VmAllocationPolicy offload = new StaticEqualDistribution();
//             VmAllocationPolicy offload = new DynamicThrottled();
            offload.initialize(tierVms);
            vmAllocMap.put(tier, offload);
        }
    }

    /**
     * Bind & submit all cloudlets
     */
    public void setupCloudlets() {
        List<Cloudlet> submittedCloudlets = new ArrayList<>();
        List<CloudletData> cloudletDataList = CloudletReader.readCloudletData();

        System.out.println("\n");
        for (CloudletData data : cloudletDataList) {
            Cloudlet c = CloudletCreator.createCloudlet(data, broker.getId());
            c.setUserId(broker.getId());

            String tier = tierPolicy.selectTier(c);
            VmAllocationPolicy offload = vmAllocMap.get(tier);
            int vmId = offload.allocate(c);
            c.setGuestId(vmId);
            submittedCloudlets.add(c);
        }
        broker.submitCloudletList(submittedCloudlets);
        System.out.println("Submitted Cloudlets to Broker ID: " + broker.getId());
    }

    /** Run, deallocate on completion, bucket results by tier. */
    public void runSimulation() {
        System.out.println("\n==========================\n");
        CloudSim.startSimulation();
        List<Cloudlet> completedCloudlets = broker.getCloudletReceivedList();

        System.out.println("Cloudlets received: " + completedCloudlets.size());
        tierResults.clear();
        for (String t : List.of("device","edge","cloud")) {
            tierResults.put(t, new ArrayList<>());
        }

        var tierMap = CreateVm.getVmTierMap();
        for (Cloudlet c : completedCloudlets) {
            int vmId = c.getGuestId();
            String tier = tierMap.get(vmId);
            // free the VM slot
            vmAllocMap.get(tier).deallocate(vmId);
            // record
            tierResults.get(tier).add(c);
        }

        CloudSim.stopSimulation();
        System.out.println("\n==========================\n");
    }

    public void analyzeResults() {
        PerformanceMetricsCalculator calculator = new PerformanceMetricsCalculator();
        double totalEnergy = 0.0;

        for (Map.Entry<String, List<Cloudlet>> entry : tierResults.entrySet()) {
            String tier = entry.getKey();
            List<Cloudlet> tierCloudlets = entry.getValue();
            double tierEnergy = calculator.calculateEnergyConsumption(tierCloudlets, tier);
            totalEnergy += tierEnergy;

            System.out.println("\nResults for " + tier + " tier:");
            System.out.println("Number of tasks: " + tierCloudlets.size());
            System.out.println("Average Execution time: " +
                    calculator.calculateExecutionTime(tierCloudlets) + " s");
            System.out.println("Energy consumption: " +
                    String.format("%.6f", calculator.calculateEnergyConsumption(tierCloudlets, tier)) + " J");
        }
        System.out.println("\nTotal Energy consumption: " + String.format("%.6f", totalEnergy) + " J");
    }
}