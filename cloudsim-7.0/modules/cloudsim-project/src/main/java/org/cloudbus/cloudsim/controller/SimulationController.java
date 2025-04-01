package org.cloudbus.cloudsim.controller;

/*
 * Title:        Simulation Controller
 * Description:  The Simulation Controller is responsible for handling the simulation process. It is the main class that will be used to start the simulation.
 * Copyright (c) 2025, The University of the Philippines Diliman
 * Electrical and Electronics Engineering Institute (EEEI), Smart Systems Laboratory (SSL)
 * Authors: Cagas, Mark Nicholas; Saw, Christyne Joie
 */

import org.cloudbus.cloudsim.Cloudlet;

import java.util.List;

public class SimulationController {
    public static void main(String[] args) throws Exception {
        SimulationManager simulationManager = new SimulationManager();
        simulationManager.initializeSimulation();
        List<Cloudlet> cloudlets = simulationManager.setupCloudlets();
        simulationManager.runSimulation(cloudlets);
        simulationManager.analyzeResults();
    }
}