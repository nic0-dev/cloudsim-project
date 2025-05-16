package org.cloudbus.cloudsim.policies;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.models.CustomBroker;

import java.util.*;

/**
 * Dynamic throttled offloading policy: assigns each incoming cloudlet to any idle VM.
 * If all VMs are busy, cloudlets are queued in arrival order.
 * When a VM finishes, the next queued cloudlet (if any) is dispatched to it.
 */
public class DynamicThrottled implements OffloadingPolicy {
    private List<Vm> vmList;
    private Map<Integer, Boolean> vmIdle;
    private Queue<Cloudlet> waitingQueue;
    private CustomBroker broker;

    public void setBroker(CustomBroker broker) {
        this.broker = broker;
    }

    /**
     * Initializes with the available VMs: marks all as idle and clears queue.
     * @param vmList list of VMs to distribute tasks across
     */
    public void initialize(List<Vm> vmList) {
        if (vmList == null || vmList.isEmpty()) {
            throw new IllegalArgumentException("VM list must be non-null and non-empty");
        }
        this.vmList = new ArrayList<>(vmList);
        this.vmIdle = new HashMap<>();
        for (Vm vm : vmList) {
            vmIdle.put(vm.getId(), true);
        }
        this.waitingQueue = new LinkedList<>();
    }

    /**
     * Attempts to allocate a cloudlet to an idle VM.
     * If successful, binds the cloudlet. Otherwise, enqueues the cloudlet
     * @return chosen VM id, or -1 if enqueued
     */
    public int allocate(Cloudlet cloudlet) {
        Map<Integer,String> tierMap = broker.getVmTierMap();
        for (Vm vm : vmList) {
            int vmId = vm.getId();
            if (vmIdle.get(vmId)) {
                // dispatch immediately
                vmIdle.put(vmId, false);
                cloudlet.setGuestId(vmId);
                return vmId;
            }
        }
        // no idle VM => enqueue
        waitingQueue.add(cloudlet);
        return -1;
    }

    /**
     * Marks the VM as idle and, if any cloudlets are queued, dispatches the next to this VM.
     * @param vmId id of VM that completed the cloudlet
     */
    @Override
    public void deallocate(int vmId) {
        // free the VM
        vmIdle.put(vmId, true);
        // dispatch next if waiting
        Cloudlet next = waitingQueue.poll();
        if (next != null) {
            vmIdle.put(vmId, false);
            next.setGuestId(vmId);

            System.out.printf("Cloudlet#%d dispatched to VM#%d%n",next.getCloudletId(), vmId);

            broker.submitCloudletList(Collections.singletonList(next));
        }
    }


    /**
     * Snapshot of how many cloudlets are waiting for processing.
     */
    public int getQueueLength() {
        return waitingQueue.size();
    }

    /**
     * Retrieves the current idle status of each VM.
     * @return an unmodifiable map of VM id to idle status
     */
    public Map<Integer, Boolean> getVmIdleStatus() {
        return Collections.unmodifiableMap(vmIdle);
    }
}