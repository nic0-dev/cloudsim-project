package org.cloudbus.cloudsim.policies;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VmAllocationPolicyCustom extends VmAllocationPolicySimple {
    private static final Map<Integer, String> vmTierMap = new HashMap<>();
    private final Map<String, HostEntity> vmToHostMap = new HashMap<>();

    public VmAllocationPolicyCustom(List<? extends Host> hostlist) {
        super(hostlist);
    }

    @Override
    public boolean allocateHostForGuest(GuestEntity guest) {
        String tier = vmTierMap.getOrDefault(guest.getId(), "cloud"); // Defualt to cloud
        for (HostEntity host : getHostList()) {
            if (host.isSuitableForGuest(guest)) {
                System.out.println("Host #" + host.getId() + " is suitable for Vm #" + guest.getId());
                boolean result = host.guestCreate(guest);
                if (result) {
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

        String vmUid = guest.getUid();
        // Return the host from the mapping we created during allocation
        return vmToHostMap.get(vmUid);
    }

    @Override
    public HostEntity getHost(int vmId, int userId) {
        // Try to find the host by VM UID
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

    public static Vm createDeviceVm(int brokerId) {
        int vmid = 0;
        int mips = 800;    // CPU speed for mobile device
        long size = 10000; // Image size (MB)
        int ram = 1024;    // VM memory (MB)
        long bw = 1000;    // Bandwidth
        int pesNumber = 1; // Number of CPUs
        String vmm = "Xen"; // VMM name
        vmTierMap.put(vmid, "device");

        return new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm,
                new CloudletSchedulerTimeShared());
    }

    public static Vm createEdgeVm(int brokerId) {
        int vmid = 1;
        int mips = 2500;    // CPU speed for edge server
        long size = 20000;  // Image size (MB)
        int ram = 4096;     // VM memory (MB)
        long bw = 10000;    // Bandwidth
        int pesNumber = 2;  // Number of CPUs
        String vmm = "Xen"; // VMM name
        vmTierMap.put(vmid, "edge");

        return new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm,
                new CloudletSchedulerTimeShared());
    }

    public static Vm createCloudVm(int brokerId) {
        int vmid = 2;
        int mips = 5000;    // CPU speed for cloud server
        long size = 40000;  // Image size (MB)
        int ram = 8192;     // VM memory (MB)
        long bw = 50000;    // Bandwidth
        int pesNumber = 4;  // Number of CPUs
        String vmm = "Xen"; // VMM name
        vmTierMap.put(vmid, "cloud");

        return new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm,
                new CloudletSchedulerTimeShared());
    }
}
