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
 * SimCloudletIoAllocatorSimple is an extension of IoProvisioner which uses a best-effort policy to
 * allocate memory to a VM.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class CloudletIoAllocatorSimple extends CloudletIoAllocator {

	/** The RAM table. */
	private Map<Integer, Long> ioTable;	//任务-IO分配记录表

	private long ioCap;

	/**
	 * Instantiates a new io provisioner simple.
	 * 
	 * @paio availableIo the available io
	 */
	public CloudletIoAllocatorSimple(long availableIo) {
		super(availableIo);
		setIoCap(availableIo);
		setIoTable(new HashMap<Integer, Long>());
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#allocateIoForSimCloudlet(cloudsim.SimCloudlet, int)
	 */
	@Override
	public boolean allocateIoForSimCloudlet(SimCloudlet cloudlet) {	//分配IO给任务
		//Log.printLine("Vm" + cloudlet.getVmId()+ " AvaiableIo: "+getAvailableIo() + " cloudlet#"+cloudlet.getCloudletId() + " requeset "+cloudlet.getIo());
		if( getAllocatedIoForSimCloudlet(cloudlet)==cloudlet.getIo()){	//如果已经分配了则不分配了
			//Log.printLine("Io has allocated to cloudlet#"+cloudlet.getCloudletId());
			return false;
		}
		
		if (getAvailableIo() >= cloudlet.getIo()) {	//有足够带宽分配
			setAvailableIo(getAvailableIo() - cloudlet.getIo());
			getIoTable().put(cloudlet.getCloudletId(), cloudlet.getIo());
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#getAllocatedIoForSimCloudlet(cloudsim.SimCloudlet)
	 */
	@Override
	public long getAllocatedIoForSimCloudlet(SimCloudlet cloudlet) {	//获得分配给任务的IO
		if (getIoTable().containsKey(cloudlet.getCloudletId())) {
			return getIoTable().get(cloudlet.getCloudletId());
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#deallocateIoForSimCloudlet(cloudsim.SimCloudlet)
	 */
	@Override
	public void deallocateIoForSimCloudlet(SimCloudlet cloudlet) {	//释放分配给任务的IO
		if (getIoTable().containsKey(cloudlet.getCloudletId())) {
			Long amountFreed = getIoTable().remove(cloudlet.getCloudletId());
			setAvailableIo(getAvailableIo() + amountFreed);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#deallocateIoForSimCloudlet(cloudsim.SimCloudlet)
	 */
	@Override
	public void deallocateIoForAllSimCloudlets() {	//释放所有任务的IO
		super.deallocateIoForAllSimCloudlets();
		getIoTable().clear();
	}



	public void resetIoTable(){	//重置记录表
		getIoTable().clear();
	}
	
	/**
	 * Gets the io table.
	 * 
	 * @return the io table
	 */
	protected Map<Integer, Long> getIoTable() {
		return ioTable;
	}

	/**
	 * Sets the io table.
	 * 
	 * @paio ioTable the io table
	 */
	protected void setIoTable(Map<Integer, Long> ioTable) {
		this.ioTable = ioTable;
	}

	public long getIoCap() {
		return ioCap;
	}

	public void setIoCap(long ioCap) {
		this.ioCap = ioCap;
	}
}
