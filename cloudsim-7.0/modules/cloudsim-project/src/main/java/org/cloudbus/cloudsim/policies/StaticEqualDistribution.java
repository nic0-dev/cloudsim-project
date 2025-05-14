package org.cloudbus.cloudsim.policies;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.*;

/**
 * Static offloading policy: distributes cloudlets evenly across a fixed VM list
 * by always assigning to the VM with the fewest previous allocations.
 */
public class StaticEqualDistribution implements OffloadingPolicy {
    private List<Vm> vmList;
    private Map<Integer, Integer> allocationCounts;

    /**
     * Initializes internal state with the available VMs.
     * @param vmList list of VMs to distribute tasks across
     */
    @Override
    public void initialize(List<Vm> vmList) {
        if (vmList == null || vmList.isEmpty()) {
            throw new IllegalArgumentException("VM list must be non-null and non-empty");
        }
        this.vmList = new ArrayList<>(vmList);
        allocationCounts = new HashMap<>();
        for (Vm vm : vmList) {
            allocationCounts.put(vm.getId(), 0);
        }
    }

    /**
     * Allocates the cloudlet to the VM with the fewest previous assignments.
     * If multiple VMs are tied, chooses the one with the lowest index.
     * @param cloudlet the task to allocate
     * @return the chosen VM's id, or -1 if no VMs exist
     */
    @Override
    public int allocate(Cloudlet cloudlet) {
        if (vmList == null) {
            throw new IllegalStateException("StaticEqualDistribution not initialized");
        }
        if (vmList.isEmpty()) {
            return -1;
        }
        // Find VM with minimum allocation count
        int selectedVmId = vmList.get(0).getId();
        int minCount = allocationCounts.get(selectedVmId);
        for (Vm vm : vmList) {
            int vmId = vm.getId();
            int count = allocationCounts.get(vmId);
            if (count < minCount) {
                minCount = count;
                selectedVmId = vmId;
            }
        }
        allocationCounts.put(selectedVmId, minCount + 1);
        return selectedVmId;
    }

    /**
     * Deallocates after completion (decrement count)
     * @param vmId id of VM that completed the cloudlet
     */
    @Override
    public void deallocate(int vmId) {
        if (allocationCounts == null) {
            throw new IllegalStateException("StaticEqualDistribution not initialized");
        }
        allocationCounts.computeIfPresent(vmId, (id, count) -> Math.max(0, count - 1));
    }

    @Override
    public void onCloudletCompletion(int vmId, Cloudlet cloudlet) {
        // fall back to decrementing the count (so it stays balanced over time)
        deallocate(vmId);
    }
}