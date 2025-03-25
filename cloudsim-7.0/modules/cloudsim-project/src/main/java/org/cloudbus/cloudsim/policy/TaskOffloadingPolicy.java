package org.cloudbus.cloudsim.policy;

import org.cloudbus.cloudsim.Cloudlet;

public class TaskOffloadingPolicy {
    public String determineExecutionTier(Cloudlet cloudlet) {
        // TODO: Implement offloading decision logic
        // Factors to consider:
        // 1) Task Computing requirements,
        // 2) Network Conditions
        // 3) Device Capabilities
        // 4) Cost
        // 5) Energy Consumption
        double taskSize = cloudlet.getCloudletLength();

        if (taskSize < 1000) return "device";
        if (taskSize < 5000) return "edge";
        return "cloud";
    }
}
