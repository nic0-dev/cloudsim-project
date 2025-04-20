package org.cloudbus.cloudsim.utils;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * VM factory and allocation policy that binds each VM to a specific tier (device/edge/cloud).
 * Tier mappings are populated at VM creation time and retrieved in ConstrainedCostOptimizer.
 */
public class CreateVm extends VmAllocationPolicySimple {
    private static final Map<Integer, String> vmTierMap = new HashMap<>();
    private final Map<String, HostEntity> vmToHostMap = new HashMap<>();

    public CreateVm(List<? extends Host> hostlist) {
        super(hostlist);
    }

    /**
     * Returns an unmodifiable map of VM ID to tier.  Every VM ID must appear here.
     */
    public static Map<Integer,String> getVmTierMap() {
        return Map.copyOf(vmTierMap);
    }

    @Override
    public boolean allocateHostForGuest(GuestEntity guest) {
        String tier = Objects.requireNonNull(
            vmTierMap.get(guest.getId()),
            () -> "VM #" + guest.getId() + " has no tier mapping"
        );
        for (HostEntity host : getHostList()) {
            if (host.isSuitableForGuest(guest)) {
                System.out.println("Host #" + host.getId() + " is suitable for Vm #" + guest.getId());
                boolean created = host.guestCreate(guest);
                if (created) {
                    vmToHostMap.put(guest.getUid(), host);
                    System.out.println("Allocated Vm #" + guest.getId() + " to Host #" + host.getId());
                    return true;
                }
            } else {
                System.out.println("Host #" + host.getId() + " is not suitable for Vm #" + guest.getId());
            }
        }
        return false;
    }

    @Override
    public HostEntity getHost(GuestEntity guest) {
        if (guest == null) {
            return null;
        }
        // Return the host from the mapping we created during allocation
        return vmToHostMap.get(guest.getUid());
    }

    @Override
    public HostEntity getHost(int vmId, int userId) {
        // Find host by VM UID
        String vmUid = GuestEntity.getUid(userId, vmId);
        System.out.println("Getting host for Vm #" + vmUid);
        return vmToHostMap.get(vmUid);
    }

    @Override
    public void deallocateHostForGuest(GuestEntity guest) {
        HostEntity host = getHost(guest);
        if (host != null) {
            host.guestDestroy(guest);
            vmToHostMap.remove(guest.getUid());
        }
    }

    /**
     * Helper: register and return a device-tier VM
     */
    public static Vm createDeviceVm(int brokerId) {
        int vmId = 0;
        vmTierMap.put(vmId, "device");
        return new Vm(
            vmId, brokerId,
            /* mips */ 4000, /* pes */ 4,
            /* ram */ 4096, /* bw */ 1000,
            /* size */ 10000, "Xen",
            new CloudletSchedulerTimeShared()
        );
    }

    /**
     * Helper: register and return an edge-tier VM
     */
    public static Vm createEdgeVm(int brokerId) {
        int vmId = 1;
        vmTierMap.put(vmId, "edge");
        return new Vm(
            vmId, brokerId,
            2500, 2,
            4096, 10000,
            20000, "Xen",
            new CloudletSchedulerTimeShared()
        );
    }

    /**
     * Helper: register and return a cloud-tier VM
     */
    public static Vm createCloudVm(int brokerId) {
        int vmId = 2;
        vmTierMap.put(vmId, "cloud");
        return new Vm(
            vmId, brokerId,
            5000, 4,
            8192, 50000,
            40000, "Xen",
            new CloudletSchedulerTimeShared()
        );
    }
}
