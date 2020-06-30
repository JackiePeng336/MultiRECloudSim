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
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.SimCloudlet;
import org.cloudbus.cloudsim.SimDatacenterBroker;
import org.cloudbus.cloudsim.SimProgressCloudlet;

import org.cloudbus.cloudsim.SimProgressCloudletSchedulerDynamicWorkloadReservation;
import org.cloudbus.cloudsim.Storage;

import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.allocator.CloudletBwAllocatorSimple;
import org.cloudbus.cloudsim.allocator.CloudletCpuAllocatorReservation;
import org.cloudbus.cloudsim.allocator.CloudletCpuAllocatorSimple;
import org.cloudbus.cloudsim.allocator.CloudletIoAllocatorSimple;
import org.cloudbus.cloudsim.allocator.CloudletRamAllocatorSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;
import org.cloudbus.cloudsim.power.SimPowerDatacenter;

import org.cloudbus.cloudsim.power.SimPowerHostReservation;
import org.cloudbus.cloudsim.power.SimPowerVm;
import org.cloudbus.cloudsim.power.model.PowerModelIoSimple;
import org.cloudbus.cloudsim.power.model.PowerModelRamSimple;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelCubic;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerIbmX3550XeonX5675;
import org.cloudbus.cloudsim.provisioners.SimIoProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.SimBwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.SimRamProvisionerSimple;

public class ProgressReservedTypeMain {

	private static final double SCHEDULING_INTERVAL = 1; // 能耗仿真间隔
	private static final int NUM_HOST = 1; // 主机数目
	private static final int NUM_VM = 2; // 虚拟机的数目
	private static final int NUM_CLOUDLET = 2; // 云任务的总数
	private static final int INTENSIVE_TYPE = 0;
	private static final int CPU = 0;
	private static final int RAM = 1;
	private static final int IO = 2;
	private static final boolean DYNAMIC_WORKLOAD = false;
	private static final boolean WORDLOAD_AWARE = true;

	private static final int CLOUDLET_ASSIGN_POLICY_SIMPLE = 0;
	private static final int CLOUDLET_ASSIGN_POLICY_RANDOM = 1;
	private static final int CLOUDLET_ASSIGN_POLICY_BALANCE = 2;
	private static final int CLOUDLET_ASSIGN_POLICY = 2; //

	public static void main(String[] args) throws IOException, URISyntaxException {

		// Log.printLine("Starting ProgressReservedMain...");
		long begin = System.currentTimeMillis();
		try {

			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);

			double schedulingInterval = SCHEDULING_INTERVAL;
			SimPowerDatacenter datacenter0 = createDatacenter("Datacenter_0", schedulingInterval, NUM_HOST, NUM_VM); // 创建SimPowerDatacenter
			SimDatacenterBroker broker = Helper.createBroker(CLOUDLET_ASSIGN_POLICY);
			int brokerId = broker.getId();

			List<SimPowerVm> vmlist = new ArrayList<SimPowerVm>();
			vmlist.addAll(createVMs(brokerId, NUM_HOST, NUM_VM)); // 创建SimPowerVm

			broker.submitVmList(vmlist);

			List<SimProgressCloudlet> cloudletList = new ArrayList<SimProgressCloudlet>();
			cloudletList.addAll(Helper.createCloudletsByType(brokerId, NUM_CLOUDLET, INTENSIVE_TYPE, DYNAMIC_WORKLOAD,
					WORDLOAD_AWARE)); // 创建SimProgressCloudlet

			broker.submitCloudletList(cloudletList);
			Helper.init(NUM_HOST);
			// Log.setDisabled(true);
			double lastClock = CloudSim.startSimulation();

			CloudSim.stopSimulation();

			List<SimCloudlet> newList = broker.getCloudletReceivedList();
			Helper.printCloudletList(newList, NUM_CLOUDLET);
			String file = "reserved_" + INTENSIVE_TYPE + ".txt";
			Helper.outputCloudletHistory(file, newList);
			Helper.outputHostUtilizationHistory("reserved_utilization_" + INTENSIVE_TYPE + ".txt");
			printArguments(broker);
			Helper.printResults((PowerDatacenter) datacenter0, vmlist, lastClock, "ProgressReservedMain");
			// Log.printLine("Total Power: " + datacenter0.getPower() + "
			// W*sec");
			// Log.printLine("ProgressReservedMain finished!");
			printStatistical();

			Log.printLine("Total Run Time:" + (System.currentTimeMillis() - begin));
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	private static SimPowerDatacenter createDatacenter(String name, double schedulingInterval, int hostNumber,
													   int vmNumberPerHost) {

		List<SimPowerHostReservation> hostList = new ArrayList<SimPowerHostReservation>();
		int mips = 3067;
		int ram = 16 * 1024; // host memory (MB)
		long storage = 1000000; // host storage
		long bw = (vmNumberPerHost) * 10000;
		long io = 500;

		double staticPowerPercent = 0.01;
		PowerModel powerModelCpu = new PowerModelSpecPowerIbmX3550XeonX5675();
		PowerModel powerModelRam = new PowerModelRamSimple(ram);
		PowerModel powerModelIo = new PowerModelIoSimple(io);
		PowerModel powerModelBw = new PowerModelLinear(100, staticPowerPercent);
		for (int i = 0; i < hostNumber; i++) { // 根据主机数目生成主机
			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < vmNumberPerHost; j++) { // 根据虚拟机数目生成核芯
				peList.add(new Pe(j, new PeProvisionerSimple(mips)));
			}
			hostList.add(new SimPowerHostReservation(i, new SimRamProvisionerSimple(ram),
					new SimBwProvisionerSimple(bw), new SimIoProvisionerSimple(io), storage, peList,
					new VmSchedulerTimeSharedOverSubscription(peList), powerModelCpu, powerModelRam, powerModelIo,
					powerModelBw));
		}

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

	private static List<SimPowerVm> createVMs(int userId, int hostNumber, int vmNumberPerHost) {// 创建虚拟机
		List<SimPowerVm> list = new ArrayList<SimPowerVm>();

		long size = 10000; // image size (MB)
		int ram = 1024; // vm memory (MB)
		int mips = 3067;// 250;
		long bw = 10000;
		long io = (long) (500 / vmNumberPerHost);
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name
		SimPowerVm vm = null;

		for (int i = 0; i < hostNumber * vmNumberPerHost; i++) {
			vm = new SimPowerVm(i, userId, mips, pesNumber, ram, io, bw, size, 1, vmm,
					new SimProgressCloudletSchedulerDynamicWorkloadReservation(mips, pesNumber,
							new CloudletCpuAllocatorReservation(mips), new CloudletRamAllocatorSimple(ram), //
							new CloudletIoAllocatorSimple(io), //
							new CloudletBwAllocatorSimple(bw)),
					5); //
			list.add(vm);
		}

		return list;
	}

	private static void printStatistical() {
		Log.printLine();
		Log.printLine(String.format("Average Sim Time: %.2f sec", getAverage(Helper.simTime)));
		Log.printLine(String.format("Average Power: %.2f Wh", getAverage(Helper.simPower)));
		Log.printLine(String.format("Average Waiting Time: %.2f sec", getAverage(Helper.aveWait)));
		Log.printLine(String.format("Average Running Time: %.2f sec", getAverage(Helper.aveRun)));
		Log.printLine(String.format("Average Sla Per Host: %.2f%%", getAverage(Helper.simSlaPerHost) * 100));
		Log.printLine(String.format("Average Overall Sla: %.2f%%", getAverage(Helper.simOverallSla) * 100));
		Log.printLine(String.format("Average average Sla: %.2f%%", getAverage(Helper.simAverageSla) * 100));
	}

	private static double getAverage(List<Double> list) {
		double average = 0.0;
		for (Double one : list)
			average += one;
		return average / list.size();
	}

	private static void printArguments(SimDatacenterBroker broker) {
		Log.printLine("DYNAMIC_WORKLOAD:" + DYNAMIC_WORKLOAD);
		Log.printLine("WORDLOAD_AWARE:" + WORDLOAD_AWARE);
		Log.printLine("CLOUDLET_ASSIGN_POLICY:" + broker.getCloudletAssignPolicy().getClass().getName());
	}

}