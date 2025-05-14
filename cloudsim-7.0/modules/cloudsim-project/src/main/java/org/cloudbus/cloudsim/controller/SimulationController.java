package org.cloudbus.cloudsim.controller;

/*
 * Title:        Simulation Controller
 * Description:  The Simulation Controller is responsible for handling the simulation process. It is the main class that will be used to start the simulation.
 * Copyright (c) 2025, The University of the Philippines Diliman
 * Electrical and Electronics Engineering Institute (EEEI), Smart Systems Laboratory (SSL)
 * Authors: Cagas, Mark Nicholas; Saw, Christyne Joie
 */

import org.cloudbus.cloudsim.cost.CostModel;
import org.cloudbus.cloudsim.cost.HeuristicCostModel;
import org.cloudbus.cloudsim.policies.DynamicThrottled;
import org.cloudbus.cloudsim.policies.OffloadingPolicy;
import org.cloudbus.cloudsim.policies.RLOffloadingPolicy;
import org.cloudbus.cloudsim.policies.StaticEqualDistribution;
import org.cloudbus.cloudsim.utils.CloudletReader;


public class SimulationController {
    public static void main(String[] args) throws Exception {
        // ------- Simulation setup parameters -------
        double L_MAX = 0.025 * CloudletReader.readCloudletData().size();              // maximum allowable latency in seconds
        double energyWeight = 0.5;       // lambda: weight for energy in RL reward
        CostModel costModel = new HeuristicCostModel();

        // choose one of: StaticEqualDistribution(), DynamicThrottled(), RLOffloadingPolicy(costModel, L_MAX, energyWeight)
//        OffloadingPolicy policy = new StaticEqualDistribution();
        OffloadingPolicy policy = new DynamicThrottled();
//        OffloadingPolicy policy = new RLOffloadingPolicy(costModel, L_MAX, energyWeight);
        SimulationManager simulationManager = new SimulationManager(policy);
        simulationManager.runOffloadingSimulation();
    }
}