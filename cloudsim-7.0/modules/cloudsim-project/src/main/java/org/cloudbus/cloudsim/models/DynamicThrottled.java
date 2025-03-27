package org.cloudbus.cloudsim.models;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.policies.TaskOffloadingPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicThrottled {
    // VM state tracking
    private Map<Integer, Boolean> vmStateTable;
    // Current allocations per VM
    private Map<Integer, Integer> vmAllocationCounts;
    // Dynamic throttle limits per VM
    private Map<Integer, Integer> vmThrottleLimits;
    // VM to tier mapping
    private Map<Integer, String> vmTierMap;
    // VM utilization tracking
    private Map<Integer, Double> vmUtilization;
    // Task offloading policy
    private TaskOffloadingPolicy offloadingPolicy;
    // Power models for each tier
    private Map<String, TieredPowerModel> powerModels;

    public DynamicThrottled() {
        vmStateTable = new HashMap<>();
        vmAllocationCounts = new HashMap<>();
        vmThrottleLimits = new HashMap<>();
        vmTierMap = new HashMap<>();
        vmUtilization = new HashMap<>();
        offloadingPolicy = new TaskOffloadingPolicy();

        // Initialize power models for each tier
        powerModels = new HashMap<>();
        powerModels.put("device", new TieredPowerModel("device"));
        powerModels.put("edge", new TieredPowerModel("edge"));
        powerModels.put("cloud", new TieredPowerModel("cloud"));
    }

    // Initialize the VM table with dynamic throttle limits based on VM capacity
    public void initializeVmTable(List<Vm> vmList) {
        for (Vm vm : vmList) {
            int vmId = vm.getId();
            vmStateTable.put(vmId, true);  // All VMs initially available
            vmAllocationCounts.put(vmId, 0);  // No initial allocations
            vmUtilization.put(vmId, 0.0);  // Initial utilization is 0

            // Determine tier based on VM characteristics
            String tier = determineVmTier(vm);
            vmTierMap.put(vmId, tier);

            // Set dynamic throttle limit based on VM capacity
            int throttleLimit = calculateThrottleLimit(vm);
            vmThrottleLimits.put(vmId, throttleLimit);
        }
    }

    // Determine VM tier based on its characteristics
    private String determineVmTier(Vm vm) {
        double mips = vm.getMips();
        int ram = vm.getRam();

        if (mips <= 1000 && ram <= 1024) {
            return "device";
        } else if (mips <= 3000 && ram <= 4096) {
            return "edge";
        } else {
            return "cloud";
        }
    }

    // Calculate dynamic throttle limit based on VM capacity
    private int calculateThrottleLimit(Vm vm) {
        int basePes = vm.getNumberOfPes();
        int baseMips = (int) vm.getMips();

        // More powerful VMs can handle more cloudlets
        return Math.max(2, basePes * (baseMips / 1000));
    }

    // Get the most suitable VM for a cloudlet based on multiple factors
    public int allocateVm(Cloudlet cloudlet, List<Vm> vmList) {
        // Determine optimal execution tier for this cloudlet
        String preferredTier = offloadingPolicy.determineExecutionTier(cloudlet);

        // First try to find a VM in the preferred tier
        int vmId = getBestVmInTier(cloudlet, vmList, preferredTier);

        // If no VM found in preferred tier, try other tiers
        if (vmId == -1) {
            // Try edge tier as fallback for device tier
            if (preferredTier.equals("device")) {
                vmId = getBestVmInTier(cloudlet, vmList, "edge");
            }

            // Try cloud tier as last resort
            if (vmId == -1) {
                vmId = getBestVmInTier(cloudlet, vmList, "cloud");
            }
        }

        // If a suitable VM was found, allocate the cloudlet
        if (vmId != -1) {
            int currentCount = vmAllocationCounts.get(vmId);
            vmAllocationCounts.put(vmId, currentCount + 1);

            // Update utilization
            updateVmUtilization(vmId, cloudlet);

            // Check if VM reaches its dynamic throttle limit
            if (vmAllocationCounts.get(vmId) >= vmThrottleLimits.get(vmId)) {
                vmStateTable.put(vmId, false);
            }

            // Set the VM ID for the cloudlet
            cloudlet.setGuestId(vmId);
        }

        return vmId;
    }

    // Find the best VM in a specific tier
    private int getBestVmInTier(Cloudlet cloudlet, List<Vm> vmList, String tier) {
        double bestFitness = Double.MAX_VALUE;
        int selectedVmId = -1;

        for (Vm vm : vmList) {
            int vmId = vm.getId();

            // Check if VM is in the requested tier and available
            if (vmTierMap.get(vmId).equals(tier) &&
                    vmStateTable.get(vmId) &&
                    vmAllocationCounts.get(vmId) < vmThrottleLimits.get(vmId)) {

                // Calculate fitness value (lower is better)
                double fitness = calculateFitness(vm, cloudlet);

                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    selectedVmId = vmId;
                }
            }
        }

        return selectedVmId;
    }

    // Calculate fitness of a VM for a specific cloudlet (combination of utilization, power efficiency, and capacity match)
    private double calculateFitness(Vm vm, Cloudlet cloudlet) {
        int vmId = vm.getId();
        String tier = vmTierMap.get(vmId);
        double currentUtilization = vmUtilization.get(vmId);

        // Get power consumption based on current utilization
        double powerConsumption = powerModels.get(tier).getPower(currentUtilization);

        // Calculate how well the VM's capacity matches the cloudlet's needs
        double capacityMatch = Math.abs(vm.getMips() - cloudlet.getCloudletLength() / 1000.0);

        // Combine factors (lower is better)
        return (currentUtilization * 0.4) + (powerConsumption * 0.3) + (capacityMatch * 0.3);
    }

    // Update VM utilization based on new cloudlet
    private void updateVmUtilization(int vmId, Cloudlet cloudlet) {
        double currentUtil = vmUtilization.get(vmId);
        double cloudletImpact = cloudlet.getCloudletLength() / 10000.0; // Normalize impact

        // Simple linear combination
        double newUtil = Math.min(1.0, currentUtil + (cloudletImpact * 0.1));
        vmUtilization.put(vmId, newUtil);
    }

    // Deallocate a VM after cloudlet completion
    public void deallocateVm(int vmId, Cloudlet completedCloudlet) {
        if (vmAllocationCounts.containsKey(vmId)) {
            int currentCount = vmAllocationCounts.get(vmId);
            if (currentCount > 0) {
                vmAllocationCounts.put(vmId, currentCount - 1);

                // Reduce utilization
                double currentUtil = vmUtilization.get(vmId);
                double cloudletImpact = completedCloudlet.getCloudletLength() / 10000.0;
                double newUtil = Math.max(0.0, currentUtil - (cloudletImpact * 0.1));
                vmUtilization.put(vmId, newUtil);

                // If VM was previously unavailable and now below threshold, make available
                if (!vmStateTable.get(vmId) && currentCount - 1 < vmThrottleLimits.get(vmId)) {
                    vmStateTable.put(vmId, true);
                }
            }
        }
    }

    // Adjust throttle limits periodically based on system performance
    public void adjustThrottleLimits(List<Vm> vmList) {
        for (Vm vm : vmList) {
            int vmId = vm.getId();
            double utilization = vmUtilization.get(vmId);
            int currentLimit = vmThrottleLimits.get(vmId);

            // Dynamic adjustment based on current utilization
            if (utilization < 0.5 && currentLimit < 10) {
                // If utilization is low, we can increase the limit
                vmThrottleLimits.put(vmId, currentLimit + 1);
            } else if (utilization > 0.8 && currentLimit > 1) {
                // If utilization is high, reduce the limit
                vmThrottleLimits.put(vmId, currentLimit - 1);
            }
        }
    }
}