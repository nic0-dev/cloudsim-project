package org.cloudbus.cloudsim.metrics;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.models.TieredPowerModel;

import java.util.List;
import java.util.Map;

public class PerformanceMetricsCalculator {
    private final TieredPowerModel devicePower;
    private final TieredPowerModel edgePower;
    private final TieredPowerModel cloudPower;

    public PerformanceMetricsCalculator() {
        this.devicePower = new TieredPowerModel("device");
        this.edgePower = new TieredPowerModel("edge");
        this.cloudPower = new TieredPowerModel("cloud");
    }

    public double calculateExecutionTime(List<Cloudlet> cloudlets) {
        if (cloudlets.isEmpty()) return 0.0;

        double totalTime = 0.0;
        for (Cloudlet cloudlet : cloudlets) {
            totalTime += cloudlet.getFinishTime() - cloudlet.getExecStartTime();
        }
        return totalTime / cloudlets.size();
    }

    public double calculateEnergyConsumption(List<Cloudlet> cloudlets, String tier) {
        double totalEnergy = 0.0;
        TieredPowerModel powerModel = switch (tier) {
            case "device" -> devicePower;
            case "edge"   -> edgePower;
            case "cloud"  -> cloudPower;
            default -> throw new IllegalArgumentException("Invalid tier: " + tier);
        };

        for (Cloudlet cloudlet : cloudlets) {
            double executionTime = cloudlet.getExecFinishTime() - cloudlet.getExecStartTime();
            double utilization = cloudlet.getUtilizationOfCpu(cloudlet.getExecStartTime());
            double power = powerModel.getPower(utilization);
            double energy = power * executionTime;
            totalEnergy += energy;
        }
        return totalEnergy;
    }

    public double calculateTotalEnergyConsumption(Map<String, List<Cloudlet>> tierResults) {
        double totalEnergy = 0.0;
        for (Map.Entry<String, List<Cloudlet>> entry : tierResults.entrySet()) {
            totalEnergy += calculateEnergyConsumption(entry.getValue(), entry.getKey());
        }
        return totalEnergy;
    }
}
