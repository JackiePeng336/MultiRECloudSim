package org.cloudbus.cloudsim;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SimCloudlet extends Cloudlet {

	private long mips; // 100%标准Mips
	private double maxMips; // 最大Mips需求
	private int ram; // 任务需求内存
	private long io; // 任务需求IO
	private long bw; // 任务需求带宽
	private long tolerateTime;//最大等待时间
	private UtilizationModel utilizationModelIo; // IO利用率模型
	private List<SimCloudletStateHistoryEntry> stateHistory = new LinkedList<SimCloudletStateHistoryEntry>();

	public SimCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize,
			long cloudletOutputSize, long mips, int ram, long io, long bw, UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam, UtilizationModel utilizationModelIo,
			UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw);
		setMips(mips);
		setMaxMips(Double.MAX_VALUE);
		setRam(ram);
		setIo(io);
		setBw(bw);
		setUtilizationModelIo(utilizationModelIo);
	}
	public SimCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize,
			long cloudletOutputSize, long mips, int ram, long io, long bw,long tolerateTime, UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam, UtilizationModel utilizationModelIo,
			UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw);
		setMips(mips);
		setMaxMips(Double.MAX_VALUE);
		setRam(ram);
		setIo(io);
		setBw(bw);
		setTolerateTime(tolerateTime);
		setUtilizationModelIo(utilizationModelIo);
	}
	public double getCurrentRequestedMips(final double time) { // 获取目前需要的Mips
		// Log.printLine("getCurrentRequestedMips :"+ getUtilizationOfCpu(time)+
		// " mips:"+getMips());
		//double ut = getUtilizationOfCpu(time);
		double aa = (getUtilizationOfCpu(time) * getMips());
		return aa;
	}

	public double getCurrentRequestedRam(final double time) { // 获取目前需要的ram
		// Log.printLine("getCurrentRequestedMips :"+ getUtilizationOfCpu(time)+
		// " mips:"+getMips());
		double aa = (getUtilizationOfRam(time) * getRam() );
		return aa;
	}

	public double getCurrentRequestedIo(final double time) { // 获取目前需要的Io
		// Log.printLine("getCurrentRequestedMips :"+ getUtilizationOfCpu(time)+
		// " mips:"+getMips());
		double aa = (getUtilizationOfIO(time) * getIo() );
		return aa;
	}

	@Override
	public double getUtilizationOfCpu(final double time) { // 获取任务当前CPU利用率
		if (time > getExecStartTime())
			return getUtilizationModelCpu().getUtilization(time - getExecStartTime());
		return getUtilizationModelCpu().getUtilization(time);
	}

	@Override
	public double getUtilizationOfRam(final double time) { // 获取任务当前内存利用率
		if (time > getExecStartTime())
			return getUtilizationModelRam().getUtilization(time - getExecStartTime());
		return getUtilizationModelRam().getUtilization(time);
	}

	public double getUtilizationOfIO(final double time) { // 获取任务当前IO利用率
		if (time > getExecStartTime())
			return getUtilizationModelIo().getUtilization(time - getExecStartTime());
		return getUtilizationModelIo().getUtilization(time);
	}

	@Override
	public double getUtilizationOfBw(final double time) { // 获取任务当前带宽利用率
		if (time > getExecStartTime())
			return getUtilizationModelBw().getUtilization(time - getExecStartTime());
		return getUtilizationModelIo().getUtilization(time);
	}

	public long getMips() {
		return mips;
	}

	public void setMips(long mips) {
		this.mips = mips;
	}

	public double getMaxMips() { // 获取任务的最大需求Mips
		if (maxMips == Double.MAX_VALUE) {
			if (getUtilizationModelCpu() instanceof UtilizationModelByFile) {
				List<Double> loadData = ((UtilizationModelByFile) getUtilizationModelCpu()).getData();
				maxMips = getMips() * Collections.max(loadData);
			} else {
				maxMips = getMips() * getUtilizationModelCpu().getUtilization(0.0);
			}
		}

		return (double) (long) maxMips;
	}

	public double getAverageMips() { // 获取任务的平均Mips
		double aveMips = Double.MAX_VALUE;
		if (getUtilizationModelCpu() instanceof UtilizationModelByFile) {
			aveMips = getMips() * ((UtilizationModelByFile) getUtilizationModelCpu()).getAverageLoad();
		} else {
			aveMips = getMips() * getUtilizationModelCpu().getUtilization(0.0);
		}
		return aveMips;
	}

	public void addStateHistoryEntry(double time, double allocatedMips, double requestedMips, int allocateRam,
			int requestedRam, long allocatedIo, long requestedIo, long allocatedBw, long requestedBw,
			int state) {
		SimCloudletStateHistoryEntry newState = new SimCloudletStateHistoryEntry(time, allocatedMips, requestedMips,
				allocateRam, requestedRam, allocatedIo, requestedIo, allocatedBw, requestedBw, state);
		if (!getStateHistory().isEmpty()) {
			SimCloudletStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
			if (previousState.getTime() == time) {
				getStateHistory().set(getStateHistory().size() - 1, newState);
				return;
			}
		}
		getStateHistory().add(newState);
	}

	public void setMaxMips(double maxMips) {
		this.maxMips = maxMips;
	}

	public int getRam() {
		return ram;
	}

	public void setRam(int ram) {
		this.ram = ram;
	}

	public long getIo() {
		return io;
	}

	public void setIo(long io) {
		this.io = io;
	}

	public long getBw() {
		return bw;
	}

	public void setBw(long bw) {
		this.bw = bw;
	}
	
	public long getTolerateTime() {
		return tolerateTime;
	}
	public void setTolerateTime(long tolerateTime) {
		this.tolerateTime = tolerateTime;
	}

	public UtilizationModel getUtilizationModelIo() {
		return utilizationModelIo;
	}

	public void setUtilizationModelIo(UtilizationModel utilizationModelIo) {
		this.utilizationModelIo = utilizationModelIo;
	}

	public List<SimCloudletStateHistoryEntry> getStateHistory() {
		return stateHistory;
	}

	public void setStateHistory(List<SimCloudletStateHistoryEntry> stateHistory) {
		this.stateHistory = stateHistory;
	}

}
