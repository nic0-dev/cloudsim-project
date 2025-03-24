package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class TaskOffloadingExample {
	
	private static final int NUM_CLOUD_DATACENTERS = 1;
	private static final int NUM_EDGE_DATACENTERS = 2;
	private static final int NUM_DEVICES = 3;
	
	public static void main(String[] args) {
		try {
			// Initialize CloudSim
			int num_user = 1;
			Calendar calender = Calendar.getInstance();
			boolean trace_flag = false;
			CloudSim.init(num_user, calender, trace_flag);
			
			// Create Edge Datacenters
			List<Datacenter> edgeDatacenters = new ArrayList<>();
			for (int i = 0; i < NUM_EDGE_DATACENTERS; i++) {
				edgeDatacenters.add(createDatacenter("Edge-Datacenter-" + i, 2048, 5000, 500, 2000));
			}
			
			// Create Cloud Datacenter
			// | RAM (MB) | Storage (MB) | Bandwidth | MIPS
			Datacenter cloudDatacenter = createDatacenter("Cloud-Datacenter", 4096, 10000, 1000, 4000);
			
			// Create Broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();
			
			// Create VMs
			List<Vm> vmList = new ArrayList<>();
			// Cloud VMs
			vmList.add(createVm(0, brokerId, 2000, 1));
			// Edge VMs
			vmList.add(createVm(1, brokerId, 1000, 1));
			vmList.add(createVm(2, brokerId, 1000, 1));
			// Device VMs
			vmList.add(createVm(3, brokerId, 500, 1));
			
			// submit vm list to the broker
			broker.submitGuestList(vmList);
			
			// Create Cloudlets
			List<Cloudlet> cloudletList = createCloudlets(brokerId);
			broker.submitCloudletList(cloudletList);
			
			CloudSim.startSimulation();
			CloudSim.stopSimulation();
			
			// Print results
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Datacenter createDatacenter(String name, int ram, int storage, int bw, int mips) {
		List<Host> hostList = new ArrayList<>();
		List<Pe> peList = new ArrayList<>();
		peList.add(new Pe(0, new PeProvisionerSimple(mips)));
		hostList.add(new Host(0, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList)));
		
		String arch = "x86";
		String os = "Linux";
		String vmm = "Xen";
		double time_zone = 10.0;
		double cost = 3.0;
		double costPerMem = 0.05;
		double costPerStorage = 0.001;
		double costPerBw = 0.0;
		
		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
		
		try {
			return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return broker;
	}
	
	private static Vm createVm(int vmid, int userId, int mips, int pesNumber) {
		long size = 1000; // image size (MB);
		int ram = 512; // vm memory (MB);
		long bw = 100;
		String vmm = "Xen";
		Vm vm = new Vm(vmid, userId, mips, pesNumber, ram, bw, size, vmm,
				new CloudletSchedulerTimeShared());
		return vm;
	}
	
	private static List<Cloudlet> createCloudlets(int userId) {
		List<Cloudlet> list = new ArrayList<>();
		
		long length = 40000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		
        // Create different types of tasks
        // Heavy computation task (for cloud)
        Cloudlet cloudlet1 = new Cloudlet(0, length * 2, pesNumber, fileSize, 
            outputSize, utilizationModel, utilizationModel, utilizationModel);
        cloudlet1.setUserId(userId);
        
        // Medium computation task (for edge)
        Cloudlet cloudlet2 = new Cloudlet(1, length, pesNumber, fileSize, 
            outputSize, utilizationModel, utilizationModel, utilizationModel);
        cloudlet2.setUserId(userId);
        
        // Light computation task (for device)
        Cloudlet cloudlet3 = new Cloudlet(2, length / 2, pesNumber, fileSize, 
            outputSize, utilizationModel, utilizationModel, utilizationModel);
        cloudlet3.setUserId(userId);
        
        list.add(cloudlet1);
        list.add(cloudlet2);
        list.add(cloudlet3);
        
        return list;
	}
	
	private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;
        
        String indent = "    ";
        DecimalFormat dft = new DecimalFormat("###.##");
        
        System.out.println("========== Task Execution Results ==========");
        System.out.println("Cloudlet ID" + indent + "Status" + indent 
        	+ "Datacenter ID" + indent + "VM ID" + indent + "Time" + indent
        	+ "Start Time" + indent + "Finish Time");
        
        for (Cloudlet value : list) {
            cloudlet = value;
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);
            
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
