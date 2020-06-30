package org.cloudbus.cloudsim.main;

import java.io.*;
import java.io.File;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.*;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.allocator.*;
import org.cloudbus.cloudsim.chart.BarChart;
import org.cloudbus.cloudsim.chart.LineChart;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.*;
import org.cloudbus.cloudsim.power.model.PowerModelIoSimple;
import org.cloudbus.cloudsim.power.model.PowerModelRamSimple;
import org.cloudbus.cloudsim.power.model.PowerModelSpecPowerDellPowerEdgeC6320XeonE52699;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelCubic;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerIbmX3550XeonX5675;
import org.cloudbus.cloudsim.provisioners.SimIoProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.SimBwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.SimRamProvisionerSimple;
import org.cloudbus.cloudsim.util.Constants;
import org.cloudbus.cloudsim.util.MathUtil;


/**
 * The Class Helper.
 * 
 * If you are using any algorithms, policies or workload included in the power
 * package, please cite the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic
 * Algorithms and Adaptive Heuristics for Energy and Performance Efficient
 * Dynamic Consolidation of Virtual Machines in Cloud Data Centers", Concurrency
 * and Computation: Practice and Experience (CCPE), Volume 24, Issue 13, Pages:
 * 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 */

public class Helper {

	public static final List<Double> simTime = new ArrayList<Double>();
	public static final List<Double> simPower = new ArrayList<Double>();
	public static final List<Double> aveWait = new ArrayList<Double>();
	public static final List<Double> aveRun = new ArrayList<Double>();
	public static final List<Double> simSlaPerHost = new ArrayList<Double>();
	public static final List<Double> simOverallSla = new ArrayList<Double>();
	public static final List<Double> simAverageSla = new ArrayList<Double>();
	public static final List<Double> hostTime = new ArrayList<Double>();
	public static final List<Double> hostUtilizationTime = new ArrayList<Double>();
	public static final List<List<Double>> hostCpuUtilization = new ArrayList<List<Double>>();
	public static final List<List<Double>> hostRamUtilization = new ArrayList<List<Double>>();
	public static final List<List<Double>> hostIoUtilization = new ArrayList<List<Double>>();
	public static final List<List<Double>> hostBwUtilization = new ArrayList<List<Double>>();
	public static final List<Double> power = new ArrayList<Double>();
	public static final List<List<Double>> hostCpuPower = new ArrayList<List<Double>>();
	public static final List<List<Double>> hostRamPower = new ArrayList<List<Double>>();
	public static final List<List<Double>> hostIoPower = new ArrayList<List<Double>>();
	public static final List<List<Double>> hostBwPower = new ArrayList<List<Double>>();
	public static final List<Integer> createdVms = new ArrayList<Integer>();
	public static final Map<Integer, Double> vmFininshedTime = new HashMap<Integer, Double>();
	public static final Map<Integer, Integer> vmToHost = new HashMap<Integer, Integer>();

	public static final List<List<Double>> hostToSlaComp = new ArrayList<>();
	public static final List<Double> engList = new ArrayList<>();
	public static final List<Map<Integer, Double>> ratioList = new ArrayList<>();
	public static final List<Double> finishTimeList = new ArrayList<>();
	public static final List<Double> migCntList = new ArrayList<>();
	public static final List<List<Double>> compCloudletToFinishTimeList = new ArrayList<>();
	public static final List<List<Double>> compIoTCloudletToFinishTimeList = new ArrayList<>();



	/**
	 *
	 * 此刻VM对应的各种资源利用情况
	 * 统计该批任务运行结束后，对应vm的平均数、众数、最大和最小利用率。
	 * 为了区分不同资源，最外层string表示：CPU、ram、io
	 * 为了区分不同统计数据，最内层string表示：avg，mode，max，min
	 *
	 *
	 * */
	public static final Map<Integer, Map<String, Map<String, Double>>> vmIdToUtilizationMap = new HashMap<>();

	//每个任务的等待时间
	public static final Map<Integer, Double> cloudletToCorreWaitingTimeMap = new HashMap<>();

	//每个任务的完成时间
	public static final Map<Integer, Double> cloudletToCorreFinishTimeMap = new HashMap<>();

	public static final Map<Integer, Double> hostToSla = new HashMap<>();

	//所有VM的整个运行过程中对应的MIPS利用率
	public static final Map<Integer, List<Double>> vmsToMipsUtilizationMap = new HashMap<>();


	//所有VM的整个运行过程中对应的ram利用率
	public static final Map<Integer, List<Double>> vmsToRamUtilizationMap = new HashMap<>();


	//所有VM的整个运行过程中对应的IO利用率
	public static final Map<Integer, List<Double>> vmsToIoUtilizationMap = new HashMap<>();

	//记录不同算法的负载平衡程度
	public static final List<Double> compLoadBalanceDegreeInMips = new ArrayList<>();
	public static final List<Double> compLoadBalanceDegreeInRam = new ArrayList<>();
	public static final List<Double> compLoadBalanceDegreeInIo = new ArrayList<>();
	//public static final List<Double> compLoadBalanceDegreeInMips = new ArrayList<>();

	public static int GlobalC = 0;

	//"D:/File/IDEA/MultiRECloudSim3.0";
	//输出实验结果
	public static final String OUTPUT = "/实验结果";
	//utilization的路径
	public static String projectPath;

	private static String csvPath;

	private static int vmPrintCnt = 20;

	static {
		try {
			projectPath = Helper.class.getClassLoader().getResource("utilization").toURI().getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void init(int hostNumber) {
		List<Double> cpuUtilization = null;/**/
		List<Double> ramUtilization = null;
		List<Double> ioUtilization = null;
		List<Double> bwUtilization = null;
		for (int i = 0; i < hostNumber; i++) {
			cpuUtilization = new ArrayList<Double>();
			ramUtilization = new ArrayList<Double>();
			ioUtilization = new ArrayList<Double>();
			bwUtilization = new ArrayList<Double>();
			hostCpuUtilization.add(cpuUtilization);
			hostRamUtilization.add(ramUtilization);
			hostIoUtilization.add(ioUtilization);
			hostBwUtilization.add(bwUtilization);
			hostCpuPower.add(new ArrayList<Double>());
			hostRamPower.add(new ArrayList<Double>());
			hostIoPower.add(new ArrayList<Double>());
			hostBwPower.add(new ArrayList<Double>());
		}
	}




	public static SimPowerDatacenter createDatacenter(String name, double schedulingInterval, int hostNumber,
			int vmNumberPerHost, int type, String scheduler) {

		List<SimPowerHost> hostList = new ArrayList<SimPowerHost>();
		int mips = 3067;
		int ram = 16 * 1024;
		PowerModel powerModelCpu = new PowerModelSpecPowerIbmX3550XeonX5675();
		if (type == 1) {
			mips = 2300;
			ram = 256 * 1024; // host memory (MB)
			powerModelCpu = new PowerModelSpecPowerDellPowerEdgeC6320XeonE52699();
		}
		long storage = 1000000; // host storage
		long bw = (vmNumberPerHost) * 10000;
		long io = 500;

		double staticPowerPercent = 0.01;

		PowerModel powerModelRam = new PowerModelRamSimple(ram);
		PowerModel powerModelIo = new PowerModelIoSimple(io);
		PowerModel powerModelBw = new PowerModelCubic(100, staticPowerPercent);
		for (int i = 0; i < hostNumber; i++) { // 鏍规嵁涓绘満鏁扮洰鐢熸垚涓绘満
			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < vmNumberPerHost; j++) { // 鏍规嵁铏氭嫙鏈烘暟鐩敓鎴愭牳鑺�
				peList.add(new Pe(j, new PeProvisionerSimple(mips)));
			}
			if (!"time".equals(scheduler)) {
				// Log.printLine("SImOVer");
				hostList.add(new SimPowerHost(i, new SimRamProvisionerSimple(ram), new SimBwProvisionerSimple(bw),
						new SimIoProvisionerSimple(io), storage, peList,
						new VmSchedulerTimeSharedOverSubscription(peList), powerModelCpu, powerModelRam, powerModelIo,
						powerModelBw));
			} else {
				// Log.printLine("TIME");
				hostList.add(new SimPowerHost(i, new SimRamProvisionerSimple(ram), new SimBwProvisionerSimple(bw),
						new SimIoProvisionerSimple(io), storage, peList, new VmSchedulerTimeShared(peList),
						powerModelCpu, powerModelRam, powerModelIo, powerModelBw));
			}
		}

		// This is our machine

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.02; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		SimPowerDatacenter datacenter = null;
		try {
			datacenter = new SimPowerDatacenter(name, characteristics, new PowerVmAllocationPolicySimple(hostList),
					storageList, schedulingInterval);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	public static List<SimPowerVm> createVMs(int userId, int hostNumber, int vmNumberPerHost, int mipsAllocator) {// 鍒涘缓铏氭嫙鏈�
		List<SimPowerVm> list = new ArrayList<SimPowerVm>();

		long size = 10000; // image size (MB)
		int ram = 2048;// 1024;// 2048; // vm memory (MB)
		int mips = 3067;
		if (vmNumberPerHost == 12) {
			ram = 1024;
			mips = 1533;
		}
		long bw = 10000;
		long io = (long) (500 / vmNumberPerHost);
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name
		SimPowerVm vm = null;

		CloudletCpuAllocator cpuAllocator = null;
		// CloudletCpuAllocator[] cpuAllocator = new CloudletCpuAllocator[2];
		// cpuAllocator[0] = new CloudletCpuAllocatorSimple(mips);
		// cpuAllocator[1] = new CloudletCpuAllocatorMaxMinFairness(mips);
		for (int i = 0; i < hostNumber * vmNumberPerHost; i++) {
			if (mipsAllocator == 1)
				cpuAllocator = new CloudletCpuAllocatorMaxMinFairness(mips);
			else
				cpuAllocator = new CloudletCpuAllocatorSimple(mips);
			vm = new SimPowerVm(i, userId, mips, pesNumber, ram, io, bw, size, 1, vmm,
					new SimProgressCloudletSchedulerDynamicWorkload(mips, // SimCloudletSchedulerDynamicWorkload
							pesNumber, cpuAllocator, new CloudletRamAllocatorSimple(ram), // CloudletRamAllocatorSimple
							new CloudletIoAllocatorSimple(io), // CloudletIoAllocatorSimple
							new CloudletBwAllocatorSimple(bw)),
					5); // CloudletBwAllocatorSimple
			list.add(vm);
		}

		return list;
	}

	public static List<SimProgressCloudlet> createCloudlets(int userId, int cloudlets, boolean loadDynamic,
			boolean loadAware) throws URISyntaxException, NumberFormatException, IOException { // 鐢熸垚浜戜换鍔″垪琛�
		SimProgressCloudlet[] list = new SimProgressCloudlet[cloudlets];
		SimProgressCloudlet[] randomList = new SimProgressCloudlet[cloudlets];
		String inputFolder = projectPath;//ProgressSimpleMain.class.getClassLoader().getResource("utilization").toURI().getPath();
		// Log.printLine("inputFolder: " + inputFolder);
		File[] files = new File(inputFolder).listFiles();
		String workloadPath = null;

		SimProgressCloudlet cloudlet = null;

		for (int i = 0; i < cloudlets; i++) {
			if (loadDynamic)
				workloadPath = files[i % 2].getPath();
			cloudlet = createIntensiveCloudlet(i, i % 3, workloadPath, loadAware);
			cloudlet.setUserId(userId);
			list[i] = cloudlet;
		}

		// for (int i = 0; i < cloudlets; i++) {
		// int seed = random(0, list.length - i);// 浠庡墿涓嬬殑闅忔満鏁伴噷鐢熸垚
		// randomList[i] = list[seed];// 璧嬪�肩粰缁撴灉鏁扮粍
		// list[seed] = list[list.length - i - 1];// 鎶婇殢鏈烘暟浜х敓杩囩殑浣嶇疆鏇挎崲涓烘湭琚�変腑鐨勫�笺��
		// }

		// for (int i = 0; i < randomList.length; i++) {
		// Log.printLine(i + " " + randomList[i].getCloudletId() + " Mips:"
		// + randomList[i].getMaxMips() + " Ram:"
		// + randomList[i].getRam() + " Io:" + randomList[i].getIo());
		// }
		/// System.exit(0);

		return Arrays.asList(list);
	}

	public static List<SimProgressCloudlet> createCloudlets(int userId, int cloudlets, boolean loadDynamic,
			boolean loadAware, boolean write) throws URISyntaxException, NumberFormatException, IOException { // 鐢熸垚浜戜换鍔″垪琛�
		SimProgressCloudlet[] list = new SimProgressCloudlet[cloudlets];
		SimProgressCloudlet[] randomList = new SimProgressCloudlet[cloudlets];
		String inputFolder = Helper.class.getClassLoader().getResource("utilization").toURI().getPath();
		// Log.printLine("inputFolder: " + inputFolder);
		File[] files = new File(inputFolder).listFiles();
		String workloadPath = null;

		SimProgressCloudlet cloudlet = null;

		for (int i = 0; i < cloudlets; i++) {
			if (loadDynamic)
				workloadPath = files[i % 2].getPath();
			cloudlet = createIntensiveCloudlet(i, i % 3, workloadPath, loadAware);
			cloudlet.setUserId(userId);
			list[i] = cloudlet;
		}

		// for (int i = 0; i < cloudlets; i++) {
		// int seed = random(0, list.length - i);// 浠庡墿涓嬬殑闅忔満鏁伴噷鐢熸垚
		// randomList[i] = list[seed];// 璧嬪�肩粰缁撴灉鏁扮粍
		// list[seed] = list[list.length - i - 1];// 鎶婇殢鏈烘暟浜х敓杩囩殑浣嶇疆鏇挎崲涓烘湭琚�変腑鐨勫�笺��
		// }

		// for (int i = 0; i < randomList.length; i++) {
		// Log.printLine(i + " " + randomList[i].getCloudletId() + " Mips:"
		// + randomList[i].getMaxMips() + " Ram:"
		// + randomList[i].getRam() + " Io:" + randomList[i].getIo());
		// }
		/// System.exit(0);
		if (write) {

			String filename = "Cloudlet_";
			String output = OUTPUT + filename + list.length + "_" + System.currentTimeMillis() % 30;
			Log.printLine("Write Cloudlets " + output);
			File file = new File(output);
			try {
				if (!file.exists())
					file.createNewFile();
				else
					file.delete();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
			DecimalFormat dft = new DecimalFormat("###.##");
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				for (int i = 0; i < list.length; i++) {
					writer.write(list[i].getCloudletLength() + "\t" + list[i].getMips() + "\t" + list[i].getRam() + "\t"
							+ list[i].getIo() + "\t" + list[i].getBw() + "\n");
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		return Arrays.asList(list);
	}

	public static List<SimProgressCloudlet> createCloudletsByData(int userId, int cloudlets, String filename)
			throws IOException {
		SimProgressCloudletIoT[] list = new SimProgressCloudletIoT[cloudlets];
		SimProgressCloudletIoT[] randomList = new SimProgressCloudletIoT[cloudlets];
		long bw = 0;
		long fileSize = 0;
		long outputSize = 0;
		int pesNumber = 1;
		String filepath = getProjectPath() ;
		UtilizationModel utilizationModelCpu = new UtilizationProgressModelByFile(filepath + "/myworkload", 5, true);
		UtilizationModel utilizationModelRam = new UtilizationProgressModelByFile(filepath + "/myworkload", 5, true);
		UtilizationModel utilizationModelIo = new UtilizationProgressModelByFile(filepath + "/myworkload", 5, true);
		UtilizationModel utilizationModelBw = new UtilizationProgressModelByFile(filepath + "/myworkload", 5, true);
		//String cloudletPath = OUTPUT + filename;
		// File file = new File(cloudletPath);
		SimProgressCloudletIoT cloudlet = null;

		//BufferedReader input = new BufferedReader(projectPath);

		//BufferedInputStream bufferedInputStream = new BufferedInputStream(projectPath);
		//BufferedReader input = new BufferedReader(bufferedInputStream);
		/**
		 *
		 *
		 * */
		int iotNum = 0;
		InputStream inputStream = Helper.class.getResourceAsStream("/LabelledOutput/" + filename);
		BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
		for (int i = 0; i < cloudlets; i++) {
			String line = input.readLine();
			//System.out.println(i + " " + line);
			if (line != null) {
				String[] data = line.split("\t");
				cloudlet = new SimProgressCloudletIoT(i, data[0], new Double(data[1]).longValue(), pesNumber, fileSize, outputSize,
						new Double(data[2]).longValue(), new Double(data[3]).intValue(), new Double(data[4]).longValue(),
						bw, utilizationModelCpu, utilizationModelRam, utilizationModelIo, utilizationModelBw);

                /*System.out.println("尝试输出cloudlet的情况：" + data[0] + " " + new Double(data[1]).longValue() + " "
                + new Double(data[2]).longValue() + " "+ new Double(data[3]).intValue()+ " " +
                        new Double(data[4]).longValue());*/

				cloudlet.setUserId(userId);
				list[i] = cloudlet;

				if(cloudlet.getLabel().equals("IoT")){
					iotNum ++;
				}
			}
		}
		/*int[] randomArray = randomArray(0,cloudlets-1,cloudlets);
		for(int i=0;i<cloudlets;i++){
			randomList[i] = list[randomArray[i]];
		}*/
		// for (int i = 0; i < cloudlets; i++) {
		// int seed = random(0, list.length - i);// 浠庡墿涓嬬殑闅忔満鏁伴噷鐢熸垚
		// randomList[i] = list[seed];// 璧嬪�肩粰缁撴灉鏁扮粍
		// list[seed] = list[list.length - i - 1];// 鎶婇殢鏈烘暟浜х敓杩囩殑浣嶇疆鏇挎崲涓烘湭琚�変腑鐨勫�笺��
		// }

		// for (int i = 0; i < randomList.length; i++) {
		// Log.printLine(i + " " + randomList[i].getCloudletId() + " Mips:" +
		// randomList[i].getMaxMips() + " Ram:"
		// + randomList[i].getRam() + " Io:" + randomList[i].getIo());
		// }

		return Arrays.asList(list);
	}

	public static int[] randomArray(int min, int max, int n) {
		int len = max - min + 1;
		if (max < min || n > len) {
			return null;
		}

		// 鍒濆鍖栫粰瀹氳寖鍥寸殑寰呴�夋暟缁�
		int[] source = new int[len];
		for (int i = min; i < min + len; i++) {
			source[i - min] = i;
		}
		if (len == 1)
			return source;
		int[] result = new int[n];
		Random rd = new Random();
		int index = 0;
		for (int i = 0; i < result.length; i++) {
			// 寰呴�夋暟缁�0鍒�(len-2)闅忔満涓�涓笅鏍�
			index = Math.abs(rd.nextInt() % len--);
			// 灏嗛殢鏈哄埌鐨勬暟鏀惧叆缁撴灉闆�
			result[i] = source[index];
			// 灏嗗緟閫夋暟缁勪腑琚殢鏈哄埌鐨勬暟锛岀敤寰呴�夋暟缁�(len-1)涓嬫爣瀵瑰簲鐨勬暟鏇挎崲
			source[index] = source[len];
		}
		return result;
	}
	
	/*
	 * public static void outputResult(String filename, List<Integer> cls) {
	 * String output = OUTPUT + filename; File file = new File(output); try { if
	 * (!file.exists()) file.createNewFile(); else file.delete(); } catch
	 * (IOException e1) { e1.printStackTrace(); System.exit(0); } DecimalFormat
	 * dft = new DecimalFormat("###.##");
	 * 
	 * try { BufferedWriter writer = new BufferedWriter(new FileWriter(file));
	 * for (int i = 0; i < simTime.size(); i++) { writer.write(cls.get(i) + " "
	 * + dft.format(simTime.get(i)) + "  " + dft.format(simPower.get(i)) + "  "
	 * + dft.format(simSlaPerHost.get(i)) + " " + "\n"); } writer.close(); }
	 * catch (IOException e) { e.printStackTrace(); System.exit(0); } }
	 */

	public static List<SimProgressCloudlet> createCloudletsByType(int userId, int cloudlets, final int type,
			boolean loadDynamic, boolean loadAware) throws URISyntaxException, NumberFormatException, IOException { // 鐢熸垚浜戜换鍔″垪琛�
		SimProgressCloudlet[] list = new SimProgressCloudlet[cloudlets];
		String inputFolder = Helper.class.getClassLoader().getResource("utilization").toURI().getPath();
		// Log.printLine("inputFolder: " + inputFolder);
		File[] files = new File(inputFolder).listFiles();
		String workloadPath = null;

		SimProgressCloudlet cloudlet = null;

		for (int i = 0; i < cloudlets; i++) {
			if (loadDynamic)
				workloadPath = files[i % 2].getPath();
			cloudlet = Helper.createIntensiveCloudlet(i, type, workloadPath, loadAware);
			cloudlet.setUserId(userId);
			list[i] = cloudlet;
		}

		return Arrays.asList(list);
	}

	public static SimProgressCloudlet createIntensiveCloudlet(int id, int type, String path, boolean loadAware)
			throws URISyntaxException, NumberFormatException, IOException {
		long length = 90000 / (id % 12 + 1);
		length += random(0, (int) length / 2) - length / 4;
		long fileSize = 0;
		long outputSize = 0;
		int pesNumber = 1;
		int mips = 200;
		int ram = 256;
		long io = 5;
		long bw = 0;
		double interval = 100;
		boolean aware = loadAware;

		final int CPU = 0;
		final int RAM = 1;
		final int IO = 2;
		final int BW = 3;
		switch (type) {
		case CPU:
			mips = 800 + random(0, 800);
			// System.out.print(r+" ");
			break;
		case RAM:
			mips = 100;
			mips += random(0, 700);
			ram = 384;
			ram += random(0, 512);
			break;
		case IO:
			mips = 100;
			mips += random(0, 700);
			io = 20;
			io += random(0, 40);
			break;
		case BW:
			break;
		}

		UtilizationModel utilizationModelCpu = null;
		if (path != null)
			utilizationModelCpu = new UtilizationProgressModelByFile(path, interval, aware);
		else
			utilizationModelCpu = new UtilizationModelFull();
		UtilizationModel utilizationModelRam = new UtilizationModelFull();
		UtilizationModel utilizationModelIo = new UtilizationModelFull();
		UtilizationModel utilizationModelBw = new UtilizationModelFull();
		return new SimProgressCloudlet(id, length, pesNumber, fileSize, outputSize, mips, ram, io, bw,
				utilizationModelCpu, utilizationModelRam, utilizationModelIo, utilizationModelBw);
	}
	
	public static SimDatacenterBroker createBroker(int cloudletAssignPolicy) {
		CloudletAssignmentPolicy[] policy = new CloudletAssignmentPolicy[5];
		policy[0] = new CloudletAssignmentPolicySimple();
		policy[1] = new CloudletAssignmentPolicyRandom();
		policy[2] = new CloudletAssignmentPolicyBalance();
		policy[3] = new CloudletAssignmentPolicyGreedyArea(0.8, 50, 5);
		policy[4] = new CloudletAssignmentPolicyTimeGreedy();

		SimDatacenterBroker broker = null;
		try {
			broker = new SimDatacenterBroker("Broker", policy[cloudletAssignPolicy]); // CloudletAssignmentPolicyBalance
			// Simple
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	

	public static SimDatacenterBroker createBroker(int cloudletAssignPolicy, List<? extends SimPowerHost> hostList) {
		CloudletAssignmentPolicy[] policy = new CloudletAssignmentPolicy[8];
		policy[0] = new CloudletAssignmentPolicySimple();
		policy[1] = new CloudletAssignmentPolicyRandom();
		policy[2] = new CloudletAssignmentPolicyBalance();
		policy[3] = new CloudletAssignmentPolicyGreedyArea(0.8, 50, 5);
		policy[4] = new CloudletAssignmentPolicyTimeGreedy();
		policy[5] = new CloudletAssignmentPolicyHeterogeneousBalance(hostList);
		policy[6] = new CloudletAssignmentPolicyTimeBalance();
		policy[7] = new CloudletAssignmentPolicyTimeEnergyBalance(hostList);
		SimDatacenterBroker broker = null;
		try {
			broker = new SimDatacenterBroker("Broker", policy[cloudletAssignPolicy]); // CloudletAssignmentPolicyBalance
			// Simple
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	public static SimDatacenterBroker createBrokerForAli() {
		SimDatacenterBroker broker = null;
		try {

			broker = new SimDatacenterBroker("Broker", new CloudletAssignmentPolicySimple()); // CloudletAssignmentPolicyBalance
			// Simple
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	public static SimDatacenterBroker createBrokerForAlphaTest(int cloudletAssignPolicy, List<? extends SimPowerHost> hostList, double alpha) {

		CloudletAssignmentPolicy[] policy = new CloudletAssignmentPolicy[8];
		policy[0] = new CloudletAssignmentPolicySimple();
		policy[1] = new CloudletAssignmentPolicyRandom();
		policy[2] = new CloudletAssignmentPolicyBalance();
		policy[3] = new CloudletAssignmentPolicyGreedyArea(0.8, 50, 5);
		policy[4] = new CloudletAssignmentPolicyTimeGreedy();
		policy[5] = new CloudletAssignmentPolicyHeterogeneousBalance(hostList);
		policy[6] = new CloudletAssignmentPolicyTimeBalance();
		policy[7] = new CloudletAssignmentPolicyTimeEnergyBalance(hostList);
		SimDatacenterBroker broker = null;
		try {
			cloudletAssignPolicy = 1;
			policy[cloudletAssignPolicy].setCPU_WEIGHT(1.0-alpha);
			policy[cloudletAssignPolicy].setTIME_WEIGHT(alpha);

			broker = new SimDatacenterBroker("Broker", policy[cloudletAssignPolicy]); // CloudletAssignmentPolicyBalance
			// Simple
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	
	public static SimDatacenterBroker createBroker(int cloudletAssignPolicy, double stopUtilization, int cpuInterval,
			double clockInterval) {
		CloudletAssignmentPolicy[] policy = new CloudletAssignmentPolicy[5];
		policy[0] = new CloudletAssignmentPolicySimple();
		policy[1] = new CloudletAssignmentPolicyRandom();
		policy[2] = new CloudletAssignmentPolicyBalance();
		policy[3] = new CloudletAssignmentPolicyGreedyArea(stopUtilization, cpuInterval, clockInterval);
		policy[4] = new CloudletAssignmentPolicyTimeGreedy();
		SimDatacenterBroker broker = null;
		try {
			broker = new SimDatacenterBroker("Broker", policy[cloudletAssignPolicy]); // CloudletAssignmentPolicyBalance
			// Simple
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	private static int random(int min, int max) {
		Random random = new Random();
		return random.nextInt(max) % (max - min + 1) + min;
	}

	public static double getAverageWaitingTime(List<? extends Cloudlet> list) {
		double ave = 0.0;
		for (Cloudlet cloudlet : list)
			ave += cloudlet.getWaitingTime();
		ave = ave / list.size();
		aveWait.add(ave);
		return ave;
	}

	public static double getAverageRunTime(List<? extends Cloudlet> list) {
		double ave = 0.0;
		for (Cloudlet cloudlet : list)
			ave += cloudlet.getActualCPUTime();
		ave = ave / list.size();
		aveRun.add(ave);
		return ave;
	}

	public static void printCloudletList(List<? extends Cloudlet> list, int numCloudlet) {
		int size = list.size();
		Collections.sort(list, new Comparator<Cloudlet>() {
			public int compare(Cloudlet cl0, Cloudlet cl1) {
				if (cl0.getVmId() > cl1.getVmId())
					return 1;
				else if (cl0.getVmId() == cl1.getVmId()) {
					return new Double(cl0.getExecStartTime()).compareTo(cl1.getExecStartTime());
				} else
					return -1;
			}
		});
		Cloudlet cloudlet = null;
		int[] missId = new int[numCloudlet];
		for (int i = 0; i < numCloudlet; i++)
			missId[i] = 1;
		String indent = "    ";
		Log.setDisabled(false);
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet" + indent + "STATUS" + indent + "Center" + indent + "Vm" + indent + "Time" + indent
				+ "WaitTime" + indent + "StartTime" + indent + "FinishTime" + indent + " Length" + indent + " Mips"
				+ indent + "Ram" + indent + "Io" + indent + "Intensive");
		long totalLength = 0;
		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			missId[cloudlet.getCloudletId()] = -1;
			totalLength += cloudlet.getCloudletLength();
			Log.print(String.format("%4d", cloudlet.getCloudletId()) + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + "   " + cloudlet.getResourceId() + indent + "  "
						+ String.format("%3d", cloudlet.getVmId())

						+ indent + String.format("%3s", dft.format(cloudlet.getActualCPUTime())) + indent
						+ String.format("%7s", dft.format(cloudlet.getWaitingTime())) + indent
						+ String.format("%7s", dft.format(cloudlet.getExecStartTime())) + indent
						+ String.format("%8s", dft.format(cloudlet.getFinishTime())) + indent + indent

						+ String.format("%5s", dft.format(cloudlet.getCloudletLength())) + indent
						+ String.format("%5d", (int) ((SimCloudlet) cloudlet).getMaxMips()) + indent
						+ String.format("%5d", ((SimCloudlet) cloudlet).getRam()) + indent
						+ ((SimCloudlet) cloudlet).getIo() + indent + String.valueOf(
								CloudletAssignmentPolicyBalance.getCloudletIntensiveType((SimCloudlet) cloudlet)));
			}
		}

		Log.printLine();
		Log.printLine("Total Received Cloudlets: " + list.size());
		Log.printLine("Total Cloudlets Length: " + totalLength);
		Log.print("Miss CLoudlet:");
		for (int i = 0; i < numCloudlet; i++) {
			if (missId[i] != -1)
				Log.print(i + " ");
		}
		Log.printLine();
		Log.printLine("Average Waiting Time: " + getAverageWaitingTime(list));
		Log.printLine("Average Run Time: " + getAverageRunTime(list));
		Log.printLine();
	}

	/*
	 * public static void printCloudletList(List<? extends Cloudlet> list, int
	 * numCloudlet) { int size = list.size(); Collections.sort(list, new
	 * Comparator<Cloudlet>() { public int compare(Cloudlet cl0, Cloudlet cl1) {
	 * if (cl0.getExecStartTime() > cl1.getExecStartTime()) return 1; else if
	 * (cl0.getExecStartTime() == cl1.getExecStartTime()) { return new
	 * Integer(cl0.getCloudletId()).compareTo(cl1.getCloudletId()); } else
	 * return -1; } }); Cloudlet cloudlet; int[] missId = new int[numCloudlet];
	 * for (int i = 0; i < numCloudlet; i++) missId[i] = 1; String indent =
	 * "    "; Log.setDisabled(false); Log.printLine(); Log.printLine(
	 * "========== OUTPUT =========="); Log.printLine("Cloudlet" + indent +
	 * "STATUS" + indent + "Center" + indent + "Vm" + indent + "Time" + indent +
	 * "WaitTime" + indent + "StartTime" + indent + "FinishTime" + indent +
	 * " Length" + indent + " Mips" + indent + "Ram" + indent + "Io" + indent +
	 * "Intensive"); long totalLength = 0; DecimalFormat dft = new
	 * DecimalFormat("###.##"); for (int i = 0; i < size; i++) { cloudlet =
	 * list.get(i); missId[cloudlet.getCloudletId()] = -1; totalLength +=
	 * cloudlet.getCloudletLength(); Log.print(String.format("%4d",
	 * cloudlet.getCloudletId()) + indent + indent);
	 * 
	 * if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
	 * Log.print("SUCCESS");
	 * 
	 * Log.printLine(indent + "   " + cloudlet.getResourceId() + indent + "  " +
	 * String.format("%3d", cloudlet.getVmId())
	 * 
	 * + indent + String.format("%3s", dft.format(cloudlet.getActualCPUTime()))
	 * + indent + String.format("%7s", dft.format(cloudlet.getWaitingTime())) +
	 * indent + String.format("%7s", dft.format(cloudlet.getExecStartTime())) +
	 * indent + String.format("%8s", dft.format(cloudlet.getFinishTime())) +
	 * indent + indent
	 * 
	 * + String.format("%5s", dft.format(cloudlet.getCloudletLength())) + indent
	 * + String.format("%5d", (int) ((SimCloudlet) cloudlet).getMaxMips()) +
	 * indent + String.format("%5d", ((SimCloudlet) cloudlet).getRam()) + indent
	 * + ((SimCloudlet) cloudlet).getIo() + indent + String.valueOf(
	 * CloudletAssignmentPolicyBalance.getCloudletIntensiveType((SimCloudlet)
	 * cloudlet))); } }
	 * 
	 * Log.printLine(); Log.printLine("Total Received Cloudlets: " +
	 * list.size()); Log.printLine("Total Cloudlets Length: " + totalLength);
	 * Log.print("Miss CLoudlet:"); for (int i = 0; i < numCloudlet; i++) { if
	 * (missId[i] != -1) Log.print(i + " "); } Log.printLine(); Log.printLine(
	 * "Average Waiting Time: " + getAverageWaitingTime(list)); Log.printLine(
	 * "Average Run Time: " + getAverageRunTime(list)); Log.printLine(); }
	 */

	public static List<Double> getTimesBeforeHostShutdown(List<Host> hosts) {
		List<Double> timeBeforeShutdown = new LinkedList<Double>();
		for (Host host : hosts) {
			boolean previousIsActive = true;
			double lastTimeSwitchedOn = 0;
			for (HostStateHistoryEntry entry : ((HostDynamicWorkload) host).getStateHistory()) {
				if (previousIsActive == true && entry.isActive() == false) {
					timeBeforeShutdown.add(entry.getTime() - lastTimeSwitchedOn);
				}
				if (previousIsActive == false && entry.isActive() == true) {
					lastTimeSwitchedOn = entry.getTime();
				}
				previousIsActive = entry.isActive();
			}
		}
		return timeBeforeShutdown;
	}

	/**
	 * Gets the times before vm migration.
	 * 
	 * @param vms
	 *            the vms
	 * @return the times before vm migration
	 */
	public static List<Double> getTimesBeforeVmMigration(List<SimPowerVm> vms) {
		List<Double> timeBeforeVmMigration = new LinkedList<Double>();
		for (Vm vm : vms) {
			boolean previousIsInMigration = false;
			double lastTimeMigrationFinished = 0;
			for (VmStateHistoryEntry entry : vm.getStateHistory()) {
				if (previousIsInMigration == true && entry.isInMigration() == false) {
					timeBeforeVmMigration.add(entry.getTime() - lastTimeMigrationFinished);
				}
				if (previousIsInMigration == false && entry.isInMigration() == true) {
					lastTimeMigrationFinished = entry.getTime();
				}
				previousIsInMigration = entry.isInMigration();
			}
		}
		return timeBeforeVmMigration;
	}

	public static void printResultsCmp(PowerDatacenter datacenter, List<SimPowerVm> vms, double lastClock,
									   String experimentName){
		Log.enable();
		List<Host> hosts = datacenter.getHostList();

		int numberOfHosts = hosts.size();
		int numberOfVms = vms.size();

		double totalSimulationTime = lastClock;
		double energy = datacenter.getPower() / (3600);

		Map<String, Double> slaMetrics = getSlaMetrics(vms);

		double slaOverall = slaMetrics.get("overall");
		double slaAverage = slaMetrics.get("average");
		// 总sla违反时间除以每个机器(SLATAH)
		double slaTimePerActiveHost = getSlaTimePerActiveHost(hosts);

		Log.setDisabled(false);
		Log.printLine();
		Log.printLine(String.format("Experiment name: " + experimentName));
		if(Constants.VM_ALLOCATION_POLICY == Constants.PFF){
			Log.printLine("Allocation Policy: " + "VM_ALLOCATION_POLICY_SIMPLE");
		}else if(Constants.VM_ALLOCATION_POLICY == Constants.SFF){
			Log.printLine("Allocation Policy: " + "POWER_VM_ALLOCATION_POLICY_SIMPLE");
		}else if(Constants.VM_ALLOCATION_POLICY == Constants.RMS){
			Log.printLine("Allocation Policy: " + "POWER_VM_ALLOCATION_POLICY_DT");
		}
		Log.printLine(String.format("Number of hosts: " + numberOfHosts));
		Log.printLine(String.format("Number of VMs: " + numberOfVms));
		Log.printLine(String.format("Total simulation time: %.2f sec", totalSimulationTime));
		Log.printLine(String.format("能耗: %.2f kWh", energy));

		Log.printLine(String.format("SLA time per active host: %.4f%%", slaTimePerActiveHost * 100));
		Log.printLine(String.format("Overall SLA violation: %.10f", slaOverall));
		Log.printLine(String.format("Average SLA violation: %.4f", slaAverage));
		Log.printLine(String.format("虚拟机迁移次数: %d", datacenter.getMigrationCount()));


		if (hosts.get(0) instanceof SimPowerHostMultiR) {
			Map<String, Double> fragMetrics = getFragMetrics(hosts);


			Log.printLine(String.format("avg1 Frag: %.4f", fragMetrics.get("avg")));
			Log.printLine(String.format("avg2 Frag: %.4f", fragMetrics.get("avg1")));
			Log.printLine(String.format("overall Frag: %.4f", fragMetrics.get("overall")));
		}


		if (Constants.VM_ALLOCATION_POLICY == Constants.RMS) {
			Log.printLine(String.format("r0: %f, under-thr: %f, safe_param: %f, look_forward: %d",
					Constants.r0, Constants.UNDER_UTILIZED_THR,
					Constants.SafetyParam, Constants.LOOK_FORWARD));
		}
		// Log.printLine(String.format("SLA time per host: %.2f%%",
		// slaTimePerHost * 100));
		simTime.add(totalSimulationTime);
		simPower.add(energy);
		simSlaPerHost.add(slaTimePerActiveHost);
		simOverallSla.add(slaOverall);
		simAverageSla.add(slaAverage);
		getFinishTimeList().add(totalSimulationTime);
		getEngList().add(energy);
	}
	/**
	 * Prints the results.
	 * 
	 * @param datacenter
	 *            the datacenter
	 * @param lastClock
	 *            the last clock
	 * @param experimentName
	 *            the experiment name
	 */
	public static void printResults(PowerDatacenter datacenter, List<? extends SimPowerVm> vms, double lastClock,
			String experimentName) {
		Log.enable();
		List<Host> hosts = new LinkedList<>();
		hosts = datacenter.getHostList();
		int numberOfHosts = hosts.size();
		int numberOfVms = vms.size();

		double totalSimulationTime = lastClock;
		double energy = datacenter.getPower() / (3600 * 1000);

		Map<String, Double> slaMetrics = getSlaMetrics(vms);

		double slaOverall = slaMetrics.get("overall");
		double slaAverage = slaMetrics.get("average");
		// 总sla违反时间除以每个机器(SLATAH)
		double slaTimePerActiveHost = getSlaTimePerActiveHost(hosts);

		Log.setDisabled(false);
		Log.printLine();
		Log.printLine(String.format("Experiment name: " + experimentName));
		if (experimentName.equals("Ali-Exp")) {
			if(Constants.VM_ALLOCATION_POLICY == Constants.PFF){
				Log.printLine("Allocation Policy: " + "VM_ALLOCATION_POLICY_SIMPLE");
			}else if(Constants.VM_ALLOCATION_POLICY == Constants.SFF){
				Log.printLine("Allocation Policy: " + "POWER_VM_ALLOCATION_POLICY_SIMPLE");
			}else if(Constants.VM_ALLOCATION_POLICY == Constants.RMS){
				Log.printLine("Allocation Policy: " + "POWER_VM_ALLOCATION_POLICY_DT_RMS");
			}else if(Constants.VM_ALLOCATION_POLICY == Constants.MMEA){
				Log.printLine("Allocation Policy: " + "POWER_VM_ALLOCATION_POLICY_DT_MMEA");
			}else if(Constants.VM_ALLOCATION_POLICY == Constants.MMEE){
				Log.printLine("Allocation Policy: " + "POWER_VM_ALLOCATION_POLICY_MAD");
			}else if(Constants.VM_ALLOCATION_POLICY == Constants.MMVM){
				Log.printLine("Allocation Policy: " + "POWER_VM_ALLOCATION_POLICY_MMVM");
			}
		}
		Log.printLine(String.format("Number of hosts: " + numberOfHosts));
		Log.printLine(String.format("Number of VMs: " + numberOfVms));
		Log.printLine(String.format("Total simulation time: %.2f sec", totalSimulationTime));
		Log.printLine(String.format("能耗: %.6f kWh", energy));

		Log.printLine(String.format("SLA time per active host: %.4f%%", slaTimePerActiveHost * 100));
		Log.printLine(String.format("Overall SLA violation: %.10f", slaOverall));
		Log.printLine(String.format("Average SLA violation: %.4f", slaAverage));
		Log.printLine(String.format("虚拟机迁移次数: %d", datacenter.getMigrationCount()));


		if (hosts.get(0) instanceof SimPowerHostMultiR) {
			Map<String, Double> fragMetrics = getFragMetrics(hosts);

			Log.printLine(String.format("avg1 Frag: %.4f", fragMetrics.get("avg")));
			Log.printLine(String.format("avg2 Frag: %.4f", fragMetrics.get("avg1")));
			Log.printLine(String.format("overall Frag: %.4f", fragMetrics.get("overall")));
		}


		if (Constants.VM_ALLOCATION_POLICY == Constants.RMS || Constants.VM_ALLOCATION_POLICY == Constants.MMEA) {
			Log.printLine(String.format("r0: %f, R0: %f, under-thr: %f, safe_param: %f, look_forward: %d, look_back: %d",
					Constants.r0, Constants.R0, Constants.UNDER_UTILIZED_THR, Constants.SafetyParam,
					Constants.LOOK_FORWARD, Constants.LOOK_BACK));
		}
		// Log.printLine(String.format("SLA time per host: %.2f%%",
		// slaTimePerHost * 100));
		simTime.add(totalSimulationTime);
		simPower.add(energy);
		simSlaPerHost.add(slaTimePerActiveHost);
		simOverallSla.add(slaOverall);
		simAverageSla.add(slaAverage);
		getFinishTimeList().add(totalSimulationTime);
		getEngList().add(energy);
	}

	/**
	 * 	碎片化的基本指标：（x-1） + （x-od）
	 * 	取三种碎片化计算指标
	 * 		单个主机指标：首先要考虑怎么用一个参数衡量一个主机在运行期间的碎片化呢？要考虑到不同主机运行时间不一样
	 * 			1）只用平均每刻frag：总frag/时间点个数,相当于单个主机平均每刻frag
	 * 			2）单个主机平均每刻frag * 执行总时长
	 *		一群主机指标：
	 *			1）avg：直接使用：所有主机的单个主机指标之和/主机数量
	 *			2）overall：所有主机平均每刻frag * 所有主机平均执行总时长
	 * **/
	private static Map<String, Double> getFragMetrics(List<Host> hosts) {

		Map<String, Double> fragMetrics = new HashMap<>();
		List tmpList= new LinkedList();
		// two schemes of the frag calculation
		double avgSch1 = 0, overall = 0, avgSch2 = 0;
		int toReduceHost = 0;

		double allHostsTp = 0, allHostsActive = 0;
		for(Host host: hosts){
			SimPowerHostMultiR h = (SimPowerHostMultiR) host;
			// 单个主机平均每刻frag = 总frag/时间点个数
			double curHostFragSch1 = 0;
			// 单个主机平均每刻frag * 执行总时长
			double curHostFragSch2 = 0;
			int timePointCnt = 0;
			double curHostTotalActiveTime = 0;
			double previousTime = -1;
			for(SimHostStateHistoryEntry entry: h.getSimStateHistory())
			{

				if(!entry.isState()){

					previousTime = entry.getTime();
					if(h.getSimStateHistory().size() == 1 && h.getSimStateHistory().get(0).getAllocatedMips() < 0.1){
						toReduceHost++;
					}
					continue;
				}
				if(previousTime > 0){
					double timeDiff = entry.getTime() - previousTime;
					curHostTotalActiveTime += timeDiff;
				}

				double curFrag = 0;
				timePointCnt++;
				double mipsUt = entry.getAllocatedMips() / h.getTotalMips(), ramUt = entry.getAllocatedRam() / h.getRam(),
						ioUt = entry.getAllocatedIo() / h.getIo();
				double ov = (mipsUt + ramUt + ioUt) / 3;

				double a = Math.pow(mipsUt - ov, 2) + Math.pow(ramUt - ov, 2) + Math.pow(ioUt - ov, 2);
				double b = Math.pow(mipsUt - 1, 2) + Math.pow(ramUt - 1, 2) + Math.pow(ioUt - 1, 2);
				curFrag = (Math.pow(a, 0.5)) + (Math.pow(b, 0.5));

				curHostFragSch1 += curFrag;
				previousTime = entry.getTime();

				overall += curFrag;
			}

			if (h.getSimStateHistory().size() > 0) {
				if (timePointCnt > 0) {
					allHostsTp += timePointCnt;
					allHostsActive += curHostTotalActiveTime;

					curHostFragSch1 /= timePointCnt;
					curHostFragSch2 = curHostFragSch1 * curHostTotalActiveTime;

					avgSch1 += curHostFragSch1;
					avgSch2 += curHostFragSch2;
					tmpList.add(timePointCnt);
				}
			}else {
				toReduceHost++;
			}
		}
		int numOfHost = hosts.size() - toReduceHost;
		fragMetrics.put("avg", avgSch1 / numOfHost);
		fragMetrics.put("avg1", avgSch2 / numOfHost);
		fragMetrics.put("overall", (overall / allHostsTp) * (allHostsActive / numOfHost));
		Log.enable();
		//Log.printLine("不同主机的活跃时间点个数（前十主机）：" + tmpList.toString());
		return fragMetrics;
	}


	public static void enhancedPrintResults(PowerDatacenter datacenter, List<SimPowerVm> vmList, List<SimProgressCloudlet> cloudletList,
											List<SimPowerHostMultiR> hostList,
											double lastClock, String experimentName, int algoCnt){
		printResults(datacenter, vmList, lastClock, experimentName);

		List<SimVmStateHistoryEntry> vmHistory ;


		setGlobalC(getGlobalC() + 1);
		/**
		 *
		 * 每类资源对应的数理统计结果
		 *
		 * */
		Map<String, Map<String, Double>> resourceType2Stats;

		/**
		 *
		 * 数理统计
		 * */
		Map<String, Double> stats;
		//每个VM每个时间片的利用率
		//遍历 vmList即可
		//此时运行已经结束了

		//历史记录里边遍历到的当前利用率数据
		double u_mips, u_ram, u_io, u_bw;

		double vmTotalMips;
		double vmTotalRam;
		double vmTotalIo;
		double vmTotalBw;

		double avg, mode, max, min;
		/*double avgRam, modeRam, maxRam, minRam;
		double avgIo, modeIo, maxIo, minIo;
		double avgBw, modeBw, maxBw, minBw;*/

		String reType[] = {"cpu", "ram", "io", "bw"};

		List<Double> u_mipsList;
		List<Double> u_ramList;
		List<Double> u_ioList ;
		List<Double> u_bwList ;

		List<List<Double>> listColletction ;



		int ind = 0;

		int j;
		for(Vm vm : vmList){


            vmTotalMips = vm.getMips() * vm.getNumberOfPes();
            vmTotalRam = vm.getRam();
            vmTotalIo = ((SimPowerVm)vm).getIo();
            vmTotalBw = vm.getBw();


			u_mipsList = new LinkedList<>();
			u_ramList = new LinkedList<>();
			u_ioList = new LinkedList<>();
			u_bwList = new LinkedList<>();

			listColletction = new LinkedList<>();

			listColletction.add(u_mipsList);
			listColletction.add(u_ramList);
			listColletction.add(u_ioList);
			listColletction.add(u_bwList);

			/**
			 *  历史数据理论上最多会有287个时间点的数据
			 * */
			vmHistory = ((SimPowerVm)vm).getMultireStateHistory();


			int id = vm.getId();

			//先遍历，遍历过程中将数据加入列表后再一一统计
			//entry是每个时间点对应的资源使用情况
			for (SimVmStateHistoryEntry entry : vmHistory){


				/*vmTotalMips = entry.getAllocatedMips();
						vmTotalRam = entry.getAllocatedRam();
						vmTotalIo = entry.getAllocatedIo();
								vmTotalBw = entry.getAllocatedBw();*/
				//每个entry存储的都是历史的时间跟资源分配、请求、利用率情况
				//4种资源依次加入对应的列表


				//i  代表当前要统计的资源种类
				//往    stats   中依次添加avg、mode、max、min。
				//由于vmHistory是list类型来的。而里面的元素又是类型。。因此可以采用Collection.sort来
				//有些资源的分配是0，因此计算利用率之前记得先验证分母有效性
				if (Math.abs(vmTotalMips - 0) < 0.01) {
					u_mips = 0;
				}
				else {
					u_mips = entry.getUsedMips() / vmTotalMips;
				}

				if (Math.abs(vmTotalRam - 0) < 0.01) {
					u_ram = 0;
				}
				else {
					u_ram = entry.getUsedRam() / vmTotalRam;
				}

				if (Math.abs(vmTotalIo - 0) < 0.01) {
					u_io = 0;
				}
				else {
					u_io = entry.getUsedIo() / vmTotalIo;
				}
				if (Math.abs(vmTotalBw - 0) < 0.01) {
					u_bw = 0;
				}
				else {
					u_bw = entry.getUsedBw() / vmTotalBw;
				}


				listColletction.get(0).add(u_mips);
				listColletction.get(1).add(u_ram);
				listColletction.get(2).add(u_io);
				listColletction.get(3).add(u_bw);

			}

			getVmsToMipsUtilizationMap().put(vm.getId(), u_mipsList);
			getVmsToRamUtilizationMap().put(vm.getId(), u_ramList);
			getVmsToIoUtilizationMap().put(vm.getId(), u_ioList);
			/**
			 *
			 *
			 * */
			//输出算法5的30个VM CPU，RAM，IO利用率到文件里面
			if(algoCnt == 2){
				if(vmPrintCnt > 0){

					Log.printLine("VM_"+vm.getId() + ":");
					Log.printLine("CPU:"+u_mipsList);

					Log.printLine("RAM:"+u_ramList);

					Log.printLine("IO:"+u_ioList);

				}
				vmPrintCnt --;
			}

			ind ++;
			resourceType2Stats = new HashMap<>();
			List<Double> tmpList;
			for(int i = 0; i< 4; i++){

				//取出的是全时间序列的    mips    利用率数据
				tmpList = listColletction.get(i);
				int size = tmpList.size();

				//System.out.println("tmpList 的大小：" + size);

				stats = new HashMap<>();

				//先排序,从小到大
				Collections.sort(tmpList, new Comparator<Double>() {
					@Override
					public int compare(Double o1, Double o2) {
						double a = o1, b = o2;
						if(a < b){
							return -1;
						}
						else if(a > b){
							return 1;
						}
						else return 0;
					}
				});

				avg = tmpList.stream().mapToDouble(Double::doubleValue).average().getAsDouble();

				j = ind;

				//寻找有序列表的众数
				mode = findMode(tmpList);

				stats.put("avg", avg);
				stats.put("mode", mode);
				stats.put("max", tmpList.get(tmpList.size() - 1));
				stats.put("min", tmpList.get(0));

				resourceType2Stats.put(reType[i], stats);
			}



			vmIdToUtilizationMap.put(vm.getId(), resourceType2Stats);

		}

		recCurAlgoBalanceDegree(vmIdToUtilizationMap);


		//VM迁移次数
		int migCnt = datacenter.getMigrationCount();

		getMigCntList().add((double) migCnt);

		Map<Integer, Double> cloudletRatioTmpList = new HashMap<>();

		List<Double> cloudletFinishTmpList = new ArrayList<>();
		List<Double> iotFinishTmpList = new ArrayList<>();
		//把获取每个任务的wating_time 和 每个主机的 SLA 合并在一起写
		for (SimProgressCloudlet cloudlet : cloudletList) {
			//任务的waiting_time,注意要的不是也不能是平均等待时间，必须先获得每个任务的等待时间以方便后续处理
			//既然是所有任务就不能只考虑正在运行的任务了。
			int cloudltId = cloudlet.getCloudletId();
			cloudletToCorreWaitingTimeMap.put(cloudltId, cloudlet.getWaitingTime());

			cloudletToCorreFinishTimeMap.put(cloudltId, cloudlet.getFinishTime());

			cloudletFinishTmpList.add(cloudlet.getFinishTime());

			if(((SimProgressCloudletIoT)cloudlet).getLabel().equals("IoT")){
				iotFinishTmpList.add(cloudlet.getFinishTime());
			}
			//cloudlet.getFinishTime()
			//cloudlet.getStateHistory()

			if(cloudlet.getFinishTime() > 0){
				cloudletRatioTmpList.put(cloudltId, cloudlet.getWaitingTime() / cloudlet.getFinishTime());
			}
			else {
				//异常值
				cloudletRatioTmpList.put(cloudltId, -1.0);
			}


		}

		getRatioList().add(cloudletRatioTmpList);

		getCompCloudletToFinishTimeList().add(cloudletFinishTmpList);

		getCompIoTCloudletToFinishTimeList().add(iotFinishTmpList);


		double timeDiff = 0.0;
		double totalTime = 0.0;
		double slaViolationTimePerHost = 0.0;
		//每个主机：正在运行的主机的SLA，需要先判断当前的状态是active还是其他
		List<SimPowerHost> hosts = datacenter.getHostList();
		List<Double> tmpHostSlaList = new ArrayList<>();
		for (Host _host : hosts) {
			HostDynamicWorkload host = (HostDynamicWorkload) _host;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;

			for (HostStateHistoryEntry entry : host.getStateHistory()) {
				if (previousTime != -1) {
					timeDiff = entry.getTime() - previousTime;
					totalTime += timeDiff;
					if (previousAllocated < previousRequested) {
						slaViolationTimePerHost += timeDiff;
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
			}
			hostToSla.put(_host.getId(), slaViolationTimePerHost / totalTime);

			tmpHostSlaList.add(slaViolationTimePerHost / totalTime);
		}

		getHostToSlaComp().add(tmpHostSlaList);


		Log.printLine(vmIdToUtilizationMap);
		Log.printLine(cloudletToCorreWaitingTimeMap);
		Log.printLine(hostToSla);

		//hostToSla.putAll();

		//非横向对比跟横向对比要分开来
		drawAll();


		//横向对比要的数据：host的sla情况，能耗，任务等待时间占任务完成时间的比重，执行完本批任务的时间，
		//迁移次数
		if(algoCnt == 0){
			drawFinal();
			outputFinal();
			//为了制作表格，还要输出IoT响应时间的平均数
			outputIoTAvgComp(getCompIoTCloudletToFinishTimeList());


			//为了制作表格，还要输出所有主机的sla平均数
			outputHostAvgSlaComp(getHostToSlaComp());
		}


	}

	private static void outputHostAvgSlaComp(List<List<Double>> hostToSlaComp) {
		Log.printLine("不同算法的主机SLA平均违反率分别是：");
		for(List<Double> dList : hostToSlaComp){
			double d = getMean(dList);
			Log.printLine(d + " ");
		}
		Log.printLine();
		Log.printLine("不同算法的所有主机SLA：");
		for(List<Double> dList : hostToSlaComp){


			Log.printLine(dList);
		}
	}

	private static void outputIoTAvgComp(List<List<Double>> compIoTCloudletToFinishTimeList) {

		Log.printLine("不同算法的IoT响应平均时间分别是：");
		for(List<Double> dList : compIoTCloudletToFinishTimeList){
			double d = getMean(dList);
			Log.printLine(d + " ");
		}

	}

	private static void outputFinal() {
		Log.printLine(getCompLoadBalanceDegreeInMips());
		Log.printLine(getCompLoadBalanceDegreeInRam());
		Log.printLine(getCompLoadBalanceDegreeInIo());
	}

	//记录当前算法的不同资源的均衡程度
	private static void recCurAlgoBalanceDegree(Map<Integer, Map<String, Map<String, Double>>> vmIdToUtilizationMap) {


		//所有vm的CPU利用率平均数

		double temp = 0;
		List<Double> data = getVmsMode("cpu", vmIdToUtilizationMap);
		int size = data.size();
		double mean = getMean(data);
		for(double a :data)
			temp += (a-mean)*(a-mean);
		temp = temp/(size-1);

		getCompLoadBalanceDegreeInMips().add(temp);

		temp = 0;
		data = getVmsMode("ram", vmIdToUtilizationMap);
		size = data.size();
		mean = getMean(data);
		for(double a :data)
			temp += (a-mean)*(a-mean);
		temp = temp/(size-1);

		getCompLoadBalanceDegreeInRam().add(temp);

		temp = 0;
		data = getVmsMode("io", vmIdToUtilizationMap);
		size = data.size();
		mean = getMean(data);
		for(double a :data)
			temp += (a-mean)*(a-mean);
		temp = temp/(size-1);

		getCompLoadBalanceDegreeInIo().add(temp);
	}

	private static double getMean(List<Double> data) {

		double temp = 0;
		for (double d : data){
			temp += d;
		}
		return temp / data.size();
	}

	private static List<Double> getVmsMode(String resourceType, Map<Integer, Map<String, Map<String, Double>>> vmIdToUtilizationMap) {

		Iterator iterator = vmIdToUtilizationMap.entrySet().iterator();
		List<Double> tmpList = new LinkedList<>();
		while (iterator.hasNext()){

			Map.Entry<Integer, Map<String, Map<String, Double>>> entry = (Map.Entry) iterator.next();
			tmpList.add(entry.getValue().get(resourceType).get("avg"));
		}
		return tmpList;

	}


	/**
	 *
	 *横向对比
	 * */
	private static void drawFinal() {

		String mark = "Algo" + getGlobalC();

		List localList = null;

		String title = "";

		String xLabel = "";

		String yLabel = "";

		int max;

		int i;

		Map<Integer, Double> tmpMap = null;


		//host的sla情况------
		title = "Comparison of algorithm's SLA";
		xLabel = "Host";
		yLabel = "SLA in percent";
		max = 3;
		barChartInit(title, xLabel, yLabel, max);
		i = 1;
		for(List l : getHostToSlaComp()){
			//Barchart是每次能加一组的柱体
			BarChart.addDataset("Algo"+(i++), l);
		}
		/*System.out.println("--------------------------------");
		System.out.println("所有算法的所有主机的sla：" );
		System.out.println(getHostToSlaComp());*/
		BarChart.draw();
		BarChart.clear();


		//能耗-------
		title = "Comparison of algorithm's energy";
		xLabel = "Algorithm";
		yLabel = "Energy consumption(KWh)";
		max = 50;
		barChartInit(title, xLabel, yLabel, max);
		//BarChart.addDataset(mark, getEngList());
		i = 1;
		for(Double d: getEngList()){
			tmpMap = new HashMap<>();
			tmpMap.put(i, d);
			BarChart.addDataset("Algo" + i, tmpMap);
			i++;
		}
		BarChart.draw();
		BarChart.clear();


		//任务等待时间占任务完成时间的比重
		title = "Comparison of algorithm's cloudlets' wait-finish ratio";
		xLabel = "Cloudlet";
		yLabel = "Ratio";
		max = 10;
		barChartInit(title, xLabel, yLabel, max);
		i = 1;
		for(Map<Integer, Double> m : getRatioList()){
			//Barchart是每次能加一组的柱体
			//有一个问题，不同算法导致最终任务列表排序是不一样的。所以还是需要Map来存储。
			BarChart.addDataset("Algo"+(i++), m);
		}
		BarChart.draw();
		BarChart.clear();


		/**
		 * 维度再次下降。一个柱体才代表一个key。下面的也一样。
		 * */
		//执行完本批任务的时间-------
		title = "Comparison of algorithm's make-span";
		xLabel = "Algorithm";
		yLabel = "Make-span(Seconds)";
		max = 50;
		barChartInit(title, xLabel, yLabel, max);

		int ind = 1;
		for(Double d: getFinishTimeList()){
			tmpMap = new HashMap<>();
			tmpMap.put(ind, d);
			BarChart.addDataset("Algo" + ind, tmpMap);
			ind++;
		}
		BarChart.draw();
		BarChart.clear();

		//迁移次数-------
		// List<Double>
		/**
		 * 无所谓了，，反正都是0
		 * */
		title = "Comparison of algorithm's VM migration counts";
		xLabel = "Algorithm";
		yLabel = "Migration";
		max = 50;
		barChartInit(title, xLabel, yLabel, max);
		tmpMap = null;
		ind = 1;
		for(Double d: getMigCntList()){
			tmpMap = new HashMap<>();
			tmpMap.put(ind, d);
			/**
			 * 用Map的原因就是因为list只能按照索引顺序依次来，导致每次开始都是0
			 *
			 * */
			BarChart.addDataset("Algo"+ind, tmpMap);
			ind++;
		}
		BarChart.draw();
		BarChart.clear();


		/**
		 * 不同算法的的每个Cloudlet的IoT的响应时间(finish time)对比
		 * 由于要画箱线图，先把所有数据写进csv。
		 * 每行一种算法，
		 * 每行的数据用'\t'分开来
		 * 箱线图不区分cloudlet的id，所以使用List<List<Double>即可
		 *
		 * 每个文件的行数对应的就是IoTAwareExp类里面的cls，也就是对比算法的个数
		 * */
		String fileName1 = "不同算法的IoT任务响应时间对比";
		String fileName2 = "不同算法的所有任务响应时间对比";
		try {
			writeCloudletsBoxPlot(fileName1, fileName2);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param fileName1 IoT响应时间
	 * @param fileName2 所有响应时间 */
	private static void writeCloudletsBoxPlot(String fileName1, String fileName2) throws IOException {

		File file1 = new File(csvPath + "/" + fileName1);
		File file2 = new File(csvPath + "/" + fileName2);

		StringBuilder sb = null;
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file1)));

		//IoT

		for(List<Double> list : getCompIoTCloudletToFinishTimeList()){
			sb = new StringBuilder();
			for(Double d : list){
				sb.append(d).append('\t');
			}
			bw.write(sb.toString());
			bw.newLine();

		}

		bw.flush();
		bw.close();
		bw = null;
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file2)));

		//ALL
		for(List<Double> list : getCompCloudletToFinishTimeList()){
			sb = new StringBuilder();
			for(Double d : list){
				sb.append(d).append('\t');
			}
			bw.write(sb.toString());
			bw.newLine();


		}
		bw.flush();
		bw.close();

	}

	/**
	 *
	 *非横向对比
	 * */
	private static <K, V> void drawAll() {

		String mark = "Algo" + getGlobalC();

		String title = "";

		String xLabel = "";

		String yLabel = "";

		int max;

		List<Map<? extends Object, ? extends Object>> mapList = null;

		//任务完成时间柱状图
		title =  "Task Finish Time - " + mark;
		xLabel =  "Task ID";
		yLabel = "Time";
		max = 30;
		barChartInit(title, xLabel, yLabel, max);
		mapList = new ArrayList<>();
		mapList.add(getCloudletToCorreFinishTimeMap());
		drawBar(mark, mapList);
		/**
		 * 清零
		 * */
		getCloudletToCorreFinishTimeMap().clear();
		BarChart.clear();


		//任务等待时间柱状图
		title =  "Task Waiting Time - " + mark;
		xLabel =  "Task ID";
		yLabel = "Time";
		max = 30;
		barChartInit(title, xLabel, yLabel, max);
		mapList = new ArrayList<>();
		mapList.add(getCloudletToCorreWaitingTimeMap());

		drawBar(mark, mapList);
		/**
		 * 清零
		 * */
		getCloudletToCorreWaitingTimeMap().clear();
		BarChart.clear();

		//max个vm的CPU利用率，记得剔除大于1的点
		title = "VMs' CPU Utilization - " + mark;
		xLabel = "Time Slot";
		yLabel = "Utilization";
		max = 30;
		lineChartInit(title, xLabel, yLabel, max);
		//由于LineChart是每个条线（即每个序列）对应一个关键字的，所以需要for循环
		//mapList = new ArrayList<>();
		//遍历vm-utilization
		drawLine("CPU", null, vmsToMipsUtilizationMap);
		/**
		 * 清零
		 * */
		getVmsToMipsUtilizationMap().clear();
		LineChart.clear();



		//max个vm的ram利用率，记得剔除大于1的点
		title = "VMs' RAM Utilization - " + mark;
		xLabel = "Time Slot";
		yLabel = "Utilization";
		//max = 30;
		lineChartInit(title, xLabel, yLabel, max);
		//由于LineChart是每个条线（即每个序列）对应一个关键字的，所以需要for循环
		//mapList = new ArrayList<>();
		//遍历vm-utilization
		drawLine("RAM", null, vmsToRamUtilizationMap);
		/**
		 * 清零
		 * */
		getVmsToRamUtilizationMap().clear();
		LineChart.clear();




		//max个vm的io利用率，记得剔除大于1的点
		title = "VMs' IO Utilization - " + mark;
		xLabel = "Time Slot";
		yLabel = "Utilization";
		//max = 30;
		lineChartInit(title, xLabel, yLabel, max);
		//由于LineChart是每个条线（即每个序列）对应一个关键字的，所以需要for循环
		//mapList = new ArrayList<>();
		//遍历vm-utilization
		drawLine("IO", null, vmsToIoUtilizationMap);
		/**
		 * 清零
		 * */
		getVmsToIoUtilizationMap().clear();
		LineChart.clear();


		/**
		 * 清零
		 * */
		getHostToSla().clear();
		getVmIdToUtilizationMap().clear();

	}

	private static void barChartInit(String title, String xLabel, String yLabel, int max){
		BarChart.setTitle(title);

		BarChart.setxLabel(xLabel);

		BarChart.setyLabel(yLabel);

		BarChart.setMAX(max);
	}

	private static void drawBar(String key, List<Map<? extends Object, ? extends Object>> mapList) {

		//比如任务等待时间这种
		for (Map map : mapList) {
			BarChart.addDataset(key, map);
		}

		BarChart.draw();
	}

	private static void lineChartInit(String title, String xLabel, String yLabel, int max){
		LineChart.setTitle(title);

		LineChart.setxLabel(xLabel);

		LineChart.setyLabel(yLabel);

		LineChart.setMAX(max);
	}

	/**
	 * 添加处理好的序列数据
	 * key代表这个序列的关键词。比如ram为内存的相关数据。
	 *
	 * */
	public static void drawLine(String key, List<Map<? extends Object, ? extends Object>> hashMapList,
								Map<? extends Object, ? extends Object> pureListMap){

		int cnt = LineChart.getMAX();
		if (hashMapList != null) {

			for (Map hashMap: hashMapList) {
				if(cnt <= 0){
					break;
				}
				cnt--;

				LineChart.addDatasetsByMap(""+hashMapList.indexOf(hashMap), hashMap);
			}
		}

		cnt = LineChart.getMAX();
		//比如  vm-资源利用率，取出的每个list都是一条线  这种
		if(pureListMap != null){

			Iterator iterator = pureListMap.entrySet().iterator();
			Map.Entry entry ;
			while (iterator.hasNext()) {
				if(cnt <= 0){
					break;
				}
				cnt--;
				//比如，，一个VM的ID对应一串CPU/RAM/IO历史记录

				entry = (Map.Entry) iterator.next();
				LineChart.addDatasetsByList(""+ entry.getKey(), (List<?>) entry.getValue());
			}
		}

		LineChart.draw();
	}

	/**
	 *
	 * 寻找有序数组的众数
	 *  用动态规划·可以做出来
	 *  */
	private static double findMode(List<Double> tmpList) {
		double cur;
		double pre = -100;
		double theMode = -100;

		int maxCnt = Integer.MIN_VALUE;
		int cnt = 0;

		int ind = 0;
		//
		for (Double d : tmpList){

			cur = d;

			//ind++;

			// 判断当前是不是才刚开始找
			if(Math.abs(-100 - pre) < 0.01){
				theMode = cur;
				cnt = 1;
			}
			else {
				// 判断是否跟前一个数字相同
				if(Math.abs(cur - pre) < 0.01){
					cnt ++;
				}
				else {
					//如果不同
					//每次都要检查是否需要替换
					if(maxCnt < cnt){
						maxCnt = cnt;
						theMode = pre;
					}
					//赋值为0重新开始计数
					cnt = 1;

				}
			}


			pre = cur;
		}

		if(maxCnt < cnt){
			theMode = pre;
		}

		return theMode;
	}


	/**
	 * Gets the sla time per active host.
	 * 	SLA violation time per active host (SLATAH):
	 * 	因为主机开启关闭在仿真过程中是动态变化的。所以要SLATAH比较好
	 * @param hosts
	 *            the hosts
	 * @return the sla time per active host
	 */
	protected static double getSlaTimePerActiveHost(List<? extends Host> hosts) {
		double sumOfSlaViolationRatePerHost = 0;

		double testSum  = 0;
		for (Host _host : hosts) {
			HostDynamicWorkload host = (HostDynamicWorkload) _host;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;
			boolean previousIsActive = true;

			double totalTime = 0;
			double totalSLAVTime = 0;
			for (HostStateHistoryEntry entry : host.getStateHistory()) {
				if (previousTime != -1 && previousIsActive) {
					double timeDiff = entry.getTime() - previousTime;
					totalTime += timeDiff;
					if (previousAllocated < previousRequested) {
						totalSLAVTime += timeDiff;
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
				previousIsActive = entry.isActive();
			}
			if(Math.abs(totalTime - 0) > 0.001){
				sumOfSlaViolationRatePerHost += (totalSLAVTime / totalTime);
			}
			testSum += totalTime;
		}
		//Log.enable();
		//Log.printLine("test sum: " + testSum);
		return sumOfSlaViolationRatePerHost / hosts.size();
	}

	/**
	 * Gets the sla time per host.
	 * 
	 * @param hosts
	 *            the hosts
	 * @return the sla time per host
	 */
	protected static double getSlaTimePerHost(List<Host> hosts) {
		double slaViolationTimePerHost = 0;
		double totalTime = 0;

		for (Host _host : hosts) {
			HostDynamicWorkload host = (HostDynamicWorkload) _host;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;

			for (HostStateHistoryEntry entry : host.getStateHistory()) {
				if (previousTime != -1) {
					double timeDiff = entry.getTime() - previousTime;
					totalTime += timeDiff;
					if (previousAllocated < previousRequested) {
						slaViolationTimePerHost += timeDiff;
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
			}
		}

		return slaViolationTimePerHost / totalTime;
	}

	/**
	 * Gets the sla metrics.
	 * 
	 * @param vms
	 *            the vms
	 * @return the sla metrics
	 */
	protected static Map<String, Double> getSlaMetrics(List<? extends SimPowerVm> vms) {
		Map<String, Double> metrics = new HashMap<String, Double>();
		List<Double> slaViolation = new LinkedList<Double>();
		double totalAllocated = 0;
		double totalRequested = 0;

		for (Vm vm : vms) {
			double vmTotalAllocated = 0;
			double vmTotalRequested = 0;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;

			for (VmStateHistoryEntry entry : vm.getStateHistory()) {
				if (previousTime != -1) {
					double timeDiff = entry.getTime() - previousTime;
					vmTotalAllocated += previousAllocated * timeDiff;
					vmTotalRequested += previousRequested * timeDiff;

					if (previousAllocated < previousRequested) {
						slaViolation.add((previousRequested - previousAllocated) / previousRequested);
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
			}

			totalAllocated += vmTotalAllocated;
			totalRequested += vmTotalRequested;
		}

		metrics.put("overall", (totalRequested - totalAllocated) / totalRequested);
		if (slaViolation.isEmpty()) {
			metrics.put("average", 0.);
		} else {
			metrics.put("average", MathUtil.mean(slaViolation));
		}

		return metrics;
	}

	/**
	 * Prints the Cloudlet objects.
	 * 
	 * @param list
	 *            list of Cloudlets
	 */
	public static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "\t";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Resource ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId());

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.printLine(indent + "SUCCESS" + indent + indent + cloudlet.getResourceId() + indent
						+ cloudlet.getVmId() + indent + dft.format(cloudlet.getActualCPUTime()) + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}

	public static void outputHostFininshedTime(List<SimPowerVm> vmlist) {
		Map<Integer, Double> hostFinishedTime = new HashMap<Integer, Double>();
		Log.printLine("outputVmHostFininshedTime vms: " + vmFininshedTime.keySet().size());
		for (Integer vmid : vmFininshedTime.keySet()) {
			System.out.print(vmFininshedTime.get(vmid) + " ");
			int hostId = vmToHost.get(vmid);
			if (hostFinishedTime.get(hostId) == null)
				hostFinishedTime.put(hostId, vmFininshedTime.get(vmid));
			else if (hostFinishedTime.get(hostId) < vmFininshedTime.get(vmid))
				hostFinishedTime.put(hostId, vmFininshedTime.get(vmid));
		}
		Log.printLine("\nHosts " + hostFinishedTime.keySet().size());
		for (Integer vmid : hostFinishedTime.keySet()) {
			System.out.print(hostFinishedTime.get(vmid) + " ");
		}
	}

	public static void outputHostUtilizationHistory(String filename) {
		String output = projectPath + OUTPUT + filename;
		File file = new File(output);
		try {
			if (!file.exists())
				file.createNewFile();
			else
				file.delete();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		DecimalFormat dft = new DecimalFormat("###.###");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i < hostCpuUtilization.size(); i++) {
				writer.write("Host#" + i + "\n");
				for (int j = 0; j < hostCpuUtilization.get(i).size(); j++) {
					writer.write(" " + hostUtilizationTime.get(j) + ": " + dft.format(hostCpuUtilization.get(i).get(j))
							+ "  " + dft.format(hostRamUtilization.get(i).get(j)) + "  "
							+ dft.format(hostIoUtilization.get(i).get(j)) + "  "
							+ dft.format(hostBwUtilization.get(i).get(j)) + "  ");
					writer.write(" Consumption: " + dft.format(hostCpuPower.get(i).get(j)) + "  "
							+ dft.format(hostRamPower.get(i).get(j)) + "  " + dft.format(hostIoPower.get(i).get(j))
							+ "  " + dft.format(hostBwPower.get(i).get(j)) + " Total:" + dft.format(power.get(j))
							+ "\n");
				}

			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

	public static void outputHostUtilizationHistory(String filename, int cloudletnum) {
		String output = OUTPUT + filename;
		String record = OUTPUT + "record.txt";
		File file = new File(output);
		File ff = new File(record);

		try {
			if (!ff.exists())
				ff.createNewFile();
			if (!file.exists())
				file.createNewFile();
			else
				file.delete();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		DecimalFormat dft = new DecimalFormat("###.###");
		try {
			double totalpower = 0.0;
			double avgpower = 0.0;
			String percent;
			double PerformanceToPower = 0.0;
			double ssj_ops = 0.0;
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			BufferedWriter writerrecord = new BufferedWriter(new FileWriter(ff, true));
			for (int i = 0; i < hostCpuUtilization.size(); i++) {
				writer.write("Host#" + i + "\r\n");
				int j;
				for (j = 0; j < hostCpuUtilization.get(i).size(); j++) {
					writer.write(" " + hostUtilizationTime.get(j) + ": " + dft.format(hostCpuUtilization.get(i).get(j))
							+ "  " + dft.format(hostRamUtilization.get(i).get(j)) + "  "
							+ dft.format(hostIoUtilization.get(i).get(j)) + "  "
							+ dft.format(hostBwUtilization.get(i).get(j)) + "  ");
					writer.write(" Consumption: CpuPower" + dft.format(hostCpuPower.get(i).get(j)) + "  "
							+ dft.format(hostRamPower.get(i).get(j)) + "  " + dft.format(hostIoPower.get(i).get(j))
							+ "  " + dft.format(hostBwPower.get(i).get(j)) + " Total:" + dft.format(power.get(j))
							+ "\r\n");
					totalpower += power.get(j);
				}

				totalpower = totalpower / 3600.0;
				avgpower = totalpower / j;
				ssj_ops = ((double) cloudletnum / j);
				PerformanceToPower = ssj_ops / avgpower;

				percent = SimVmSchedulerTimeSharedOverLimit.getSourcePercent();
				writerrecord.write("============================================================\r\n");
				writerrecord.write(" " + percent + "\r\n");
				writerrecord.write(" " + String.format("ssj_ops: %f", ssj_ops) + ";  "
						+ String.format("AvgPower: %f", avgpower) + "\r\n");
				writerrecord.write(" " + String.format("PerformanceToPower: %f", PerformanceToPower) + "\r\n");
			}

			Log.printLine(" ");
			Log.printLine(String.format("TotalPower: %f", totalpower));
			Log.printLine(String.format("AvgPower: %f", avgpower));
			writer.write(String.format("TotalPower: %f", totalpower) + "\r\n");
			writer.write(String.format("AvgPower: %f", avgpower) + "\r\n");
			writer.close();
			writerrecord.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

	public static void outputCloudletHistory(String filename, List<SimCloudlet> list) {
		String output = projectPath + OUTPUT + filename;
		File file = new File(output);
		try {
			if (!file.exists())
				file.createNewFile();
			else
				file.delete();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		DecimalFormat dft = new DecimalFormat("###.##");

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (SimCloudlet cloudlet : list) {
				writer.write("Cloudlet#" + cloudlet.getCloudletId() + "\n");
				writer.write(" [Mips] " + cloudlet.getMaxMips() + " [Ram] " + cloudlet.getRam() + " [Io] "
						+ cloudlet.getIo() + " [Bw] " + cloudlet.getBw() + "\n");
				writer.write(" [ActualCPU] " + dft.format(cloudlet.getActualCPUTime()) + " [Wait] "
						+ cloudlet.getWaitingTime() + " [Start] " + cloudlet.getExecStartTime() + " [End] "
						+ cloudlet.getFinishTime() + "\n");
				for (SimCloudletStateHistoryEntry state : cloudlet.getStateHistory()) {
					writer.write("	" + state.getTime() + ": [mips] " + state.getAllocatedMips() + " / "
							+ state.getRequestedMips());
					writer.write("	: [ram] " + state.getAllocatedRam() + " / " + state.getRequestedRam());
					writer.write("	: [io] " + state.getAllocatedIo() + " / " + state.getRequestedIo());
					writer.write("	: [bw] " + state.getAllocatedBw() + " / " + state.getRequestedBw());
					writer.write("	: [state] " + state.getState() + " \n");
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void outputResult(String filename, List<Integer> cls) {
		String output = OUTPUT + filename;
		File file = new File(output);
		try {
			if (!file.exists())
				file.createNewFile();
			else
				file.delete();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		DecimalFormat dft = new DecimalFormat("###.##");

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i < simTime.size(); i++) {
				writer.write(cls.get(i) + " " + dft.format(simTime.get(i)) + "  " + dft.format(simPower.get(i)) + "  "
						+ dft.format(simSlaPerHost.get(i)) + " " + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void outputResult(String filename) {
		String output = OUTPUT ;
		output += "/MyOutput/";
		output += filename;
		File file = new File(output);
		try {
			if (!file.exists())
				file.createNewFile();
			else
				file.delete();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		DecimalFormat dft = new DecimalFormat("###0.00");

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write( "Simulation time this time" +"        "+ "Simulation energy this time"+"            "+"SLA"+"\n");
			for (int i = 0; i < simTime.size(); i++) {
				writer.write( "      " + dft.format(simTime.get(i)) + "                        " + dft.format(simPower.get(i)) + "                           "
						+ dft.format(simSlaPerHost.get(i)) + "  " + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}



	/**
	 * Prints the metric history.
	 * 
	 * @param hosts
	 *            the hosts
	 * @param vmAllocationPolicy
	 *            the vm allocation policy
	 */
	public static void printMetricHistory(List<? extends Host> hosts,
			PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy) {
		for (int i = 0; i < 10; i++) {
			Host host = hosts.get(i);

			Log.printLine("Host #" + host.getId());
			Log.printLine("Time:");
			if (!vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
				continue;
			}
			for (Double time : vmAllocationPolicy.getTimeHistory().get(host.getId())) {
				Log.format("%.2f, ", time);
			}
			Log.printLine();

			for (Double utilization : vmAllocationPolicy.getUtilizationHistory().get(host.getId())) {
				Log.format("%.2f, ", utilization);
			}
			Log.printLine();

			for (Double metric : vmAllocationPolicy.getMetricHistory().get(host.getId())) {
				Log.format("%.2f, ", metric);
			}
			Log.printLine();
		}
	}


	public static List<Double> getSimTime() {
		return simTime;
	}

	public static List<Double> getSimPower() {
		return simPower;
	}

	public static List<Double> getAveWait() {
		return aveWait;
	}

	public static List<Double> getAveRun() {
		return aveRun;
	}

	public static List<Double> getSimSlaPerHost() {
		return simSlaPerHost;
	}

	public static List<Double> getSimOverallSla() {
		return simOverallSla;
	}

	public static List<Double> getSimAverageSla() {
		return simAverageSla;
	}

	public static List<Double> getHostTime() {
		return hostTime;
	}

	public static List<Double> getHostUtilizationTime() {
		return hostUtilizationTime;
	}

	public static List<List<Double>> getHostCpuUtilization() {
		return hostCpuUtilization;
	}

	public static List<List<Double>> getHostRamUtilization() {
		return hostRamUtilization;
	}

	public static List<List<Double>> getHostIoUtilization() {
		return hostIoUtilization;
	}

	public static List<List<Double>> getHostBwUtilization() {
		return hostBwUtilization;
	}

	public static List<Double> getPower() {
		return power;
	}

	public static List<List<Double>> getHostCpuPower() {
		return hostCpuPower;
	}

	public static List<List<Double>> getHostRamPower() {
		return hostRamPower;
	}

	public static List<List<Double>> getHostIoPower() {
		return hostIoPower;
	}

	public static List<List<Double>> getHostBwPower() {
		return hostBwPower;
	}

	public static List<Integer> getCreatedVms() {
		return createdVms;
	}

	public static Map<Integer, Double> getVmFininshedTime() {
		return vmFininshedTime;
	}

	public static Map<Integer, Integer> getVmToHost() {
		return vmToHost;
	}

	public static Map<Integer, Map<String, Map<String, Double>>> getVmIdToUtilizationMap() {
		return vmIdToUtilizationMap;
	}

	public static Map<Integer, Double> getCloudletToCorreWaitingTimeMap() {
		return cloudletToCorreWaitingTimeMap;
	}

	public static Map<Integer, Double> getHostToSla() {
		return hostToSla;
	}

	public static String getOUTPUT() {
		return OUTPUT;
	}

	public static String getProjectPath() {
		return projectPath;
	}

	public static void setProjectPath(String projectPath) {
		Helper.projectPath = projectPath;
	}

	public static Map<Integer, Double> getCloudletToCorreFinishTimeMap() {
		return cloudletToCorreFinishTimeMap;
	}

	public static Map<Integer, List<Double>> getVmsToMipsUtilizationMap() {
		return vmsToMipsUtilizationMap;
	}

	public static Map<Integer, List<Double>> getVmsToRamUtilizationMap() {
		return vmsToRamUtilizationMap;
	}

	public static Map<Integer, List<Double>> getVmsToIoUtilizationMap() {
		return vmsToIoUtilizationMap;
	}


	public static List<List<Double>> getHostToSlaComp() {
		return hostToSlaComp;
	}

	public static List<Double> getEngList() {
		return engList;
	}

	public static List<Map<Integer, Double>> getRatioList() {
		return ratioList;
	}

	public static List<Double> getFinishTimeList() {
		return finishTimeList;
	}

	public static List<Double> getMigCntList() {
		return migCntList;
	}

	public static int getGlobalC() {
		return GlobalC;
	}

	public static void setGlobalC(int globalC) {
		GlobalC = globalC;
	}

	public static List<List<Double>> getCompCloudletToFinishTimeList() {
		return compCloudletToFinishTimeList;
	}

	public static List<List<Double>> getCompIoTCloudletToFinishTimeList() {
		return compIoTCloudletToFinishTimeList;
	}

	public static String getCsvPath() {
		return csvPath;
	}

	public static void setCsvPath(String csvPath) {
		Helper.csvPath = csvPath;
	}

	public static List<Double> getCompLoadBalanceDegreeInMips() {
		return compLoadBalanceDegreeInMips;
	}

	public static List<Double> getCompLoadBalanceDegreeInRam() {
		return compLoadBalanceDegreeInRam;
	}

	public static List<Double> getCompLoadBalanceDegreeInIo() {
		return compLoadBalanceDegreeInIo;
	}
}
