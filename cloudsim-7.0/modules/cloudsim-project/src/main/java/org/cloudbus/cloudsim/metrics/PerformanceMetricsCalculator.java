package org.cloudbus.cloudsim.metrics;

import org.cloudbus.cloudsim.Cloudlet;

import java.util.List;

public class PerformanceMetricsCalculator {
    public double calculateExecutionTime(List<Cloudlet> cloudlets) {
        if (cloudlets.isEmpty()) return 0.0;

        double totalTime = 0.0;
        for (Cloudlet cloudlet : cloudlets) {
            totalTime += cloudlet.getFinishTime() - cloudlet.getExecStartTime();
        }
        return totalTime / cloudlets.size();
    }
}
