package org.cloudbus.cloudsim.policies;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.models.TieredPowerModel;

public class TaskOffloadingPolicy {
    // Maximum acceptable latency in seconds
    private static final double L_MAX = 3S.0;
    // Network latency for offloading to higher tiers
    private static final double LATENCY_TO_EDGE = 0.05;
    private static final double LATENCY_TO_CLOUD = 0.15;
    // Computing capacity in MIPS
    private static final double DEVICE_MIPS = 8000;
    private static final double EDGE_MIPS = 2500;
    private static final double CLOUD_MIPS = 5000;

    private final TieredPowerModel devicePower;
    private final TieredPowerModel edgePower;
    private final TieredPowerModel cloudPower;

    public TaskOffloadingPolicy() {
        this.devicePower = new TieredPowerModel("device");
        this.edgePower = new TieredPowerModel("edge");
        this.cloudPower = new TieredPowerModel("cloud");
    }

    public String determineExecutionTier(Cloudlet cloudlet) {
        double taskSize = cloudlet.getCloudletLength();
        double deviceExecutionTime = taskSize / DEVICE_MIPS;
        double edgeExecutionTime = taskSize / EDGE_MIPS;
        double cloudExecutionTime = taskSize / CLOUD_MIPS;

        double deviceLatency = deviceExecutionTime;
        double edgeLatency = edgeExecutionTime + LATENCY_TO_EDGE * 2; // Round trip
        double cloudLatency = cloudExecutionTime + LATENCY_TO_CLOUD * 2; // Round trip

        double utilization = cloudlet.getUtilizationOfCpu(cloudlet.getExecStartTime());
        double deviceEnergy = devicePower.getPower(utilization) * deviceExecutionTime;
        double edgeEnergy = edgePower.getPower(utilization) * edgeExecutionTime;
        double cloudEnergy = cloudPower.getPower(utilization) * cloudExecutionTime;

        String selectedTier = "cloud"; // Default to cloud if no tier meets constraint
        double minEnergy = Double.MAX_VALUE;

        if (deviceLatency <= L_MAX && deviceEnergy < minEnergy) {
            selectedTier = "device";
            minEnergy = deviceEnergy;
        }
        if (edgeLatency <= L_MAX && edgeEnergy < minEnergy) {
            selectedTier = "edge";
            minEnergy = edgeEnergy;
        }
        if (cloudLatency <= L_MAX && cloudEnergy < minEnergy) {
            selectedTier = "cloud";
            minEnergy = cloudEnergy;
        }
        System.out.println("Cloudlet #" + cloudlet.getCloudletId() + " selected tier: " + selectedTier);
        return selectedTier;
    }
}
