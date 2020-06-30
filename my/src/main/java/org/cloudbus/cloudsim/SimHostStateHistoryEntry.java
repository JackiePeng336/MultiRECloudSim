package org.cloudbus.cloudsim;

public class SimHostStateHistoryEntry {

    @Override
    public String toString() {
        return "SimHostStateHistoryEntry{" +
                "time=" + time +
                ", allocatedMips=" + allocatedMips +
                ", requestedMips=" + requestedMips +
                ", usedMips=" + usedMips +
                ", allocatedRam=" + allocatedRam +
                ", requestedRam=" + requestedRam +
                ", usedRam=" + usedRam +
                ", allocatedIo=" + allocatedIo +
                ", requestedIo=" + requestedIo +
                ", usedIo=" + usedIo +
                ", allocatedBw=" + allocatedBw +
                ", requestedBw=" + requestedBw +
                ", usedBw=" + usedBw +
                ", state=" + state +
                '}';
    }

    public SimHostStateHistoryEntry(double time, double allocatedMips, double requestedMips,
                                    double allocatedRam, double requestedRam,
                                    double allocatedIo, double requestedIo,
                                    boolean state) {
        this.time = time;
        this.allocatedMips = allocatedMips;
        this.requestedMips = requestedMips;
        //this.usedMips = usedMips;
        this.allocatedRam = allocatedRam;
        this.requestedRam = requestedRam;
        //this.usedRam = usedRam;
        this.allocatedIo = allocatedIo;
        this.requestedIo = requestedIo;
        //this.usedIo = usedIo;
//        this.allocatedBw = allocatedBw;
//        this.requestedBw = requestedBw;
//        this.usedBw = usedBw;
        this.state = state;
    }

    private double time;

    private double allocatedMips;
    private double requestedMips;
    private double usedMips;

    private double allocatedRam;
    private double requestedRam;
    private double usedRam;

    private double  allocatedIo;
    private double  requestedIo;
    private double  usedIo;

    private double  allocatedBw;
    private double  requestedBw;
    private double  usedBw;

    public double getUsedMips() {
        return usedMips;
    }

    public void setUsedMips(double usedMips) {
        this.usedMips = usedMips;
    }

    public double getUsedRam() {
        return usedRam;
    }

    public void setUsedRam(int usedRam) {
        this.usedRam = usedRam;
    }

    public double getUsedIo() {
        return usedIo;
    }

    public void setUsedIo(long usedIo) {
        this.usedIo = usedIo;
    }

    public double getUsedBw() {
        return usedBw;
    }

    public void setUsedBw(long usedBw) {
        this.usedBw = usedBw;
    }

    private boolean state;

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getAllocatedMips() {
        return allocatedMips;
    }

    public void setAllocatedMips(double allocatedMips) {
        this.allocatedMips = allocatedMips;
    }

    public double getRequestedMips() {
        return requestedMips;
    }

    public void setRequestedMips(double requestedMips) {
        this.requestedMips = requestedMips;
    }

    public double getAllocatedRam() {
        return allocatedRam;
    }

    public void setAllocatedRam(double allocatedRam) {
        this.allocatedRam = allocatedRam;
    }

    public double getRequestedRam() {
        return requestedRam;
    }

    public void setRequestedRam(double requestedRam) {
        this.requestedRam = requestedRam;
    }

    public void setUsedRam(double usedRam) {
        this.usedRam = usedRam;
    }

    public double getAllocatedIo() {
        return allocatedIo;
    }

    public void setAllocatedIo(double allocatedIo) {
        this.allocatedIo = allocatedIo;
    }

    public double getRequestedIo() {
        return requestedIo;
    }

    public void setRequestedIo(double requestedIo) {
        this.requestedIo = requestedIo;
    }

    public void setUsedIo(double usedIo) {
        this.usedIo = usedIo;
    }

    public double getAllocatedBw() {
        return allocatedBw;
    }

    public void setAllocatedBw(double allocatedBw) {
        this.allocatedBw = allocatedBw;
    }

    public double getRequestedBw() {
        return requestedBw;
    }

    public void setRequestedBw(double requestedBw) {
        this.requestedBw = requestedBw;
    }

    public void setUsedBw(double usedBw) {
        this.usedBw = usedBw;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
