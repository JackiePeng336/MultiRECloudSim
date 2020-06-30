/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.allocator;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudlet;

/**
 * SimCloudletRamAllocatorSimple is an extension of RamProvisioner which uses a
 * best-effort policy to allocate memory to a VM.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class CloudletRamAllocatorSimple extends CloudletRamAllocator {

	/** The RAM table. */
	private Map<Integer, Integer> ramTable; // 任务-内存分配记录表

	private int ramCap;

	/**
	 * Instantiates a new ram provisioner simple.
	 * 
	 * @param availableRam
	 *            the available ram
	 */
	public CloudletRamAllocatorSimple(int availableRam) {
		super(availableRam);
		setRamTable(new HashMap<Integer, Integer>());
		setRamCap(availableRam);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cloudsim.provisioners.RamProvisioner#allocateRamForSimCloudlet(cloudsim
	 * .SimCloudlet, int)
	 */
	@Override
	public boolean allocateRamForSimCloudlet(SimCloudlet cloudlet) { // 分配内存给任务
		//Log.printLine("Vm" + cloudlet.getVmId() + " AvaiableRam: "
		//		+ getAvailableRam() + " cloudlet#" + cloudlet.getCloudletId()
		//		+ " requeset " + cloudlet.getRam());
		if (getAllocatedRamForSimCloudlet(cloudlet) == cloudlet.getRam()) { // 如果已经分配了则不分配了
		//	Log.printLine("Ram has allocated to cloudlet#"
		//			+ cloudlet.getCloudletId());
			return false;
		}
		if (getAvailableRam() >= cloudlet.getRam()) { // 有足够内存分配
			setAvailableRam(getAvailableRam() - cloudlet.getRam());
			getRamTable().put(cloudlet.getCloudletId(), cloudlet.getRam());
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cloudsim.provisioners.RamProvisioner#getAllocatedRamForSimCloudlet(cloudsim
	 * .SimCloudlet)
	 */
	@Override
	public int getAllocatedRamForSimCloudlet(SimCloudlet cloudlet) { // 获得分配给任务的内存
		if (getRamTable().containsKey(cloudlet.getCloudletId())) {
			return getRamTable().get(cloudlet.getCloudletId());
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cloudsim.provisioners.RamProvisioner#deallocateRamForSimCloudlet(cloudsim
	 * .SimCloudlet)
	 */
	@Override
	public void deallocateRamForSimCloudlet(SimCloudlet cloudlet) { // 释放分配给任务的内存
		if (getRamTable().containsKey(cloudlet.getCloudletId())) {
			int amountFreed = getRamTable().remove(cloudlet.getCloudletId());
			setAvailableRam(getAvailableRam() + amountFreed);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cloudsim.provisioners.RamProvisioner#deallocateRamForSimCloudlet(cloudsim
	 * .SimCloudlet)
	 */
	@Override
	public void deallocateRamForAllSimCloudlets() { // 释放所有任务的内存
		super.deallocateRamForAllSimCloudlets();
		getRamTable().clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cloudsim.provisioners.RamProvisioner#isSuitableForSimCloudlet(cloudsim
	 * .SimCloudlet, int)
	 */

	public void resetRamTable() { // 重置记录表
		getRamTable().clear();
	}

	/**
	 * Gets the ram table.
	 * 
	 * @return the ram table
	 */
	protected Map<Integer, Integer> getRamTable() {
		return ramTable;
	}

	/**
	 * Sets the ram table.
	 * 
	 * @param ramTable
	 *            the ram table
	 */
	protected void setRamTable(Map<Integer, Integer> ramTable) {
		this.ramTable = ramTable;
	}


	public int getRamCap() {
		return ramCap;
	}

	public void setRamCap(int ramCap) {
		this.ramCap = ramCap;
	}
}
