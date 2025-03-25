package org.cloudbus.cloudsim.controller;

/*
 * Title:        Simulation Controller
 * Description:  The Simulation Controller is responsible for handling the simulation process. It is the main class that will be used to start the simulation.
 * Copyright (c) 2025, The University of the Philippines Diliman
 * Electrical and Electronics Engineering Institute (EEEI), Smart Systems Laboratory (SSL)
 * Authors: Cagas, Mark Nicholas; Saw, Christyne Joie
 */

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.entities.*;
import org.cloudbus.cloudsim.metrics.*;
import org.cloudbus.cloudsim.model.*;
import org.cloudbus.cloudsim.policy.*;
import org.cloudbus.cloudsim.util.*;

import java.util.*;

public class SimulationController {
    public static DatacenterBroker broker;
    private static List<Vm> vmList;
    private static Map<String, List<Cloudlet>> tierResults;

    public static void main(String[] args) {
        initializeSimulation();
        List<Cloudlet> cloudlets = setupCloudlets();
        runSimulation(cloudlets);
        analyzeResults();
    }

    private static void initializeSimulation() {
        int num_user = 1; // Number of cloud users
        Calendar calendar = Calendar.getInstance(); // Calendar whose fields have been initialized with the current date and time.
        boolean trace_flag = false; // Disable detailed trace unless debugging

        CloudSim.init(num_user, calendar, trace_flag);

        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        int brokerId = broker.getId();

        // Tier Setup
        Datacenter deviceTier = DeviceDatacenter.createDeviceDatacenter();
        Datacenter edgeTier = DeviceDatacenter.createEdgeDatacenter();
        Datacenter cloudTier = DeviceDatacenter.createCloudDatacenter();

        vmList = new ArrayList<>();
        vmList.add(VmAllocationPolicyCustom.createDeviceVm(brokerId));
        vmList.add(VmAllocationPolicyCustom.createEdgeVm(brokerId));
        vmList.add(VmAllocationPolicyCustom.createCloudVm(brokerId));

        broker.submitGuestList(vmList);
    }

    private static List<Cloudlet> setupCloudlets() {
        List<CloudletData> cloudletDataList = CloudletReader.readCloudletData();
        System.out.println("Louded Cloudlet Data: " + cloudletDataList.size() + " cloudlets from JSON.");

        List<Cloudlet> cloudlets = new ArrayList<>();
        TaskOffloadingPolicy taskOffloadingPolicy = new TaskOffloadingPolicy();

        for (CloudletData data : cloudletDataList) {
            Cloudlet cloudlet = CloudletCreator.createCloudlet(data, broker.getId());
            String tier = offloadingPolicy.determineExecutionTier(cloudlet);
            cloudlet.setGuestId(getVmIdForTier(tier));
            cloudlets.add(cloudlet);
        }
        broker.submitCloudletList(cloudlets);
        return cloudlets;
    }

    private static void runSimulation(List<Cloudlet> cloudlets) {
        CloudSim.startSimulation();
        List<Cloudlet> completedCloudlets = broker.getCloudletReceivedList();
        CloudSim.stopSimulation();

        tierResults = new HashMap<>();
        tierResults.put("device", new ArrayList<>());
        tierResults.put("edge", new ArrayList<>());
        tierResults.put("cloud", new ArrayList<>());

        for (Cloudlet cloudlet : completedCloudlets) {
            String tier = getTierForVmId(cloudlet.getGuestId());
            tierResults.get(tier).add(cloudlet);
        }
    }

    private static void analyzeResults() {
        PerformanceMetricsCalculator calculator = new PerformanceMetricsCalculator();

        for (Map.Entry<String, List<Cloudlet>> entry : tierResults.entrySet()) {
            String tier = entry.getKey();
            List<Cloudlet> cloudlets = entry.getValue();

            System.out.println("\nResults for " + tier + " tier:");
            System.out.println("Number of tasks: " + tierCloudlets.size());
            System.out.println("Average Execution time: " +
                calculator.calculateExecutionTime(tierCloudlets) + " ms");
            System.out.println("Energy consumption: " +
                calculator.calculateEnergyConsumption(tierCloudlets, tier) + " J");
        }
    }

    private static int getVmIdForTier(String tier) {
        switch (tier) {
            case "device": return vmList.get(0).getId();
            case "edge": return vmList.get(1).getId();
            case "cloud": return vmList.get(2).getId();
            default:
                try {
                    throw new IllegalAccessException("Invalid tier: " + tier);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
        }
    }

    private static String getTierForVmId(int vmId) {
        if (vmId == vmList.get(0).getId()) return "device";
        if (vmId == vmList.get(1).getId()) return "edge";
        if (vmId == vmList.get(2).getId()) return "cloud";
        try {
            throw new IllegalAccessException("Invalid VM ID: " + vmId);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
