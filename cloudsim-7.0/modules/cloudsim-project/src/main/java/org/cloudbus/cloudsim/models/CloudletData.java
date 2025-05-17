package org.cloudbus.cloudsim.models;

import lombok.Data;
import org.cloudbus.cloudsim.UtilizationModel;

@Data
public class CloudletData {
    private int id;
    private long length;
    private int pesNumber;
    private long fileSize;
    private long outputSize;
    private UtilizationModel utilizationCpu;
    private UtilizationModel utilizationRam;
    private UtilizationModel utilizationBw;

    // Constructor
    public CloudletData(int id,
                        long length,
                        int pesNumber,
                        long fileSize,
                        long outputSize,
                        UtilizationModel utilizationCpu,
                        UtilizationModel utilizationRam,
                        UtilizationModel utilizationBw) {

        this.id = id;
        this.length = length;
        this.pesNumber = pesNumber;
        this.fileSize = fileSize;
        this.outputSize = outputSize;
        this.utilizationCpu = utilizationCpu;
        this.utilizationRam = utilizationRam;
        this.utilizationBw = utilizationBw;
    }
}