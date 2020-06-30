package org.cloudbus.cloudsim.allocator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudlet;

public class CloudletCpuAllocatorMaxMinFairness extends CloudletCpuAllocatorSimple {

	public CloudletCpuAllocatorMaxMinFairness(long availableMips) {
		super(availableMips);
	}

	@Override
	public boolean allocateMipsForSimCloudlets(List<SimCloudlet> list,
			final double time) { //Max-Min公平算法 参考http://www.cnblogs.com/549294286/p/3935408.html
		Collections.sort(list, new Comparator<SimCloudlet>() {
			public int compare(SimCloudlet cl0, SimCloudlet cl1) {
				return new Double(cl0.getCurrentRequestedMips(time))
						.compareTo(cl1.getCurrentRequestedMips(time));
			}
		});
		int n = list.size();

		for (SimCloudlet cl : list) {
			double averMips = getAvailableMips() / n;
			if (cl.getCurrentRequestedMips(time) > averMips)
				allocateMipsToSimCloudlet(cl, averMips);
			else
				allocateMipsToSimCloudlet(cl, cl.getCurrentRequestedMips(time));
			n--;
		}

		return true;
	}

	public boolean allocateMipsToSimCloudlet(SimCloudlet cloudlet, double mips) { // 分配Mips给任务
		//Log.printLine("Vm" + cloudlet.getVmId() + " AvaiableMips: "
		//		+ getAvailableMips() + " cloudlet#" + cloudlet.getCloudletId());

		if (getAvailableMips() >= mips) { // 有足够Mips分配
			setAvailableMips(getAvailableMips() - mips);
			getMipsTable().put(cloudlet.getCloudletId(), mips);
			return true;
		}

		return false;
	}



}
