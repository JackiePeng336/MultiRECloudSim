package org.cloudbus.cloudsim.allocator;

import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudlet;

public abstract class CloudletCpuAllocator {

	private double mips;	//总Mips
	private double availableMips;	//可用Mips
	
	public CloudletCpuAllocator(long mips) {
		setMips(mips);
		setAvailableMips(mips);
	}

	//最重要的方法，确定给多任务的分配策略
	public abstract boolean allocateMipsForSimCloudlets(List<SimCloudlet> list, final double time);
	
	//给一个任务分配Mips
	public abstract boolean allocateMipsForSimCloudlet(SimCloudlet cloudlet, final double time);

	/**
	 * Gets the allocated Mips for VM.
	 * 
	 * @pamips cloudlet the VM
	 * 
	 * @return the allocated RAM for cloudlet
	 */
	//返回给任务分配的Mips
	public abstract double getAllocatedMipsForSimCloudlet(SimCloudlet cloudlet);

	/**
	 * Releases Mips used by a VM.
	 * 
	 * @pamips cloudlet the cloudlet
	 * 
	 * @pre $none
	 * @post none
	 */
	//释放给任务分配的Mips
	public abstract void deallocateMipsForSimCloudlet(SimCloudlet cloudlet);

	/**
	 * Releases Mips used by a all VMs.
	 * 
	 * @pre $none
	 * @post none
	 */
	public void deallocateMipsForAllSimCloudlets() {
		setAvailableMips(getMips());
	}

	/**
	 * Checks if is suitable for cloudlet.
	 * 
	 * @pamips cloudlet the cloudlet
	 * @pamips mips the mips
	 * 
	 * @return true, if is suitable for cloudlet
	 */
	public abstract boolean isMipsAddForSimCloudlet(SimCloudlet cloudlet);
	
	public abstract boolean isSuitableForSimCloudlet(SimCloudlet cloudlet); // 检查Mips是否足够

	public abstract boolean isSuitableForSimCloudlet(SimCloudlet cloudlet, final double time);  // 检查Mips是否足够	
	
	public void resetMips(double mips){	//重置占用的Mips
		//Log.printLine("Reset Mips Original AvailableMips:"+getAvailableMips()+" mips: "+getMips()+" new: " + mips);
		setAvailableMips(mips);
		setMips(mips);
	}
	
	/**
	 * Gets the mips.
	 * 
	 * @return the mips
	 */
	public double getMips() {
		return mips;
	}

	/**
	 * Sets the mips.
	 * 
	 * @pamips mips the mips to set
	 */
	public void setMips(double mips) {
		this.mips = mips;
	}

	/**
	 * Gets the amount of used Mips in the VM.
	 * 
	 * @return used mips
	 * 
	 * @pre $none
	 * @post $none
	 */
	public double getUsedMips() {
		return mips - availableMips;
	}

	/**
	 * Gets the available Mips in the VM.
	 * 
	 * @return available mips
	 * 
	 * @pre $none
	 * @post $none
	 */
	public double getAvailableMips() {
		return availableMips;
	}

	/**
	 * Sets the available mips.
	 * 
	 * @pamips availableMips the availableMips to set
	 */
	public void setAvailableMips(double availableMips) {
		this.availableMips = availableMips;
	}
	
	
}
