package org.cloudbus.cloudsim;


//为了方便计算VM的当前时刻利用率，最核心应该在这个POJO中保存当前使用的资源(正在运行的cloudlet资源总和)
//  跟 全部可用资源(allocated)
//vm 资源利用率是  usedMips/allocatedMips
//vm 的sla要看   requestedMips - allocatedMips
public class SimVmStateHistoryEntry {

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

    public SimVmStateHistoryEntry(double time, double allocatedMips, double requestedMips, double usedMips, double allocatedRam, double requestedRam, double usedRam, double allocatedIo, double requestedIo, double usedIo, double allocatedBw, double requestedBw, double usedBw, boolean state) {
        this.time = time;
        this.allocatedMips = allocatedMips;
        this.requestedMips = requestedMips;
        this.usedMips = usedMips;
        this.allocatedRam = allocatedRam;
        this.requestedRam = requestedRam;
        this.usedRam = usedRam;
        this.allocatedIo = allocatedIo;
        this.requestedIo = requestedIo;
        this.usedIo = usedIo;
        this.allocatedBw = allocatedBw;
        this.requestedBw = requestedBw;
        this.usedBw = usedBw;
        this.state = state;
    }

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

    public void setAllocatedRam(int allocatedRam) {
        this.allocatedRam = allocatedRam;
    }

    public double getRequestedRam() {
        return requestedRam;
    }

    public void setRequestedRam(int requestedRam) {
        this.requestedRam = requestedRam;
    }

    public double getAllocatedIo() {
        return allocatedIo;
    }

    public void setAllocatedIo(long allocatedIo) {
        this.allocatedIo = allocatedIo;
    }

    public double getRequestedIo() {
        return requestedIo;
    }

    public void setRequestedIo(long requestedIo) {
        this.requestedIo = requestedIo;
    }

    public double getAllocatedBw() {
        return allocatedBw;
    }

    public void setAllocatedBw(long allocatedBw) {
        this.allocatedBw = allocatedBw;
    }

    public double getRequestedBw() {
        return requestedBw;
    }

    public void setRequestedBw(long requestedBw) {
        this.requestedBw = requestedBw;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }


}
