package org.cloudbus.cloudsim.policies;

import lombok.Data;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.cost.CostModel;
import org.cloudbus.cloudsim.utils.CreateVm;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A Q-learning based offloading policy that jointly optimizes latency and energy.
 * Reward = - (latency + λ * energy)
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
    private final Set<Integer> visitedVms = new HashSet<>();

    private double currentEpisodeReward = 0.0;
    private double currentTemperature;

    private double learningRate;      // α
    private double discountFactor;    // γ

    private double qValueChangeThreshold = 1e-3;
    private final double initialTemperature = 1.0;
    private final double minimumTemperature = 0.1;
    private final double decayRate = 0.995;
    private int episodeCount = 0;
    private double minDelta = Double.MAX_VALUE;

    private double maxLatency;
    private double maxEnergy;
    private Random random;

    public RLOffloadingPolicy(CostModel costModel, double L_MAX, double lambda, double alpha, double gamma, long seed) {
        this.costModel = Objects.requireNonNull(costModel, "costModel");
        this.L_MAX    = L_MAX;
        this.lambda   = lambda;
        this.learningRate = alpha;
        this.discountFactor = gamma;
        this.random = new Random(seed);
    }

    @Override
    public void initialize(List<Vm> vmList) {
        if (vmList == null || vmList.isEmpty()) {
            throw new IllegalArgumentException("VM list must be non-null and non-empty");
        }
        this.vmList = new ArrayList<>(vmList);
        allocations = new HashMap<>();
        vmTierMap = new HashMap<>();
        visitedVms.clear();
        Map<Integer, String> globalTierMap = CreateVm.getVmTierMap();

        if (qValues == null || qValues.isEmpty()) {
            qValues = new HashMap<>();
        }
        for (Vm vm : vmList) {
            int id = vm.getId();
            qValues.putIfAbsent(vm.getId(), 0.0);
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
        currentTemperature = decayTemperature();
        int vmId = selectVmWithSoftmax(currentTemperature);
        allocations.put(vmId, allocations.get(vmId) + 1);
        visitedVms.add(vmId);
        System.out.printf("[Episode %d] SOFTMAX @ T=%.3f → VM #%d (Q=%.4f)%n",
                episodeCount, currentTemperature, vmId, qValues.get(vmId));
        return vmId;
    }

    private double decayTemperature() {
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

    public void saveQValues(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Double> toSave = qValues.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        Map.Entry::getValue
                ));
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(path), toSave);
    }

    public void loadQValues(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) return;
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Double> loaded = mapper.readValue(
                file, new TypeReference<Map<String, Double>>() {}
        );

        if (qValues == null) {
            qValues = new HashMap<>();
        }
        qValues.clear();

        for (var e : loaded.entrySet()) {
            qValues.put(Integer.valueOf(e.getKey()), e.getValue());
        }
    }
}
