package org.cloudbus.cloudsim.allocator;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudlet;

public abstract class CloudletBwAllocator {

	private long bw;	//总带宽
	private long availableBw;	//可用带宽
	
	public CloudletBwAllocator(long bw) {
		setBw(bw);
		setAvailableBw(bw);
	}


	public abstract boolean allocateBwForSimCloudlet(SimCloudlet cloudlet);

	/**
	 * Gets the allocated BW for VM.
	 * 
	 * @pabw cloudlet the VM
	 * 
	 * @return the allocated RAM for cloudlet
	 */
	public abstract long getAllocatedBwForSimCloudlet(SimCloudlet cloudlet);

	/**
	 * Releases BW used by a VM.
	 * 
	 * @pabw cloudlet the cloudlet
	 * 
	 * @pre $none
	 * @post none
	 */
	public abstract void deallocateBwForSimCloudlet(SimCloudlet cloudlet);

	/**
	 * Releases BW used by a all VMs.
	 * 
	 * @pre $none
	 * @post none
	 */
	public void deallocateBwForAllSimCloudlets() {
		setAvailableBw(getBw());
	}

	/**
	 * Checks if is suitable for cloudlet.
	 * 
	 * @pabw cloudlet the cloudlet
	 * @pabw bw the bw
	 * 
	 * @return true, if is suitable for cloudlet
	 */
	public boolean isSuitableForSimCloudlet(SimCloudlet cloudlet){
		Log.printLine("Bw isSuitableForSimCloudlet: Available: "+ getAvailableBw() + " Required " + cloudlet.getBw());
		return getAvailableBw()>=cloudlet.getBw();
	}
	
	public void resetBw(long bw){	//重置占用的带宽
		//Log.printLine("Reset Bw Original AvailableBw:"+getAvailableBw()+" bw: "+getBw()+" new: " + bw);
		setAvailableBw(bw);
		setBw(bw);
	}
	
	/**
	 * Gets the bw.
	 * 
	 * @return the bw
	 */
	public long getBw() {
		return bw;
	}

	/**
	 * Sets the bw.
	 * 
	 * @pabw bw the bw to set
	 */
	protected void setBw(long bw) {
		this.bw = bw;
	}

	/**
	 * Gets the amount of used BW in the VM.
	 * 
	 * @return used bw
	 * 
	 * @pre $none
	 * @post $none
	 */
	public long getUsedBw() {
		return bw - availableBw;
	}

	/**
	 * Gets the available Bw in the VM.
	 * 
	 * @return available bw
	 * 
	 * @pre $none
	 * @post $none
	 */
	public long getAvailableBw() {
		return availableBw;
	}

	/**
	 * Sets the available bw.
	 * 
	 * @pabw availableBw the availableBw to set
	 */
	protected void setAvailableBw(long availableBw) {
		this.availableBw = availableBw;
	}
	
	
}
