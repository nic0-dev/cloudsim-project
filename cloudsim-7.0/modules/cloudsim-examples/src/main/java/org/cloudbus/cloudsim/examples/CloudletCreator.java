package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModelFull;
import java.util.ArrayList;
import java.util.List;

public class CloudletCreator {
    public static List<Cloudlet> createCloudlets(List<CloudletData> dataList, int pesNumber, int userId) {
        List<Cloudlet> cloudlets = new ArrayList<>();

        for (CloudletData data : dataList) {
            Cloudlet cloudlet = new Cloudlet(
                data.getId(),           // Cloudlet ID
                data.getLength(),       // Length of the cloudlet
                pesNumber,              // Number of processing elements required
                data.getFileSize(),     // File size
                data.getOutputSize(),   // Output size
                new UtilizationModelFull(),  // CPU utilization model (for simplicity)
                new UtilizationModelFull(),  // RAM utilization model
                new UtilizationModelFull()   // Bandwidth utilization model
            );
            cloudlet.setUserId(userId);
            cloudlets.add(cloudlet);
        }
        return cloudlets;
    }
}
