package org.cloudbus.cloudsim.examples;

public class CloudletData {
    private int id;
    private long length;
    private long fileSize;
    private long outputSize;
    private double utilizationCpu;
    private double utilizationRam;
    private double utilizationBw;

    // Default constructor (required for Jackson)
    public CloudletData() { }

    public CloudletData(int id, long length, long fileSize, long outputSize, double utilizationCpu, double utilizationRam, double utilizationBw) {
        this.id = id;
        this.length = length;
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

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public long getOutputSize() { return outputSize; }
    public void setOutputSize(long outputSize) { this.outputSize = outputSize; }

    public double getUtilizationCpu() { return utilizationCpu; }
    public void setUtilizationCpu(double utilizationCpu) { this.utilizationCpu = utilizationCpu; }

    public double getUtilizationRam() { return utilizationRam; }
    public void setUtilizationRam(double utilizationRam) { this.utilizationRam = utilizationRam; }

    public double getUtilizationBw() { return utilizationBw; }
    public void setUtilizationBw(double utilizationBw) { this.utilizationBw = utilizationBw; }
}