package org.cloudbus.cloudsim;

public class SimCloudletStateHistoryEntry {
	
	private double time;

	private double allocatedMips;
	private double requestedMips;
	private int allocatedRam;
	private int requestedRam;
	private long allocatedIo;
	private long requestedIo;
	private long allocatedBw;
	private long requestedBw;
	
	private int state;
		
	public SimCloudletStateHistoryEntry(double time, double allocatedMips, double requestedMips, int allocatedRam,
			int requestedRam, long allocatedIo, long requestedIo, long allocatedBw, long requestedBw, int state) {
		super();
		this.time = time;
		this.allocatedMips = allocatedMips;
		this.requestedMips = requestedMips;
		this.allocatedRam = allocatedRam;
		this.requestedRam = requestedRam;
		this.allocatedIo = allocatedIo;
		this.requestedIo = requestedIo;
		this.allocatedBw = allocatedBw;
		this.requestedBw = requestedBw;
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

	public int getRequestedRam() {
		return requestedRam;
	}
	public void setRequestedRam(int requestedRam) {
		this.requestedRam = requestedRam;
	}
	public long getAllocatedIo() {
		return allocatedIo;
	}
	public void setAllocatedIo(long allocatedIo) {
		this.allocatedIo = allocatedIo;
	}
	public long getRequestedIo() {
		return requestedIo;
	}
	public void setRequestedIo(long requestedIo) {
		this.requestedIo = requestedIo;
	}
	public long getAllocatedBw() {
		return allocatedBw;
	}
	public void setAllocatedBw(long allocatedBw) {
		this.allocatedBw = allocatedBw;
	}
	public long getRequestedBw() {
		return requestedBw;
	}
	public void setRequestedBw(long requestedBw) {
		this.requestedBw = requestedBw;
	}
	public void setAllocatedRam(int allocatedRam) {
		this.allocatedRam = allocatedRam;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
		
	
}
