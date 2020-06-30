package org.cloudbus.cloudsim.allocator;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudlet;

public abstract class CloudletRamAllocator {

	private int ram;	//总内存
	private int availableRam;	//可用内存
	
	public CloudletRamAllocator(int ram) {
		setRam(ram);
		setAvailableRam(ram);
	}


	public abstract boolean allocateRamForSimCloudlet(SimCloudlet cloudlet);

	/**
	 * Gets the allocated RAM for VM.
	 * 
	 * @param cloudlet the VM
	 * 
	 * @return the allocated RAM for cloudlet
	 */
	public abstract int getAllocatedRamForSimCloudlet(SimCloudlet cloudlet);

	/**
	 * Releases BW used by a VM.
	 * 
	 * @param cloudlet the cloudlet
	 * 
	 * @pre $none
	 * @post none
	 */
	public abstract void deallocateRamForSimCloudlet(SimCloudlet cloudlet);

	/**
	 * Releases BW used by a all VMs.
	 * 
	 * @pre $none
	 * @post none
	 */
	public void deallocateRamForAllSimCloudlets() {
		setAvailableRam(getRam());
	}

	/**
	 * Checks if is suitable for cloudlet.
	 * 
	 * @param cloudlet the cloudlet
	 * @param ram the ram
	 * 
	 * @return true, if is suitable for cloudlet
	 */

	public boolean isSuitableForSimCloudlet(SimCloudlet cloudlet) {	//检查内存是否足够
		//Log.printLine("Ram isSuitableForSimCloudlet#" + cloudlet.getCloudletId() + ": Available: "+ getAvailableRam() + " Required " + cloudlet.getRam());
		return getAvailableRam()>= cloudlet.getRam();
	}
	
	public void resetRam(int ram){	//重置占用的内存
		//Log.printLine("Reset ram Original Availableram:"+getAvailableRam()+" ram: "+getRam()+" new: " + ram);
		setAvailableRam(ram);
		setRam(ram);
	}
	
	/**
	 * Gets the ram.
	 * 
	 * @return the ram
	 */
	public int getRam() {
		return ram;
	}

	/**
	 * Sets the ram.
	 * 
	 * @param ram the ram to set
	 */
	protected void setRam(int ram) {
		this.ram = ram;
	}

	/**
	 * Gets the amount of used RAM in the host.
	 * 
	 * @return used ram
	 * 
	 * @pre $none
	 * @post $none
	 */
	public int getUsedRam() {
		return ram - availableRam;
	}

	/**
	 * Gets the available RAM in the host.
	 * 
	 * @return available ram
	 * 
	 * @pre $none
	 * @post $none
	 */
	public int getAvailableRam() {
		return availableRam;
	}

	/**
	 * Sets the available ram.
	 * 
	 * @param availableRam the availableRam to set
	 */
	protected void setAvailableRam(int availableRam) {
		this.availableRam = availableRam;
	}
	
	
}
