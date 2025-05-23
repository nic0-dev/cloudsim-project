package org.cloudbus.cloudsim.policies;

import org.cloudbus.cloudsim.Cloudlet;

public interface OffloadingPolicy extends VmAllocationPolicy {
    // Deallocates the VM and frees up resources.
    default void onCloudletCompletion(int vmId, Cloudlet cloudlet) {}
}
