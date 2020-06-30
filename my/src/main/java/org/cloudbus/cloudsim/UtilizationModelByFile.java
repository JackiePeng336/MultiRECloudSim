package org.cloudbus.cloudsim;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UtilizationModelByFile implements UtilizationModel {

	private double schedulingInterval;
	private final List<Double> data;
	private boolean loadAware;
	public UtilizationModelByFile(String filePath, double schedulingInterval,
			boolean loadAware) throws NumberFormatException, IOException { // 读取负载文件数据，存到data
		data = new ArrayList<Double>();
		setSchedulingInterval(schedulingInterval);
		BufferedReader input = new BufferedReader(new FileReader(filePath));
		setLoadAware(loadAware);
		while (input.readLine() != null)
			data.add(Double.valueOf(input.readLine()) / 100.0);
		input.close();
	}

	@Override
	public double getUtilization(double time) { // 根据时间返回利用率，线性拟合
		if (time % getSchedulingInterval() == 0) {
			return data.get((int) time / (int) getSchedulingInterval());
		}
		int time1 = (int) Math.floor(time / getSchedulingInterval());
		int time2 = (int) Math.ceil(time / getSchedulingInterval());
		if (time2 > getSchedulingInterval() * (data.size() - 1)) {
			Log.printLine("UtilizationModelByFile:Utilization exceeds");
			System.exit(0);
		}
		double utilization1 = data.get(time1);
		double utilization2 = data.get(time2);
		double delta = (utilization2 - utilization1)
				/ ((time2 - time1) * getSchedulingInterval());
		double utilization = utilization1 + delta
				* (time - time1 * getSchedulingInterval());
		return utilization;
	}

	public double getSchedulingInterval() {
		return schedulingInterval;
	}

	public void setSchedulingInterval(double schedulingInterval) {
		this.schedulingInterval = schedulingInterval;
	}

	public List<Double> getData() {
		return data;
	}

	public boolean isLoadAware() {
		return loadAware;
	}

	public void setLoadAware(boolean loadAware) {
		this.loadAware = loadAware;
	}

	public double getAverageLoad() {
		if (isLoadAware()) {
			double sum = 0.0;
			for (int i = 0; i < data.size(); i++)
				sum += data.get(i);
			return sum / data.size();
		}
		
		return Collections.max(data);
	}

}
