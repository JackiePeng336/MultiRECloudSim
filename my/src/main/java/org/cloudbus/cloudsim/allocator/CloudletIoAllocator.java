package org.cloudbus.cloudsim.allocator;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudlet;

public abstract class CloudletIoAllocator {

	private long io;	//总IO
	private long availableIo;	//可用IO
	
	public CloudletIoAllocator(long io) {
		setIo(io);
		setAvailableIo(io);
	}


	public abstract boolean allocateIoForSimCloudlet(SimCloudlet cloudlet);

	/**
	 * Gets the allocated RAM for VM.
	 * 
	 * @paio cloudlet the VM
	 * 
	 * @return the allocated RAM for cloudlet
	 */
	public abstract long getAllocatedIoForSimCloudlet(SimCloudlet cloudlet);

	/**
	 * Releases BW used by a VM.
	 * 
	 * @paio cloudlet the cloudlet
	 * 
	 * @pre $none
	 * @post none
	 */
	public abstract void deallocateIoForSimCloudlet(SimCloudlet cloudlet);

	/**
	 * Releases BW used by a all VMs.
	 * 
	 * @pre $none
	 * @post none
	 */
	public void deallocateIoForAllSimCloudlets() {
		setAvailableIo(getIo());
	}

	/**
	 * Checks if is suitable for cloudlet.
	 * 
	 * @paio cloudlet the cloudlet
	 * @paio io the io
	 * 
	 * @return true, if is suitable for cloudlet
	 */

	public boolean isSuitableForSimCloudlet(SimCloudlet cloudlet) {	//检查IO是否足够
		//Log.printLine("IO isSuitableForSimCloudlet#" + cloudlet.getCloudletId() + ": Available: "+ getAvailableIo() + " Required " + cloudlet.getIo());
		return getAvailableIo()>=cloudlet.getIo();
	}
	
	public void resetIo(long io){	//重置占用的IO
		//Log.printLine("Reset IO Original AvailableIo:"+getAvailableIo()+" IO: "+getIo()+" new: " + io);
		setAvailableIo(io);
		setIo(io);
	}
	
	/**
	 * Gets the io.
	 * 
	 * @return the io
	 */
	public long getIo() {
		return io;
	}

	/**
	 * Sets the io.
	 * 
	 * @paio io the io to set
	 */
	protected void setIo(long io) {
		this.io = io;
	}

	/**
	 * Gets the amount of used RAM in the host.
	 * 
	 * @return used io
	 * 
	 * @pre $none
	 * @post $none
	 */
	public long getUsedIo() {
		return io - availableIo;
	}

	/**
	 * Gets the available RAM in the host.
	 * 
	 * @return available io
	 * 
	 * @pre $none
	 * @post $none
	 */
	public long getAvailableIo() {
		return availableIo;
	}

	/**
	 * Sets the available io.
	 * 
	 * @paio availableIo the availableIo to set
	 */
	protected void setAvailableIo(long availableIo) {
		this.availableIo = availableIo;
	}
	
	
}
