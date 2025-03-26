package org.cloudbus.cloudsim.utils;

import org.cloudbus.cloudsim.models.CloudletData;
import org.cloudbus.cloudsim.Cloudlet;


public class CloudletCreator {
    public static Cloudlet createCloudlet(CloudletData data, int userId) {
        // TODO: Receive PEs based on tier
        int pesNumber = 1;
        return new Cloudlet(
            data.getId(),           // Cloudlet ID
            data.getLength(),       // Length of the cloudlet
            pesNumber,              // Number of processing elements required
            data.getFileSize(),     // File size
            data.getOutputSize(),   // Output size
            data.getUtilizationCpu(),  // CPU utilization model (for simplicity)
            data.getUtilizationRam(),  // RAM utilization model
            data.getUtilizationBw()   // Bandwidth utilization model
        );
    }
}
