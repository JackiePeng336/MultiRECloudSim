package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.allocator.*;
import org.cloudbus.cloudsim.core.CloudSim;

public class SimCloudletSchedulerDynamicWorkload extends CloudletSchedulerDynamicWorkload {

	private double cacheCurrentRequestedIo; // 多余的，实际上用不上，求了完整和统一而留下
	private List<? extends ResCloudlet> cloudletWaitingList; // 任务等待列表
	private CloudletCpuAllocatorSimple cpuAllocator; // 任务Mips分配器
	private CloudletRamAllocatorSimple ramAllocator; // 任务内存分配器
	private CloudletIoAllocatorSimple ioAllocator; // 任务io分配器
	private CloudletBwAllocator bwAllocator; // 任务带宽分配器

	public SimCloudletSchedulerDynamicWorkload(double mips, int numberOfPes, CloudletCpuAllocator cpuAllocator,
			CloudletRamAllocator ramAllocator, CloudletIoAllocator ioAllocator, CloudletBwAllocator bwAllocator) {
		super(mips, numberOfPes);
		cloudletWaitingList = new ArrayList<ResCloudlet>();
		setCpuAllocator(cpuAllocator);
		setRamAllocator(ramAllocator);
		setIoAllocator(ioAllocator);
		setBwAllocator(bwAllocator);
	}

	@Override
	public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
		setCurrentMipsShare(mipsShare);
		setMips(getTotalAvailableMips(mipsShare));
		getCpuAllocator().setAvailableMips(getTotalAvailableMips(mipsShare));
		double timeSpan = currentTime - getPreviousTime();
		double nextEvent = Double.MAX_VALUE;// currentTime -
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
			// Log.printLine("Cloudlet #"
			// + rcl.getCloudletId()
			// + " Allocate Mips:"
			// + getTotalCurrentAllocatedMipsForCloudlet(rcl,
			// getPreviousTime()));
			rcl.updateCloudletFinishedSoFar((long) (timeSpan
					* getTotalCurrentAllocatedMipsForCloudlet(rcl, getPreviousTime()) * Consts.MILLION)); // 更新任务进度

			if (rcl.getRemainingCloudletLength() == 0) { // finished: remove
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
					// Log.printLine("333333333333333 cloudlet#"
					// + scl.getCloudletId() + " waiting in VM#"
					// + scl.getVmId() + " starts to run.");
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

	@Override
	public double cloudletSubmit(Cloudlet cl, double fileTransferTime) { // 提交任务
		SimCloudlet cloudlet = (SimCloudlet) cl;
		ResCloudlet rcl = new ResCloudlet(cloudlet);
		if (IsCloudletRunnable(cloudlet)) { // 如果资源足够任务
			allocateReasourcesForCloudlet(rcl); // 分配资源给该任务
			cloudlet.addStateHistoryEntry(getPreviousTime(), getCpuAllocator().getAllocatedMipsForSimCloudlet(cloudlet),
					cloudlet.getCurrentRequestedMips(getPreviousTime()),
					getRamAllocator().getAllocatedRamForSimCloudlet(cloudlet), cloudlet.getRam(),
					getIoAllocator().getAllocatedIoForSimCloudlet(cloudlet), cloudlet.getIo(),
					getBwAllocator().getAllocatedBwForSimCloudlet(cloudlet), cloudlet.getBw(), Cloudlet.INEXEC);
//			Log.printLine(
//					" 1111111111111 cloudlet#" + cloudlet.getCloudletId() + " Submit to VM#" + cloudlet.getVmId());
			rcl.setCloudletStatus(Cloudlet.INEXEC);
			for (int i = 0; i < cl.getNumberOfPes(); i++) {
				rcl.setMachineAndPeId(0, i);
			}
			getCloudletExecList().add(rcl);
			return getEstimatedFinishTime(rcl, getPreviousTime());

		} else { // 否则任务等待
			 Log.printLine("22222222222222 cloudlet#" +
			 cloudlet.getCloudletId()
			 + " Wait in VM#" + cloudlet.getVmId());
			cloudlet.addStateHistoryEntry(getPreviousTime(), getCpuAllocator().getAllocatedMipsForSimCloudlet(cloudlet),
					cloudlet.getCurrentRequestedMips(getPreviousTime()),
					getRamAllocator().getAllocatedRamForSimCloudlet(cloudlet), cloudlet.getRam(),
					getIoAllocator().getAllocatedIoForSimCloudlet(cloudlet), cloudlet.getIo(),
					getBwAllocator().getAllocatedBwForSimCloudlet(cloudlet), cloudlet.getBw(), Cloudlet.QUEUED);
			rcl.setCloudletStatus(Cloudlet.QUEUED);
			getCloudletWaitingList().add(rcl);
			return 0.0;
		}
	}

	protected void allocateMipsForCloudlets(List<Double> mipsShare, double currentTime) { // 给任务分配Mips
		//Log.print("Scheduler allocateMipsForCloudlets totalMips:" + getCpuAllocator().getAvailableMips());
		List<SimCloudlet> list = new ArrayList<SimCloudlet>();
		for (ResCloudlet rcl : getCloudletExecList()) {
			list.add((SimCloudlet) rcl.getCloudlet());
			//Log.print("　 " + rcl.getCloudlet().getVmId());
		}
		//Log.printLine();
		getCpuAllocator().allocateMipsForSimCloudlets(list, currentTime);
	}

	@Override
	public double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, double time) { // 获取当前任务需求的Mips
		return ((SimCloudlet) rcl.getCloudlet()).getCurrentRequestedMips(time);
	}

	/**
	 * Gets the total current mips for the clouddlet.
	 * 
	 * @param mipsShare
	 *            the mips share
	 * @return the total current mips
	 */

	public double getTotalAvailableMips(List<Double> mipsShare) { // 获取总的Mips
		double totalMips = 0.0;
		if(null==mipsShare) return totalMips;
		for (Double mips : mipsShare) {
			totalMips += mips;
		}
		return totalMips;
	}

	/**
	 * Gets the current mips.
	 * 
	 * @param rcl
	 *            the rcl
	 * @param time
	 *            the time
	 * @return the current mips
	 */
	@Override
	public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time) { // 获取当前分配给任务的Mips
		return getCpuAllocator().getAllocatedMipsForSimCloudlet((SimCloudlet) rcl.getCloudlet());
	}

	protected boolean IsResourceAddForCloudlet(SimCloudlet cl) { // 检查是否有资源让任务启动
		if (!getCpuAllocator().isMipsAddForSimCloudlet(cl))
			return false;
		if (!getRamAllocator().isSuitableForSimCloudlet(cl))
			return false;
		if (!getIoAllocator().isSuitableForSimCloudlet(cl))
			return false;
		if (!getBwAllocator().isSuitableForSimCloudlet(cl))
			return false;
		return true;
	}

	protected boolean IsCloudletRunnable(SimCloudlet cl) { // 检查是否有资源让任务启动
		if (!getRamAllocator().isSuitableForSimCloudlet(cl))
			return false;
		if (!getIoAllocator().isSuitableForSimCloudlet(cl))
			return false;
		if (!getBwAllocator().isSuitableForSimCloudlet(cl))
			return false;
		return true;
	}

	@Override
	public void cloudletFinish(ResCloudlet rcl) {
		super.cloudletFinish(rcl);
		releaseResourcesForCloudlet(rcl); // 任务结束，释放资源
		SimCloudlet cloudlet = (SimCloudlet) rcl.getCloudlet();
		cloudlet.addStateHistoryEntry(getPreviousTime(), getCpuAllocator().getAllocatedMipsForSimCloudlet(cloudlet),
				cloudlet.getCurrentRequestedMips(getPreviousTime()),
				getRamAllocator().getAllocatedRamForSimCloudlet(cloudlet), cloudlet.getRam(),
				getIoAllocator().getAllocatedIoForSimCloudlet(cloudlet), cloudlet.getIo(),
				getBwAllocator().getAllocatedBwForSimCloudlet(cloudlet), cloudlet.getBw(), Cloudlet.SUCCESS);
	}

	protected boolean checkAllocatedReasourcesForCloudlet(ResCloudlet rcl) { // 检查任务被分配的资源是否足够
		SimCloudlet scl = (SimCloudlet) rcl.getCloudlet();
		int allocatedRam = getRamAllocator().getAllocatedRamForSimCloudlet(scl);
		long allocatedIo = getIoAllocator().getAllocatedIoForSimCloudlet(scl);
		long allocatedBw = getBwAllocator().getAllocatedBwForSimCloudlet(scl);
		if (scl.getRam() != allocatedRam) {
			Log.printLine("Cloudlet #" + scl.getCloudletId() + " requires " + scl.getRam() + " Ram but is allocated "
					+ allocatedRam);
			return false;
		}
		if (scl.getIo() != allocatedIo) {
			Log.printLine("Cloudlet #" + scl.getCloudletId() + " requires " + scl.getIo() + " Io but is allocated "
					+ allocatedIo);
			return false;
		}
		if (scl.getBw() != allocatedBw) {
			Log.printLine("Cloudlet #" + scl.getCloudletId() + " requires " + scl.getBw() + " Bw but is allocated "
					+ allocatedBw);
			return false;
		}

		return true;
	}

	protected void allocateReasourcesForCloudlet(ResCloudlet rcl) { // 如果分配资源给任务
		SimCloudlet scl = (SimCloudlet) rcl.getCloudlet();
		// Log.printLine("***************allocateReasourcesForCloudlet#"
		// + scl.getCloudletId() + "************");
		getRamAllocator().allocateRamForSimCloudlet(scl);
		getIoAllocator().allocateIoForSimCloudlet(scl);
		getBwAllocator().allocateBwForSimCloudlet(scl);
	}

	protected void releaseResourcesForCloudlet(ResCloudlet rcl) { // 释放任务的资源
		SimCloudlet scl = (SimCloudlet) rcl.getCloudlet();
		// Log.printLine("***************VM#" + scl.getVmId()
		// + " Cloudlet Finish!releaseResourcesForCloudlet#"
		// + scl.getCloudletId() + "************");
		// Log.printLine("Ram:" + scl.getRam() + " Io:" + scl.getIo() + " Bw:"
		// + scl.getBw());
		getCpuAllocator().deallocateMipsForSimCloudlet(scl);
		getRamAllocator().deallocateRamForSimCloudlet(scl);
		getIoAllocator().deallocateIoForSimCloudlet(scl);
		getBwAllocator().deallocateBwForSimCloudlet(scl);
	}

	@Override
	public List<Double> getCurrentRequestedMips() { // 获取当前需求的Mips
		// Log.printLine("getCachePreviousTime() "+getCachePreviousTime()+"
		// previousTime: "+getPreviousTime()
		// + ": getCurrentRequestedMips()");
		if (getCachePreviousTime() == getPreviousTime()) {
			return getCacheCurrentRequestedMips();
		}
		List<Double> currentMips = new ArrayList<Double>();
		double totalMips = 0.0;
		for (ResCloudlet rcl : getCloudletExecList()) {
			totalMips += ((SimCloudlet) rcl.getCloudlet()).getCurrentRequestedMips(getPreviousTime())
					* rcl.getNumberOfPes();
		}
		//Log.printLine(getCloudletExecList().size()+" totalMips " + totalMips);
		double mipsForPe = totalMips / getNumberOfPes();

		for (int i = 0; i < getNumberOfPes(); i++) {
			currentMips.add(mipsForPe);
		}
		setCachePreviousTime(getPreviousTime());
		setCacheCurrentRequestedMips(currentMips);

		return currentMips;
	}

	public List<Double> addCurrentRequestedMips(double mips) { // 增加当前需求的Mips
		List<Double> currentMips = new ArrayList<Double>();

		double totalMips = mips;
		for (Double cacheMips : getCacheCurrentRequestedMips())
			totalMips += cacheMips;
		double mipsForPe = totalMips / getNumberOfPes();
		// Log.printLine(getPreviousTime() + ": AddCurrentRequestedMips: "
		// + totalMips + " mips: " + mips);
		for (int i = 0; i < getNumberOfPes(); i++) {
			currentMips.add(mipsForPe);
		}

		setCacheCurrentRequestedMips(currentMips);
		// Log.printLine("CurrentMips:" + currentMips + " cache "
		// + getCacheCurrentRequestedMips());
		return currentMips;
	}

	@Override
	public double getTotalUtilizationOfCpu(double time) {
		double totalMips = 0;
		for (ResCloudlet rcl : getCloudletExecList()) {
			totalMips += ((SimCloudlet) rcl.getCloudlet()).getCurrentRequestedMips(time);
		}
		return totalMips / (getCpuAllocator().getMipsCap());
	}


	public double getCurrentRequestedUtilizationOfIo() {
		double io = 0;
		for (ResCloudlet cloudlet : getCloudletExecList()) {
			io += ((SimCloudlet)(cloudlet.getCloudlet())).getUtilizationOfIO(CloudSim.clock());
		}
		return io;
	}

	/**
	 * 	不要使用这个函数！！
	 * @deprecated
	 * */
	public double getCurrentRequestedIo() { // 用不上的，多余的，为了完整统一
		if (getCachePreviousTime() == getPreviousTime()) {
			return getCacheCurrentRequestedIo();
		}
		double currentIO = 0;
		double curTime = CloudSim.clock();
		for (ResCloudlet cloudlet : getCloudletExecList()) { // 每个正在执行的任务
			SimCloudlet scl = (SimCloudlet) cloudlet.getCloudlet();
			currentIO += (double) scl.getIo() * scl.getUtilizationOfIO(curTime); // 利用率乘以任务最大IO
		}

		setCacheCurrentRequestedIo(currentIO);
		return currentIO;
	}


	public <T extends ResCloudlet> List<T> getCloudletExecList() { // 返回正在执行的任务列表
		return super.getCloudletExecList();
	}

	public double getCacheCurrentRequestedIo() {
		return cacheCurrentRequestedIo;
	}

	public void setCacheCurrentRequestedIo(double cacheCurrentRequestedIo) {
		this.cacheCurrentRequestedIo = cacheCurrentRequestedIo;
	}

	@SuppressWarnings("unchecked")
	public <T extends ResCloudlet> List<T> getCloudletWaitingList() { // 返回任务等待队列
		return (List<T>) cloudletWaitingList;
	}

	protected <T extends ResCloudlet> void cloudletWaitingList(List<T> cloudletWaitingList) {
		this.cloudletWaitingList = cloudletWaitingList;
	}

	public CloudletCpuAllocatorSimple getCpuAllocator() {
		return cpuAllocator;
	}

	public void setCpuAllocator(CloudletCpuAllocator cpuAllocator) {
		this.cpuAllocator = (CloudletCpuAllocatorSimple) cpuAllocator;
	}

	public CloudletRamAllocatorSimple getRamAllocator() {
		return ramAllocator;
	}

	public void setRamAllocator(CloudletRamAllocator ramAllocator) {
		this.ramAllocator = (CloudletRamAllocatorSimple) ramAllocator;
	}

	public CloudletIoAllocatorSimple getIoAllocator() {
		return ioAllocator;
	}

	public void setIoAllocator(CloudletIoAllocator ioAllocator) {
		this.ioAllocator = (CloudletIoAllocatorSimple) ioAllocator;
	}

	public CloudletBwAllocator getBwAllocator() {
		return bwAllocator;
	}

	public void setBwAllocator(CloudletBwAllocator bwAllocator) {
		this.bwAllocator = bwAllocator;
	}


}
