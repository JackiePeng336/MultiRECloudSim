package org.cloudbus.cloudsim.provisioners;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;

public class SimRamProvisionerSimple extends RamProvisionerSimple {


	public SimRamProvisionerSimple(int availableRam) {
		super(availableRam);

		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean allocateRamForVm(Vm vm, int ram) {	//分配内存给Vm
		int maxRam = vm.getRam();
		if (ram >= maxRam) {
			ram = maxRam;
		}
		deallocateRamForVm(vm);
		//Log.printLine("Allocate Ram For Vm# "+vm.getId()+" AvailableRam:"+getAvailableRam()+" wantRam: "+ ram);
		if (getAvailableRam() >= ram) {
			setAvailableRam(getAvailableRam() - ram);
			getRamTable().put(vm.getUid(), ram);
			vm.setCurrentAllocatedRam(getAllocatedRamForVm(vm));
			//Log.printLine("Successfully Allocate Ram For Vm# "+vm.getId()+" LeftAvailableRam:"+getAvailableRam()+" totalAllocated: "+ getAllocatedRamForVm(vm));
			((SimCloudletSchedulerDynamicWorkload) vm
					.getCloudletScheduler()).getRamAllocator().resetRam(getAllocatedRamForVm(vm));	//重置VM的内存值
			return true;
		}

		//Log.printLine("Successfully Allocate Ram For Vm# "+vm.getId()+" AvailableRam:"+getAvailableRam()+" totalAllocated: "+ getAllocatedRamForVm(vm));
		// 分配失败肯定一毛都不给
		vm.setCurrentAllocatedRam(getAllocatedRamForVm(vm));
		((SimCloudletSchedulerDynamicWorkload) vm
				.getCloudletScheduler()).getRamAllocator().resetRam(getAllocatedRamForVm(vm));	
		return false;
	}
	
	public boolean addRamForVm(Vm vm, int ram) {	//追加内存给Vm
		//Log.printLine("Add Ram For Vm# "+vm.getId()+" AvailableRam:"+getAvailableRam()+" wantRam: "+ ram);
		if (getAvailableRam() >= ram) {
			setAvailableRam(getAvailableRam() - ram);
			if (getRamTable().containsKey(vm.getUid())){
				getRamTable().put(vm.getUid(), getRamTable().get(vm.getUid())+ram);
			}else{
				getRamTable().put(vm.getUid(), ram);
			}
			//Log.printLine("Successfully Add Ram For Vm# " + vm.getId() + " LeftAvailableRam:"+getAvailableRam()+" totalAllocated: "+ getAllocatedRamForVm(vm));
			vm.setCurrentAllocatedRam(getAllocatedRamForVm(vm));
			((SimCloudletSchedulerDynamicWorkload) vm
					.getCloudletScheduler()).getRamAllocator().resetRam(getAllocatedRamForVm(vm));	//重置VM的内存值
			return true;
		}

		//Log.printLine("Failure Add Ram For Vm# "+vm.getId()+" AvailableRam:"+getAvailableRam()+" totalAllocated: "+ getAllocatedRamForVm(vm));
		vm.setCurrentAllocatedRam(getAllocatedRamForVm(vm));
		((SimCloudletSchedulerDynamicWorkload) vm
				.getCloudletScheduler()).getRamAllocator().resetRam(getAllocatedRamForVm(vm));	
		return false;
	}
	
	public boolean isSuitableForCloudlet(int ram) {
		//Log.printLine("getAvailableRam()="+getAvailableRam()+" request:"+ram);
		return getAvailableRam() >= ram;
	}
	
	@Override
	public void setAvailableRam(int ram){
		super.setAvailableRam(ram);
	}

}
