package org.cloudbus.cloudsim;

import java.util.List;
import java.util.Random;

public class CloudletAssignmentPolicyRandom extends CloudletAssignmentPolicy {


	public CloudletAssignmentPolicyRandom() {//随机任务分配器
	}

	@Override
	public int[] assignCloudletsToVm(List<SimCloudlet> cloudletlist, List<Vm> vmlist) {
		int n = cloudletlist.size();
		int m = vmlist.size();
		int[] result = new int[n];
		for (int i = 0; i < n; i++) {
			if (cloudletlist.get(i).getVmId() == -1)
				result[i] = vmlist.get(random(0, m)).getId();
			else
				result[i] = cloudletlist.get(i).getVmId();
		}

		return result;
	}

	private int random(int min, int max) {
		Random random = new Random();
		return random.nextInt(max) % (max - min + 1) + min;
	}

}
