package org.cloudbus.cloudsim.models;

import org.cloudbus.cloudsim.power.models.PowerModel;

public class TieredPowerModel implements PowerModel {
    private final double idlePower;     // Power when idle (Watts)
    private final double maxPower;      // Maximum power (Watts)
    private final double staticPower;   // Static power consumption (Watts)
    private final String tierType;      // device, edge, or cloud

    public TieredPowerModel(String tierType) {
        this.tierType = tierType;
        switch (tierType) {
            case "device" -> {
                this.idlePower = 0.5;
                this.maxPower = 2.5;
                this.staticPower = 0.3;
            }
            case "edge" -> {
                this.idlePower = 120.0;
                this.maxPower = 250.0;
                this.staticPower = 100.0;
            }
            case "cloud" -> {
                this.idlePower = 200.0;
                this.maxPower = 400.0;
                this.staticPower = 180.0;
            }
            default -> throw new IllegalArgumentException("Invalid tier type");
        }
    }

    @Override
    public double getPower(double utilization) {
        if (utilization < 0 || utilization > 1) {
            throw new IllegalArgumentException("Utilization must be between 0 and 1");
        }
        // Linear power model: static power + dynamic power based on utilization
        return staticPower + (maxPower - idlePower) * utilization + idlePower;
    }
}
