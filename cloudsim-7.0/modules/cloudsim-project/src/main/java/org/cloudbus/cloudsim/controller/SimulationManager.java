package org.cloudbus.cloudsim.controller;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.cost.*;
import org.cloudbus.cloudsim.metrics.*;
import org.cloudbus.cloudsim.models.*;
import org.cloudbus.cloudsim.policies.*;
import org.cloudbus.cloudsim.utils.*;

import java.util.*;

public class SimulationManager {
    private CustomBroker broker;
    private TierSelectionPolicy tierPolicy;
    private OffloadingPolicy globalPolicy;

    private final List<CustomDatacenter> datacenters = new ArrayList<>();
    private final List<Vm> vmList = new ArrayList<>();
    private Map<String, List<Cloudlet>> tierResults = new LinkedHashMap<>();
    private Map<String,OffloadingPolicy> perTierPolicies = new HashMap<>();
    private Map<Integer,String> vmTierMap;
    private static final List<CloudletData> cloudletDataList = CloudletReader.readCloudletData();

    private final double L_MAX;
    private final int maxEpisodes;
    CostModel heuristic = new HeuristicCostModel();

    private Map<String, List<Cloudlet>> bestTierResults = null;
    private double bestEpisodeReward = Double.NEGATIVE_INFINITY;
    private int bestEpisodeNum = -1;

    public SimulationManager(OffloadingPolicy offloadingPolicy, double lmax, int maxEpisode) {
        this.globalPolicy = Objects.requireNonNull(offloadingPolicy, "offloadingPolicy");
        this.L_MAX =lmax;
        this.maxEpisodes = maxEpisode;
    }

    public void initializeSimulation() throws Exception {
        CloudSim.init(1, Calendar.getInstance(), false);

        broker = new CustomBroker("Broker");

        datacenters.clear();
        datacenters.add(CreateDatacenter.createDeviceDatacenter());
        datacenters.add(CreateDatacenter.createEdgeDatacenter());
        datacenters.add(CreateDatacenter.createCloudDatacenter());
        System.out.println("Created " + datacenters.size() + (datacenters.size() == 1 ? " Datacenter" : " Datacenters"));

        int deviceHosts = datacenters.get(0).getHostList().size();
        int edgeHosts   = datacenters.get(1).getHostList().size();
        int cloudHosts  = datacenters.get(2).getHostList().size();
        int vmsPerHost = 2;

        vmList.clear();
        vmList.addAll(CreateVm.createDeviceVms(broker.getId(), deviceHosts * 2, 0));
        vmList.addAll(CreateVm.createEdgeVms(broker.getId(),edgeHosts   * vmsPerHost,deviceHosts * vmsPerHost));
        vmList.addAll(CreateVm.createCloudVms(broker.getId(),cloudHosts  * vmsPerHost,(deviceHosts + edgeHosts) * vmsPerHost));
        System.out.println("Created " + vmList.size() + (vmList.size() == 1 ? " VM" : " VMs"));

        broker.submitGuestList(vmList);
        System.out.println("Submitted " + vmList.size() + " VMs to Broker ID: " + broker.getId());


        vmTierMap = CreateVm.getVmTierMap();
        tierResults.clear();
        for (String tier : List.of("device","edge","cloud")) {
            tierResults.put(tier, new ArrayList<>());
        }
        if (globalPolicy instanceof RLOffloadingPolicy) {
            globalPolicy.initialize(vmList);
            Map<String,OffloadingPolicy> rlMap = new HashMap<>();
            for (var tier : List.of("device", "edge", "cloud")) {
                rlMap.put(tier, globalPolicy);
            }
            broker.setTierPolicies(rlMap);
            broker.setVmTierMap(vmTierMap);
        } else {
            tierPolicy = new ConstrainedCostOptimizer(L_MAX, heuristic);
            tierPolicy.initialize(vmList);

            perTierPolicies = new HashMap<>();
            for (var tier : List.of("device","edge","cloud")) {
                OffloadingPolicy p = globalPolicy.getClass().getDeclaredConstructor().newInstance();
                if (p instanceof DynamicThrottled) {
                    ((DynamicThrottled)p).setBroker(broker);
                }
                // instantiate a fresh copy of whatever policy class was passed in:
                var tierVms = tierPolicy.getVmsForTier(tier);
                p.initialize(tierVms);
                perTierPolicies.put(tier, p);
            }
            broker.setTierPolicies(perTierPolicies);
            broker.setVmTierMap(vmTierMap);
        }
    }

    public void setupCloudlets() {
        List<Cloudlet> submittedCloudlets = new ArrayList<>();

        double maxLatency = 0.0;
        double maxEnergy = 0.0;

        for (CloudletData data : cloudletDataList) {
            Cloudlet c = CloudletCreator.createCloudlet(data, broker.getId());
            c.setUserId(broker.getId());

            int vmId;
            if (globalPolicy instanceof RLOffloadingPolicy) {
                vmId = globalPolicy.allocate(c);
                for (String tier: List.of("device", "edge", "cloud")) {
                    maxLatency = Math.max(maxLatency, heuristic.latency(c, tier));
                    maxEnergy = Math.max(maxEnergy, heuristic.energy(c, tier));
                }
            } else {
                String tier = tierPolicy.selectTier(c);
                vmId = perTierPolicies.get(tier).allocate(c);
            }
            if (vmId >= 0) {
                c.setGuestId(vmId);
                System.out.println("Cloudlet#" + c.getCloudletId() + " dispatched to VM#" + vmId);
            } else {
                System.out.println("Cloudlet#" + c.getCloudletId() + " queued (no idle VM)");
            }
            submittedCloudlets.add(c);

            if (globalPolicy instanceof RLOffloadingPolicy) {
                ((RLOffloadingPolicy) globalPolicy).setMaxValues(maxLatency, maxEnergy);
                System.out.printf("Max normalization values: latency=%.4f s, energy=%.4f J%n",
                        maxLatency, maxEnergy);
            }
        }
        broker.submitCloudletList(submittedCloudlets);
        System.out.println("Submitted Cloudlets to Broker ID: " + broker.getId());
    }

    public double runSimulation() {
        System.out.println("\n==========================\n");
        double simTime = CloudSim.startSimulation();
        List<Cloudlet> completedCloudlets = broker.getCloudletReceivedList();

        tierResults.values().forEach(List::clear);
        Map<Integer,String> tierMap = CreateVm.getVmTierMap();

        System.out.println("Cloudlets received: " + completedCloudlets.size());
        for (Cloudlet c : completedCloudlets) {
            int vmId = c.getGuestId();
            String tier = tierMap.get(vmId);
            System.out.printf("→ Cloudlet#%d ran on VM#%d (mapped to tier=%s)%n", c.getCloudletId(), vmId, tier);
            tierResults.get(tier).add(c);

            if (globalPolicy instanceof RLOffloadingPolicy) {
                globalPolicy.onCloudletCompletion(vmId, c);
                globalPolicy.deallocate(vmId);
            } else {
                OffloadingPolicy policy = perTierPolicies.get(tier);
                if (policy instanceof DynamicThrottled) {
                    policy.deallocate(vmId);
                } else {
                    policy.onCloudletCompletion(vmId, c);
                    policy.deallocate(vmId);
                }
            }
        }
        CloudSim.stopSimulation();
        System.out.println("\n==========================\n");
        return simTime;
    }

    public void runOffloadingSimulation() throws Exception {
        CloudSim.init(1, Calendar.getInstance(), false);

        boolean converged = false;
        int currentEpisode = 0;
        double cumulativeSimTime = 0.0;

        initializeSimulation();

        if (!(globalPolicy instanceof RLOffloadingPolicy)) {
            setupCloudlets();
            runSimulation();
            analyzeResults();
            return;
        }

        RLOffloadingPolicy rl = (RLOffloadingPolicy) globalPolicy;
        do {
            currentEpisode++;
            System.out.println("\n========== EPISODE " + currentEpisode + " ==========");
            rl.startNewEpisode();

            setupCloudlets();
            double thisEpisodeSim = runSimulation();
            cumulativeSimTime += thisEpisodeSim;
            System.out.printf("Episode %d simulated time: %.3f s (cumulative: %.3f s)%n", currentEpisode, thisEpisodeSim, cumulativeSimTime);

            analyzeResults();

            double episodeReward = rl.getCurrentEpisodeReward();
            if (episodeReward > bestEpisodeReward || bestEpisodeNum == Double.NEGATIVE_INFINITY) {
                bestEpisodeReward = episodeReward;
                bestEpisodeNum = currentEpisode;
                bestTierResults = new HashMap<>();
                for (var e : tierResults.entrySet()) {
                    bestTierResults.put(e.getKey(), new ArrayList<>(e.getValue()));
                }
            }
            converged = rl.hasConverged();
            if (!converged && currentEpisode < maxEpisodes) {
                broker.getCloudletReceivedList().clear();
                resetForNextEpisode();
            }
        } while (!converged && currentEpisode < maxEpisodes);

        System.out.println(converged
                ? "Q-Learning converged after " + currentEpisode + " episodes."
                : "Reached max episodes (" + maxEpisodes + ") without convergence.");

        System.out.println("Total simulated time across all episodes: " + cumulativeSimTime + " s\n");
        System.out.println("Smallest Q-change " + rl.getMinDelta());

        System.out.printf("Best episode: %d with reward %.4f%n", bestEpisodeNum, bestEpisodeReward);
        tierResults = bestTierResults;
        analyzeResults();

        System.out.println("Final Q-values:");
        rl.getQValues().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("  VM #%d → Q=%.6f%n", e.getKey(), e.getValue()));

    }

    private void resetForNextEpisode() throws Exception {
        CloudSim.stopSimulation();
        initializeSimulation();
    }

    public void analyzeResults() {
        PerformanceMetricsCalculator calculator = new PerformanceMetricsCalculator();
        double totalEnergy = 0.0;
        double simTime = 0.0;

        for (Map.Entry<String, List<Cloudlet>> entry : tierResults.entrySet()) {
            String tier = entry.getKey();
            List<Cloudlet> tierCloudlets = entry.getValue();
            System.out.println("\nResults for " + tier + " tier:");
            System.out.println("Number of tasks: " + tierCloudlets.size());
            double tierEnergy = calculator.calculateEnergyConsumption(tierCloudlets, tier);
            double executionTime = calculator.calculateExecutionTime(tierCloudlets);
            totalEnergy += tierEnergy;
            simTime += executionTime;

            System.out.println("Average Execution time: " +
                   executionTime/tierCloudlets.size() + " s");
            System.out.println("Energy consumption: " +
                    String.format("%.6f", calculator.calculateEnergyConsumption(tierCloudlets, tier)) + " J\n");
        }
        System.out.println("\nTotal Energy consumption across tiers: " + String.format("%.6f", totalEnergy) + " J");
        System.out.println("Total Execution time across tiers: " + String.format("%.6f", simTime) + " s");
    }
}