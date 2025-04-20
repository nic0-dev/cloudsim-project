package org.cloudbus.cloudsim.utils;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.entities.CustomDatacenter;
import org.cloudbus.cloudsim.policies.VmAllocationPolicyCustom;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.ArrayList;
import java.util.List;

public class CreateDatacenter {
    public static CustomDatacenter createDeviceDatacenter() throws Exception {
        // Get the host list
        List<Host> hostList = getDeviceHostList();
        System.out.println("Device Tier: Created " + hostList.size() + (hostList.size() == 1 ? " Host" : " Hosts"));
        System.out.println("Device Tier: Created Host #" + hostList.getFirst().getId());
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86", "Android", "Mobile", hostList, 0.0, 5.0, 0.05, 0.001, 0.1
        );

        return new CustomDatacenter(
                "Device_Tier", "device", characteristics, new VmAllocationPolicyCustom(hostList),
                new ArrayList<>(), 0.1, 50.0, 20.0
        );
    }

    public static CustomDatacenter createEdgeDatacenter() throws Exception {
        List<Host> hostList = getEdgeHostList(); // call once
        System.out.println("Edge Tier: Created " + hostList.size() + (hostList.size() == 1 ? " Host" : " Hosts"));
        System.out.println("Edge Tier: Created Host #" + hostList.getFirst().getId());
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            "x86","Linux","Xen", hostList,0.0,3.0,0.05,0.001, 0.1
        );

        return new CustomDatacenter(
            "Edge_Tier","edge",characteristics, new VmAllocationPolicyCustom(hostList),new ArrayList<>(),0.1,1000.0, 10.0
        );
    }

    public static CustomDatacenter createCloudDatacenter() throws Exception {
        List<Host> hostList = getCloudHostList(); // call once
        System.out.println("Cloud Tier: Created " + hostList.size() + (hostList.size() == 1 ? " Host" : " Hosts"));
        System.out.println("Cloud Tier: Created Host #" + hostList.getFirst().getId());
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            "x86", "Linux", "Xen", hostList,0.0,2.0,0.03,0.0005, 0.05            // cost per bw
        );

        return new CustomDatacenter(
        "Cloud_Tier","cloud",characteristics,new VmAllocationPolicyCustom(hostList),new ArrayList<>(),0.1,10000.0,0.0
        );
    }

    public static List<Host> getDeviceHostList() {
        List<Host> hostList = new ArrayList<>();

        int mipsPerCore = 1000; // MIPS per core
        int numCores = 8;       // For an 8-core Google Pixel 7
        int ram = 4096;         // Optionally, you can update RAM to 4GB for modern devices
        long storage = 32768;   // 32 GB storage (can remain unchanged)
        int bw = 10000;         // Bandwidth

        List<Pe> peList = new ArrayList<>();
        // Create 8 PEs to aggregate to 8000 MIPS total
        for (int i = 0; i < numCores; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mipsPerCore)));
        }

        hostList.add(new Host(
                0,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)
        ));

        return hostList;
    }

    public static List<Host> getEdgeHostList() {
        List<Host> hostList = new ArrayList<>();
        int mips = 2500;
        int ram = 8192;
        long storage = 1000000;
        int bw = 25000;

        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
        }

        hostList.add(new Host(1, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList)));
        return hostList;
    }

    public static List<Host> getCloudHostList() {
        List<Host> hostList = new ArrayList<>();
        int mips = 5000;
        int ram = 16384;
        long storage = 10000000;
        int bw = 100000;

        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
        }

        hostList.add(new Host(2, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList)));
        return hostList;
    }
}
