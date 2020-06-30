package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.allocator.CloudletBwAllocator;
import org.cloudbus.cloudsim.allocator.CloudletCpuAllocator;
import org.cloudbus.cloudsim.allocator.CloudletCpuAllocatorReservation;
import org.cloudbus.cloudsim.allocator.CloudletIoAllocator;
import org.cloudbus.cloudsim.allocator.CloudletRamAllocator;

public class SimCloudletSchedulerDynamicWorkloadReservation extends
		SimCloudletSchedulerDynamicWorkload {

	public SimCloudletSchedulerDynamicWorkloadReservation(double mips,
			int numberOfPes, CloudletCpuAllocator cpuAllocator,
			CloudletRamAllocator ramAllocator, CloudletIoAllocator ioAllocator,
			CloudletBwAllocator bwAllocator) {
		super(mips, numberOfPes, cpuAllocator, ramAllocator, ioAllocator,
				bwAllocator);
	}	

	@Override
	protected boolean IsCloudletRunnable(SimCloudlet cl) { // 检查是否有资源让任务启动
		if (!getCpuAllocator().isSuitableForSimCloudlet(cl, getPreviousTime()))
			return false;
		if (!getRamAllocator().isSuitableForSimCloudlet(cl))
			return false;
		if (!getIoAllocator().isSuitableForSimCloudlet(cl))
			return false;
		if (!getBwAllocator().isSuitableForSimCloudlet(cl))
			return false;
		return true;
	}

	@Override
	protected boolean checkAllocatedReasourcesForCloudlet(ResCloudlet rcl) { //检查分配给任务的资源是否足够
		SimCloudlet scl = (SimCloudlet) rcl.getCloudlet();
		double allocatedMips = getCpuAllocator()
				.getAllocatedMipsForSimCloudlet(scl);
		int allocatedRam = getRamAllocator().getAllocatedRamForSimCloudlet(scl);
		long allocatedIo = getIoAllocator().getAllocatedIoForSimCloudlet(scl);
		long allocatedBw = getBwAllocator().getAllocatedBwForSimCloudlet(scl);
		if (scl.getCurrentRequestedMips(getPreviousTime()) != allocatedMips) {
			Log.printLine("Cloudlet #" + scl.getCloudletId() + " requires "
					+ scl.getCurrentRequestedMips(getPreviousTime())
					+ " Mips but is allocated " + allocatedMips);
			return false;
		}
		if (scl.getRam() != allocatedRam) {
			Log.printLine("Cloudlet #" + scl.getCloudletId() + " requires "
					+ scl.getRam() + " Ram but is allocated " + allocatedRam);
			return false;
		}
		if (scl.getIo() != allocatedIo) {
			Log.printLine("Cloudlet #" + scl.getCloudletId() + " requires "
					+ scl.getIo() + " Io but is allocated " + allocatedIo);
			return false;
		}
		if (scl.getBw() != allocatedBw) {
			Log.printLine("Cloudlet #" + scl.getCloudletId() + " requires "
					+ scl.getBw() + " Bw but is allocated " + allocatedBw);
			return false;
		}

		return true;
	}

	@Override
	protected void allocateReasourcesForCloudlet(ResCloudlet rcl) { // 分配资源给任务
		SimCloudlet scl = (SimCloudlet) rcl.getCloudlet();
		//Log.printLine("***************allocateReasourcesForCloudlet#"
		//		+ scl.getCloudletId() + "************");
		((CloudletCpuAllocatorReservation) getCpuAllocator())
				.reserveMipsForSimCloudlet(scl);	//预留所需要的资源
		getCpuAllocator().allocateMipsForSimCloudlet(scl, getPreviousTime());
		getRamAllocator().allocateRamForSimCloudlet(scl);
		getIoAllocator().allocateIoForSimCloudlet(scl);
		getBwAllocator().allocateBwForSimCloudlet(scl);
	}

	@Override
	protected void releaseResourcesForCloudlet(ResCloudlet rcl) { // 释放任务的资源
		SimCloudlet scl = (SimCloudlet) rcl.getCloudlet();
		//Log.printLine("***************VM#" + scl.getVmId()
		//		+ " Cloudlet Finish!releaseResourcesForCloudlet#"
		//		+ scl.getCloudletId() + "************");
		//Log.printLine("Mips:" + scl.getMaxMips() + " Ram:" + scl.getRam()
		//		+ " Io:" + scl.getIo() + " Bw:" + scl.getBw());
		((CloudletCpuAllocatorReservation) getCpuAllocator())
				.dereserveMipsForSimCloudlet(scl);	//释放预留Mips
		getCpuAllocator().deallocateMipsForSimCloudlet(scl);
		getRamAllocator().deallocateRamForSimCloudlet(scl);
		getIoAllocator().deallocateIoForSimCloudlet(scl);
		getBwAllocator().deallocateBwForSimCloudlet(scl);
	}
	
}
