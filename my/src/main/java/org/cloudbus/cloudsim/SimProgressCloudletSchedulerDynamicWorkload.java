package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.allocator.CloudletBwAllocator;
import org.cloudbus.cloudsim.allocator.CloudletCpuAllocator;
import org.cloudbus.cloudsim.allocator.CloudletIoAllocator;
import org.cloudbus.cloudsim.allocator.CloudletRamAllocator;
import org.cloudbus.cloudsim.core.CloudSim;

public class SimProgressCloudletSchedulerDynamicWorkload extends SimCloudletSchedulerDynamicWorkload {

	public SimProgressCloudletSchedulerDynamicWorkload(double mips, int numberOfPes, CloudletCpuAllocator cpuAllocator,
			CloudletRamAllocator ramAllocator, CloudletIoAllocator ioAllocator, CloudletBwAllocator bwAllocator) {
		super(mips, numberOfPes, cpuAllocator, ramAllocator, ioAllocator, bwAllocator);

	}

	@Override
	public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
		setCurrentMipsShare(mipsShare);
		setMips(getTotalAvailableMips(mipsShare));
		double timeSpan = currentTime - getPreviousTime();
		double nextEvent = currentTime - getPreviousTime();// Double.MAX_VALUE;//currentTime
															// -
															// getPreviousTime();//
															// Double.MAX_VALUE;
		List<ResCloudlet> cloudletsToFinish = new ArrayList<ResCloudlet>();
		/*
		 * Log.printLine("SimCloudletScheduler mipsShare:" +
		 * getTotalAvailableMips(mipsShare) + " TotalRequetedMips:" +
		 * getTotalAvailableMips(getCurrentRequestedMips()) +
		 * " cpuAllocator Available Mips:" +
		 * getCpuAllocator().getAvailableMips()); Log.printLine(
		 * "SimCloudletScheduler AvailableRam:" +
		 * getRamAllocator().getAvailableRam() + " TotalRam:" +
		 * getRamAllocator().getRam()); Log.printLine(
		 * "SimCloudletScheduler AvailableIo:" +
		 * getIoAllocator().getAvailableIo() + " TotalIo:" +
		 * getIoAllocator().getIo()); Log.printLine(
		 * "SimCloudletScheduler AvailableBw:" +
		 * getBwAllocator().getAvailableBw() + " TotalBw:" +
		 * getBwAllocator().getBw());
		 */
		// 检查等待的任务是否有可以执行的，有资源的则让它执行，因为可能已经分配更多资源了
		List<ResCloudlet> toRemove = new ArrayList<ResCloudlet>();
		if (!getCloudletWaitingList().isEmpty()) {
			for (ResCloudlet rcl : getCloudletWaitingList()) {
				SimCloudlet scl = (SimCloudlet) rcl.getCloudlet();
				// Log.printLine("Check Waiting CLoudlet#" +
				// scl.getCloudletId());
				if (IsResourceAddForCloudlet(scl)) { // 如果资源追加给任务
					allocateReasourcesForCloudlet(rcl); // 分配资源给该任务
					Log.printLine(currentTime + " 333333333333333 cloudlet#" + scl.getCloudletId() + " waiting in VM#"
							+ scl.getVmId() + " starts to run.");
					scl.addStateHistoryEntry(getPreviousTime(), getCpuAllocator().getAllocatedMipsForSimCloudlet(scl),
							scl.getCurrentRequestedMips(getPreviousTime()),
							getRamAllocator().getAllocatedRamForSimCloudlet(scl), scl.getRam(),
							getIoAllocator().getAllocatedIoForSimCloudlet(scl), scl.getIo(),
							getBwAllocator().getAllocatedBwForSimCloudlet(scl), scl.getBw(), Cloudlet.INEXEC);
					rcl.setCloudletStatus(Cloudlet.INEXEC);
					for (int k = 0; k < rcl.getNumberOfPes(); k++) {
						rcl.setMachineAndPeId(0, k);
					}
					getCloudletExecList().add(rcl);
					toRemove.add(rcl);
				}
			}
			getCloudletWaitingList().removeAll(toRemove);
		}

		// 给正在执行的任务分配资源
		for (ResCloudlet rcl : getCloudletExecList()) {
			allocateReasourcesForCloudlet(rcl);
		}

		allocateMipsForCloudlets(mipsShare, currentTime); // 分配Mips给任务
		// 更新正在执行的任务
		for (ResCloudlet rcl : getCloudletExecList()) {
			SimCloudlet cloudlet = (SimCloudlet) rcl.getCloudlet();
			int state;
			if (getCpuAllocator().getAllocatedMipsForSimCloudlet(cloudlet) > 0) {
				state = Cloudlet.INEXEC;
			} else {
				state = Cloudlet.QUEUED;
			}
			cloudlet.addStateHistoryEntry(getPreviousTime(), getCpuAllocator().getAllocatedMipsForSimCloudlet(cloudlet),
					cloudlet.getCurrentRequestedMips(getPreviousTime()),
					getRamAllocator().getAllocatedRamForSimCloudlet(cloudlet), cloudlet.getRam(),
					getIoAllocator().getAllocatedIoForSimCloudlet(cloudlet), cloudlet.getIo(),
					getBwAllocator().getAllocatedBwForSimCloudlet(cloudlet), cloudlet.getBw(), state);
			if (!checkAllocatedReasourcesForCloudlet(rcl)) { // 检查任务的资源是不是足够
				Log.printLine("Cloudlet #" + rcl.getCloudletId() + " failed to update due to Lack of resources");
				// System.exit(0);
			}
			/*
			 * Log.printLine("Cloudlet #" + rcl.getCloudletId() +
			 * " Allocate Mips:" + getTotalCurrentAllocatedMipsForCloudlet(rcl,
			 * getPreviousTime()));
			 */

			long updateLength = (long) (timeSpan * getTotalCurrentAllocatedMipsForCloudlet(rcl, getPreviousTime()));

			rcl.updateCloudletFinishedSoFar(updateLength * Consts.MILLION);
			SimProgressCloudlet pcl = ((SimProgressCloudlet) rcl.getCloudlet());
			pcl.updateCloudletFinishedSoFar(updateLength); // 更新任务进度
			Log.printLine("Cloudlet#" + pcl.getCloudletId() + " Mips:"
					+ getTotalCurrentAllocatedMipsForCloudlet(rcl, getPreviousTime()) + " updateLength: " + updateLength
					+ " timeSpan:" + timeSpan + "  progress: " + pcl.getProgress());
			if (pcl.getProgress() > 0.999) { // finished: remove
				cloudletsToFinish.add(rcl);
				continue;
			}
		}

		for (ResCloudlet rgl : cloudletsToFinish) {
			getCloudletExecList().remove(rgl);
			cloudletFinish(rgl);
		}

		if (getCloudletExecList().size() == 0 && getCloudletWaitingList().size() == 0) {
			setPreviousTime(currentTime);
			return 0.0;
		}

		// 如果有任务结束，则让检查是否有资源让等待的任务启动
		if (!getCloudletWaitingList().isEmpty()) {
			toRemove.clear();
			for (ResCloudlet rcl : getCloudletWaitingList()) {
				SimCloudlet scl = (SimCloudlet) rcl.getCloudlet();
				if (IsCloudletRunnable(scl)) { // 如果资源足够任务
					allocateReasourcesForCloudlet(rcl); // 分配资源给该任务
					Log.printLine("333333333333333 cloudlet#" + scl.getCloudletId() + " waiting in VM#" + scl.getVmId()
							+ " starts to run.");
					rcl.setCloudletStatus(Cloudlet.INEXEC);
					for (int k = 0; k < rcl.getNumberOfPes(); k++) {
						rcl.setMachineAndPeId(0, k);
					}
					getCloudletExecList().add(rcl);
					toRemove.add(rcl);
					break;
				}
			}
			getCloudletWaitingList().removeAll(toRemove);

		}

		// estimate finish time of cloudlets in the execution queue
		for (ResCloudlet rcl : getCloudletExecList()) {
			double estimatedFinishTime = getEstimatedFinishTime(rcl, currentTime);
			if (estimatedFinishTime - currentTime < CloudSim.getMinTimeBetweenEvents()) {
				estimatedFinishTime = currentTime + CloudSim.getMinTimeBetweenEvents();
			}
			if (estimatedFinishTime < nextEvent) {
				nextEvent = estimatedFinishTime;
			}
		}

		setPreviousTime(currentTime);
		return nextEvent;
	}

}
