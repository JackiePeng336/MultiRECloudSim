package org.cloudbus.cloudsim.allocator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudlet;
import org.cloudbus.cloudsim.util.Constants;

public class CloudletCpuAllocatorSimple extends CloudletCpuAllocator {

	/** The Mips table. */
	private Map<Integer, Double> mipsTable; // 任务-Mips分配记录表
	private Map<Integer, Double> mipsAddTable; // 任务-Mips分配记录表

	private long mipsCap;
	
	public CloudletCpuAllocatorSimple(long availableMips) {
		super(availableMips);
		setMipsCap(availableMips);
		setMipsTable(new HashMap<Integer, Double>());
		setMipsAddTable(new HashMap<Integer, Double>());
	}

	@Override
	public boolean allocateMipsForSimCloudlets(List<SimCloudlet> list,
			final double time) { //
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
		boolean recover = false;
		// 本来可以输出
		if(!Log.isDisabled()){
			// 但是这边不让输出
			if(!Constants.allocateForSimCloudletInAllocator){
				Log.disable();
				recover = true;
			}
		}else {
			if(Constants.allocateForSimCloudletInAllocator){
				Log.enable();
				recover = true;
			}
		}
		double requestedMips = cloudlet.getCurrentRequestedMips(time)
				* cloudlet.getNumberOfPes();
		if (getAllocatedMipsForSimCloudlet(cloudlet) == requestedMips) {
			// 如果已经分配了则不分配了
			//Log.printLine("Mips has allocated to cloudlet#"
			//		+ cloudlet.getCloudletId());
			if(recover){
				if(Log.isDisabled()){
					Log.enable();
				}else {
					Log.disable();
				}
			}
			return false;
		}
		if (getAvailableMips() == 0.0)
		{
			if(recover){
				if(Log.isDisabled()){
					Log.enable();
				}else {
					Log.disable();
				}
			}
			return false;
		}
		
		if (getAvailableMips() >= requestedMips) { // 有足够Mips分配
			Log.printLine("AllocateMips For Cloudlet Vm" + cloudlet.getVmId()
			+ " AvaiableMips: " + getAvailableMips() + " cloudlet#"
			+ cloudlet.getCloudletId() + " requeset " + cloudlet.getMips());
			setAvailableMips(getAvailableMips() - requestedMips);
			getMipsTable().put(cloudlet.getCloudletId(), requestedMips);
			if(recover){
				if(Log.isDisabled()){
					Log.enable();
				}else {
					Log.disable();
				}
			}
			
			return true;
		} else {
			getMipsTable().put(cloudlet.getCloudletId(), getAvailableMips());
			setAvailableMips(0.0);
			Log.printLine("AllocateMips For Cloudlet Vm" + cloudlet.getVmId()
			+ " AvaiableMips: " + getAvailableMips() + " cloudlet#"
			+ cloudlet.getCloudletId() + " requeset " + cloudlet.getMips());
			if(recover){
				if(Log.isDisabled()){
					Log.enable();
				}else {
					Log.disable();
				}
			}
			return true;
		}


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

	public boolean addMipsForSimCloudlet(SimCloudlet cloudlet, double time){
		double requestedMips = cloudlet.getCurrentRequestedMips(time)
				* cloudlet.getNumberOfPes();
		if (!getMipsAddTable().containsKey(cloudlet.getCloudletId())) {
			getMipsAddTable().put(cloudlet.getCloudletId(), requestedMips);
			return true;
		}else return false;
	}
	
	public void deaddMipsForAllSimCloudlet(){
		getMipsAddTable().clear();
	}
	
	public void deaddMipsForSimCloudlet(SimCloudlet cloudlet) { // 释放分配给任务的Mips
		if (getMipsAddTable().containsKey(cloudlet.getCloudletId())) {
			getMipsAddTable().remove(cloudlet.getCloudletId());
			
		}
	}
	
	public boolean isMipsAddForSimCloudlet(SimCloudlet cloudlet){
		return getMipsAddTable().containsKey(cloudlet.getCloudletId());
	}
	
	@Override
	public boolean isSuitableForSimCloudlet(SimCloudlet cloudlet) { // 检查Mips是否足够
		return true;
		//return getAvailableMips() >= cloudlet.getMips() * cloudlet.getNumberOfPes();
	}

	@Override
	public boolean isSuitableForSimCloudlet(SimCloudlet cloudlet,
			final double time) { // 检查Mips是否足够
		return true;
		//return getAvailableMips() >= cloudlet.getCurrentRequestedMips(time) * cloudlet.getNumberOfPes();
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

	/**
	 * Sets the mips table.
	 * 
	 * @pamips mipsTable the mips table
	 */
	protected void setMipsTable(Map<Integer, Double> mipsTable) {
		this.mipsTable = mipsTable;
	}

	protected Map<Integer, Double> getMipsAddTable() {
		return mipsAddTable;
	}

	protected void setMipsAddTable(Map<Integer, Double> mipsAddTable) {
		this.mipsAddTable = mipsAddTable;
	}

	public long getMipsCap() {
		return mipsCap;
	}

	public void setMipsCap(long mipsCap) {
		this.mipsCap = mipsCap;
	}
}
