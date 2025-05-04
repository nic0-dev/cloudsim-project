package org.cloudbus.cloudsim.cost;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.utils.CreateVm;

import java.util.*;

/**
 * Tier selection policy that
 *   minimize E(c, tier)
 *   subject to T(c, tier) <= L_MAX
 * among {device, edge, cloud}.
 */

public class ConstrainedCostOptimizer implements TierSelectionPolicy {
    private final double L_MAX;
    private final CostModel costModel;
    private Map<String,List<Vm>> vmsByTier;

    /**
     * @param L_MAX Maximum allowed round-trip latency in seconds
     * @param costModel Cost model for energy and latency calculations
     */
    public ConstrainedCostOptimizer(double L_MAX, CostModel costModel) {
        this.L_MAX = L_MAX;
        this.costModel = Objects.requireNonNull(costModel, "costModel");
    }

    /**
     * Builds a map of tier->List<VM> using your VmAllocation.vmTierMap.
     */
    @Override
    public void initialize(List<Vm> vmList) {
        if (vmList == null || vmList.isEmpty()) {
            throw new IllegalArgumentException("VM list must be non‑null and non‑empty");
        }
        Map<Integer, String> tierMap = CreateVm.getVmTierMap();
        vmsByTier = new HashMap<>();
        for (Vm vm: vmList) {
            String tier = tierMap.get(vm.getId());
            if (tier == null) {
                throw new IllegalStateException("VM #" + vm.getId() + " has no tier entry in CreateVm.vmTierMap");
            }
            vmsByTier.computeIfAbsent(tier, k -> new ArrayList<>()).add(vm);
        }
    }

    /**
     * Examines each tier, discards those whose latency > L_MAX,
     * and picks the one with minimal energy.
     * @return one of {"device","edge","cloud"}
     */
    @Override
    public String selectTier(Cloudlet cloudlet) {
        String selectedTier = "device";
        double minEnergy = Double.MAX_VALUE;
        List<String> tiers = List.of("device", "edge", "cloud");

        for (String tier : tiers) {
            double latency = costModel.latency(cloudlet, tier);
            if (latency <= L_MAX) {
                double energy = costModel.energy(cloudlet, tier);
                if (energy < minEnergy) {
                    minEnergy = energy;
                    selectedTier = tier;
                }
            }
        }

        return selectedTier;
    }

    /**
     * After selectTier(), SimulationManager will call getVmsForTier(...) and then
     * hand that VM list to a VmAllocationPolicy (static/dynamic) to pick a concrete VM.
     */
    @Override
    public List<Vm> getVmsForTier(String tier) {
        return Collections.unmodifiableList(
                vmsByTier.getOrDefault(tier, Collections.emptyList())
        );
    }
}
