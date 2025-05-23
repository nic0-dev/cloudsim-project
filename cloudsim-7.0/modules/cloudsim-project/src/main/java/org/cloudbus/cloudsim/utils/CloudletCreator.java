package org.cloudbus.cloudsim.utils;

import org.cloudbus.cloudsim.models.CloudletData;
import org.cloudbus.cloudsim.Cloudlet;


public class CloudletCreator {
    private static final int pesNumber = 1;
    /**
     * Creates a Cloudlet object based on the provided CloudletData.
     *
     * @param data The CloudletData object containing the parameters for the Cloudlet.
     * @return A new Cloudlet object.
     */
    public static Cloudlet createCloudlet(CloudletData data) {
        return new Cloudlet(
            data.getId(),               // Cloudlet ID
            data.getLength(),           // Length of the cloudlet
            pesNumber,                  // Number of processing elements required
            data.getFileSize(),         // File size
            data.getOutputSize(),       // Output size
            data.getUtilizationCpu(),   // CPU utilization model (for simplicity)
            data.getUtilizationRam(),   // RAM utilization model
            data.getUtilizationBw()     // Bandwidth utilization model
        );
    }
}
