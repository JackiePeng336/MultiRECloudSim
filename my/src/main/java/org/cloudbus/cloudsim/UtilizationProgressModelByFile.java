package org.cloudbus.cloudsim;

import java.io.IOException;

public class UtilizationProgressModelByFile extends UtilizationModelByFile {

	public UtilizationProgressModelByFile(String filePath,
			double schedulingInterval, boolean aware)
			throws NumberFormatException, IOException {
		super(filePath, schedulingInterval, aware);
	}

	@Override
	public double getUtilization(double progress) { // 根据进度返回利用率
		int n = getData().size() - 1;
		if (n * progress % 1 == 0.0) {
			return getData().get((int) (n * progress));
		}

		int progress1 = (int) Math.floor(n * progress);
		int progress2 = (int) Math.ceil(n * progress);
		if (progress2 > n) {
			Log.printLine("UtilizationModelByFile:Utilization exceeds");
			System.exit(0);
		}
		double utilization1 = getData().get(progress1);
		double utilization2 = getData().get(progress2);
		double delta = (utilization2 - utilization1) / (progress2 - progress1);
		double utilization = utilization1 + delta * (n * progress - progress1);
		return utilization;
	}

}
