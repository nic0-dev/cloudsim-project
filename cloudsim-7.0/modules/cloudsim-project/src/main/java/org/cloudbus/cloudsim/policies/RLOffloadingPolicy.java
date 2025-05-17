package org.cloudbus.cloudsim.policies;

import lombok.Data;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.cost.CostModel;
import org.cloudbus.cloudsim.utils.CreateVm;

import java.util.*;

/**
 * A Q-learning based offloading policy that jointly optimizes latency and energy.
 * Reward = - (latency + λ * energy), with a hard penalty if latency > L_MAX.
 */
@Data
public class RLOffloadingPolicy implements OffloadingPolicy {
    private final CostModel costModel;
    private final double L_MAX;
    private final double lambda;

    private List<Vm> vmList;
    private Map<Integer, Double> qValues;
    private Map<Integer, Integer> allocations;
    private Map<Integer, String> vmTierMap;
    private Map<Integer, Double> lastEpisodeQ = new HashMap<>();

    private double currentEpisodeReward = 0.0;

    private double learningRate;      // α
    private double discountFactor;    // γ

    private double qValueChangeThreshold = 0.0125;
    private final double initialTemperature = 1.0;
    private final double minimumTemperature = 0.1;
    private final double decayRate = 0.995;
    private int episodeCount = 0;
    private double minDelta = Double.MAX_VALUE;

    private double maxLatency;
    private double maxEnergy;
    private Random random = new Random();

    public RLOffloadingPolicy(CostModel costModel, double L_MAX, double lambda, double alpha, double gamma) {
        this.costModel = Objects.requireNonNull(costModel, "costModel");
        this.L_MAX    = L_MAX;
        this.lambda   = lambda;
        this.learningRate = alpha;
        this.discountFactor = gamma;
    }

    @Override
    public void initialize(List<Vm> vmList) {
        if (vmList == null || vmList.isEmpty()) {
            throw new IllegalArgumentException("VM list must be non-null and non-empty");
        }
        this.vmList = new ArrayList<>(vmList);
        qValues = new HashMap<>();
        allocations = new HashMap<>();
        vmTierMap = new HashMap<>();
        // Build a map from VM ID to its tier ("device","edge","cloud")
        Map<Integer, String> globalTierMap = CreateVm.getVmTierMap();
        for (Vm vm : vmList) {
            int id = vm.getId();
            qValues.put(vm.getId(), 0.1);
            allocations.put(id, 0);
            String tier = globalTierMap.get(id);
            if (tier == null) {
                throw new IllegalStateException("VM #" + id + " has no tier entry in CreateVm.vmTierMap");
            }
            vmTierMap.put(id, tier);
        }
        System.out.println("RL Offloading Policy initialized with " + vmList.size() +
                " VMs, L_MAX=" + L_MAX + ", λ=" + lambda);
    }

    @Override
    public int allocate(Cloudlet cloudlet) {
        double T = getCurrentTemperature();
        int vmId = selectVmWithSoftmax(T);
        allocations.put(vmId, allocations.get(vmId) + 1);
        System.out.printf("[Episode %d] SOFTMAX @ T=%.3f → VM #%d (Q=%.4f)%n",
                episodeCount, T, vmId, qValues.get(vmId));
        return vmId;
    }

    private double getCurrentTemperature() {
        return Math.max(minimumTemperature, initialTemperature * Math.pow(decayRate, episodeCount));
    }

    private int selectVmWithSoftmax(double temp) {
        double[] probabilities = new double[vmList.size()];
        double sum = 0.0;

        for (int i = 0; i < vmList.size(); i++) {
            double qValue = qValues.get(vmList.get(i).getId());
            probabilities[i] = Math.exp(qValue / temp);
            sum += probabilities[i];
        }
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] /= sum;
        }

        double rand = random.nextDouble();
        double cumulativeProbability = 0.0;
        for (int i = 0; i < vmList.size(); i++) {
            cumulativeProbability += probabilities[i];
            if (rand <= cumulativeProbability) {
                return vmList.get(i).getId();
            }
        }
        return vmList.getFirst().getId();
    }

    @Override
    public void deallocate(int vmId) {
        allocations.put(vmId, Math.max(0, allocations.get(vmId) - 1));
    }

    @Override
    public void onCloudletCompletion(int vmId, Cloudlet cloudlet) {
        String tier = vmTierMap.get(vmId);
        double latency = costModel.latency(cloudlet, tier);
        double energy  = costModel.energy(cloudlet, tier);
        double normalizedLatency = Math.min(1.0, latency / maxLatency);
        double normalizedEnergy = Math.min(1.0, energy / maxEnergy);

        double reward = -(lambda * normalizedLatency + (1 - lambda) * normalizedEnergy);

        // accumulate episode reward
        currentEpisodeReward += reward;

        double oldQ = qValues.get(vmId);
        double maxNextQ = qValues.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double newQ = oldQ + learningRate * (reward + discountFactor * maxNextQ - oldQ);
        qValues.put(vmId, newQ);

        System.out.printf(
                "[Episode %d] VM #%d: L=%.4f s, E=%.4f J, R=%.4f → Q: %.4f → %.4f%n",
                episodeCount, vmId, latency, energy, reward, oldQ, newQ
        );
    }

    /**
     * Prepare for a new episode: snapshot Qs and reset counters.
     */
    public void startNewEpisode() {
        lastEpisodeQ.clear();
        lastEpisodeQ.putAll(qValues);
        currentEpisodeReward = 0.0;
        episodeCount++;
        allocations.replaceAll((k, v) -> 0);
    }

    public void setMaxValues(double maxLatency, double maxEnergy) {
        this.maxLatency = maxLatency;
        this.maxEnergy = maxEnergy;
    }

    /**
     * Check convergence by seeing if Q-values have moved less than threshold.
     */
    public boolean hasConverged() {
        double maxDelta = 0;
        for (var entry : qValues.entrySet()) {
            double prev = lastEpisodeQ.getOrDefault(entry.getKey(), 0.0);
            maxDelta = Math.max(maxDelta, Math.abs(entry.getValue() - prev));
        }
        System.out.println("Max Q-value change: " + maxDelta);
        minDelta = Math.min(minDelta, maxDelta);

        return maxDelta < qValueChangeThreshold;
    }

    public Map<Integer, Double> getQValues() {
        return Collections.unmodifiableMap(qValues);
    }
}
