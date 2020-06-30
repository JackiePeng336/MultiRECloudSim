package org.cloudbus.cloudsim.allocator;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudlet;


public class CloudletBwAllocatorSimple extends CloudletBwAllocator {

	/** The BW table. */
	private Map<Integer, Long> bwTable;	//任务-带宽分配记录表


	public CloudletBwAllocatorSimple(long availableBw) {
		super(availableBw);
		setBwTable(new HashMap<Integer, Long>());
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisbwners.BwProvisbwner#allocateBwForSimCloudlet(cloudsim.SimCloudlet, int)
	 */
	@Override
	public boolean allocateBwForSimCloudlet(SimCloudlet cloudlet) {	//分配带宽给任务
		//Log.printLine("Vm" + cloudlet.getVmId()+ " AvaiableBw: "+getAvailableBw()+ " cloudlet#" + cloudlet.getCloudletId() + " requeset "+cloudlet.getBw());
		if(getAllocatedBwForSimCloudlet(cloudlet)==cloudlet.getBw()){	//如果已经分配了则不分配了
		//	Log.printLine("Bw has allocated to cloudlet#"+cloudlet.getCloudletId());
			return false;
		}
		
		if (getAvailableBw() >= cloudlet.getBw()) {	//有足够带宽分配
			setAvailableBw(getAvailableBw() - cloudlet.getBw());
			getBwTable().put(cloudlet.getCloudletId(), cloudlet.getBw());
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisbwners.BwProvisbwner#getAllocatedBwForSimCloudlet(cloudsim.SimCloudlet)
	 */
	@Override
	public long getAllocatedBwForSimCloudlet(SimCloudlet cloudlet) {	//获得分配给任务的带宽
		if (getBwTable().containsKey(cloudlet.getCloudletId())) {
			return getBwTable().get(cloudlet.getCloudletId());
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisbwners.BwProvisbwner#deallocateBwForSimCloudlet(cloudsim.SimCloudlet)
	 */
	@Override
	public void deallocateBwForSimCloudlet(SimCloudlet cloudlet) {	//释放分配给任务的带宽
		if (getBwTable().containsKey(cloudlet.getCloudletId())) {
			Long amountFreed = getBwTable().remove(cloudlet.getCloudletId());
			setAvailableBw(getAvailableBw() + amountFreed);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisbwners.BwProvisbwner#deallocateBwForSimCloudlet(cloudsim.SimCloudlet)
	 */
	@Override
	public void deallocateBwForAllSimCloudlets() {	//释放所有任务的带宽
		super.deallocateBwForAllSimCloudlets();
		getBwTable().clear();
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisbwners.BwProvisbwner#isSuitableForSimCloudlet(cloudsim.SimCloudlet, int)
	 */
	@Override
	public boolean isSuitableForSimCloudlet(SimCloudlet cloudlet) {	//检查带宽是否足够
		return getAvailableBw()>=cloudlet.getBw();
	}

	public void resetBwTable(){	//重置记录表
		getBwTable().clear();
	}
	
	/**
	 * Gets the bw table.
	 * 
	 * @return the bw table
	 */
	protected Map<Integer, Long> getBwTable() {
		return bwTable;
	}

	/**
	 * Sets the bw table.
	 * 
	 * @pabw bwTable the bw table
	 */
	protected void setBwTable(Map<Integer, Long> bwTable) {
		this.bwTable = bwTable;
	}

}
