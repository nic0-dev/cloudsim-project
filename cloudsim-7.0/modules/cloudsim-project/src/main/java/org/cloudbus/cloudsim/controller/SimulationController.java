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
import org.cloudbus.cloudsim.models.*;
import org.cloudbus.cloudsim.policies.*;
import org.cloudbus.cloudsim.utils.CloudletCreator;
import org.cloudbus.cloudsim.utils.CloudletReader;

import java.util.*;

public class SimulationController {
    public static DatacenterBroker broker;
    private static List<Vm> vmList;
    private static Map<String, List<Cloudlet>> tierResults;

    public static void main(String[] args) throws Exception {
        initializeSimulation();
        List<Cloudlet> cloudlets = setupCloudlets();
        runSimulation(cloudlets);
        analyzeResults();
    }

    private static void initializeSimulation() throws Exception {
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
        DeviceDatacenter deviceTier = createDeviceDatacenter();
//        Datacenter edgeTier = DeviceDatacenter.createEdgeDatacenter();
//        Datacenter cloudTier = DeviceDatacenter.createCloudDatacenter();

        vmList = new ArrayList<>();
        vmList.add(VmAllocationPolicyCustom.createDeviceVm(brokerId));
//        vmList.add(VmAllocationPolicyCustom.createEdgeVm(brokerId));
//        vmList.add(VmAllocationPolicyCustom.createCloudVm(brokerId));

        broker.submitGuestList(vmList);
    }

    private static List<Cloudlet> setupCloudlets() {
        List<CloudletData> cloudletDataList = CloudletReader.readCloudletData();
        System.out.println("Loaded Cloudlet Data: " + cloudletDataList.size() + " cloudlets from JSON.");

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
    }

    private static DeviceDatacenter createDeviceDatacenter() throws Exception {
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            "x86",          // architecture
            "Android",      // OS
            "Mobile",       // VMM
            getHostList(),  // hostList
            0.0,           // time zone
            5.0,           // cost per sec
            0.05,          // cost per mem
            0.001,         // cost per storage
            0.1            // cost per bw
        );
        return new DeviceDatacenter(
            "Device Tier",
            "device",
            characteristics,
            new VmAllocationPolicySimple(getHostList()),
            new ArrayList<Storage>(),
            0.1, // scheduling interval
            50.0,   //bandwidth (50 Mbps for mobile)
            20.0 // latency (20ms to edge)
        );
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
