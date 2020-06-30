package org.cloudbus.cloudsim.power.model;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

public class PowerModelSpecPowerDellPowerEdgeC6320XeonE52699 extends
		PowerModelSpecPower {

	/** The power. */
	private final double[] power = { 210, 371, 449, 522, 589, 647, 705, 802, 924, 1071, 1229 };

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int)
	 */
	@Override
	protected double getPowerData(int index) {
		return power[index];
	}

}
