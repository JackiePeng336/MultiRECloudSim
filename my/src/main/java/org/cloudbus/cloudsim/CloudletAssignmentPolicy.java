package org.cloudbus.cloudsim;

import java.util.List;

public abstract class CloudletAssignmentPolicy {//任务分配器抽象类


	public double getTIME_WEIGHT() {
		return TIME_WEIGHT;
	}

	public void setTIME_WEIGHT(double TIME_WEIGHT) {
		this.TIME_WEIGHT = TIME_WEIGHT;
	}

	public double getCPU_WEIGHT() {
		return CPU_WEIGHT;
	}

	public void setCPU_WEIGHT(double CPU_WEIGHT) {
		this.CPU_WEIGHT = CPU_WEIGHT;
	}

	private double TIME_WEIGHT = 0.7;
	private double CPU_WEIGHT = 0.3;

	public CloudletAssignmentPolicy() {

	}
	
	public abstract int[] assignCloudletsToVm(List<SimCloudlet> cloudletlist, List<Vm> vmlist);
		
}
