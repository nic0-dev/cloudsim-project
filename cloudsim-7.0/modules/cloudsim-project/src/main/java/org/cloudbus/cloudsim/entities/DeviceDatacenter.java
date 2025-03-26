package org.cloudbus.cloudsim.entities;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.List;

public class DeviceDatacenter extends Datacenter {
    private final String tier;  // "device", "edge", or "cloud"
    private final double bandwidth;  // Available bandwidth in Mbps
    private final double latency;    // Network latency in ms

    public DeviceDatacenter(String name,
                            String tier,
                            DatacenterCharacteristics characteristics,
                            VmAllocationPolicy vmAllocationPolicy,
                            List<Storage> storageList,
                            double schedulingInterval,
                            double bandwidth,
                            double latency
    ) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
        this.tier = tier;
        this.bandwidth = bandwidth;
        this.latency = latency;
    }

    public String getTier() {
        return tier;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public double getLatency() {
        return latency;
    }

    // Helper method to calculate transfer time between datacenters
    public double calculateTransferTime(long dataSize, DeviceDatacenter target) {
        // Convert data size from bytes to bits
        double dataSizeBits = dataSize * 8;
        // Calculate transfer time based on bandwidth (in seconds)
        double transferTime = dataSizeBits / (bandwidth * 1000000);
        // Add network latency (convert from ms to seconds)
        return transferTime + (latency / 1000.0);
    }
}
