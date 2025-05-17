package org.cloudbus.cloudsim.metrics;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.models.TieredPowerModel;

import java.util.List;

public class PerformanceMetricsCalculator {
    private final TieredPowerModel devicePower;
    private final TieredPowerModel edgePower;
    private final TieredPowerModel cloudPower;

    public PerformanceMetricsCalculator() {
        this.devicePower = new TieredPowerModel("device");
        this.edgePower = new TieredPowerModel("edge");
        this.cloudPower = new TieredPowerModel("cloud");
    }

    /***
     * Calculates the execution time of a list of Cloudlets.
     * @param cloudlets
     * @return the total time taken to execute all the Cloudlets
     */
    public double calculateExecutionTime(List<Cloudlet> cloudlets) {
        if (cloudlets.isEmpty()) return 0.0;

        double totalTime = 0.0;
        for (Cloudlet cloudlet : cloudlets) {
            double executionTime = cloudlet.getExecFinishTime() - cloudlet.getExecStartTime();
            System.out.print("Cloudlet #" + cloudlet.getCloudletId() + " \t| Finish Time: " + String.format("%.6f", cloudlet.getExecFinishTime()));
            System.out.println("s\t| Start Time: " + String.format("%.6f", cloudlet.getExecStartTime()) + "s\t\t| Execution Time: " + String.format("%.6f", executionTime) + "s |");
            totalTime += cloudlet.getExecFinishTime() - cloudlet.getExecStartTime();
        }
        return totalTime;
    }

    /***
     * Calculates the energy consumption of a list of Cloudlets.
     * @param cloudlets
     * @param tier
     * @return the total energy consumed by all the Cloudlets
     */
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
}
