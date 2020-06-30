package org.cloudbus.cloudsim;

import java.util.List;

public class CloudletAssignmentPolicySimple extends CloudletAssignmentPolicy {

	public CloudletAssignmentPolicySimple() {//顺序任务分配器
	}

	@Override
	public int[] assignCloudletsToVm(List<SimCloudlet> cloudletlist, List<Vm> vmlist) {
		int n = cloudletlist.size();
		int m = vmlist.size();
		int[] result = new int[n];
//		if(n != m){
//			Log.formatLine("n=%d, m=%d", n, m);
//			Log.printLine("虚拟机调度实验不应该发生n不等于m的情况,如果是非虚拟机调度实验请注释该行。");
//			System.exit(1);
//		}
		for (int i = 0; i < n; i++) {
			if (cloudletlist.get(i).getVmId() == -1)
			{
				result[i] = i % m;

			}
			else{
				result[i] = cloudletlist.get(i).getVmId();
			}

		}
			
		return result;
	}

}
