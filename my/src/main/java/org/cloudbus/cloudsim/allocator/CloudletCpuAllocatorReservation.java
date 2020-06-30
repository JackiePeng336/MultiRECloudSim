package org.cloudbus.cloudsim.allocator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudlet;

public class CloudletCpuAllocatorReservation extends CloudletCpuAllocatorSimple {

	/** The Mips table. */
	private Map<Integer, Double> mipsTable; // 任务-Mips分配记录表
	private Map<Integer, Double> reservedMipsTable;
	private double reservedAvailableMips;

	public CloudletCpuAllocatorReservation(long availableMips) {
		super(availableMips);
		setMipsTable(new HashMap<Integer, Double>());
		setReservedMipsTable(new HashMap<Integer, Double>());
		setReservedAvailableMips(availableMips);
	}

	@Override
	public boolean allocateMipsForSimCloudlets(List<SimCloudlet> list,
			final double time) {
		for (SimCloudlet cl : list)
			allocateMipsForSimCloudlet(cl, time);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cloudsim.provismipsners.MipsProvismipsner#allocateMipsForSimCloudlet(
	 * cloudsim.SimCloudlet, int)
	 */
	@Override
	public boolean allocateMipsForSimCloudlet(SimCloudlet cloudlet, double time) { // 分配Mips给任务
		Log.printLine("AllocateMips For Cloudlet Vm" + cloudlet.getVmId()
				+ " AvaiableMips: " + getAvailableMips() + " cloudlet#"
				+ cloudlet.getCloudletId() + " requeset " + cloudlet.getCurrentRequestedMips(time));
		double requestedMips = cloudlet.getCurrentRequestedMips(time)
				* cloudlet.getNumberOfPes();
		if (getAllocatedMipsForSimCloudlet(cloudlet) == requestedMips) {
			// 如果已经分配了则不分配了
		//	Log.printLine("Mips has allocated to cloudlet#"
		//			+ cloudlet.getCloudletId());
			return false;
		}

		if (getAvailableMips() == 0.0)
			return false;

		if (getAvailableMips() >= requestedMips) { // 有足够Mips分配
			setAvailableMips(getAvailableMips() - requestedMips);
			getMipsTable().put(cloudlet.getCloudletId(), requestedMips);
			return true;
		} 
		return false;
		
	}

	public boolean reserveMipsForSimCloudlet(SimCloudlet cloudlet) {
		Log.printLine("Vm" + cloudlet.getVmId() + " AvaiableReservedMips: "
				+ getReservedAvailableMips() + " cloudlet#"
				+ cloudlet.getCloudletId() + " requeset "
				+ cloudlet.getMaxMips());
		double maxMips = cloudlet.getMaxMips();
		if (getReservedMipsForSimCloudlet(cloudlet) == 0
				&& getReservedAvailableMips() >= maxMips) {
			getReservedMipsTable().put(cloudlet.getCloudletId(), maxMips);
			setReservedAvailableMips(getReservedAvailableMips() - maxMips);
			return true;
		}
		Log.printLine("Vm" + cloudlet.getVmId() + ": cloudlet#"
				+ cloudlet.getCloudletId() + " Has Reserved Mips "
				+ getReservedMipsForSimCloudlet(cloudlet) + "/"
				+ cloudlet.getMaxMips());
		return false;
	}

	public double getReservedMipsForSimCloudlet(SimCloudlet cloudlet) { // 获得分配给任务的Mips
		if (getReservedMipsTable().containsKey(cloudlet.getCloudletId())) {
			return getReservedMipsTable().get(cloudlet.getCloudletId());
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cloudsim.provismipsners.MipsProvismipsner#getAllocatedMipsForSimCloudlet
	 * (cloudsim.SimCloudlet)
	 */
	@Override
	public double getAllocatedMipsForSimCloudlet(SimCloudlet cloudlet) { // 获得分配给任务的Mips
		if (getMipsTable().containsKey(cloudlet.getCloudletId())) {
			return getMipsTable().get(cloudlet.getCloudletId());
		}
		return 0;
	}

	@Override
	public boolean isSuitableForSimCloudlet(SimCloudlet cloudlet) { // 检查Mips是否足够
		//Log.printLine("getReservedAvailableMips() "+getReservedAvailableMips()+" REQUEST "+cloudlet.getMaxMips());
		return getReservedAvailableMips() >= cloudlet.getMaxMips()
				* cloudlet.getNumberOfPes();
	}

	@Override
	public boolean isSuitableForSimCloudlet(SimCloudlet cloudlet,
			final double time) { // 检查Mips是否足够
		/*Log.printLine("CLoudlet#" + cloudlet.getCloudletId()
				+ " check ReservedAvailableMips " + getReservedAvailableMips()
				+ " MaxMips:" + cloudlet.getMaxMips() + " getAvailableMips():"
				+ getAvailableMips() + " cl REquest:"
				+ cloudlet.getCurrentRequestedMips(time));*/
		if(getReservedMipsForSimCloudlet(cloudlet)!=0){
			if(getReservedMipsForSimCloudlet(cloudlet)==(cloudlet.getMaxMips())){
				//Log.printLine("isSuitableForSimCloudlet has reserved cloudlet#"+cloudlet.getCloudletId()+" mips "+getReservedMipsForSimCloudlet(cloudlet));
				}
			return true;
			}
		//Log.printLine("ReservedAva"+(getReservedAvailableMips() >= cloudlet.getMaxMips())+" avail "+(getAvailableMips() >= cloudlet
		//		.getCurrentRequestedMips(time)));
		return (getReservedAvailableMips() >= cloudlet.getMaxMips()
				* cloudlet.getNumberOfPes() && (getAvailableMips() >= cloudlet
				.getCurrentRequestedMips(time)));
	}

	public void dereserveMipsForSimCloudlet(SimCloudlet cloudlet) { // 释放分配给任务的Mips
		if (getReservedMipsTable().containsKey(cloudlet.getCloudletId())) {
			Double amountFreed = getReservedMipsTable().remove(
					cloudlet.getCloudletId());
			Log.printLine("Cloudlet#"+cloudlet.getCloudletId()+" dereserveMips " + amountFreed+" original" + getReservedAvailableMips());
			setReservedAvailableMips(getReservedAvailableMips() + amountFreed);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cloudsim.provismipsners.MipsProvismipsner#deallocateMipsForSimCloudlet
	 * (cloudsim.SimCloudlet)
	 */
	@Override
	public void deallocateMipsForSimCloudlet(SimCloudlet cloudlet) { // 释放分配给任务的Mips
		if (getMipsTable().containsKey(cloudlet.getCloudletId())) {
			Double amountFreed = getMipsTable()
					.remove(cloudlet.getCloudletId());
			setAvailableMips(getAvailableMips() + amountFreed);
		}
	}

	public void dereserveMipsForAllSimCloudlets() { // 释放所有任务的Mips
		setReservedAvailableMips(getMips());
		getReservedMipsTable().clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cloudsim.provismipsners.MipsProvismipsner#deallocateMipsForSimCloudlet
	 * (cloudsim.SimCloudlet)
	 */
	@Override
	public void deallocateMipsForAllSimCloudlets() { // 释放所有任务的Mips
		super.deallocateMipsForAllSimCloudlets();
		getMipsTable().clear();
	}

	public void resetMipsTable() { // 重置记录表
		getMipsTable().clear();
	}

	/**
	 * Gets the mips table.
	 * 
	 * @return the mips table
	 */
	protected Map<Integer, Double> getMipsTable() {
		return mipsTable;
	}

	protected void setMipsTable(Map<Integer, Double> mipsTable) {
		this.mipsTable = mipsTable;
	}

	public Map<Integer, Double> getReservedMipsTable() {
		return reservedMipsTable;
	}

	public void setReservedMipsTable(Map<Integer, Double> reservedMipsTable) {
		this.reservedMipsTable = reservedMipsTable;
	}

	public double getReservedAvailableMips() {
		return reservedAvailableMips;
	}

	public void setReservedAvailableMips(double reservedAvailableMips) {
		this.reservedAvailableMips = reservedAvailableMips;
	}

}
