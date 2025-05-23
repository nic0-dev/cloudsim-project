package org.cloudbus.cloudsim.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GraphData {
    public Map<Integer, EpisodeSnapshot> episodeMetrics = new TreeMap<>();
    public Map<Integer, QSnapshot> qSnapshots = new TreeMap<>();
    public List<MovingAverage> movingAverages = new ArrayList<>();
    public TradeoffPoint bestTradeoff = null;

    public static class QSnapshot {
        public Map<Integer, Double> qValues;
        public List<Integer> vmUsed;
    }

    public static class EpisodeSnapshot {
        public double reward;
        public double temperature;
    }

    public static class TradeoffPoint {
        public double normalizedLatency;
        public double normalizedEnergy;
    }

    public static class MovingAverage {
        public double maReward;
        public double reward;
        public double temperature;
        public Integer episode;
    }
}
