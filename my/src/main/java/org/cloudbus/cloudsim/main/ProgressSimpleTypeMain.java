package org.cloudbus.cloudsim.main;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.SimCloudlet;
import org.cloudbus.cloudsim.SimDatacenterBroker;
import org.cloudbus.cloudsim.SimProgressCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.SimPowerDatacenter;
import org.cloudbus.cloudsim.power.SimPowerVm;

public class ProgressSimpleTypeMain {

	private static final double SCHEDULING_INTERVAL = 1; // 能耗仿真间隔
	private static final int NUM_HOST = 50; // 主机数目
	private static final int NUM_VM = 6; // 虚拟机的数目
	private static final int NUM_CLOUDLET = 10; // 云任务的总数
	private static final int LOOP = 1;
	private static final int INTENSIVE_TYPE = 0;
	private static final int CPU = 0;
	private static final int RAM = 1;
	private static final int IO = 2;
	private static final boolean DYNAMIC_WORKLOAD = false;
	private static final boolean WORDLOAD_AWARE = true;

	private static final String VMSCHEDULER="time";

	private static final int CLOUDLET_ASSIGN_POLICY_SIMPLE = 0;
	private static final int CLOUDLET_ASSIGN_POLICY_RANDOM = 1;
	private static final int CLOUDLET_ASSIGN_POLICY_BALANCE = 2;
	private static final int CLOUDLET_ASSIGN_POLICY = 2; //

	private static final int CPU_ALLOCATOR_SIMPLE = 0; //
	private static final int CPU_ALLOCATOR_MAXMIN = 1; //
	private static final int CPU_ALLOCATOR = 1; //

	public static void main(String[] args) throws IOException,
			URISyntaxException {

		// Log.printLine("Starting ProgressSimpleMain...");
		long begin = System.currentTimeMillis();
		try {
			for (int i = 0; i < LOOP; i++) {
				int num_user = 1; // number of cloud users
				Calendar calendar = Calendar.getInstance();
				boolean trace_flag = false; // mean trace events

				CloudSim.init(num_user, calendar, trace_flag);

				double schedulingInterval = SCHEDULING_INTERVAL;
				SimPowerDatacenter datacenter0 = Helper.createDatacenter(
						"Datacenter0", schedulingInterval, NUM_HOST, NUM_VM, 0, VMSCHEDULER); // 创建SimPowerDatacenter
				SimDatacenterBroker broker = Helper
						.createBroker(CLOUDLET_ASSIGN_POLICY);
				int brokerId = broker.getId();

				List<SimPowerVm> vmlist = new ArrayList<SimPowerVm>();
				vmlist.addAll(Helper.createVMs(brokerId, NUM_HOST, NUM_VM,
						CPU_ALLOCATOR)); // 创建SimPowerVm

				broker.submitVmList(vmlist);

				List<SimProgressCloudlet> cloudletList = new ArrayList<SimProgressCloudlet>();
				cloudletList.addAll(Helper.createCloudletsByType(brokerId,
						NUM_CLOUDLET, INTENSIVE_TYPE, DYNAMIC_WORKLOAD,
						WORDLOAD_AWARE)); // 创建SimProgressCloudlet

				broker.submitCloudletList(cloudletList);
				Helper.init(NUM_HOST);
				Log.setDisabled(true);
				double lastClock = CloudSim.startSimulation();

				CloudSim.stopSimulation();

				List<SimCloudlet> newList = broker.getCloudletReceivedList();
				String file = "simple_" + INTENSIVE_TYPE + ".txt";
				Helper.outputCloudletHistory(file, newList);
				Helper.outputHostUtilizationHistory("simple_utilization_"
						+ INTENSIVE_TYPE + ".txt");
				Helper.printCloudletList(newList, NUM_CLOUDLET);
				printArguments(broker);
				Helper.printResults((PowerDatacenter) datacenter0, vmlist,
						lastClock, "ProgressSimpleTypeMain");

				// Log.printLine("Total Power: " + datacenter0.getPower()
				// + " W*sec");
				// Log.printLine("ProgressSimpleMain finished!");
			}

			printStatistical();
			Log.printLine("Total Run Time:"
					+ (System.currentTimeMillis() - begin));
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	private static void printStatistical() {
		Log.printLine();
		Log.printLine("Loop Time: " + LOOP);
		Log.printLine(String.format("Average Sim Time: %.2f sec",
				getAverage(Helper.simTime)));
		Log.printLine(String.format("Average Power: %.2f Wh",
				getAverage(Helper.simPower)));
		Log.printLine(String.format("Average Host Run Time: %.2f sec",
				getAverage(Helper.hostTime)));
		Log.printLine(String.format("Average Cloudlet Waiting Time: %.2f sec",
				getAverage(Helper.aveWait)));
		Log.printLine(String.format("Average Cloudlet Running Time: %.2f sec",
				getAverage(Helper.aveRun)));
		Log.printLine(String.format("Average Sla Per Host: %.2f%%",
				getAverage(Helper.simSlaPerHost) * 100));
		Log.printLine(String.format("Average Overall Sla: %.2f%%",
				getAverage(Helper.simOverallSla) * 100));
		Log.printLine(String.format("Average average Sla: %.2f%%",
				getAverage(Helper.simAverageSla) * 100));
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
		Log.printLine("CLOUDLET_ASSIGN_POLICY:"
				+ broker.getCloudletAssignPolicy().getClass().getName());
		Log.print("CpuAllocator:");
		if (CPU_ALLOCATOR == 0)
			Log.print("Simple");
		else if (CPU_ALLOCATOR == 1)
			Log.print("MaxMin");
	}

}