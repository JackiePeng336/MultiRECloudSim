package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.SimCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.SimVmStateHistoryEntry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SimPowerVm extends PowerVm {

	private long io;	//虚拟机Io
	private long currentAllocatedIo;	//目前分配的IO

	public List<SimVmStateHistoryEntry> getMultireStateHistory() {
		return multireStateHistory;
	}

	public void setMultireStateHistory(List<SimVmStateHistoryEntry> multireStateHistory) {
		this.multireStateHistory = multireStateHistory;
	}


	private List<Double> cachedRestoreRequestedMips;
	private int cachedRestoreRequestedRam;
	private long cachedRestoreRequestedIo;

	private boolean restore = false;
	//新添加的
	private List<SimVmStateHistoryEntry> multireStateHistory = new LinkedList<>();
	
	public SimPowerVm(final int id, final int userId, final double mips,
			final int pesNumber, final int ram, long  io, final long bw, final long size,
			final int priority, final String vmm,
			final CloudletScheduler cloudletScheduler,
			final double schedulingInterval) {
		super(id, userId, mips, pesNumber, ram, bw, size, priority, vmm,
				cloudletScheduler, schedulingInterval);
		setIo(io);
	}


	public long getCurrentRequestedIo() {	//获取目前需求的Io
		if(restore){
			return getCachedRestoreRequestedIo();
		}

		long res = (long) (((SimCloudletSchedulerDynamicWorkload) getCloudletScheduler())
				.getCurrentRequestedUtilizationOfIo() * getIo());

//        if(this.isInMigration()){
//            return (long) (Constants.MIGRATING_IN_DEGRADATION * res);
//        }

		if (isBeingInstantiated()) {
			return getIo();
		}
		return res ;
	}


	@Override
	public List<Double> getCurrentRequestedMips() {
		if(restore){
			if(getCachedRestoreRequestedMips() == null){
				//Log.formatLine("VM: %d, Host: %d", getId(), getHost().getId());
				System.out.println(getCurrentRequestedRam());
				System.out.println(getCurrentRequestedIo());
				System.exit(1);
			}
			return getCachedRestoreRequestedMips();
		}

		List<Double> currentRequestedMips = getCloudletScheduler().getCurrentRequestedMips();
//        if(this.isInMigration()){
//            int ind = 0;
//            for(double d : currentRequestedMips){
//                currentRequestedMips.set(ind++, d * Constants.MIGRATING_IN_DEGRADATION);
//            }
//            return currentRequestedMips;
//        }



		if (isBeingInstantiated()) {
			currentRequestedMips = new ArrayList<Double>();
			for (int i = 0; i < getNumberOfPes(); i++) {
				currentRequestedMips.add(getMips());
			}
		}
		return currentRequestedMips;
	}


	@Override
	public int getCurrentRequestedRam() {
		if(restore){
			return getCachedRestoreRequestedRam();
		}

		int res = (int) (getCloudletScheduler()
				.getCurrentRequestedUtilizationOfRam() * getRam());

//        if(this.isInMigration()){
//            return (int) (Constants.MIGRATING_IN_DEGRADATION * res);
//        }

		if (isBeingInstantiated()) {
			return getRam();
		}
		return (int) (getCloudletScheduler().getCurrentRequestedUtilizationOfRam() * getRam());
	}


	public void addStateHistoryEntry(double time,
									 double allocatedMips, double requestedMips, double usedMips,
									 double allocatedRam, double requestedRam, double usedRam,
									 double allocatedIo, double requestedIo, double usedIo,
									 double allocatedBw, double requestedBw, double usedBw,
									 boolean isInMigration) {
		super.addStateHistoryEntry(time, allocatedMips, requestedMips, isInMigration);

		SimVmStateHistoryEntry newState = new SimVmStateHistoryEntry(time,
				allocatedMips, requestedMips, usedMips,
				allocatedRam,requestedRam,usedRam,
				allocatedIo, requestedIo, usedIo,
				allocatedBw, requestedBw, usedBw,isInMigration);

		//判断时间相等的话就要覆盖
		if (!getMultireStateHistory().isEmpty()) {
			SimVmStateHistoryEntry previousState = getMultireStateHistory().get(getMultireStateHistory().size() - 1);
			if (previousState.getTime() == time) {
				getMultireStateHistory().set(getStateHistory().size() - 1, newState);
				return;
			}
		}

		getMultireStateHistory().add(newState);

	}

	public long getIo() {
		return io;
	}

	public void setIo(long io) {
		this.io = io;
	}

	public long getCurrentAllocatedIo() {
		return currentAllocatedIo;
	}

	public void setCurrentAllocatedIo(long currentAllocatedIo) {
		this.currentAllocatedIo = currentAllocatedIo;
	}

	public List<Double> getCachedRestoreRequestedMips() {
		return cachedRestoreRequestedMips;
	}

	public void setCachedRestoreRequestedMips(List<Double> cachedRestoreRequestedMips) {
		if(this.cachedRestoreRequestedMips == null){
			this.cachedRestoreRequestedMips = new LinkedList<>();
		}
		if(cachedRestoreRequestedMips == null){
			this.cachedRestoreRequestedMips = null;
			return;
		}
		for(double d : cachedRestoreRequestedMips){
			this.cachedRestoreRequestedMips.add(d);
		}
	}

	public int getCachedRestoreRequestedRam() {
		return cachedRestoreRequestedRam;
	}

	public void setCachedRestoreRequestedRam(int cachedRestoreRequestedRam) {
		this.cachedRestoreRequestedRam = cachedRestoreRequestedRam;
	}

	public long getCachedRestoreRequestedIo() {
		return cachedRestoreRequestedIo;
	}

	public void setCachedRestoreRequestedIo(long cachedRestoreRequestedIo) {
		this.cachedRestoreRequestedIo = cachedRestoreRequestedIo;
	}

	public boolean isRestore() {
		return restore;
	}

	public void setRestore(boolean restore) {
		this.restore = restore;
	}
}
