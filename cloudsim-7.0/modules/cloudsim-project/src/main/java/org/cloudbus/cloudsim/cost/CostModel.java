package org.cloudbus.cloudsim.cost;

import org.cloudbus.cloudsim.Cloudlet;

public interface CostModel {
    // Calculates the energy cost of executing a Cloudlet on a given tier.
    double energy(Cloudlet c, String tier);
    // Calculates the latency cost of executing a Cloudlet on a given tier.
    double latency(Cloudlet c, String tier);
}
