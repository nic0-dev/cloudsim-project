package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.loadbalancer.ThrottledLoadBalancer;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ThrottledLoadBalancerExample {
    private static final int NUMBER_OF_VMS = 5;
    private static final int NUMBER_OF_CLOUDLETS = 10;

    public static void main(String[] args) {
        try {
            // Initialize CloudSim
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudSim.init(num_user, calendar, trace_flag);

            // Create Datacenter
            Datacenter datacenter = createDatacenter("Datacenter_0");

            // Create Broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker != null ? broker.getId() : 0;

            // Create VMs
            List<Vm> vmList = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_VMS; i++) {
                Vm vm = new Vm(i, brokerId, 1000, 1, 1024, 100, 10000, "Xen",
                        new CloudletSchedulerTimeShared());
                vmList.add(vm);
            }
            // Create Cloudlets
            List<Cloudlet> cloudletList = new ArrayList<>();
            for (int i = 0; i < NUMBER_OF_CLOUDLETS; i++) {
                Cloudlet cloudlet = new Cloudlet(i, 1000, 1, 1000, 1000,
                        new UtilizationModelFull(), new UtilizationModelFull(),
                        new UtilizationModelFull());
                cloudlet.setUserId(brokerId);
                cloudletList.add(cloudlet);
            }

            // Initialize Throttled Load Balancer
            ThrottledLoadBalancer loadBalancer = new ThrottledLoadBalancer();
            loadBalancer.initializeVmTable(vmList);

            // Submit VM list to the broker
            broker.submitGuestList(vmList);

            // Allocate cloudlets to VMs using the load balancer
            for (Cloudlet cloudlet : cloudletList) {
                int vmId = loadBalancer.allocateVm(cloudlet, vmList);
                if (vmId != -1) {
                    System.out.println("Cloudlet " + cloudlet.getCloudletId() +
                            " allocated to VM " + vmId);
                } else {
                    System.out.println("Cloudlet " + cloudlet.getCloudletId() +
                            " couldn't be allocated. Waiting for VM availability.");
                }
            }

            // Submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);

            // Start the simulation
            CloudSim.startSimulation();

            // Stop the simulation
            CloudSim.stopSimulation();

            // Print results
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            printCloudletList(newList);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private static Datacenter createDatacenter (String name){
        // Create host list
        List<Host> hostList = new ArrayList<>();


        int mips = 1000;
        // Create PEs (CPU cores)
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        // Create Host with its id and list of PEs
        int hostId = 0;
        int ram = 2048; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;

        Host host = new Host(hostId, new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw), storage, peList,
                new VmSchedulerTimeShared(peList));
        hostList.add(host);

        // Create a DatacenterCharacteristics object
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage,
                costPerBw);

        try {
            return new Datacenter(name, characteristics,
                    new VmAllocationPolicySimple(hostList), new ArrayList<Storage>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DatacenterBroker createBroker () {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    private static void printCloudletList (List < Cloudlet > list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        System.out.println();
        System.out.println("========== OUTPUT ==========");
        System.out.println("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent +
                "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            System.out.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                Log.print("SUCCESS");

                Log.println(indent + indent + cloudlet.getResourceId()
                        + indent + indent + indent + cloudlet.getGuestId()
                        + indent + indent
                        + dft.format(cloudlet.getActualCPUTime()) + indent
                        + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent
                        + dft.format(cloudlet.getExecFinishTime()));
            }
        }
    }
}