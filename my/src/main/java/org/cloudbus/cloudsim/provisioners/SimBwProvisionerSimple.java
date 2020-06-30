package org.cloudbus.cloudsim.provisioners;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Vm;

public class SimBwProvisionerSimple extends BwProvisionerSimple {

	public SimBwProvisionerSimple(long bw) {
		super(bw);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean allocateBwForVm(Vm vm, long bw) {	//分配带宽给Vm
		deallocateBwForVm(vm);
		//Log.printLine("Allocate Bw For Vm# "+vm.getId()+" AvailableBw:"+getAvailableBw()+" wantBw: "+ bw);
		if (getAvailableBw() >= bw) {
			setAvailableBw(getAvailableBw() - bw);
			getBwTable().put(vm.getUid(), bw);
			vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
			//Log.printLine("Successfully Allocate Bw For Vm# "+vm.getId()+" LeftAvailableBw:"+getAvailableBw()+" totalAllocated: "+ getAllocatedBwForVm(vm));
			((SimCloudletSchedulerDynamicWorkload) vm
					.getCloudletScheduler()).getBwAllocator().resetBw(getAllocatedBwForVm(vm));
			return true;
		}
		//Log.printLine("Failure Allocate Bw For Vm# "+vm.getId()+" AvailableBw:"+getAvailableBw()+" totalAllocated: "+ getAllocatedBwForVm(vm));
		vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
		((SimCloudletSchedulerDynamicWorkload) vm
				.getCloudletScheduler()).getBwAllocator().resetBw(getAllocatedBwForVm(vm));	//重置VM的带宽值
		return false;
	}
	
	public boolean addBwForVm(Vm vm, long bw) {		//追加带宽给Vm
		//Log.printLine("Add Bw For Vm# "+vm.getId()+" AvailableBw:"+getAvailableBw()+" wantBw: "+ bw);
		if (getAvailableBw() >= bw) {
			setAvailableBw(getAvailableBw() - bw);
			if (getBwTable().containsKey(vm.getUid())){
				getBwTable().put(vm.getUid(), getBwTable().get(vm.getUid())+bw);
			}else{
				getBwTable().put(vm.getUid(), bw);
			}
			//Log.printLine("Successfully Add Bw For Vm# "+vm.getId()+" LeftAvailableBw:"+getAvailableBw()+" totalAllocated: "+ getAllocatedBwForVm(vm));
			vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
			((SimCloudletSchedulerDynamicWorkload) vm
					.getCloudletScheduler()).getBwAllocator().resetBw(getAllocatedBwForVm(vm));	//重置VM的带宽值
			return true;
		}

		//Log.printLine("Failure Add Bw For Vm# "+vm.getId()+" AvailableBw:"+getAvailableBw()+" totalAllocated: "+ getAllocatedBwForVm(vm));
		vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
		((SimCloudletSchedulerDynamicWorkload) vm
				.getCloudletScheduler()).getBwAllocator().resetBw(getAllocatedBwForVm(vm));
		return false;
	}
	
	public boolean isSuitableForCloudlet(long bw) {
		//Log.printLine("getAvailableBw()="+getAvailableBw()+" request:"+bw);
		return getAvailableBw() >= bw;
	}
	
	@Override
	public void setAvailableBw(long bw){
		super.setAvailableBw(bw);
	}
	
}
