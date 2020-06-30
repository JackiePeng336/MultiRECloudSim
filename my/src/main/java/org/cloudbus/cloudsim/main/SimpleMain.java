package org.cloudbus.cloudsim.main;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletAssignmentPolicyBalance;
import org.cloudbus.cloudsim.CloudletAssignmentPolicySimple;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.SimCloudlet;
import org.cloudbus.cloudsim.SimCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.SimDatacenterBroker;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelByFile;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.allocator.CloudletBwAllocatorSimple;
import org.cloudbus.cloudsim.allocator.CloudletCpuAllocatorMaxMinFairness;
import org.cloudbus.cloudsim.allocator.CloudletCpuAllocatorSimple;
import org.cloudbus.cloudsim.allocator.CloudletIoAllocatorSimple;
import org.cloudbus.cloudsim.allocator.CloudletRamAllocatorSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;
import org.cloudbus.cloudsim.power.SimPowerDatacenter;
import org.cloudbus.cloudsim.power.SimPowerHost;
import org.cloudbus.cloudsim.power.SimPowerVm;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelCubic;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.SimIoProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.SimBwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.SimRamProvisionerSimple;

public class SimpleMain {

	private final static double SCHEDULING_INTERVAL = 5; // 能耗仿真间隔
	private static final int NUM_VM = 3; // 虚拟机的数目
	private static final int NUM_CLOUDLET = 60; // 云任务的总数

	public static void main(String[] args) throws IOException,
			URISyntaxException {

		Log.printLine("Starting SimpleMain...");

		try {

			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);

			Helper.init(1);
			double schedulingInterval = SCHEDULING_INTERVAL;
			SimPowerDatacenter datacenter0 = createDatacenter("Datacenter_0",
					schedulingInterval, NUM_VM); // 创建SimPowerDatacenter
			SimDatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			List<SimPowerVm> vmlist = new ArrayList<SimPowerVm>();
			vmlist.addAll(createVMs(brokerId, NUM_VM)); // 创建SimPowerVm

			broker.submitVmList(vmlist);

			List<SimCloudlet> cloudletList = new ArrayList<SimCloudlet>();
			cloudletList.addAll(createCloudlets(brokerId, NUM_CLOUDLET)); // 创建SimCloudlet

			broker.submitCloudletList(cloudletList);

			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);
			Log.printLine("Total Power: " + datacenter0.getPower() + " W*sec");
			Log.printLine("SimpleMain finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	private static SimPowerDatacenter createDatacenter(String name,
													   double schedulingInterval, int vmNumber) {

		List<SimPowerHost> hostList = new ArrayList<SimPowerHost>();
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1000;
		for (int i = 0; i < vmNumber; i++) {
			peList.add(new Pe(i, new PeProvisionerSimple(mips)));
		}

		int hostId = 0;
		int ram = (vmNumber + 1) * 512; // host memory (MB)
		long storage = 1000000; // host storage
		long bw = (vmNumber + 1) * 10000;
		long io = (vmNumber + 1) * 10000;
		double staticPowerPercent = 0.01;
		PowerModel powerModelCpu = new PowerModelLinear(1000,
				staticPowerPercent);
		PowerModel powerModelRam = new PowerModelCubic(100, staticPowerPercent);
		PowerModel powerModelIo = new PowerModelCubic(1000, staticPowerPercent);
		PowerModel powerModelBw = new PowerModelCubic(100, staticPowerPercent);

		hostList.add(new SimPowerHost(hostId, new SimRamProvisionerSimple(ram),
				new SimBwProvisionerSimple(bw), new SimIoProvisionerSimple(io),
				storage, peList, new VmSchedulerTimeSharedOverSubscription(
				peList), powerModelCpu, powerModelRam, powerModelIo,
				powerModelBw)); // 创建SimPowerHost

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

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		SimPowerDatacenter datacenter = null;
		try {
			datacenter = new SimPowerDatacenter(name, characteristics,
					new PowerVmAllocationPolicySimple(hostList), storageList,
					schedulingInterval);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	private static SimDatacenterBroker createBroker() {
		SimDatacenterBroker broker = null;
		try {
			broker = new SimDatacenterBroker("Broker", new CloudletAssignmentPolicyBalance());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	private static List<SimPowerVm> createVMs(int userId, int vms) {// 创建虚拟机
		List<SimPowerVm> list = new ArrayList<SimPowerVm>();

		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		int mips = 1000;// 250;
		long bw = 10000;
		long io = 10000;
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name
		SimPowerVm vm = null;

		for (int i = 0; i < vms; i++) {
			vm = new SimPowerVm(i, userId, mips, pesNumber, ram, io, bw, size,
					1, vmm, new SimCloudletSchedulerDynamicWorkload(
					mips, // SimCloudletSchedulerDynamicWorkload
					pesNumber, new CloudletCpuAllocatorSimple(mips),
					new CloudletRamAllocatorSimple(ram), // CloudletRamAllocatorSimple
					new CloudletIoAllocatorSimple(io), // CloudletIoAllocatorSimple
					new CloudletBwAllocatorSimple(bw)), 20); // CloudletBwAllocatorSimple
			list.add(vm);
		}

		return list;
	}

	public static List<SimCloudlet> createCloudlets(int userId, int cloudlets)
			throws URISyntaxException, NumberFormatException, IOException { // 生成云任务列表
		List<SimCloudlet> list = new ArrayList<SimCloudlet>();

		long length = 60000;
		long fileSize = 0;
		long outputSize = 0;
		int pesNumber = 1;
		long mips = 600;
		int ram = 256;
		long io = 3000;
		long bw = 6000;
		double interval = 100;
		boolean aware = true;
		String inputFolder = Helper.class.getClassLoader()
				.getResource("utilization").toURI().getPath();
		// Log.printLine("inputFolder: " + inputFolder);
		File[] files = new File(inputFolder).listFiles();
		UtilizationModel utilizationModelCpu = new UtilizationModelByFile(files[0].getAbsolutePath(),interval,aware);
		UtilizationModel utilizationModelRam = new UtilizationModelFull();
		UtilizationModel utilizationModelIo = new UtilizationModelFull();
		UtilizationModel utilizationModelBw = new UtilizationModelFull();

		SimCloudlet cloudlet = null;

		for (int i = 0; i < cloudlets; i++) {
			cloudlet = new SimCloudlet(i, length, pesNumber, fileSize,
					outputSize, mips/(i%4+1), ram/(i%2+1), io/(i%3+1), 0, utilizationModelCpu,//outputSize, mips/(i%4+1), ram/(i%2+1), io/(i%3+1), 0, utilizationModelCpu, // (i+1)
					utilizationModelRam, utilizationModelIo, utilizationModelBw);
			cloudlet.setUserId(userId);
			list.add(cloudlet);
		}

		return list;
	}

	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;
		int[] missId= new int[NUM_CLOUDLET];
		for(int i=0; i< NUM_CLOUDLET; i++)
			missId[i] = 1;
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet" + indent + "STATUS" + indent
				+ "Center" + indent + "Vm" + indent + "Time"
				+ indent + "StartTime" + indent + "FinishTime" + indent
				+ " Mips" + indent + "Ram" + indent + "Io");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			missId[cloudlet.getCloudletId()] = -1;
			Log.print(String.format("%3d",cloudlet.getCloudletId()) + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent +"   " + cloudlet.getResourceId()
						+ indent + "  " + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ String.format("%6s",dft.format(cloudlet.getExecStartTime()))
						+ indent
						+ String.format("%8s",dft.format(cloudlet.getFinishTime())) + indent
						+ String.format("%5d",(int)((SimCloudlet) cloudlet).getMaxMips())
						+ indent
						+ String.format("%5d",((SimCloudlet) cloudlet).getRam())
						+ indent + indent + ((SimCloudlet) cloudlet).getIo());
			}
		}

		Log.printLine("Total Received Cloudlets:"+ list.size());
		Log.print("Miss CLoudlet:");
		for(int i=0; i< NUM_CLOUDLET; i++){
			if(missId[i]!=-1)
				Log.print(i+" ");
		}
	}

}