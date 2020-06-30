package org.cloudbus.cloudsim.power;


import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;

import org.cloudbus.cloudsim.SimCloudlet;
import org.cloudbus.cloudsim.SimCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.allocator.CloudletCpuAllocatorReservation;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.SimIoProvisioner;
import org.cloudbus.cloudsim.provisioners.SimIoProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.provisioners.SimBwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.SimRamProvisionerSimple;

public class SimPowerHostReservation extends SimPowerHost {

	public SimPowerHostReservation(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, SimIoProvisioner ioProvisioner,
			long storage, List<? extends Pe> peList, VmScheduler vmScheduler,
			PowerModel powerModelCpu, PowerModel powerModelRam,
			PowerModel powerModelIO, PowerModel powerModelBw) {
		super(id, ramProvisioner, bwProvisioner, ioProvisioner, storage,
				peList, vmScheduler, powerModelCpu, powerModelRam, powerModelIO,
				powerModelBw);
	}

	@Override
	protected void addResourcesToVm(SimCloudlet scl, Vm vm, double currentTime) { //追加任务资源给Vm
		getVmScheduler().deallocatePesForVm(vm); // 清空PEs分配
		SimCloudletSchedulerDynamicWorkload scheduler = ((SimCloudletSchedulerDynamicWorkload) vm
				.getCloudletScheduler());
		scheduler.addCurrentRequestedMips(scl
				.getCurrentRequestedMips(currentTime));
		//Log.printLine("cloudlet #" + scl.getCloudletId()
		//		+ " Add After: Vm Current RequestMips:"
		//		+ ((SimPowerVm) vm).getCurrentRequestedMips().toString());
		getVmScheduler().allocatePesForVm(vm,
				((SimPowerVm) vm).getCurrentRequestedMips());

		CloudletCpuAllocatorReservation cpuAllocator = (CloudletCpuAllocatorReservation) scheduler
				.getCpuAllocator();
		cpuAllocator.setAvailableMips(scheduler
				.getTotalAvailableMips(getVmScheduler().getAllocatedMipsForVm(
						vm))); //重新确定被分配的MIPS
		cpuAllocator.reserveMipsForSimCloudlet(scl); //预留Mips给任务
		cpuAllocator.addMipsForSimCloudlet(scl, currentTime);
		

		//Log.printLine("addResources Vm allocate Mips:"
		//		+ getVmScheduler().getAllocatedMipsForVm(vm));
		//Log.printLine("//VM#" + vm.getId() + " cloudlet# "
		//		+ scl.getCloudletId() + " asks for more Reasources. mips:"
		//		+ vm.getCurrentRequestedTotalMips());
		
		//追加内存，IO，带宽资源
		((SimRamProvisionerSimple) getRamProvisioner()).addRamForVm(vm,
				scl.getRam());
		((SimIoProvisionerSimple) getIoProvisioner()).addIoForVm((SimPowerVm) vm,
				scl.getIo());
		((SimBwProvisionerSimple) getBwProvisioner()).addBwForVm(vm,
				scl.getBw());
	}

}
