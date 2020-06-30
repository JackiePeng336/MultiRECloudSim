package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.SimPowerHost;
import org.cloudbus.cloudsim.power.SimPowerVm;

public class CloudletAssignmentPolicyTimeEnergyBalance extends CloudletAssignmentPolicy {

	private Map<Integer, Double> cpuLoadIndicator; // 任务-CPU负载指标
	private Map<Integer, Double> ramLoadIndicator; // 任务-内存负载指标
	private Map<Integer, Double> ioLoadIndicator; // 任务-IO负载指标
	private Map<Integer, Double> bwLoadIndicator; // 任务-带宽负载指标
	private Map<Integer, Double> timeLoadIndicator;

	private final static int NORMAL_LENGTH = 1000; // 任务长度参考标准值
	private final static int NORMAL_MIPS = 3067; // 任务MIPS参考标准值
	private final static int NORMAL_RAM = 1024; // 任务内存参考标准值
	private final static int NORAML_IO = 83; // 任务IO参考标准值
	private final static int NORMAL_BW = 10000; // 任务带宽参考标准值
	private final static int NORMAL_TIME = 90; // 任务时间参考标准值

	private final static double α = 0.4;//概率参数

	private final static int CPU = 0; // CPU密集型
	private final static int RAM = 1; // 内存密集型
	private final static int IO = 2; // IO密集型
	private final static int BW = 3; // 带宽密集型
	private final static int TIME = 4; // 时间密集型

	private final static double TIME_WEIGHT = 0.7;
	private final static double CPU_WEIGHT = 0.3;
	private final static double RAM_WEIGHT = 0.1;
	private final static double IO_WEIGHT = 0.1;
	private final static double BW_WEIGHT = 0.0;

	private final static double REFERENCE_TIME = 30;//排序时间参考值
	private final static double REFERENCE_CPU = 900;//排序CPU参考值
	private final static double REFERENCE_RAM = 768;
	private final static double REFERENCE_IO = 30;
	private final static double REFERENCE_BW = 1000;

	private List<Vm> vmList; // 待接收任务的虚拟机列表
	private static Map<Integer, Integer> hostCpuLoadLine = new HashMap<Integer, Integer>();
	private static Map<Integer, Integer> hostRamLoadLine = new HashMap<Integer, Integer>();
	private static Map<Integer, Long> hostIoLoadLine = new HashMap<Integer, Long>();
	private static Map<Integer, Long> hostBwLoadLine = new HashMap<Integer, Long>();

	private static Map<Integer, Integer> vmCpuLoadLine = new HashMap<Integer, Integer>();
	private static Map<Integer, Integer> vmRamLoadLine = new HashMap<Integer, Integer>();
	private static Map<Integer, Long> vmIoLoadLine = new HashMap<Integer, Long>();
	private static Map<Integer, Long> vmBwLoadLine = new HashMap<Integer, Long>();



	public CloudletAssignmentPolicyTimeEnergyBalance(List<? extends SimPowerHost> hostList) { // 均衡任务分配器
		setTimeLoadIndicator(new HashMap<Integer, Double>());
		setCpuLoadIndicator(new HashMap<Integer, Double>());
		setRamLoadIndicator(new HashMap<Integer, Double>());
		setIoLoadIndicator(new HashMap<Integer, Double>());
		setBwLoadIndicator(new HashMap<Integer, Double>());
		setVmList(new ArrayList<Vm>());
		for (SimPowerHost host : hostList) {
			//timeLoadIndicator.put(host.getId(), (double) host.getTotalMips());

		}
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
		// double priority =
		// (double)c.getCloudletLength()/(double)c.getMaxMips();
		return priority;
	}

	@Override
	public int[] assignCloudletsToVm(List<SimCloudlet> cloudletlist, List<Vm> vmlist) {
		addNewVmList(vmlist);//初始化资源负载
		int n = cloudletlist.size();
		int[] result = new int[n];
		for (Vm vm : vmlist) {
			timeLoadIndicator.put(vm.getId(), 0.0);
		}//初始化时间负载
		List<SimCloudlet> scList = new ArrayList<SimCloudlet>();
		Map<Integer, Integer> assign = new HashMap<Integer, Integer>();
		Collections.sort(cloudletlist, new Comparator<SimCloudlet>() {//根据权重对云任务进行排序
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
		int j =0;
		for (int i = 0; i < n; i++) {
			if (cloudletlist.get(i).getVmId() != -1)
				assign.put(cloudletlist.get(i).getCloudletId(), cloudletlist.get(i).getVmId());
			else {
				int algorithmSelection = random(0, 99);//选取一个随机数
				int vmid = 0;
				//System.out.println("algorithmSelection "+ algorithmSelection);
				if (algorithmSelection < α * 100) {
					int type = getCloudletintensiveType(cloudletlist.get(i));
					vmid = findMinLoadVmByType(type);//根据任务的主资源选择合适的虚拟机
					j++;
				}else{
					vmid = findMinTimeLoadVm();//根据时间负载选择合适的虚拟机
				}
				assign.put(cloudletlist.get(i).getCloudletId(), vmid);
				// Log.printLine("findMinLoadVmByType Type " + type + "
				// result[i]" + result[i]);
				// printVmAllLoad();
				addLoadForVm(VmList.getById(vmlist, vmid), cloudletlist.get(i));
			}
		}
		//System.out.println("j= " +j+ " n-j= " + (n-j));
		for (int i = 0; i < n; i++) {
			result[i] = assign.get(cloudletlist.get(i).getCloudletId());
		}
		return result;
	}

	private int random(int min, int max) {
		Random random = new Random();
		return random.nextInt(max) % (max - min + 1) + min;
	}

	/*
	 * @Override public int[] assignCloudletsToVm(List<SimCloudlet>
	 * cloudletlist, List<Vm> vmlist) { addNewVmList(vmlist); int n =
	 * cloudletlist.size(); int[] result = new int[n]; List<SimCloudlet> scList
	 * = new ArrayList<SimCloudlet>(); scList.addAll(cloudletlist); Map<Integer,
	 * Integer> assign = new HashMap<Integer, Integer>();
	 * Collections.sort(scList, new Comparator<SimCloudlet>() { public int
	 * compare(SimCloudlet sc0, SimCloudlet sc1) { double priority0 =
	 * getCloudletPriroity(sc0); double priority1 = getCloudletPriroity(sc1); if
	 * (priority0 < priority1) return 1; else if (priority0 == priority1) {
	 * return 0; } else return -1; } });
	 *
	 * for (int i = 0; i < n; i++) { if (cloudletlist.get(i).getVmId() != -1)
	 * assign.put(scList.get(i).getCloudletId(), scList.get(i).getVmId()); else
	 * { int type = getCloudletIntensiveType(scList.get(i)); int vmid =
	 * findMinLoadVmByType(type); assign.put(scList.get(i).getCloudletId(),
	 * vmid); // Log.printLine("findMinLoadVmByType Type " + type + " //
	 * result[i]" + result[i]); // printVmAllLoad();
	 * addLoadForVm(VmList.getById(vmlist, vmid), scList.get(i)); } } for (int i
	 * = 0; i < n; i++) { result[i] = assign.get(scList.get(i).getCloudletId());
	 * } return result; }
	 */

	public static int getCloudletintensiveType(SimCloudlet cloudlet) { // 判断任务是哪种密集型
		double[] type = new double[5];
		type[CPU] = (double) cloudlet.getAverageMips() / (double) NORMAL_MIPS;
		type[RAM] = (double) cloudlet.getRam() / (double) NORMAL_RAM;
		type[IO] = (double) cloudlet.getIo() / (double) NORAML_IO;
		type[BW] = (double) cloudlet.getBw() / (double) NORMAL_BW;
		//type[TIME] = (double) cloudlet.getCloudletLength() / (double) cloudlet.getAverageMips() / (double) NORMAL_TIME;
		//System.out.println("getCloudletIntensiveType " + type[CPU] + " " + type[RAM] + " " + type[IO] + " " + type[BW]
		//		+ " " + type[TIME]);
		double max = 0.0;
		int maxType = 0;
		for (int i = 0; i < 5; i++) {
			if (type[i] > max) {
				max = type[i];
				maxType = i;
			}
		}
		return maxType;
	}

	private int findMinTimeLoadVm() { // 找时间负载最小的虚拟机

		double minLoad = Double.MAX_VALUE;
		int id = 0;
		for (Integer vmid : timeLoadIndicator.keySet()) {
			if (timeLoadIndicator.get(vmid) < minLoad) {
				minLoad = timeLoadIndicator.get(vmid);
				id = vmid;
			}
		}
		// System.out.println("findMinTimeLoadVm " + minLoad + " id " + id);
		return id;
	}

	private int findMinLoadVmByType(int type) { // 根据密集型找该负载最小的虚拟机
		Map<Integer, Double> loadIndicator = null;
		switch (type) {
		case CPU:
			loadIndicator = getCpuLoadIndicator();
			break;
		case RAM:
			loadIndicator = getRamLoadIndicator();
			break;
		case IO:
			loadIndicator = getIoLoadIndicator();
			break;
		case BW:
			loadIndicator = getBwLoadIndicator();
			break;
		//case TIME:
		//	loadIndicator = getTimeLoadIndicator();
		//	break;
		}

		double minLoad = Double.MAX_VALUE;
		int id = 0;
		for (Integer vmid : loadIndicator.keySet()) {
			if (getVmLoadByIndicator(loadIndicator, vmid) < minLoad) {
				minLoad = getVmLoadByIndicator(loadIndicator, vmid);
				id = vmid;
			}
		}
		return id;
	}

	private void initLoadIndicator(List<Vm> vmlist) { // 初始化虚拟机负载指标
		if (vmlist == null)
			return;
		for (Vm vm : vmlist) {

			getCpuLoadIndicator().put(vm.getId(), 0.0);
			getRamLoadIndicator().put(vm.getId(), 0.0);
			getIoLoadIndicator().put(vm.getId(), 0.0);
			getBwLoadIndicator().put(vm.getId(), 0.0);

			vmCpuLoadLine.put(vm.getId(), (int) (vm.getMips() * vm.getNumberOfPes()));
			vmRamLoadLine.put(vm.getId(), vm.getRam());
			vmIoLoadLine.put(vm.getId(), ((SimPowerVm)vm).getIo());
			vmBwLoadLine.put(vm.getId(), vm.getBw());

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

	private double getVmLoadByIndicator(Map<Integer, Double> indicator, int vmid) { // 根据指标和虚拟机Id返回负载
		if (indicator.containsKey(vmid))
			return indicator.get(vmid);
		else
			return 0.0;
	}

	/*
	 * private void addLoadForVm(Vm vm, SimCloudlet cloudlet) { // 根据任务给虚拟机添加负载
	 * double cpuLoad = getCpuLoadForVm(vm); double ramLoad =
	 * getRamLoadForVm(vm); double ioLoad = getIoLoadForVm(vm); double bwLoad =
	 * getBwLoadForVm(vm); double weight = (double)
	 * (cloudlet.getCloudletLength()/cloudlet.getAverageMips());// /
	 * cloudlet.getAverageMips());//cloudlet.getAverageMips());//NORMAL_LENGTH;
	 * // 长度是权重
	 *
	 * getCpuLoadIndicator().put( vm.getId(), (double) (cpuLoad + weight *
	 * cloudlet.getAverageMips() / vm.getMips()));
	 *
	 * getRamLoadIndicator().put(vm.getId(), (double) (ramLoad + weight *
	 * cloudlet.getRam() / vm.getRam()));
	 *
	 * getIoLoadIndicator().put(vm.getId(), (double) (ioLoad + weight *
	 * cloudlet.getIo() / ((SimPowerVm)vm).getIo()));
	 *
	 * getBwLoadIndicator().put(vm.getId(), (double) (bwLoad + weight *
	 * cloudlet.getBw() / vm.getBw())); }
	 */
	/*private void addLoadForHost(int hostId, SimCloudlet cloudlet) {
		double timeLoad = getTimeLoadForHost(hostId);
		double cpuLoad = getCpuLoadForHost(hostId);
		double ramLoad = getRamLoadForHost(hostId);
		double ioLoad = getIoLoadForHost(hostId);
		double bwLoad = getBwLoadForHost(hostId);
		double weight = (double) (cloudlet.getCloudletLength() / cloudlet.getAverageMips());//
		double newtimeLoad = (double) cloudlet.getCloudletLength() / (double) cloudlet.getMaxMips()
				/ getHostCpuLoadLineById(hostId) * 1000;
		timeLoadIndicator.put(hostId, timeLoad + weight * newtimeLoad);
		cpuLoadIndicator.put(hostId,
				(double) (cpuLoad + weight * cloudlet.getAverageMips() / getHostCpuLoadLineById(hostId)));

		ramLoadIndicator.put(hostId, (double) (ramLoad + weight * cloudlet.getRam() / getHostRamLoadLineById(hostId)));

		ioLoadIndicator.put(hostId, (double) (ioLoad + weight * cloudlet.getIo() / getHostIoLoadLineById(hostId)));

		bwLoadIndicator.put(hostId, (double) (bwLoad + weight * cloudlet.getBw() / getHostBwLoadLineById(hostId)));

	}*/

	private void addLoadForVm(Vm vm, SimCloudlet cloudlet) { // 根据任务给虚拟机添加负载
		//double timeLoad = getTimeLoadForVm(vm);
		double cpuLoad = getCpuLoadForVm(vm);
		double ramLoad = getRamLoadForVm(vm);
		double ioLoad = getIoLoadForVm(vm);
		double bwLoad = getBwLoadForVm(vm);
		double weight = (double) (cloudlet.getCloudletLength() / cloudlet.getAverageMips());// /
																							// cloudlet.getAverageMips());//cloudlet.getAverageMips());//NORMAL_LENGTH;
																							// //
																							// 长度是权重
		//double newtimeLoad = (double) cloudlet.getCloudletLength() / (double) cloudlet.getMaxMips()
		//		/ vm.getMips() * 1000;
		//timeLoadIndicator.put(vm.getId(), timeLoad + weight * newtimeLoad);
		double timeLoad = (double) cloudlet.getCloudletLength() / (double) cloudlet.getMaxMips()
				/ vm.getMips()*1000;
		timeLoadIndicator.put(vm.getId(), timeLoadIndicator.get(vm.getId()) + timeLoad);


		cpuLoadIndicator.put(vm.getId(),
				(double) (cpuLoad + weight * cloudlet.getAverageMips() / getVmCpuLoadLineById(vm.getId())));

		ramLoadIndicator.put(vm.getId(),
				(double) (ramLoad + weight * cloudlet.getRam() / getVmRamLoadLineById(vm.getId())));

		ioLoadIndicator.put(vm.getId(),
				(double) (ioLoad + weight * cloudlet.getIo() / getVmIoLoadLineById(vm.getId())));

		bwLoadIndicator.put(vm.getId(),
				(double) (bwLoad + weight * cloudlet.getBw() / getVmBwLoadLineById(vm.getId())));
	}

	public void removeLoadForVm(Vm vm, SimCloudlet cloudlet) {// 根据任务给虚拟机减少负载
		double cpuLoad = getCpuLoadForVm(vm);
		double ramLoad = getRamLoadForVm(vm);
		double ioLoad = getIoLoadForVm(vm);
		double bwLoad = getBwLoadForVm(vm);
		double weight = (double) (cloudlet.getCloudletLength() / cloudlet.getAverageMips());// cloudlet.getAverageMips());
																							// //
																							// NORMAL_LENGTH;
		getCpuLoadIndicator().put(vm.getId(),
				(double) (cpuLoad - weight * cloudlet.getAverageMips() / getVmCpuLoadLineById(vm.getHost().getId())));
		getRamLoadIndicator().put(vm.getId(),
				(double) (ramLoad - weight * cloudlet.getRam() / getVmRamLoadLineById(vm.getHost().getId())));
		getIoLoadIndicator().put(vm.getId(),
				(double) (ioLoad - weight * cloudlet.getIo() / getVmIoLoadLineById(vm.getHost().getId())));
		getBwLoadIndicator().put(vm.getId(),
				(double) (bwLoad - weight * cloudlet.getBw() / getVmBwLoadLineById(vm.getHost().getId())));
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

	private double getTimeLoadForHost(int hostId) {
		if (getTimeLoadIndicator().containsKey(hostId))
			return getTimeLoadIndicator().get(hostId);
		else
			return 0.0;
	}

	private double getCpuLoadForHost(int hostId) { // 获取Vm的CPU负载
		if (getCpuLoadIndicator().containsKey(hostId))
			return getCpuLoadIndicator().get(hostId);
		else
			return (double) 0.0;
	}

	private double getRamLoadForHost(int hostId) { // 获取Vm的内存负载
		if (getRamLoadIndicator().containsKey(hostId))
			return getRamLoadIndicator().get(hostId);
		else
			return (double) 0.0;
	}

	private double getIoLoadForHost(int hostId) { // 获取Vm的IO负载
		if (getIoLoadIndicator().containsKey(hostId))
			return getIoLoadIndicator().get(hostId);
		else
			return (double) 0.0;
	}

	private double getBwLoadForHost(int hostId) { // 获取Vm的带宽负载
		if (getBwLoadIndicator().containsKey(hostId))
			return getBwLoadIndicator().get(hostId);
		else
			return (double) 0.0;
	}

	private double getTimeLoadForVm(Vm vm) {
		if (getTimeLoadIndicator().containsKey(vm.getId()))
			return getTimeLoadIndicator().get(vm.getId());
		else
			return 0.0;
	}

	private double getCpuLoadForVm(Vm vm) { // 获取Vm的CPU负载
		if (getCpuLoadIndicator().containsKey(vm.getId()))
			return getCpuLoadIndicator().get(vm.getId());
		else
			return (double) 0.0;
	}

	private double getRamLoadForVm(Vm vm) { // 获取Vm的内存负载
		if (getRamLoadIndicator().containsKey(vm.getId()))
			return getRamLoadIndicator().get(vm.getId());
		else
			return (double) 0.0;
	}

	private double getIoLoadForVm(Vm vm) { // 获取Vm的IO负载
		if (getIoLoadIndicator().containsKey(vm.getId()))
			return getIoLoadIndicator().get(vm.getId());
		else
			return (double) 0.0;
	}

	private double getBwLoadForVm(Vm vm) { // 获取Vm的带宽负载
		if (getBwLoadIndicator().containsKey(vm.getId()))
			return getBwLoadIndicator().get(vm.getId());
		else
			return 0.0;
	}

	private Integer getVmCpuLoadLineById(int id) {
		return vmCpuLoadLine.get(id);
	}

	private Integer getVmRamLoadLineById(int id) {
		return vmRamLoadLine.get(id);
	}

	private Long getVmIoLoadLineById(int id) {
		return vmIoLoadLine.get(id);
	}

	private Long getVmBwLoadLineById(int id) {
		return vmBwLoadLine.get(id);
	}

	public Map<Integer, Double> getCpuLoadIndicator() {
		return cpuLoadIndicator;
	}

	public void setCpuLoadIndicator(Map<Integer, Double> cpuLoadIndicator) {
		this.cpuLoadIndicator = cpuLoadIndicator;
	}

	public Map<Integer, Double> getRamLoadIndicator() {
		return ramLoadIndicator;
	}

	public void setRamLoadIndicator(Map<Integer, Double> ramLoadIndicator) {
		this.ramLoadIndicator = ramLoadIndicator;
	}

	public Map<Integer, Double> getIoLoadIndicator() {
		return ioLoadIndicator;
	}

	public void setIoLoadIndicator(Map<Integer, Double> ioLoadIndicator) {
		this.ioLoadIndicator = ioLoadIndicator;
	}

	public Map<Integer, Double> getBwLoadIndicator() {
		return bwLoadIndicator;
	}

	public void setBwLoadIndicator(Map<Integer, Double> bwLoadIndicator) {
		this.bwLoadIndicator = bwLoadIndicator;
	}

	public Map<Integer, Double> getTimeLoadIndicator() {
		return timeLoadIndicator;
	}

	public void setTimeLoadIndicator(Map<Integer, Double> timeLoadIndicator) {
		this.timeLoadIndicator = timeLoadIndicator;
	}

	public List<Vm> getVmList() {
		return vmList;
	}

	public void setVmList(List<Vm> vmlist) {
		this.vmList = vmlist;
	}

}
