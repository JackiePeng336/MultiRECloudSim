package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.lists.PeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimVmSchedulerTimeSharedOverLimit extends SimVmSchedulerTimeSharedOverSubscription{
//	private double availableMips;
//	private double TotalMips;
	private double reserveMips;
	private static double scale = 0.01;

	public SimVmSchedulerTimeSharedOverLimit(List<? extends Pe> pelist) {
		super(pelist);
		this.reserveMips = ((double) PeList.getTotalMips(getPeList()))*(1-scale);
//		setAvailableMips(TotalMips);
//
//		System.out.println("=========================================");
//		System.out.println(String.format("the totalMips: %f mips", getAvailableMips()));
//		System.out.println("=========================================");
//		System.out.println("=========================================");
//		System.out.println(String.format("the reserveMips: %f mips", reserveMips));
//		System.out.println("=========================================");
	}

	@Override
	protected boolean allocatePesForVm(String vmUid, List<Double> mipsShareRequested) {
		double totalRequestedMips = 0;

		// if the requested mips is bigger than the capacity of a single PE, we cap
		// the request to the PE's capacity
		List<Double> mipsShareRequestedCapped = new ArrayList<Double>();
		double peMips = getPeCapacity();
		for (Double mips : mipsShareRequested) {
			if (mips > peMips) {
				mipsShareRequestedCapped.add(peMips);
				totalRequestedMips += peMips;
			} else {
				mipsShareRequestedCapped.add(mips);
				totalRequestedMips += mips;
			}
		}

		getMipsMapRequested().put(vmUid, mipsShareRequested);
		setPesInUse(getPesInUse() + mipsShareRequested.size());

		if (getVmsMigratingIn().contains(vmUid)) {
			// the destination host only experience 10% of the migrating VM's MIPS
			totalRequestedMips *= 0.1;
		}


		if (getAvailableMips() - totalRequestedMips >= reserveMips) {
			List<Double> mipsShareAllocated = new ArrayList<Double>();
			for (Double mipsRequested : mipsShareRequestedCapped) {
				if (getVmsMigratingOut().contains(vmUid)) {
					// performance degradation due to migration = 10% MIPS
					mipsRequested *= 0.9;
				} else if (getVmsMigratingIn().contains(vmUid)) {
					// the destination host only experience 10% of the migrating VM's MIPS
					mipsRequested *= 0.1;
				}
				mipsShareAllocated.add(mipsRequested);
			}

			getMipsMap().put(vmUid, mipsShareAllocated);
			setAvailableMips(getAvailableMips() - totalRequestedMips);
//			System.out.println(String.format("getAvailableMips: %f", getAvailableMips()));
		} else {
			redistributeMipsDueToOverSubscription();
		}

		return true;
	}


	/**
	 * This method recalculates distribution of MIPs among VMs considering eventual shortage of MIPS
	 * compared to the amount requested by VMs.
	 */
	@Override
	protected void redistributeMipsDueToOverSubscription() {
		System.out.println("Over Subscription");
		// First, we calculate the scaling factor - the MIPS allocation for all VMs will be scaled
		// proportionally
		double totalRequiredMipsByAllVms = 0;

		Map<String, List<Double>> mipsMapCapped = new HashMap<String, List<Double>>();
		for (Map.Entry<String, List<Double>> entry : getMipsMapRequested().entrySet()) {

			double requiredMipsByThisVm = 0.0;
			String vmId = entry.getKey();
			List<Double> mipsShareRequested = entry.getValue();
			List<Double> mipsShareRequestedCapped = new ArrayList<Double>();
			double peMips = getPeCapacity();
			for (Double mips : mipsShareRequested) {
				if (mips > peMips) {
					mipsShareRequestedCapped.add(peMips);
					requiredMipsByThisVm += peMips;
				} else {
					mipsShareRequestedCapped.add(mips);
					requiredMipsByThisVm += mips;
				}
			}

			mipsMapCapped.put(vmId, mipsShareRequestedCapped);

			if (getVmsMigratingIn().contains(entry.getKey())) {
				// the destination host only experience 10% of the migrating VM's MIPS
				requiredMipsByThisVm *= 0.1;
			}
			totalRequiredMipsByAllVms += requiredMipsByThisVm;
		}

		double totalAvailableMips = PeList.getTotalMips(getPeList())*scale;
//		System.out.println("=====================================================");
//		System.out.println(String.format("totalAvailableMips: %f", totalAvailableMips));
//		System.out.println("=====================================================");

		double scalingFactor = totalAvailableMips / totalRequiredMipsByAllVms;

		// Clear the old MIPS allocation
		getMipsMap().clear();

		// Update the actual MIPS allocated to the VMs
		for (Map.Entry<String, List<Double>> entry : mipsMapCapped.entrySet()) {
			String vmUid = entry.getKey();
			List<Double> requestedMips = entry.getValue();

			List<Double> updatedMipsAllocation = new ArrayList<Double>();
			for (Double mips : requestedMips) {
				if (getVmsMigratingOut().contains(vmUid)) {
					// the original amount is scaled
					mips *= scalingFactor;
					// performance degradation due to migration = 10% MIPS
					mips *= 0.9;
				} else if (getVmsMigratingIn().contains(vmUid)) {
					// the destination host only experiences 10% of the migrating VM's MIPS
					mips *= 0.1;
					// the final 10% of the requested MIPS are scaled
					mips *= scalingFactor;
				} else {
					mips *= scalingFactor;
				}

				updatedMipsAllocation.add(Math.floor(mips));
			}

			// add in the new map
			getMipsMap().put(vmUid, updatedMipsAllocation);

		}

		// As the host is oversubscribed, there no more available MIPS
		setAvailableMips(0);
	}

	public static String getSourcePercent()
	{
		double percent = scale*100;
		return "The sources percent is "+percent+"%";
	}


}
