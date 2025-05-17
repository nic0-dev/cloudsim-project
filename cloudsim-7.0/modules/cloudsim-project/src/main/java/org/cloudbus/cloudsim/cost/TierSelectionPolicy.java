package org.cloudbus.cloudsim.cost;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;

public interface TierSelectionPolicy {
    void initialize(List<Vm> vmList);
    // Selects a tier based on the energy and latency computation of the given cloudet
    String selectTier(Cloudlet cloudlet);
    // Returns the list of VMs for a given tier.
    List<Vm> getVmsForTier(String tier);
}
