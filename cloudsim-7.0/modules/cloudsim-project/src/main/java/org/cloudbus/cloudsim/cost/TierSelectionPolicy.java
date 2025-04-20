package org.cloudbus.cloudsim.cost;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;

public interface TierSelectionPolicy {
    void initialize(List<Vm> vmList);
    String selectTier(Cloudlet cloudlet);
    List<Vm> getVmsForTier(String tier);
}
