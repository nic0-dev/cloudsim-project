package org.cloudbus.cloudsim.models;

import org.cloudbus.cloudsim.power.models.PowerModel;

/**
 * A power model that estimates the power consumption of a device, edge, or cloud tier.
 * The model is based on a linear interpolation between idle and maximum power consumption.
 */
public class TieredPowerModel implements PowerModel {
    private final double overheadPower;
    private final double idlePower;
    private final double maxPower;

    public TieredPowerModel(String tierType) {
        switch (tierType) {
            case "device" -> {
                overheadPower = 0.0;
                idlePower = 2.0;
                maxPower = 3.0;
            }
            case "edge" -> {
                overheadPower = 4.0;
                idlePower = 12.0;
                maxPower = 20.0;
            }
            case "cloud" -> {
                overheadPower = 10.0;
                idlePower = 15.0;
                maxPower = 30.0;
            }
            default -> throw new IllegalArgumentException("Invalid tier type: " + tierType);
        }
    }

    @Override
    public double getPower(double utilization) {
        if (utilization < 0.0 || utilization > 1.0) {
            throw new IllegalArgumentException("Utilization must be between 0 and 1");
        }
        // Linear interpolation between idlePower and maxPower
        // P(u) = P_idle + (P_max - P_idle) * u
        double cpuDraw = idlePower + (maxPower - idlePower) * utilization;
        // The overheadPower is added to the CPU draw to account for the static power consumption
        return overheadPower + cpuDraw;
    }
}
