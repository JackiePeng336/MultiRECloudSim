package org.cloudbus.cloudsim.power.model;

import java.text.DecimalFormat;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.power.models.PowerModel;

public class PowerModelRamSimple implements PowerModel{

	private int ram;
	private double maxPower;
	private double constant;

	public PowerModelRamSimple(int ram) {
		super();
		constant = 1/1024;
		setMaxPower(ram /1024);
		DecimalFormat dft = new DecimalFormat("###.##");
		//Log.printLine("ram="+ram+" constant"+dft.format(constant) +" PowerModelRamSimple:"+dft.format(getMaxPower())+" p="+dft.format(getPower(0.0234475)));
		setRam(ram);
	}

	@Override
	public double getPower(double utilization) throws IllegalArgumentException {
		if (utilization < -0.1 || utilization > 1.1) {
			throw new IllegalArgumentException("Utilization value must be between 0 and 1");
		}
		if (utilization == 0) {
			return 0;
		}
		return getMaxPower() * utilization;// * 100;
	}

	public int getRam() {
		return ram;
	}


	public void setRam(int ram) {
		this.ram = ram;
	}

	public double getMaxPower() {
		return maxPower;
	}

	public void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	
}
