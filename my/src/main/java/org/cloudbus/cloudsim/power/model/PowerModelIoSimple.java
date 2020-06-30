package org.cloudbus.cloudsim.power.model;

import org.cloudbus.cloudsim.power.models.PowerModel;

public class PowerModelIoSimple implements PowerModel{

	private long io;
	private double maxPower;
	private double constant;

	public PowerModelIoSimple(long io) {
		super();
		constant = 0.0314573;	//3.00**1024*1024*10^-8
		setMaxPower(io * constant);
		setIo(io);
	}

	@Override
	public double getPower(double utilization) throws IllegalArgumentException {
		if (utilization < 0) {
			return 0;
		}
		if (utilization < 0 || utilization > 1) {
			throw new IllegalArgumentException("Utilization value must be between 0 and 1");
		}
		if (utilization == 0) {
			return 0;
		}
		return getMaxPower() * utilization;// * 100;
	}



	public long getIo() {
		return io;
	}

	public void setIo(long io) {
		this.io = io;
	}

	public double getMaxPower() {
		return maxPower;
	}

	public void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	
}
