package org.cloudbus.cloudsim.cost;

import org.cloudbus.cloudsim.Cloudlet;

public interface CostModel {
    double energy(Cloudlet c, String tier);
    double latency(Cloudlet c, String tier);
}
