package org.cloudbus.cloudsim.cost;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.models.TieredPowerModel;
import org.cloudbus.cloudsim.utils.CloudletReader;

/**
 * Estimates execution latency and energy
 * via:
 *   executionTime = length (MI) / MIPS;
 *   latency = executionTime + networkRTT;
 *   energy = power(util) * executionTime.
 */
public class HeuristicCostModel implements CostModel {
    private static final double DEVICE_MIPS = 1200.0; // Pixel 7 total
    private static final double EDGE_MIPS   = 8000.0;
    private static final double CLOUD_MIPS  = 20000.0;

    private static final double BW_UPLINK   = 10000000.0; // 10 Mbps
    private static final double BW_DOWNLINK = 100000000.0; // 100 Mbps

    private final TieredPowerModel devicePower = new TieredPowerModel("device");
    private final TieredPowerModel edgePower   = new TieredPowerModel("edge");
    private final TieredPowerModel cloudPower  = new TieredPowerModel("cloud");

    /**
     * Calculates the latency of a Cloudlet on a given tier.
     * @param c Cloudlet to be executed
     * @param tier Tier where the Cloudlet will be executed
     * @return Latency in seconds
     */
    public double latency(Cloudlet c, String tier) {
        double length = c.getCloudletLength(); // in MI
        double executionTime; // in seconds
        double netRTT; // in seconds

        switch(tier) {
            case "device" -> {
                executionTime = length / DEVICE_MIPS;
                netRTT = 0.0; // No network delay
            }
            case "edge" -> {
                executionTime = length / EDGE_MIPS;
                netRTT = (c.getCloudletFileSize() / BW_UPLINK) + (c.getCloudletOutputSize() / BW_DOWNLINK);
            }
            default -> {
                executionTime = length / CLOUD_MIPS;
                netRTT = ((c.getCloudletFileSize() / BW_UPLINK) + (c.getCloudletOutputSize() / BW_DOWNLINK)) * 2;
            }
        }
        return executionTime + netRTT;
    }

    /**
     * Calculates the energy consumption of a Cloudlet on a given tier.
     * @param c Cloudlet to be executed
     * @param tier Tier where the Cloudlet will be executed
     * @return Energy consumption in Joules
     */
    @Override
    public double energy(Cloudlet c, String tier) {
        double executionTime, power;
        double minLength = CloudletReader.getMinLength();
        double maxLength = CloudletReader.getMaxLength();
        double util = (c.getCloudletLength() - minLength) / (maxLength - minLength);

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
