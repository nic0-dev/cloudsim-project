package org.cloudbus.cloudsim.models;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.policies.OffloadingPolicy;

import java.util.HashMap;
import java.util.Map;

public class ThrottlingBroker extends DatacenterBroker {
    // drop the finals so we can mutate them in setters
    private Map<Integer,String> vmTierMap    = new HashMap<>();
    private Map<String,OffloadingPolicy> tierPolicies = new HashMap<>();

    /** simple single-arg ctor so you can write `new ThrottlingBroker("Broker")` */
    public ThrottlingBroker(String name) throws Exception {
        super(name);
    }

    /** after you build your maps, call this */
    public void setVmTierMap(Map<Integer,String> vmTierMap) {
        this.vmTierMap.clear();
        this.vmTierMap.putAll(vmTierMap);
    }

    /** after you build your per-tier policies, call this */
    public void setTierPolicies(Map<String,OffloadingPolicy> tierPolicies) {
        this.tierPolicies.clear();
        this.tierPolicies.putAll(tierPolicies);
    }

    @Override
    protected void processCloudletReturn(SimEvent ev) {
        super.processCloudletReturn(ev);

        Cloudlet c   = (Cloudlet) ev.getData();
        int     vmId = c.getGuestId();
        String  tier = vmTierMap.get(vmId);
        OffloadingPolicy policy = tierPolicies.get(tier);

        // free & maybe re-enqueue
        policy.deallocate(vmId);
        policy.onCloudletCompletion(vmId, c);
    }
}
