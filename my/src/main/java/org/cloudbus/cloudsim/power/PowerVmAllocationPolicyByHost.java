package org.cloudbus.cloudsim.power;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

public class PowerVmAllocationPolicyByHost extends PowerVmAllocationPolicyAbstract {

	public PowerVmAllocationPolicyByHost(List<? extends Host> list) {
		super(list);
	}

	@Override
	public PowerHost findHostForVm(Vm vm) {
		for (PowerHost host : this.<PowerHost> getHostList()) {
			//System.out.println("FindHostFor Vm "+vm.getMips()+" Host:"+host.getPeList().get(0).getMips());
			if (vm.getMips()==(double)host.getPeList().get(0).getMips()&&host.isSuitableForVm(vm)) {
				return host;
			}
		}
		return null;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}
}
