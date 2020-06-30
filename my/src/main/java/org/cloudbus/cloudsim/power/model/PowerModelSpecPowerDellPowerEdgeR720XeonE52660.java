package org.cloudbus.cloudsim.power.model;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

/**
 * The power model of an Dell server PowerEdgeR720 (2U [Xeon E5-2660 8 core, 2.20 GHz],24GB).
 * http://www.spec.org/power_ssj2008/results/res2012q4/power_ssj2008-20121030-00569.html
 */

public class PowerModelSpecPowerDellPowerEdgeR720XeonE52660 
			 extends PowerModelSpecPower{
	
	/** The power. */
	private final double[] power = {53.8, 77.1, 87.4, 98.4, 112, 122, 140, 160, 177, 199, 230};
	
	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int)
	 */
	
	@Override
	protected double getPowerData(int index) {
		
		return power[index];
	}

}
