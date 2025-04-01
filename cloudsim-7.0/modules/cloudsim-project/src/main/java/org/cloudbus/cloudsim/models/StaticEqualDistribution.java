package org.cloudbus.cloudsim.models;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticEqualDistribution {
    // Track allocation counts for each VM
    private Map<Integer, Integer> vmAllocationCounts;
    // Current VM index for round-robin allocation
    private int currentVmIndex;
    // Total number of VMs
    private int totalVms;

    public StaticEqualDistribution() {
        vmAllocationCounts = new HashMap<>();
        currentVmIndex = -1;
        totalVms = 0;
    }

    /**
     * Initialize the VM table for equal distribution
     */
    public void initializeVmTable(List<Vm> vmList) {
        totalVms = vmList.size();

        for (Vm vm : vmList) {
            int vmId = vm.getId();
            vmAllocationCounts.put(vmId, 0);
        }

        // Reset the VM index
        currentVmIndex = -1;
    }

    /**
     * Allocate a VM for the cloudlet using round-robin distribution
     */
    public int allocateVm(Cloudlet cloudlet, List<Vm> vmList) {
        if (vmList.isEmpty()) {
            return -1;
        }

        // Simple round-robin allocation
        currentVmIndex = (currentVmIndex + 1) % totalVms;
        Vm selectedVm = vmList.get(currentVmIndex);
        int vmId = selectedVm.getId();

        // Update allocation count
        int currentCount = vmAllocationCounts.get(vmId);
        vmAllocationCounts.put(vmId, currentCount + 1);

        // Set VM ID for the cloudlet
        cloudlet.setVmId(vmId);

        return vmId;
    }

    /**
     * Alternative allocation method that uses the least loaded VM
     */
    public int allocateVmByLoad(Cloudlet cloudlet, List<Vm> vmList) {
        if (vmList.isEmpty()) {
            return -1;
        }

        // Find VM with minimum load
        int minLoadVmId = -1;
        int minLoad = Integer.MAX_VALUE;

        for (Vm vm : vmList) {
            int vmId = vm.getId();
            int load = vmAllocationCounts.get(vmId);

            if (load < minLoad) {
                minLoad = load;
                minLoadVmId = vmId;
            }
        }

        // Update allocation count
        int currentCount = vmAllocationCounts.get(minLoadVmId);
        vmAllocationCounts.put(minLoadVmId, currentCount + 1);

        // Set VM ID for the cloudlet
        cloudlet.setVmId(minLoadVmId);

        return minLoadVmId;
    }

    /**
     * Deallocate a VM after cloudlet completion
     */
    public void deallocateVm(int vmId, Cloudlet completedCloudlet) {
        if (vmAllocationCounts.containsKey(vmId)) {
            int currentCount = vmAllocationCounts.get(vmId);
            if (currentCount > 0) {
                vmAllocationCounts.put(vmId, currentCount - 1);
            }
        }
    }

    /**
     * Get the current allocation counts for all VMs
     */
    public Map<Integer, Integer> getVmAllocationCounts() {
        return new HashMap<>(vmAllocationCounts);
    }

    /**
     * Get the total number of allocations across all VMs
     */
    public int getTotalAllocations() {
        int total = 0;
        for (int count : vmAllocationCounts.values()) {
            total += count;
        }
        return total;
    }
}