package org.cloudbus.cloudsim.loadbalancer;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThrottledLoadBalancer {
	 // Maintains the state of each VM (available = true, busy = false)
    private Map<Integer, Boolean> vmStateTable;
    // Maximum number of cloudlets that can be assigned to a VM at once
    private final int THROTTLE_LIMIT = 3;
    // Keeps track of current allocations per VM
    private Map<Integer, Integer> vmAllocationCounts;
    
    public ThrottledLoadBalancer() {
        vmStateTable = new HashMap<>();
        vmAllocationCounts = new HashMap<>();
    }
    
    /**
     * Initialize the state table for all VMs
     * @param vmList List of all available VMs
     */
    public void initializeVmTable(List<Vm> vmList) {
        for (Vm vm : vmList) {
            vmStateTable.put(vm.getId(), true);  // All VMs initially available
            vmAllocationCounts.put(vm.getId(), 0);  // No initial allocations
        }
    }
    
    /**
     * Finds the next available VM for cloudlet allocation
     * @param vmList List of all VMs
     * @return ID of the selected VM, or -1 if none available
     */
    public int getNextAvailableVm(List<Vm> vmList) {
        for (Vm vm : vmList) {
            int vmId = vm.getId();
            if (vmStateTable.get(vmId) && vmAllocationCounts.get(vmId) < THROTTLE_LIMIT) {
                return vmId;
            }
        }
        return -1;  // No VM available
    }
    
    /**
     * Allocates a cloudlet to a VM
     * @param cloudlet The cloudlet to be allocated
     * @param vmList List of all VMs
     * @return ID of the allocated VM, or -1 if allocation failed
     */
    public int allocateVm(Cloudlet cloudlet, List<Vm> vmList) {
        int vmId = getNextAvailableVm(vmList);
        
        if (vmId != -1) {
            // Update VM allocation count
            int currentCount = vmAllocationCounts.get(vmId);
            vmAllocationCounts.put(vmId, currentCount + 1);
            
            // If VM reaches throttle limit, mark as unavailable
            if (vmAllocationCounts.get(vmId) >= THROTTLE_LIMIT) {
                vmStateTable.put(vmId, false);
            }
            
            // Set the VM ID for the cloudlet
            cloudlet.setVmId(vmId);
        }
        
        return vmId;
    }
    
    /**
     * Deallocates a VM after cloudlet completion
     * @param vmId ID of the VM to deallocate
     */
    public void deallocateVm(int vmId) {
        if (vmAllocationCounts.containsKey(vmId)) {
            int currentCount = vmAllocationCounts.get(vmId);
            if (currentCount > 0) {
                vmAllocationCounts.put(vmId, currentCount - 1);
                // If VM was previously unavailable and now below threshold, make available
                if (!vmStateTable.get(vmId) && currentCount - 1 < THROTTLE_LIMIT) {
                    vmStateTable.put(vmId, true);
                }
            }
        }
    }
    
    /**
     * Gets the current allocation count for a VM
     * @param vmId ID of the VM
     * @return Current number of cloudlets allocated to the VM
     */
    public int getCurrentAllocationCount(int vmId) {
        return vmAllocationCounts.getOrDefault(vmId, 0);
    }
    
    /**
     * Checks if a VM is available for allocation
     * @param vmId ID of the VM to check
     * @return true if VM is available, false otherwise
     */
    public boolean isVmAvailable(int vmId) {
        return vmStateTable.getOrDefault(vmId, false) && 
               vmAllocationCounts.getOrDefault(vmId, 0) < THROTTLE_LIMIT;
    }
}
