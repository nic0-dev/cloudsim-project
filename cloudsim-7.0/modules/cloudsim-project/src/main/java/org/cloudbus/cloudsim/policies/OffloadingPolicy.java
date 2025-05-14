package org.cloudbus.cloudsim.policies;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;

public interface OffloadingPolicy extends VmAllocationPolicy {
    /**
     * Called after a cloudlet completes on the given VM
     * Default no-op for non-RL policies.
     */
    default void onCloudletCompletion(int vmId, Cloudlet cloudlet) {}
    default void setBroker(DatacenterBroker broker) { }
}
