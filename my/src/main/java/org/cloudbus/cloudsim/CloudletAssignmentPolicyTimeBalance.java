package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.lists.VmList;

public class CloudletAssignmentPolicyTimeBalance extends CloudletAssignmentPolicy {

	private Map<Integer, Float> cpuLoadIndicator; // 任务-CPU负载指标
	private Map<Integer, Float> ramLoadIndicator; // 任务-内存负载指标
	private Map<Integer, Float> ioLoadIndicator; // 任务-IO负载指标
	private Map<Integer, Float> bwLoadIndicator; // 任务-带宽负载指标
	private Map<Integer, Double> timeLoadIndicator;

	private final static int NORMAL_LENGTH = 1000; // 任务长度参考标准值
	private final static int NORMAL_MIPS = 3067; // 任务MIPS参考标准值
	private final static int NORMAL_RAM = 1024; // 任务内存参考标准值
	private final static int NORAML_IO = 83; // 任务IO参考标准值
	private final static int NORMAL_BW = 10000; // 任务带宽参考标准值

	private final static double TIME_WEIGHT = 0.7;
	private final static double CPU_WEIGHT = 0.3;
	private final static double RAM_WEIGHT = 0.1;
	private final static double IO_WEIGHT = 0.1;
	private final static double BW_WEIGHT = 0.0;

	private final static double REFERENCE_TIME = 30;
	private final static double REFERENCE_CPU = 900;
	private final static double REFERENCE_RAM = 768;
	private final static double REFERENCE_IO = 30;
	private final static double REFERENCE_BW = 1000;

	private final static int CPU = 0; // CPU密集型
	private final static int RAM = 1; // 内存密集型
	private final static int IO = 2; // IO密集型
	private final static int BW = 3; // 带宽密集型
	private List<Vm> vmList; // 待接收任务的虚拟机列表
	
	public CloudletAssignmentPolicyTimeBalance() { // 均衡任务分配器
		setCpuLoadIndicator(new HashMap<Integer, Float>());
		setRamLoadIndicator(new HashMap<Integer, Float>());
		setIoLoadIndicator(new HashMap<Integer, Float>());
		setBwLoadIndicator(new HashMap<Integer, Float>());
		setTimeLoadIndicator(new HashMap<Integer, Double>());
		setVmList(new ArrayList<Vm>());
	}

	private double getCloudletPriroity(SimCloudlet c) {
		double priority = TIME_WEIGHT * c.getCloudletLength() / c.getAverageMips() / REFERENCE_TIME;
		priority += CPU_WEIGHT * c.getAverageMips() / REFERENCE_CPU;// +
																	// RAM_WEIGHT
																	// *
																	// c.getRam()
																	// /
																	// REFERENCE_RAM;
		// priority += IO_WEIGHT * c.getIo() / REFERENCE_IO + BW_WEIGHT *
		// c.getBw() / REFERENCE_BW;
		//double priority = (double)c.getCloudletLength()/(double)c.getMaxMips();
		return priority;
	}

	@Override
	public int[] assignCloudletsToVm(List<SimCloudlet> cloudletlist, List<Vm> vmlist) {
		List<SimCloudlet> scList = new ArrayList<SimCloudlet>();
		scList.addAll(cloudletlist);
		Map<Integer, Integer> assign = new HashMap<Integer, Integer>();
		addNewVmList(vmlist);
		for (Vm vm : vmlist) {
			timeLoadIndicator.put(vm.getId(), 0.0);
		}
		Collections.sort(scList, new Comparator<SimCloudlet>() {
			public int compare(SimCloudlet sc0, SimCloudlet sc1) {
				double priority0 = getCloudletPriroity(sc0);
				double priority1 = getCloudletPriroity(sc1);
				if (priority0 < priority1)
					return 1;
				else if (priority0 == priority1) {
					return 0;
				} else
					return -1;
			}
		});
		int n = cloudletlist.size();
		int[] result = new int[n];
		for (int i = 0; i < n; i++) {
			if (scList.get(i).getVmId() != -1)
				assign.put(scList.get(i).getCloudletId(), scList.get(i).getVmId());
			else {
				// int type = getCloudletIntensiveType(cloudletlist.get(i));
				int vmid = findMinTimeLoadVm();
				assign.put(scList.get(i).getCloudletId(), vmid);
				// Log.printLine("findMinLoadVmByType Type " + type + "
				// result[i]" + result[i]);
				// printVmAllLoad();
				addLoadForVm(VmList.getById(vmlist, vmid), scList.get(i));
			}
		}
		for (int i = 0; i < n; i++) {
			//System.out.println("TB "+cloudletlist.get(i).getCloudletId());
			result[i] = assign.get(cloudletlist.get(i).getCloudletId());
		}
		return result;
	}

	public static int getCloudletIntensiveType(SimCloudlet cloudlet) { // 判断任务是哪种密集型
		double[] type = new double[4];
		type[CPU] = (double) cloudlet.getAverageMips() / (double) NORMAL_MIPS;
		type[RAM] = (double) cloudlet.getRam() / (double) NORMAL_RAM;
		type[IO] = (double) cloudlet.getIo() / (double) NORAML_IO;
		type[BW] = (double) cloudlet.getBw() / (double) NORMAL_BW;
		// Log.printLine("getCloudletIntensiveType "+type[CPU]+" "+type[RAM]+"
		// "+type[IO]+" "+type[BW]);
		double max = 0.0;
		int maxType = 0;
		for (int i = 0; i < 4; i++) {
			if (type[i] > max) {
				max = type[i];
				maxType = i;
			}
		}
		return maxType;
	}

	private int findMinTimeLoadVm() { // 根据密集型找该负载最小的虚拟机
		/*
		 * Map<Integer, Float> loadIndicator = null; switch (type) { case CPU:
		 * loadIndicator = getCpuLoadIndicator(); break; case RAM: loadIndicator
		 * = getRamLoadIndicator(); break; case IO: loadIndicator =
		 * getIoLoadIndicator(); break; case BW: loadIndicator =
		 * getBwLoadIndicator(); break; }
		 */

		double minLoad = Double.MAX_VALUE;
		int id = 0;
		for (Integer vmid : timeLoadIndicator.keySet()) {
			if (timeLoadIndicator.get(vmid) < minLoad) {
				minLoad = timeLoadIndicator.get(vmid);
				id = vmid;
			}
		}
		//System.out.println("findMinTimeLoadVm " + minLoad + " id " + id);
		return id;
	}

	private void initLoadIndicator(List<Vm> vmlist) { // 初始化虚拟机负载指标
		if (vmlist == null)
			return;
		for (Vm vm : vmlist) {
			getCpuLoadIndicator().put(vm.getId(), (float) 0.0);
			getRamLoadIndicator().put(vm.getId(), (float) 0.0);
			getIoLoadIndicator().put(vm.getId(), (float) 0.0);
			getBwLoadIndicator().put(vm.getId(), (float) 0.0);

		}
	}

	private void addNewVmList(List<Vm> vmlist) { // 若有新的虚拟机则添加
		List<Vm> newVmList = new ArrayList<Vm>();
		if (!getVmList().containsAll(vmlist)) {
			newVmList.addAll(vmlist);
		} else {
			for (Vm vm : vmlist) {
				if (!getVmList().contains(vm))
					newVmList.add(vm);
			}
		}

		getVmList().addAll(newVmList);
		initLoadIndicator(newVmList);
	}

	private float getVmLoadByIndicator(Map<Integer, Float> indicator, int vmid) { // 根据指标和虚拟机Id返回负载
		if (indicator.containsKey(vmid))
			return indicator.get(vmid);
		else
			return (float) 0.0;
	}

	private void addLoadForVm(Vm vm, SimCloudlet cloudlet) { // 根据任务给虚拟机添加负载
		/*
		 * float cpuLoad = getCpuLoadForVm(vm); float ramLoad =
		 * getRamLoadForVm(vm); float ioLoad = getIoLoadForVm(vm); float bwLoad
		 * = getBwLoadForVm(vm); float weight = (float)
		 * (cloudlet.getCloudletLength()/cloudlet.getAverageMips());// /
		 * cloudlet.getAverageMips());//cloudlet.getAverageMips());//
		 * NORMAL_LENGTH; // 长度是权重
		 * 
		 * getCpuLoadIndicator().put( vm.getId(), (float) (cpuLoad + weight *
		 * cloudlet.getAverageMips() / NORMAL_MIPS));
		 * 
		 * getRamLoadIndicator().put(vm.getId(), (float) (ramLoad + weight *
		 * cloudlet.getRam() / NORMAL_RAM));
		 * 
		 * getIoLoadIndicator().put(vm.getId(), (float) (ioLoad + weight *
		 * cloudlet.getIo() / NORAML_IO));
		 * 
		 * getBwLoadIndicator().put(vm.getId(), (float) (bwLoad + weight *
		 * cloudlet.getBw() / NORMAL_BW));
		 */
		double timeLoad = (double) cloudlet.getCloudletLength() / (double) cloudlet.getMaxMips();
		timeLoadIndicator.put(vm.getId(), timeLoadIndicator.get(vm.getId()) + timeLoad);
	}

	public void removeLoadForVm(Vm vm, SimCloudlet cloudlet) {// 根据任务给虚拟机减少负载
		double timeLoad = (double) cloudlet.getCloudletLength() / (double) cloudlet.getMaxMips();
		timeLoadIndicator.put(vm.getId(), timeLoadIndicator.get(vm.getId()) - timeLoad);
	}

	public void printVmAllLoad() {
		Log.printLine("printVmAllLoad");
		for (Integer vmid : getCpuLoadIndicator().keySet()) {
			Log.printLine("Vm#" + vmid + " " + getCpuLoadIndicator().get(vmid) + " " + getRamLoadIndicator().get(vmid)
					+ " " + getIoLoadIndicator().get(vmid) + " " + getBwLoadIndicator().get(vmid));
		}
	}

	public void printVmCpuLoad() {
		for (Integer vmid : getCpuLoadIndicator().keySet()) {
			Log.printLine("Vm#" + vmid + " " + getCpuLoadIndicator().get(vmid));
		}
	}

	public void printRamCpuLoad() {
		for (Integer vmid : getRamLoadIndicator().keySet()) {
			Log.printLine("Vm#" + vmid + " " + getRamLoadIndicator().get(vmid));
		}
	}

	public void printIoCpuLoad() {
		for (Integer vmid : getIoLoadIndicator().keySet()) {
			Log.printLine("Vm#" + vmid + " " + getIoLoadIndicator().get(vmid));
		}
	}

	public void printBwCpuLoad() {
		for (Integer vmid : getBwLoadIndicator().keySet()) {
			Log.printLine("Vm#" + vmid + " " + getBwLoadIndicator().get(vmid));
		}
	}

	private float getCpuLoadForVm(Vm vm) { // 获取Vm的CPU负载
		if (getCpuLoadIndicator().containsKey(vm.getId()))
			return getCpuLoadIndicator().get(vm.getId());
		else
			return (float) 0.0;
	}

	private float getRamLoadForVm(Vm vm) { // 获取Vm的内存负载
		if (getRamLoadIndicator().containsKey(vm.getId()))
			return getRamLoadIndicator().get(vm.getId());
		else
			return (float) 0.0;
	}

	private float getIoLoadForVm(Vm vm) { // 获取Vm的IO负载
		if (getIoLoadIndicator().containsKey(vm.getId()))
			return getIoLoadIndicator().get(vm.getId());
		else
			return (float) 0.0;
	}

	private float getBwLoadForVm(Vm vm) { // 获取Vm的带宽负载
		if (getBwLoadIndicator().containsKey(vm.getId()))
			return getBwLoadIndicator().get(vm.getId());
		else
			return (float) 0.0;
	}

	public Map<Integer, Float> getCpuLoadIndicator() {
		return cpuLoadIndicator;
	}

	public void setCpuLoadIndicator(Map<Integer, Float> cpuLoadIndicator) {
		this.cpuLoadIndicator = cpuLoadIndicator;
	}

	public Map<Integer, Float> getRamLoadIndicator() {
		return ramLoadIndicator;
	}

	public void setRamLoadIndicator(Map<Integer, Float> ramLoadIndicator) {
		this.ramLoadIndicator = ramLoadIndicator;
	}

	public Map<Integer, Float> getIoLoadIndicator() {
		return ioLoadIndicator;
	}

	public void setIoLoadIndicator(Map<Integer, Float> ioLoadIndicator) {
		this.ioLoadIndicator = ioLoadIndicator;
	}

	public Map<Integer, Float> getBwLoadIndicator() {
		return bwLoadIndicator;
	}

	public void setBwLoadIndicator(Map<Integer, Float> bwLoadIndicator) {
		this.bwLoadIndicator = bwLoadIndicator;
	}

	public List<Vm> getVmList() {
		return vmList;
	}

	public void setVmList(List<Vm> vmlist) {
		this.vmList = vmlist;
	}

	public Map<Integer, Double> getTimeLoadIndicator() {
		return timeLoadIndicator;
	}

	public void setTimeLoadIndicator(Map<Integer, Double> timeLoadIndicator) {
		this.timeLoadIndicator = timeLoadIndicator;
	}

}
