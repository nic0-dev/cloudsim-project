package org.cloudbus.cloudsim.controller;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.entities.*;
import org.cloudbus.cloudsim.metrics.*;
import org.cloudbus.cloudsim.models.*;
import org.cloudbus.cloudsim.policies.*;
import org.cloudbus.cloudsim.utils.*;

import java.util.*;

public class SimulationManager {
    private DatacenterBroker broker;
    private final List<Datacenter> datacenters;
    private final List<Vm> vmList;
    private final Map<String, List<Cloudlet>> tierResults;

    public SimulationManager() {
        datacenters = new ArrayList<>();
        vmList = new ArrayList<>();
        tierResults = new HashMap<>();
    }

    public void initializeSimulation() throws Exception {
        int num_user = 1;
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false;

        CloudSim.init(num_user, calendar, trace_flag);

        broker = new DatacenterBroker("Broker");
        int brokerId = broker.getId();

        datacenters.add(CreateDatacenter.createDeviceDatacenter());
        datacenters.add(CreateDatacenter.createEdgeDatacenter());
        datacenters.add(CreateDatacenter.createCloudDatacenter());

        vmList.add(VmAllocationPolicyCustom.createDeviceVm(brokerId));
        vmList.add(VmAllocationPolicyCustom.createEdgeVm(brokerId));
        vmList.add(VmAllocationPolicyCustom.createCloudVm(brokerId));

        broker.submitGuestList(vmList);
    }

    public List<Cloudlet> setupCloudlets() {
        List<CloudletData> cloudletDataList = CloudletReader.readCloudletData();
        List<Cloudlet> cloudlets = new ArrayList<>();
        TaskOffloadingPolicy taskOffloadingPolicy = new TaskOffloadingPolicy();

        for (CloudletData data : cloudletDataList) {
            Cloudlet cloudlet = CloudletCreator.createCloudlet(data, broker.getId());
            String tier = taskOffloadingPolicy.determineExecutionTier(cloudlet);
            cloudlet.setGuestId(getVmIdForTier(tier));
            cloudlets.add(cloudlet);
        }
        broker.submitCloudletList(cloudlets);
        return cloudlets;
    }

    public void runSimulation(List<Cloudlet> cloudlets) {
        CloudSim.startSimulation();
        List<Cloudlet> completedCloudlets = broker.getCloudletReceivedList();
        tierResults.put("device", new ArrayList<>());
        tierResults.put("edge", new ArrayList<>());
        tierResults.put("cloud", new ArrayList<>());

        for (Cloudlet cloudlet : completedCloudlets) {
            String tier = getTierForVmId(cloudlet.getGuestId());
            tierResults.get(tier).add(cloudlet);
        }
        CloudSim.stopSimulation();
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
                    calculator.calculateExecutionTime(tierCloudlets) + " ms");
            System.out.println("Energy consumption: " +
                    calculator.calculateEnergyConsumption(tierCloudlets, tier) + " J");
        }
        System.out.println("Total Energy consumption: " + totalEnergy + " J");
    }

    private int getVmIdForTier(String tier) {
        return switch (tier) {
            case "device" -> vmList.get(0).getId();
            case "edge" -> vmList.get(1).getId();
            case "cloud" -> vmList.get(2).getId();
            default -> throw new IllegalArgumentException("Invalid tier: " + tier);
        };
    }

    private String getTierForVmId(int vmId) {
        if (vmId == vmList.get(0).getId()) return "device";
        if (vmId == vmList.get(1).getId()) return "edge";
        if (vmId == vmList.get(2).getId()) return "cloud";
        throw new IllegalArgumentException("Invalid VM ID: " + vmId);
    }
}