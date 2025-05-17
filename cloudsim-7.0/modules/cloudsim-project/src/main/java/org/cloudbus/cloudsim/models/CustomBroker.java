package org.cloudbus.cloudsim.models;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.policies.OffloadingPolicy;

import java.util.HashMap;
import java.util.Map;

public class CustomBroker extends DatacenterBroker {
    private final Map<Integer,String> vmTierMap = new HashMap<>();
    private final Map<String,OffloadingPolicy> tierPolicies = new HashMap<>();

    public CustomBroker(String name) throws Exception {
        super(name);
    }

    public void setVmTierMap(Map<Integer,String> vmTierMap) {
        this.vmTierMap.clear();
        this.vmTierMap.putAll(vmTierMap);
    }

    public Map<Integer,String> getVmTierMap() {
        return Map.copyOf(vmTierMap);
    }

    public void setTierPolicies(Map<String,OffloadingPolicy> tierPolicies) {
        this.tierPolicies.clear();
        this.tierPolicies.putAll(tierPolicies);
    }

    /***
     * This method is called when a Cloudlet is returned from a VM.
     * It deallocates the VM and re-enqueues the Cloudlet for further processing.
     */
    @Override
    protected void processCloudletReturn(SimEvent ev) {
        super.processCloudletReturn(ev);

        Cloudlet c = (Cloudlet) ev.getData();
        int vmId = c.getGuestId();
        String tier = vmTierMap.get(vmId);
        OffloadingPolicy policy = tierPolicies.get(tier);

        // free & maybe re-enqueue
        policy.deallocate(vmId);
        policy.onCloudletCompletion(vmId, c);
    }
}
