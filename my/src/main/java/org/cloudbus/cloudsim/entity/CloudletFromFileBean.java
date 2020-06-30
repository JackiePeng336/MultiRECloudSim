package org.cloudbus.cloudsim.entity;

public class CloudletFromFileBean {

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getMips() {
        return mips;
    }

    public void setMips(double mips) {
        this.mips = mips;
    }

    public double getRam() {
        return ram;
    }

    public void setRam(double ram) {
        this.ram = ram;
    }

    public double getIo() {
        return io;
    }

    public void setIo(double io) {
        this.io = io;
    }

    public double getBw() {
        return bw;
    }

    public void setBw(double bw) {
        this.bw = bw;
    }

    public CloudletFromFileBean(String label, double length, double mips, double ram, double io, double bw) {
        this.label = label;
        this.length = length;
        this.mips = mips;
        this.ram = ram;
        this.io = io;
        this.bw = bw;
    }

    private String label;
    private double length;
    private double mips;
    private double ram;
    private double io;
    private double bw;


}
