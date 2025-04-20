package org.cloudbus.cloudsim.policies;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

import java.util.List;

public interface VmAllocationPolicy {
    /** Called once at simulation start to give this policy the full VM list */
    void initialize(List<Vm> vmList);
    /** Bind c to a VM.  Returns the VM’s id, or –1 if it is queued */
    int  allocate(Cloudlet c);
    /** Called when a cloudlet is finished; may trigger dispatch of another cloudlet */
    void deallocate(int vmId);
}
