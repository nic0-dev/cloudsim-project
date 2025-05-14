package org.cloudbus.cloudsim.utils;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.models.CustomDatacenter;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.ArrayList;
import java.util.List;

public class CreateDatacenter {
    private static final int HOSTS_PER_TIER = 3;    // Create 3 hosts per tier

    public static CustomDatacenter createDeviceDatacenter() throws Exception {
        // Get the host list
        List<Host> hostList = getDeviceHostList();
        System.out.println("Device Tier: Created " + hostList.size() + (hostList.size() == 1 ? " Host" : " Hosts"));
        for (Host host : hostList) {
            System.out.println("Device Tier: Created Host #" + host.getId());
        }
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86", "Android", "Mobile", hostList, 0.0, 5.0, 0.05, 0.001, 0.1
        );

        return new CustomDatacenter(
                "Device_Tier", "device", characteristics, new CreateVm(hostList),
                new ArrayList<>(), 0.1, 50.0, 20.0
        );
    }

    public static CustomDatacenter createEdgeDatacenter() throws Exception {
        List<Host> hostList = getEdgeHostList(); // call once
        System.out.println("Edge Tier: Created " + hostList.size() + (hostList.size() == 1 ? " Host" : " Hosts"));
        for (Host host : hostList) {
            System.out.println("Edge Tier: Created Host #" + host.getId());
        }
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            "x86","Linux","Xen", hostList,0.0,3.0,0.05,0.001, 0.1
        );

        return new CustomDatacenter(
            "Edge_Tier","edge",characteristics, new CreateVm(hostList),new ArrayList<>(),0.1,1000.0, 10.0
        );
    }

    public static CustomDatacenter createCloudDatacenter() throws Exception {
        List<Host> hostList = getCloudHostList(); // call once
        System.out.println("Cloud Tier: Created " + hostList.size() + (hostList.size() == 1 ? " Host" : " Hosts"));
        for (Host host : hostList) {
            System.out.println("Cloud Tier: Created Host #" + host.getId());
        }
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            "x86", "Linux", "Xen", hostList,0.0,2.0,0.03,0.0005, 0.05            // cost per bw
        );

        return new CustomDatacenter(
        "Cloud_Tier","cloud",characteristics,new CreateVm(hostList),new ArrayList<>(),0.1,10000.0,0.0
        );
    }

    public static List<Host> getDeviceHostList() {
        List<Host> hostList = new ArrayList<>();

        int mipsPerCore = 1200; // MIPS per core
        int numCores = 8;       // For an 8-core Google Pixel 7
        int ram = 6144;         // 6GB
        long storage = 40000;   //
        int bw = 12000;         // Bandwidth

        for (int hostId = 0; hostId < HOSTS_PER_TIER; hostId++) {
            List<Pe> peList = new ArrayList<>();
            // Create 8 PEs to aggregate to 8000 MIPS total
            for (int i = 0; i < numCores; i++) {
                peList.add(new Pe(i, new PeProvisionerSimple(mipsPerCore)));
            }

            hostList.add(new Host(
                    hostId,
                    new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw),
                    storage,
                    peList,
                    new VmSchedulerTimeShared(peList)
            ));
        }

        return hostList;
    }

    public static List<Host> getEdgeHostList() {
        List<Host> hostList = new ArrayList<>();
        int mips = 8000;
        int numCores = 4;
        int ram = 10240;
        long storage = 1000000;
        int bw = 30000;

        for (int hostId = HOSTS_PER_TIER; hostId < HOSTS_PER_TIER * 2; hostId++) {
            List<Pe> peList = new ArrayList<>();
            for (int i = 0; i < numCores; i++) {
                peList.add(new Pe(i, new PeProvisionerSimple(mips)));
            }

            hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList)));
        }
        return hostList;
    }

    public static List<Host> getCloudHostList() {
        List<Host> hostList = new ArrayList<>();
        int mips = 20000;
        int numCores = 8;
        int ram = 20480;
        long storage = 10000000;
        int bw = 120000;

        for (int hostId = HOSTS_PER_TIER * 2; hostId < HOSTS_PER_TIER * 3; hostId++) {
            List<Pe> peList = new ArrayList<>();
            for (int i = 0; i < numCores; i++) {
                peList.add(new Pe(i, new PeProvisionerSimple(mips)));
            }

            hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList)));
        }
        return hostList;
    }
}
