package org.cloudbus.cloudsim.provisioners;

import org.cloudbus.cloudsim.power.SimPowerVm;

public abstract class IOProvisioner {

	/** The io. */
	private long io;

	/** The available io. */
	private long availableIo;

	/**
	 * Creates the new ioProvisioner.
	 * 
	 * @paio io the io
	 * 
	 * @pre io>=0
	 * @post $none
	 */
	public IOProvisioner(long io) {
		setIo(io);
		setAvailableIo(io);
	}

	/**
	 * Allocates io for a given VM.
	 * 
	 * @paio vm virtual machine for which the io are being allocated
	 * @paio io the io
	 * 
	 * @return $true if the io could be allocated; $false otherwise
	 * 
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateIoForVm(SimPowerVm vm, long io);

	/**
	 * Gets the allocated io for VM.
	 * 
	 * @paio vm the VM
	 * 
	 * @return the allocated io for vm
	 */
	public abstract long getAllocatedIoForVm(SimPowerVm vm);

	/**
	 * Releases BW used by a VM.
	 * 
	 * @paio vm the vm
	 * 
	 * @pre $none
	 * @post none
	 */
	public abstract void deallocateIoForVm(SimPowerVm vm);

	/**
	 * Releases BW used by a all VMs.
	 * 
	 * @pre $none
	 * @post none
	 */
	public void deallocateIoForAllVms() {
		setAvailableIo(getIo());
	}

	/**
	 * Checks if is suitable for vm.
	 * 
	 * @paio vm the vm
	 * @paio io the io
	 * 
	 * @return true, if is suitable for vm
	 */
	public abstract boolean isSuitableForVm(SimPowerVm vm, long io);

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
	 * Gets the amount of used io in the host.
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
	 * Gets the available io in the host.
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
	 * @paio availableio the availableio to set
	 */
	protected void setAvailableIo(long io) {
		this.availableIo = io;
	}

	
}
