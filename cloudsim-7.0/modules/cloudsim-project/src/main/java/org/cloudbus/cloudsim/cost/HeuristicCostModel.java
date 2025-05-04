package org.cloudbus.cloudsim.cost;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.models.TieredPowerModel;

/**
 * Estimates execution latency and energy
 * via:
 *   executionTime = length (MI) / MIPS;
 *   latency = executionTime + networkRTT;
 *   energy = power(util) * executionTime.
 */

public class HeuristicCostModel implements CostModel {
    // Tier MIPS
    private static final double DEVICE_MIPS = 8000.0; // Pixel 7 total
    private static final double EDGE_MIPS = 10000.0;
    private static final double CLOUD_MIPS = 40000.0;

    private static final double LATENCY_EDGE = 0.05;
    private static final double LATENCY_CLOUD = 0.15;
    private static final double BW_UPLINK   = 10000000.0; // 10 Mbps
    private static final double BW_DOWNLINK = 100000000.0; // 100 Mbps

    private final TieredPowerModel devicePower = new TieredPowerModel("device");
    private final TieredPowerModel edgePower = new TieredPowerModel("edge");
    private final TieredPowerModel cloudPower = new TieredPowerModel("cloud");

    public double latency(Cloudlet c, String tier) {
        double length = c.getCloudletLength(); // in MI
        double executionTime;
        double netRTT; // in seconds

        switch(tier) {
            case "device" -> {
                executionTime = length / DEVICE_MIPS;
                netRTT = 0.0; // No network delay
            }
            case "edge" -> {
                executionTime = length / EDGE_MIPS;
                // executionTime = 30 / 10000 = 0.003
                netRTT = (c.getCloudletFileSize() / BW_UPLINK) + (c.getCloudletOutputSize() / BW_DOWNLINK);
//                netRTT = LATENCY_EDGE * 2;
                // netRTT = 112 / 10000000 + 1000 / 100000000 = 0.0000112 + 0.00000112 = 0.00001232
                // netRTT = 19764352 / 10000000 + 1000 / 10000000 = 1.9764352 + 0.0001 = 1.9765352
            }
            default -> {
                executionTime = length / CLOUD_MIPS;
                netRTT = ((c.getCloudletFileSize() / BW_UPLINK) + (c.getCloudletOutputSize() / BW_DOWNLINK)) * 2;
//                netRTT = LATENCY_CLOUD * 2;
            }
        }
        return executionTime + netRTT;
        // netRTT = latency_up + latency_down = (fileSize / BW_up) + (output / BW_down)
    }

    @Override
    public double energy(Cloudlet c, String tier) {
        double util = c.getUtilizationOfCpu(0.0); // assume steady-state at start
        double executionTime;
        double power;

        switch (tier) {
            case "device" -> {
                executionTime = c.getCloudletLength() / DEVICE_MIPS;
                power = devicePower.getPower(util);
            }
            case "edge" -> {
                executionTime = c.getCloudletLength() / EDGE_MIPS;
                power = edgePower.getPower(util);
            }
            default -> {
                executionTime = c.getCloudletLength() / CLOUD_MIPS;
                power = cloudPower.getPower(util);
            }
        }
        return power * executionTime;
    }
}
