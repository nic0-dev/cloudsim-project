package org.cloudbus.cloudsim.models;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.policies.OffloadingPolicy;

import java.util.Map;

public class ThrottlingBroker extends DatacenterBroker {
    private final Map<Integer,String> vmTierMap;
    private final Map<String, OffloadingPolicy> tierPolicies;

    public ThrottlingBroker(
            String name,
            Map<Integer,String> vmTierMap,
            Map<String,OffloadingPolicy> tierPolicies
    ) throws Exception {
        super(name);
        this.vmTierMap    = vmTierMap;
        this.tierPolicies = tierPolicies;
    }

    @Override
    protected void processCloudletReturn(SimEvent ev) {
        // first let the parent class do its usual work
        super.processCloudletReturn(ev);

        // now _immediately_ free the VM and re-submit the next queued
        Cloudlet c   = (Cloudlet)ev.getData();
        int     vmId = c.getGuestId();
        String  tier = vmTierMap.get(vmId);
        OffloadingPolicy policy = tierPolicies.get(tier);

        // this deallocate will, if there's a waiting Cloudlet,
        // bind it to vmId and call broker.submitCloudletList(...)
        policy.deallocate(vmId);
    }
}
