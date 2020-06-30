package org.cloudbus.cloudsim.power;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.main.Helper;

public class SimPowerDatacenter extends PowerDatacenter {

	public SimPowerDatacenter(String name, DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval)
					throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected void processVmMigrate(SimEvent ev, boolean ack) {
		updateCloudetProcessingWithoutSchedulingFutureEvents();
		Object tmp = ev.getData();
		if (!(tmp instanceof Map<?, ?>)) {
			throw new ClassCastException("The data object must be Map<String, Object>");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> migrate = (HashMap<String, Object>) tmp;

		Vm vm = (Vm) migrate.get("vm");
		Host host = (Host) migrate.get("host");

		getVmAllocationPolicy().deallocateHostForVm(vm);

		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, host);
		if (!result) {
			Log.printLine("[Datacenter.processVmMigrate] VM allocation to the destination host failed");
			System.out.println("vm request ram = "+vm.getCurrentRequestedRam()
					+" Host ram avail = "+host.getRamProvisioner().getAvailableRam());
			System.exit(0);
		}
		host.removeMigratingInVm(vm);
		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(ev.getSource(), CloudSimTags.VM_CREATE_ACK, data);
		}

		Log.formatLine(
				"%.2f: Migration of VM #%d to Host #%d is completed",
				CloudSim.clock(),
				vm.getId(),
				host.getId());
		vm.setInMigration(false);

		SimEvent event = CloudSim.findFirstDeferred(getId(), new PredicateType(CloudSimTags.VM_MIGRATE));
		if (event == null || event.eventTime() > CloudSim.clock()) {
			updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
	}

	@Override
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;

		Helper.hostUtilizationTime.add(currentTime);
		for (int i = 0; i < this.<SimPowerHost> getHostList().size(); i++) {

			SimPowerHost host = (SimPowerHost) getHostList().get(i);
			double time = host.updateVmsProcessing(currentTime); // inform VMs
																	// to update
																	// processing
			if (time < minTime) {
				minTime = time;
			}

			Helper.hostCpuUtilization.get(i).add(host.getUtilizationOfCpu());
			Helper.hostRamUtilization.get(i).add(host.getUtilizationOfRam());
			Helper.hostIoUtilization.get(i).add(host.getUtilizationOfIO());
			Helper.hostBwUtilization.get(i).add(host.getUtilizationOfBw());

			Log.disable();
			Log.formatLine("%.2f: [Host #%d] CPU utilization is %.2f%%", currentTime, host.getId(),
					host.getUtilizationOfCpu() * 100);
			Log.formatLine("%.2f: [Host #%d] Ram utilization is %.2f%%", currentTime, host.getId(),
					host.getUtilizationOfRam() * 100);
			Log.formatLine("%.2f: [Host #%d] IO utilization is %.2f%%", currentTime, host.getId(),
					host.getUtilizationOfIO() * 100);
			Log.formatLine("%.2f: [Host #%d] Bw utilization is %.2f%%", currentTime, host.getId(),
					host.getUtilizationOfBw() * 100);
			Log.enable();
		}

		Log.disable();
		if (timeDiff > 0) {
			Log.formatLine("\nEnergy consumption for the last time frame from %.2f to %.2f:", getLastProcessTime(),
					currentTime);

			for (int i = 0; i < this.<SimPowerHost> getHostList().size(); i++) {
				SimPowerHost host = (SimPowerHost) getHostList().get(i);
				double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu(); // 前一时刻CPU利用率
				double utilizationOfCpu = host.getUtilizationOfCpu(); // 当前CPU利用率
				double previousUtilizationOfRam = host.getPreviousUtilizationRam(); // 前一时刻内存利用率
				double utilizationOfRam = host.getUtilizationRam(); // 当前内存利用率
				double previousUtilizationOfIO = host.getPreviousUtilizationIO(); // 前一时刻IO利用率
				double utilizationOfIO = host.getUtilizationIO(); // 当前IO利用率
				double previousUtilizationOfBw = host.getPreviousUtilizationBw(); // 前一时刻带宽利用率
				double utilizationOfBw = host.getUtilizationBw(); // 当前带宽利用率

				Helper.hostCpuPower.get(i).add(host.getEnergyLinearInterpolation( // 线性拟合CPU能耗
						previousUtilizationOfCpu, utilizationOfCpu, timeDiff, host.getPowerModelCPU()));
				Helper.hostRamPower.get(i).add(host.getEnergyLinearInterpolation( // 线性拟合内存能耗
						previousUtilizationOfRam, utilizationOfRam, timeDiff, host.getPowerModelRam()));
				Helper.hostIoPower.get(i).add(host.getEnergyLinearInterpolation( // 线性拟合IO能耗
						previousUtilizationOfIO, utilizationOfIO, timeDiff, host.getPowerModelIO()));
				Helper.hostBwPower.get(i).add(host.getEnergyLinearInterpolation( // 线性拟合带宽能耗
						previousUtilizationOfBw, utilizationOfBw, timeDiff, host.getPowerModelBw()));
				int end = Helper.hostCpuPower.get(i).size() - 1;

				double timeFrameHostEnergy = (double) Helper.hostCpuPower.get(i).get(end)
						+ (double) Helper.hostRamPower.get(i).get(end) + (double) Helper.hostIoPower.get(i).get(end)
						+ (double) Helper.hostBwPower.get(i).get(end);
				Log.printLine("Host i=" + i + " end=" + end + " cpu:" + Helper.hostCpuPower.get(i).get(end) + " Ram:"
						+ Helper.hostRamPower.get(i).get(end) + " Io:" + Helper.hostIoPower.get(i).get(end) + " Bw:"
						+ Helper.hostBwPower.get(i).get(end) + " timeFrameHostEnergy=" + timeFrameHostEnergy);
				timeFrameDatacenterEnergy += timeFrameHostEnergy;
				// Log.printLine("Host i="+i+" end="+end+"
				// cpu:"+Helper.hostCpuPower.get(i).get(end));

				Log.printLine();
				Log.formatLine("%.2f: [Host #%d] CPU utilization at %.2f was %.2f%%, now is %.2f%%", currentTime,
						host.getId(), getLastProcessTime(), previousUtilizationOfCpu * 100, utilizationOfCpu * 100);
				Log.formatLine("%.2f: [Host #%d] Ram utilization at %.2f was %.2f%%, now is %.2f%%", currentTime,
						host.getId(), getLastProcessTime(), previousUtilizationOfRam * 100, utilizationOfRam * 100);
				Log.formatLine("%.2f: [Host #%d] IO utilization at %.2f was %.2f%%, now is %.2f%%", currentTime,
						host.getId(), getLastProcessTime(), previousUtilizationOfIO * 100, utilizationOfIO * 100);
				Log.formatLine("%.2f: [Host #%d] Bw utilization at %.2f was %.2f%%, now is %.2f%%", currentTime,
						host.getId(), getLastProcessTime(), previousUtilizationOfBw * 100, utilizationOfBw * 100);
				Log.formatLine("%.2f: [Host #%d] energy is %.2f W*sec", currentTime, host.getId(), timeFrameHostEnergy);
			}
			Helper.power.add(timeFrameDatacenterEnergy);
			Log.formatLine("\n%.2f: Data center's energy is %.2f W*sec\n", currentTime, timeFrameDatacenterEnergy);
		}
		Log.enable();

		setPower(getPower() + timeFrameDatacenterEnergy);

		checkCloudletCompletion();

		/** Remove completed VMs **/
		for (SimPowerHost host : this.<SimPowerHost> getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				Helper.vmToHost.put(vm.getId(), host.getId());
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
				Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
			}
			if (host.getCompletedVms().size() == host.getVmList().size())
				Helper.hostTime.add(currentTime);
		}

		setLastProcessTime(currentTime);
		return minTime;
	}
}
