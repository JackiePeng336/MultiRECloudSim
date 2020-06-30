package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.SimCloudlet;
import org.cloudbus.cloudsim.SimCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.allocator.CloudletBwAllocatorSimple;
import org.cloudbus.cloudsim.allocator.CloudletCpuAllocatorSimple;
import org.cloudbus.cloudsim.allocator.CloudletIoAllocatorSimple;
import org.cloudbus.cloudsim.allocator.CloudletRamAllocatorSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.main.Helper;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.SimIoProvisioner;
import org.cloudbus.cloudsim.provisioners.SimIoProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.provisioners.SimBwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.SimRamProvisionerSimple;
import org.cloudbus.cloudsim.util.Constants;
import org.cloudbus.cloudsim.util.MathUtil;

public class SimPowerHost extends PowerHostUtilizationHistory {

	private double utilizationRam; // 当前内存利用率
	private double previousUtilizationRam; // 前一时刻内存利用率
	private double utilizationIO; // 当前IO利用率
	private double previousUtilizationIO; // 前一时刻IO利用率
	private double utilizationBw; // 当前带宽利用率
	private double previousUtilizationBw; // 前一时刻带宽利用率

	private SimIoProvisioner ioProvisioner; // IO配置器
	private PowerModel powerModelCPU; // CPU能耗模型
	private PowerModel powerModelRam; // 内存能耗模型
	private PowerModel powerModelIO; // IO能耗模型
	private PowerModel powerModelBw; // 带宽能耗模型

	public SimPowerHost(int id, RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner, SimIoProvisioner ioProvisioner,
			long storage, List<? extends Pe> peList, VmScheduler vmScheduler,
			PowerModel powerModelCpu, PowerModel powerModelRam,
			PowerModel powerModelIO, PowerModel powerModelBw) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler,
				powerModelCpu);
		setIoProvisioner(ioProvisioner);
		setPowerModelCPU(powerModelCpu);
		setPowerModelRam(powerModelRam);
		setPowerModelIO(powerModelIO);
		setPowerModelBw(powerModelBw);
		setUtilizationRam(0);
		setPreviousUtilizationRam(0);
		setUtilizationIO(0);
		setPreviousUtilizationIO(0);
		setUtilizationBw(0);
		setPreviousUtilizationBw(0);
	}

	@Override
	public boolean isSuitableForVm(Vm vm) { // 检查IO及其他条件是否满足创建虚拟机
		boolean a = getVmScheduler().getPeCapacity()  >= vm.getCurrentRequestedMaxMips();
		boolean b = getVmScheduler().getAvailableMips()  >= vm.getCurrentRequestedTotalMips();

		boolean c = getRamProvisioner().isSuitableForVm(vm,
				vm.getCurrentRequestedRam());

		boolean d = getBwProvisioner().isSuitableForVm(vm,
				vm.getCurrentRequestedBw());

		boolean e = getIoProvisioner().isSuitableForVm((SimPowerVm) vm,
				((SimPowerVm) vm).getCurrentRequestedIo());

		return a
				&& b
				&& c
				&& d
				&& e;
	}

	@Override
	public boolean vmCreate(Vm vm) { // 创建虚拟机
		if (getStorage() < vm.getSize()) {
			Log.printLine("Allocation of VM #"
					+ vm.getId() + " to Host #" + getId()
					+ " failed by storage");
			return false;
		}

		if (!getRamProvisioner().allocateRamForVm(vm,
				vm.getCurrentRequestedRam())) {
			Log.printLine("Allocation of VM #"
					+ vm.getId() + " to Host #" + getId() + " failed by RAM");
			return false;
		}

		if (!getBwProvisioner().allocateBwForVm(vm, vm.getCurrentRequestedBw())) {
			Log.printLine("Allocation of VM #"
					+ vm.getId() + " to Host #" + getId() + " failed by BW");
			getRamProvisioner().deallocateRamForVm(vm);
			return false;
		}

		// IO资源分配
		if (!getIoProvisioner().allocateIoForVm((SimPowerVm) vm,
				((SimPowerVm) vm).getCurrentRequestedIo())) {
			Log.printLine("Allocation of VM #"
					+ vm.getId() + " to Host #" + getId() + " failed by IO");
			getRamProvisioner().deallocateRamForVm(vm);
			getBwProvisioner().deallocateBwForVm(vm);
			getIoProvisioner().deallocateIoForVm((SimPowerVm) vm);
			return false;
		}

		if(vm.getCurrentRequestedMips() == null){
			System.out.println(vm.getCurrentAllocatedRam());
			System.out.println(((SimPowerVm)vm).getCurrentAllocatedIo());
			System.exit(1);
		}

		if (!getVmScheduler()
				.allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
			Log.printLine("Allocation of VM #"
					+ vm.getId() + " to Host #" + getId() + " failed by MIPS");
			getRamProvisioner().deallocateRamForVm(vm);
			getIoProvisioner().deallocateIoForVm((SimPowerVm) vm);
			getBwProvisioner().deallocateBwForVm(vm);
			return false;
		}

		setStorage(getStorage() - vm.getSize());
		getVmList().add(vm);
		vm.setHost(this);
		return true;
	}

	@Override
	protected void vmDeallocate(Vm vm) {
		getRamProvisioner().deallocateRamForVm(vm);
		getBwProvisioner().deallocateBwForVm(vm);
		getIoProvisioner().deallocateIoForAllVms();
		getVmScheduler().deallocatePesForVm(vm);
		setStorage(getStorage() + vm.getSize());
	}

	@Override
	protected void vmDeallocateAll() {
		getRamProvisioner().deallocateRamForAllVms();
		getBwProvisioner().deallocateBwForAllVms();
		getIoProvisioner().deallocateIoForAllVms();
		getVmScheduler().deallocatePesForAllVms();
	}

	@Override
	public double updateVmsProcessing(double currentTime) { // 更新虚拟机任务进度
		double smallerTime = Double.MAX_VALUE;

		for (Vm vm : getVmList()) {
			double time = vm.updateVmProcessing(currentTime, getVmScheduler()
					.getAllocatedMipsForVm(vm)); // 更新虚拟机任务进度
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}

		setPreviousUtilizationMips(getUtilizationMips()); // 设置上一时刻的Mip利用率
		setPreviousUtilizationRam(getUtilizationRam()); // 设置上一时刻的内存利用率
		setPreviousUtilizationIO(getUtilizationIO()); // 设置上一时刻的IO利用率
		setPreviousUtilizationBw(getUtilizationBw()); // 设置上一时刻的带宽利用率
		setUtilizationMips(0);

		double hostTotalRequestedMips = 0; // 主机总Mip

		for (Vm vm : getVmList()) {
			getVmScheduler().deallocatePesForVm(vm); // 清空PEs分配
			SimCloudletSchedulerDynamicWorkload scheduler = ((SimCloudletSchedulerDynamicWorkload) vm
					.getCloudletScheduler());
			CloudletCpuAllocatorSimple cpuAllocator = (CloudletCpuAllocatorSimple) scheduler
					.getCpuAllocator();
			CloudletRamAllocatorSimple ramAllocator = (CloudletRamAllocatorSimple) scheduler
					.getRamAllocator();
			CloudletIoAllocatorSimple ioAllocator = (CloudletIoAllocatorSimple) scheduler
					.getIoAllocator();
			CloudletBwAllocatorSimple bwAllocator = (CloudletBwAllocatorSimple) scheduler
					.getBwAllocator();
			cpuAllocator.resetMipsTable();
			ramAllocator.resetRamTable(); // 重置任务-内存分配表
			ioAllocator.resetIoTable(); // 重置任务-IO分配表
			bwAllocator.resetBwTable(); // 重置任务-带宽分配表
		}

		getRamProvisioner().deallocateRamForAllVms(); // 清空内存分配
		getIoProvisioner().deallocateIoForAllVms(); // 清空IO分配
		getBwProvisioner().deallocateBwForAllVms(); // 清空带宽分配

		for (Vm vm : getVmList()) { // 分配PE，内存，IO，带宽资源给虚拟机
			//Log.printLine("allocatePesForVm "+vm.getId()+" "+ ((SimPowerVm) vm).getCurrentRequestedMips());
			getVmScheduler().allocatePesForVm(vm,
					((SimPowerVm) vm).getCurrentRequestedMips()); // 分配核芯
			SimCloudletSchedulerDynamicWorkload scheduler = ((SimCloudletSchedulerDynamicWorkload) vm
					.getCloudletScheduler());
			Log.printLine(
					"AllocatePesForVm " + vm.getId() + " " + ((SimPowerVm) vm).getCurrentRequestedMips() + " Scheduler "
							+ scheduler.getCurrentRequestedMips().toString() + " " + scheduler.getTotalCurrentMips() + " " + scheduler.getCurrentMipsShare().toString());
			CloudletCpuAllocatorSimple cpuAllocator = (CloudletCpuAllocatorSimple) scheduler
					.getCpuAllocator();
			cpuAllocator.setAvailableMips(scheduler
					.getTotalAvailableMips(getVmScheduler()
							.getAllocatedMipsForVm(vm)));
			if(null!= getVmScheduler().getAllocatedMipsForVm(vm)){
				Log.printLine("Vm #" + vm.getId() + " Current getAllocatedMips:"
					+ getVmScheduler().getAllocatedMipsForVm(vm).toString()
					+ " RequestMips:"
					+ ((SimPowerVm) vm).getCurrentRequestedMips().toString());
			}
			List<ResCloudlet> cloudletExecList = ((SimCloudletSchedulerDynamicWorkload) vm
					.getCloudletScheduler()).getCloudletExecList(); // 虚拟机的正在执行任务队列

			int requestedRam = 0;
			long requestedIo = 0;
			long requestedBw = 0;
			for (ResCloudlet rcl : cloudletExecList) {
				SimCloudlet scl = (SimCloudlet) rcl.getCloudlet();
				requestedRam += scl.getRam();
				requestedIo += scl.getIo();
				requestedBw += scl.getBw();
				Log.printLine("SimPowerHost Exe scl#" + scl.getCloudletId()
						+ " Ram:" + scl.getRam() + " Io:" + scl.getIo()
						+ " bw:" + scl.getBw());
			}
			//Log.printLine("———————————————— Host update Allocate Reasources ——————————————————————————");
			getRamProvisioner().allocateRamForVm(vm, requestedRam);
			getIoProvisioner().allocateIoForVm((SimPowerVm) vm, requestedIo);
			getBwProvisioner().allocateBwForVm(vm, requestedBw);
			// Log.printLine("---------------Vm #" +
			// vm.getId()+" alocate finished.------------------");
		}

		for (Vm vm : getVmList()) { // 追加内存，IO，带宽资源给虚拟机
			List<ResCloudlet> cloudletWaitingList = ((SimCloudletSchedulerDynamicWorkload) vm
					.getCloudletScheduler()).getCloudletWaitingList(); // 虚拟机的任务等待队列

			//Log.printLine("———————————————— Host update Vm# " + vm.getId()
			//		+ " add Reasources ——————————————————————————");
			for (ResCloudlet rcl : cloudletWaitingList) {
				SimCloudlet scl = (SimCloudlet) rcl.getCloudlet();
				if (isCloudletRunnable(scl, vm)) {
					// Log.printLine("SimPowerHost Mips check isSuitableForSimCloudlet:Cloudlet#"
					// + scl.getCloudletId());
					addResourcesToVm(scl, vm, currentTime);

				}
			}

		}

		for (Vm vm : getVmList()) {
			double vmRequestedMips = vm.getCurrentRequestedTotalMips();
			double vmAllocatedMips = getVmScheduler()
					.getTotalAllocatedMipsForVm(vm);
			
			if (!Log.isDisabled()) {
				Log.formatLine(
						"%.2f: [Host #"
								+ getId()
								+ "] Total allocated MIPS for VM #"
								+ vm.getId()
								+ " (Host #"
								+ vm.getHost().getId()
								+ ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
						CloudSim.clock(), vmAllocatedMips, vmRequestedMips,
						vm.getMips(), vmRequestedMips / vm.getMips() * 100);
				if(vmAllocatedMips==0.0){
					//System.exit(0);
				}
				List<Pe> pes = getVmScheduler().getPesAllocatedForVM(vm);
				StringBuilder pesString = new StringBuilder();
				if(pes!=null){
				for (Pe pe : pes) {
					pesString.append(String.format(" PE #" + pe.getId()
							+ ": %.2f.", pe.getPeProvisioner()
							.getTotalAllocatedMipsForVm(vm)));
				}
				Log.formatLine("%.2f: [Host #" + getId() + "] MIPS for VM #"
						+ vm.getId() + " by PEs (" + getNumberOfPes() + " * "
						+ getVmScheduler().getPeCapacity() + ")." + pesString,
						CloudSim.clock());
				}
			}

			if (getVmsMigratingIn().contains(vm)) {
				Log.formatLine(
						"%.2f: [Host #" + getId() + "] VM #" + vm.getId()
								+ " is being migrated to Host #" + getId(),
						CloudSim.clock());
			} else {
				if (vmAllocatedMips + 0.1 < vmRequestedMips) {
					Log.formatLine("%.2f: [Host #" + getId()
							+ "] Under allocated MIPS for VM #" + vm.getId()
							+ ": %.2f", CloudSim.clock(), vmRequestedMips
							- vmAllocatedMips);
				}

				vm.addStateHistoryEntry(currentTime, vmAllocatedMips,
						vmRequestedMips,
						(vm.isInMigration() && !getVmsMigratingIn()
								.contains(vm)));

				if (vm.isInMigration()) {
					Log.formatLine(
							"%.2f: [Host #" + getId() + "] VM #" + vm.getId()
									+ " is in migration", CloudSim.clock());
					vmAllocatedMips /= 0.9; // performance degradation due to
											// migration - 10%
				}
			}

			setUtilizationMips(getUtilizationMips() + vmAllocatedMips);
			hostTotalRequestedMips += vmRequestedMips;
		}

		setUtilizationRam(getUtilizationOfRam());
		setUtilizationIO(getUtilizationOfIO());
		setUtilizationBw(getUtilizationOfBw());
		
		addStateHistoryEntry(currentTime, getUtilizationMips(),
				hostTotalRequestedMips, (getUtilizationMips() > 0));

		return smallerTime;
	}

	public boolean isCloudletRunnable(SimCloudlet cloudlet, Vm vm) { // 检查任务是否可以启动
		// Log.printLine("IsCloudlet Runnable #"+cloudlet.getCloudletId()+"");
		List<ResCloudlet> executing = ((SimCloudletSchedulerDynamicWorkload) ((SimPowerVm) vm).getCloudletScheduler())
				.getCloudletExecList();
		double occupiedMips = vm.getCurrentRequestedTotalMips();
		int occupiedRam = vm.getCurrentRequestedRam();
		long occupiedIo = ((SimPowerVm)vm).getCurrentRequestedIo();
		long occupiedBw = vm.getCurrentRequestedBw();
		/*for (ResCloudlet c : executing) {
			occupiedMips += ((SimCloudlet) c.getCloudlet()).getMips();
			occupiedRam += ((SimCloudlet) c.getCloudlet()).getRam();
			occupiedIo += ((SimCloudlet) c.getCloudlet()).getIo();
			occupiedBw += ((SimCloudlet) c.getCloudlet()).getBw();
			System.out.println("Vm# "+vm.getId()+" sc#"+ c.getCloudletId()+" occMips:"+occupiedMips);
		}*/
		SimCloudletSchedulerDynamicWorkload scheduler = ((SimCloudletSchedulerDynamicWorkload) vm
				.getCloudletScheduler());
		//Log.printLine("isCloudletRunnable scheduler requested "+ ((SimPowerVm)vm).getCurrentRequestedMips().toString());
		//System.out.println("Sc#"+cloudlet.getCloudletId()+" " + cloudlet.getMips() + " Occupy:" + occupiedMips + " " + (cloudlet.getMips() + occupiedMips)
		//		+ " VM# "+vm.getId()+ ":" + vm.getMips());
		if (cloudlet.getMips() > getVmScheduler().getAvailableMips() || cloudlet.getMips() + occupiedMips > vm.getMips())
			return false;
		if (!((SimRamProvisionerSimple) getRamProvisioner()).isSuitableForCloudlet(cloudlet.getRam())
				|| cloudlet.getRam() + occupiedRam > vm.getRam())
			return false;
		if (!((SimIoProvisionerSimple) getIoProvisioner()).isSuitableForCloudlet(cloudlet.getIo())
				|| cloudlet.getIo() + occupiedIo > ((SimPowerVm) vm).getIo())
			return false;
		if (!((SimBwProvisionerSimple) getBwProvisioner()).isSuitableForCloudlet(cloudlet.getBw())
				|| cloudlet.getBw() + occupiedBw > vm.getBw())
			return true;
		
		/*if (!((SimCloudletSchedulerDynamicWorkload) vm.getCloudletScheduler())
				.getCpuAllocator().isSuitableForSimCloudlet(cloudlet))
			return false;
		if (!((SimRamProvisionerSimple) getRamProvisioner())
				.isSuitableForCloudlet(cloudlet.getRam()))
			return false;
		if (!((SimIoProvisionerSimple) getIoProvisioner())
				.isSuitableForCloudlet(cloudlet.getIo()))
			return false;
		if (!((SimBwProvisionerSimple) getBwProvisioner())
				.isSuitableForCloudlet(cloudlet.getBw()))
			return false;*/
		return true;
	}

	protected void addResourcesToVm(SimCloudlet scl, Vm vm, double currentTime) { //追加任务资源给Vm
		getVmScheduler().deallocatePesForVm(vm); // 清空PEs分配
		SimCloudletSchedulerDynamicWorkload scheduler = ((SimCloudletSchedulerDynamicWorkload) vm
				.getCloudletScheduler());
		scheduler.addCurrentRequestedMips(scl
				.getCurrentRequestedMips(currentTime)); //增加当前需求的Mips
		//Log.printLine("cloudlet #" + scl.getCloudletId()
		//		+ " Add After: Vm Current RequestMips:"
		//		+ ((SimPowerVm) vm).getCurrentRequestedMips().toString());
		getVmScheduler().allocatePesForVm(vm,
				((SimPowerVm) vm).getCurrentRequestedMips()); // 主机分配Mips给Vm

		CloudletCpuAllocatorSimple cpuAllocator = (CloudletCpuAllocatorSimple) scheduler
				.getCpuAllocator();
		cpuAllocator.setAvailableMips(scheduler
				.getTotalAvailableMips(getVmScheduler().getAllocatedMipsForVm(
						vm))); //重新确定被分配的MIPS

		//Log.printLine("addResources Vm allocate Mips:"
		//		+ getVmScheduler().getAllocatedMipsForVm(vm));
		Log.printLine("//VM#" + vm.getId() + " cloudlet#"
				+ scl.getCloudletId() + " asks for more Reasources. mips:"
				+ vm.getCurrentRequestedTotalMips());
		
		//追加内存，IO，带宽资源
		((SimRamProvisionerSimple) getRamProvisioner()).addRamForVm(vm,
				scl.getRam());
		((SimIoProvisionerSimple) getIoProvisioner()).addIoForVm((SimPowerVm) vm,
				scl.getIo());
		((SimBwProvisionerSimple) getBwProvisioner()).addBwForVm(vm,
				scl.getBw());
	}

	@Override
	public List<Vm> getCompletedVms() { //已经完成任务的虚拟机
		List<Vm> vmsToRemove = new ArrayList<Vm>();
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				continue;
			}
			SimCloudletSchedulerDynamicWorkload scheduler = ((SimCloudletSchedulerDynamicWorkload) vm
					.getCloudletScheduler());

			if (scheduler.getCloudletWaitingList().size() == 0
					&& scheduler.getCloudletExecList().size() == 0) { //已经完成所有任务
				vmsToRemove.add(vm);
			}
		}
		return vmsToRemove;
	}

	public double getPower() {
		double power = getPowerCPU(getUtilizationOfCpu())
				+ getPowerRam(getUtilizationOfRam())
				+ getPowerIO(getUtilizationOfIO())
				+ getPowerBw(getUtilizationOfBw());
		return power;
	}

	/**
	 * Gets the power. For this moment only consumed by all PEs.
	 * 
	 * @param utilization
	 *            the utilization
	 * @return the power
	 */
	protected double getPowerCPU(double utilization) { // 返回CPU能耗
		double power = 0;
		try {
			power = getPowerModelCPU().getPower(utilization);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	protected double getPowerRam(double utilization) { // 返回内存能耗
		double power = 0;
		try {
			power = getPowerModelRam().getPower(utilization);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	protected double getPowerIO(double utilization) { // 返回IO能耗
		double power = 0;
		try {
			power = getPowerModelIO().getPower(utilization);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	protected double getPowerBw(double utilization) { // 返回带宽能耗
		double power = 0;
		try {
			power = getPowerModelBw().getPower(utilization);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	/**
	 * Gets the max power that can be consumed by the host.
	 * 
	 * @return the max power
	 */
	public double getMaxPower() {
		double power = 0;
		try {
			power = getPowerCPU(1) + getPowerRam(1) + getPowerIO(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	/**
	 * Gets the energy consumption using linear interpolation of the utilization
	 * change.
	 * 
	 * @param fromUtilization
	 *            the from utilization
	 * @param toUtilization
	 *            the to utilization
	 * @param time
	 *            the time
	 * @return the energy
	 */

	public double getEnergyLinearInterpolation(double fromUtilization,
			double toUtilization, double time, PowerModel powerModel) {
		if (fromUtilization == 0) {
			return 0;
		}
		double fromPower = powerModel.getPower(fromUtilization);
		double toPower = powerModel.getPower(toUtilization);
		return (fromPower + (toPower - fromPower) / 2) * time;
	}

	@Override
	public double getUtilizationOfRam() { // 返回当前内存利用率
		double u = (double) getRamProvisioner().getUsedRam() / (double) getRam();
		return u;
	}

	public double getUtilizationOfIO() { // 返回当前IO利用率
		return (double) getIoProvisioner().getUsedIo() / (double) getIo();
	}

	@Override
	public double getUtilizationOfBw() { // 返回当前带宽利用率
		return (double) getBwProvisioner().getUsedBw() / (double) getBw();
	}

	public long getIo() {
		return getIoProvisioner().getIo();
	}

	/**
	 * 	别用这个  这个通常是用来设置previous用的
	 * @deprecated
	 * */
	public double getUtilizationRam() {
		return utilizationRam;
	}

	public void setUtilizationRam(double utilizationRam) {
		this.utilizationRam = utilizationRam;
	}

	public double getPreviousUtilizationRam() {
		return previousUtilizationRam;
	}

	public void setPreviousUtilizationRam(double previousUtilizationRam) {
		this.previousUtilizationRam = previousUtilizationRam;
	}

	/**
	 * Gets the host utilization history.
	 *	For MMEE
	 * @return the host utilization history
	 */
	public double[] getUtilizationHistory() {
		double[] utilizationHistory = new double[Constants.HISTORY_LENGTH];
		double hostMips = getTotalMips();
		for (PowerVm vm : this.<PowerVm> getVmList()) {
			for (int i = 0; i < vm.getUtilizationHistory().size(); i++) {
				utilizationHistory[i] += vm.getUtilizationHistory().get(i) * vm.getMips() / hostMips;
			}
		}
		return MathUtil.trimZeroTail(utilizationHistory);
	}
	/**
	 * 	别用这个  这个通常是用来设置previous用的
	 * @deprecated
	 * */
	public double getUtilizationIO() {
		return utilizationIO;
	}

	public void setUtilizationIO(double utilizationIO) {
		this.utilizationIO = utilizationIO;
	}

	public double getPreviousUtilizationIO() {
		return previousUtilizationIO;
	}

	public void setPreviousUtilizationIO(double previousUtilizationIO) {
		this.previousUtilizationIO = previousUtilizationIO;
	}

	public double getUtilizationBw() {
		return utilizationBw;
	}

	public void setUtilizationBw(double utilizationBw) {
		this.utilizationBw = utilizationBw;
	}

	public double getPreviousUtilizationBw() {
		return previousUtilizationBw;
	}

	public void setPreviousUtilizationBw(double previousUtilizationBw) {
		this.previousUtilizationBw = previousUtilizationBw;
	}

	public PowerModel getPowerModelCPU() {
		return powerModelCPU;
	}

	public void setPowerModelCPU(PowerModel powerModelCPU) {
		this.powerModelCPU = powerModelCPU;
	}

	public PowerModel getPowerModelRam() {
		return powerModelRam;
	}

	public void setPowerModelRam(PowerModel powerModelRam) {
		this.powerModelRam = powerModelRam;
	}

	public PowerModel getPowerModelIO() {
		return powerModelIO;
	}

	public void setPowerModelIO(PowerModel powerModelIO) {
		this.powerModelIO = powerModelIO;
	}

	public PowerModel getPowerModelBw() {
		return powerModelBw;
	}

	public void setPowerModelBw(PowerModel powerModelBw) {
		this.powerModelBw = powerModelBw;
	}

	public SimIoProvisioner getIoProvisioner() {
		return ioProvisioner;
	}

	public void setIoProvisioner(SimIoProvisioner ioProvisioner) {
		this.ioProvisioner = ioProvisioner;
	}

}
