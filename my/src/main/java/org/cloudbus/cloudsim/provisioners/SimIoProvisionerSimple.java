/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.SimPowerVm;

/**
 * IoProvisionerSimple is a class that implements a simple best effort allocation policy: if there
 * is Io available to request, it allocates; otherwise, it fails.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class SimIoProvisionerSimple extends SimIoProvisioner {

	/** The Io table. */
	private Map<String, Long> ioTable;	//Vm-Io分配表

	/**
	 * Instantiates a new Io provisioner simple.
	 * 
	 * @param Io the Io
	 */
	public SimIoProvisionerSimple(long io) {
		super(io);
		setIoTable(new HashMap<String, Long>());
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#allocateIoForVm(cloudsim.Vm, long)
	 */
	@Override
	public boolean allocateIoForVm(SimPowerVm vm, long io) {	//分配IO给Vm
		deallocateIoForVm(vm);

		//Log.printLine("Allocate Io For Vm# "+vm.getId()+" AvailableIo:"+getAvailableIo()+" wantIo: "+ io);
		if (getAvailableIo() >= io) {
			setAvailableIo(getAvailableIo() - io);
			getIoTable().put(vm.getUid(), io);
			vm.setCurrentAllocatedIo(getAllocatedIoForVm(vm));
			//Log.printLine("Successfully Allocate Io For Vm# "+vm.getId()+" LeftAvailableIo:"+getAvailableIo()+" totalAllocated: "+ getAllocatedIoForVm(vm));
			((SimCloudletSchedulerDynamicWorkload) vm
					.getCloudletScheduler()).getIoAllocator().resetIo(io);
			return true;
		}
		//Log.printLine("Successfully Allocate Io For Vm# "+vm.getId()+" AvailableIo:"+getAvailableIo()+" totalAllocated: "+ getAllocatedIoForVm(vm));
		vm.setCurrentAllocatedIo(getAllocatedIoForVm(vm));
		return false;
	}

	public boolean addIoForVm(SimPowerVm vm, long io) {	//追加IO给Vm
		//Log.printLine("Add Io For Vm# "+vm.getId()+" AvailableIo:"+getAvailableIo()+" wantIo: "+ io);
		if (getAvailableIo() >= io) {
			setAvailableIo(getAvailableIo() - io);
			if (getIoTable().containsKey(vm.getUid())){
				getIoTable().put(vm.getUid(), getIoTable().get(vm.getUid())+io);
			}else{
				getIoTable().put(vm.getUid(), io);
			}
			vm.setCurrentAllocatedIo(getAllocatedIoForVm(vm));
			//Log.printLine("Successfully Add Io For Vm# "+vm.getId()+" LeftAvailableIo:"+getAvailableIo()+" totalAllocated: "+ getAllocatedIoForVm(vm));
			((SimCloudletSchedulerDynamicWorkload) vm
					.getCloudletScheduler()).getIoAllocator().resetIo(getAllocatedIoForVm(vm));
			
			return true;
		}
		//Log.printLine("Successfully Add Io For Vm# "+vm.getId()+" AvailableIo:"+getAvailableIo()+" totalAllocated: "+ getAllocatedIoForVm(vm));
		vm.setCurrentAllocatedIo(getAllocatedIoForVm(vm));
		((SimCloudletSchedulerDynamicWorkload) vm
				.getCloudletScheduler()).getIoAllocator().resetIo(getAllocatedIoForVm(vm));
		return false;
	}
	
	public boolean isSuitableForCloudlet(long io) {
		//Log.printLine("getAvailableIo()="+getAvailableIo()+" request:"+io);
		return getAvailableIo() >= io;
	}
	
	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#getAllocatedIoForVm(cloudsim.Vm)
	 */
	@Override
	public long getAllocatedIoForVm(SimPowerVm vm) {
		if (getIoTable().containsKey(vm.getUid())) {
			return getIoTable().get(vm.getUid());
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#deallocateIoForVm(cloudsim.Vm)
	 */
	@Override
	public void deallocateIoForVm(SimPowerVm vm) {
		if (getIoTable().containsKey(vm.getUid())) {
			long amountFreed = getIoTable().remove(vm.getUid());
			setAvailableIo(getAvailableIo() + amountFreed);
			vm.setCurrentAllocatedIo(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.provisioners.IoProvisioner#deallocateIoForVm(cloudsim.Vm)
	 */
	@Override
	public void deallocateIoForAllVms() {
		super.deallocateIoForAllVms();
		getIoTable().clear();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * gridsim.virtualization.power.provisioners.IoProvisioner#isSuitableForVm(gridsim.virtualization
	 * .power.VM, long)
	 */
	@Override
	public boolean isSuitableForVm(SimPowerVm vm, long Io) {
		long allocatedIo = getAllocatedIoForVm(vm);
		boolean result = allocateIoForVm(vm, Io);
		deallocateIoForVm(vm);
		if (allocatedIo > 0) {
			allocateIoForVm(vm, allocatedIo);
		}
		return result;
	}

	/**
	 * Gets the Io table.
	 * 
	 * @return the Io table
	 */
	protected Map<String, Long> getIoTable() {
		return ioTable;
	}

	/**
	 * Sets the Io table.
	 * 
	 * @param ioTable the Io table
	 */
	protected void setIoTable(Map<String, Long> IoTable) {
		this.ioTable = IoTable;
	}

}
