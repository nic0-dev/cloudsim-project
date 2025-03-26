package org.cloudbus.cloudsim.models;

import org.cloudbus.cloudsim.UtilizationModel;

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

    // Getters and setters for each field
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getLength() { return length; }
    public void setLength(long length) { this.length = length; }

    public int getPesNumber() { return pesNumber; }
    public void setPesNumber(int pesNumber) { this.pesNumber = pesNumber; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public long getOutputSize() { return outputSize; }
    public void setOutputSize(long outputSize) { this.outputSize = outputSize; }

    public UtilizationModel getUtilizationCpu() { return utilizationCpu; }
    public void setUtilizationCpu(UtilizationModel utilizationCpu) { this.utilizationCpu = utilizationCpu; }

    public UtilizationModel getUtilizationRam() { return utilizationRam; }
    public void setUtilizationRam(UtilizationModel utilizationRam) { this.utilizationRam = utilizationRam; }

    public UtilizationModel getUtilizationBw() { return utilizationBw; }
    public void setUtilizationBw(UtilizationModel utilizationBw) { this.utilizationBw = utilizationBw; }
}