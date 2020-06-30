package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.SimPowerVm;

public class CloudletAssignmentPolicyGreedyArea extends CloudletAssignmentPolicy {
	protected Map<Integer, List<SimCloudlet>> cpuNeighborCloudletList = new TreeMap<Integer, List<SimCloudlet>>();
	protected List<SimCloudlet> waittingList = new ArrayList<SimCloudlet>();
	protected List<Integer> areaList = new ArrayList<Integer>();
	protected double stopUtilization;
	protected int cpuAreaInterval;
	protected double minMips;
	protected Map<Integer, Double> vmAssignTime = new HashMap<Integer, Double>();

	private final static double TIME_WEIGHT = 0.5;
	private final static double CPU_WEIGHT = 0.3;
	private final static double RAM_WEIGHT = 0.1;
	private final static double IO_WEIGHT = 0.1;
	private final static double BW_WEIGHT = 0.0;

	private final static double NORMAL_TIME = 30;
	private final static double NORMAL_CPU = 900;
	private final static double NORMAL_RAM = 768;
	private final static double NORMAL_IO = 30;
	private final static double NORMAL_BW = 1000;
	private final static double TOLERANCE = 0.1;

	private Map<Integer, Double> vmUsedMips = new HashMap<Integer, Double>();
	private Map<Integer, Integer> vmUsedRam = new HashMap<Integer, Integer>();
	private Map<Integer, Long> vmUsedIo = new HashMap<Integer, Long>();
	private Map<Integer, Long> vmUsedBw = new HashMap<Integer, Long>();
	private Map<Integer, List<SimCloudlet>> currentAssignedCloudletList = new HashMap<Integer, List<SimCloudlet>>();
	// private Map<Integer, List<SimCloudlet>> AssignedResult = new
	// HashMap<Integer, List<SimCloudlet>>();
	private Map<Integer, Integer> cloudletVmMap = new HashMap<Integer, Integer>();
	private List<SimCloudlet> cloudletClockList = new ArrayList<SimCloudlet>();
	private Map<Integer, Double> cloudletFinishedTime = new HashMap<Integer, Double>();
	private double clockInterval;
	private int finished = 0;
	public CloudletAssignmentPolicyGreedyArea(double stopUtilization, int cpuAreaInterval, double clockInterval) {
		super();
		setStopUtilization(stopUtilization);
		setCpuAreaInterval(cpuAreaInterval);
		minMips = 10000;
		setClockInterval(clockInterval);
	}

	private void addCloudletToMap(Map<Integer, List<SimCloudlet>> map, SimCloudlet sc, int vmid) {
		List<SimCloudlet> list = null;
		if (map.get(vmid) == null) {
			list = new ArrayList<SimCloudlet>();
		} else
			list = map.get(vmid);
		list.add(sc);
		map.put(vmid, list);
	}

	private void assignCloudlet(List<SimCloudlet> toPlaced, int vmid, double usedMips, int usedRam, long usedIo,
			long usedBw) {
		/*
		 * List<SimCloudlet> list = new ArrayList<SimCloudlet>(); if
		 * (AssignedResult.get(vmid) != null) list = AssignedResult.get(vmid);
		 * list.addAll(toPlaced); AssignedResult.put(vmid, list);
		 */
		if (toPlaced == null)
			return;
		for (SimCloudlet sc : toPlaced) {
			cloudletVmMap.put(sc.getCloudletId(), vmid);
			//System.out.println("assignCloudlet sc#" + sc.getCloudletId() + " to Vm#" + vmid);
		}

		vmUsedMips.put(vmid, vmUsedMips.get(vmid) + usedMips);
		vmUsedRam.put(vmid, vmUsedRam.get(vmid) + usedRam);
		vmUsedIo.put(vmid, vmUsedIo.get(vmid) + usedIo);
		vmUsedBw.put(vmid, vmUsedBw.get(vmid) + usedBw);

	}

	private List<SimCloudlet> updateCloudlet(double currentTime) {
		List<SimCloudlet> finished = new ArrayList<SimCloudlet>();
		for (SimCloudlet sc : cloudletClockList) {
			if (cloudletFinishedTime.get(sc.getCloudletId()) < currentTime){
				finished.add(sc);
				this.finished++;
			}else
				break;
		}
		cloudletClockList.removeAll(finished);
		for (SimCloudlet sc : finished) {
			//System.out.println(currentTime + ":updateCloudlet Sc#" + sc.getCloudletId());
			int vmid = cloudletVmMap.get(sc.getCloudletId());
			//System.out.println(currentTime + ":updateCloudlet Sc#" + sc.getCloudletId() + " Vm#" + vmid);
			//System.out.println("Mips " + vmUsedMips.get(vmid) + " Ram:" + vmUsedRam.get(vmid) + " Io:"
			//		+ vmUsedIo.get(vmid) + " bw:" + vmUsedBw.get(vmid));
			vmUsedMips.put(vmid, vmUsedMips.get(vmid) - sc.getMips());
			vmUsedRam.put(vmid, vmUsedRam.get(vmid) - sc.getRam());
			vmUsedIo.put(vmid, vmUsedIo.get(vmid) - sc.getIo());
			vmUsedBw.put(vmid, vmUsedBw.get(vmid) - sc.getBw());
			/*if (vmUsedMips.get(vmid) < 0) {
				System.out.println("vmUsedMips.get(vmid) " + vmUsedMips.get(vmid));
				System.exit(1);
			}*/

		}
		return finished;
	}

	protected int insertClockList(SimCloudlet sc, double clock) {
		if (sc == null || cloudletClockList == null || cloudletClockList.contains(sc))
			return -1;
		/*
		 * for (int i = 0; i < cloudletClockList.size(); i++) { int id =
		 * cloudletClockList.get(i).getCloudletId(); System.out.println(
		 * "cloudletClockList Sc#" +id +" t:"+cloudletFinishedTime.get(id)); }
		 */
		for (int i = 0; i < cloudletClockList.size(); i++) {
			// long length = sc.getCloudletLength();
			// long mips = sc.getMips();
			// double time = (double)length / (double)mips + clock;
			// System.out.println("insertClockList Sc#" +
			// cloudletClockList.get(i).getCloudletId() + " time:"+time);
			if (cloudletFinishedTime.get(sc.getCloudletId()) < cloudletFinishedTime
					.get(cloudletClockList.get(i).getCloudletId())) {
				//System.out.println("Sc# "+sc.getCloudletId()+" insertClockList IF:"+cloudletClockList.size());
				cloudletClockList.add(i, sc);
				return i;
			}
		}

		cloudletClockList.add(sc);
		//System.out.println("Sc# "+sc.getCloudletId()+" insertClockList "+cloudletClockList.size());
		return cloudletClockList.size() - 1;
	}

	protected void addCloudletFinishedTime(SimCloudlet sc, double clock) {
		long length = sc.getCloudletLength();
		long mips = sc.getMips();
		double time = (double) length / (double) mips;
		cloudletFinishedTime.put(sc.getCloudletId(), clock + time);
		//System.out.println("addCloudletFinishedTime Sc#" + sc.getCloudletId()
		//+ " time:" + cloudletFinishedTime.get(sc.getCloudletId()));
	}

	@Override
	public int[] assignCloudletsToVm(List<SimCloudlet> cloudletlist, List<Vm> vmlist) {
		waittingList.addAll(cloudletlist);
		Map<Integer, Integer> assign = new HashMap<Integer, Integer>();

		 //for (int i = 0; i < cloudletlist.size(); i++) {
		//	 System.out.println("BeforeSc# " + cloudletlist.get(i).getCloudletId());;
		 //}
		// 任务优先度降序排列
		Collections.sort(waittingList, new Comparator<SimCloudlet>() {
			public int compare(SimCloudlet sc0, SimCloudlet sc1) {
				double priority0 = getCloudletPriroity(sc0);
				double priority1 = getCloudletPriroity(sc1);
				if (priority0 < priority1)
					return 1;
				else if (priority0 == priority1) {
					return 0;
				} else
					return -1;
			}
		});
		//for (int i = 0; i < waittingList.size(); i++) {
		//	 System.out.println("AfterSc# " + waittingList.get(i).getCloudletId());;
		// }
		// Vm Mips降序排列
		Collections.sort(vmlist, new Comparator<Vm>() {
			public int compare(Vm vm0, Vm vm1) {
				if (vm0.getMips() < vm1.getMips())
					return 1;
				else if (vm0.getMips() == vm1.getMips()) {
					return 0;
				} else
					return -1;
			}
		});

		/*
		 * System.out.println("Sort Vm mips:"); for (Vm vm : vmlist) {
		 * System.out.print(vm.getMips() + " "); }
		 */

		for (SimCloudlet sc : waittingList) {
			if (sc.getMips() < minMips)
				minMips = sc.getMips();
		}

		minMips--;
		for (SimCloudlet sc : waittingList) {
			int area = getAreaByMips(sc.getMips());
			List<SimCloudlet> areaCloudletList = null;
			if (cpuNeighborCloudletList.get(area) == null) {
				areaCloudletList = new ArrayList<SimCloudlet>();
				areaCloudletList.add(sc);
				areaList.add(area);
				cpuNeighborCloudletList.put(area, areaCloudletList);
			} else {
				cpuNeighborCloudletList.get(area).add(sc);
			}
		}

		//System.out.println("MiniMips: " + minMips);
		for (Integer area : cpuNeighborCloudletList.keySet()) {
			System.out.println("Area #" + area + " :");
			List<SimCloudlet> list = cpuNeighborCloudletList.get(area);
			for (SimCloudlet sc : list) {
				System.out.println("cloudlet #" + sc.getCloudletId() + " :" + sc.getCloudletLength() / sc.getMips()
						+ " " + sc.getMips() + " " + sc.getRam() + " " + sc.getIo() + " " + sc.getBw() + " "
						+ getCloudletPriroity(sc));
			}
		}

		double clock = 0.0;

		List<SimCloudlet> toRemove = new ArrayList<SimCloudlet>();
		for (int i = 0; i < vmlist.size(); i++) {
			vmUsedMips.put(vmlist.get(i).getId(), 0.0);
			vmUsedRam.put(vmlist.get(i).getId(), 0);
			vmUsedIo.put(vmlist.get(i).getId(), 0L);
			vmUsedBw.put(vmlist.get(i).getId(), 0L);
			assign.put(waittingList.get(i).getCloudletId(), vmlist.get(i).getId());
			//addCloudletFinishedTime(waittingList.get(i), clock);
			//insertClockList(waittingList.get(i), clock);
			toRemove.add(waittingList.get(i));
			int area = getAreaByMips(waittingList.get(i).getMips());
			//System.out.println("initRemove:#" + waittingList.get(i).getCloudletId() + " "
			//		+ cpuNeighborCloudletList.get(area).remove(waittingList.get(i)));
		}

		Collections.sort(areaList, new Comparator<Integer>() {
			public int compare(Integer i0, Integer i1) {
				return i0.compareTo(i1);
			}
		});
		/*System.out.println("areaList:");
		for (int i = 0; i < areaList.size(); i++) {
			System.out.print(areaList.get(i) + " ");
		}*/

		for (int i = 0; i < vmlist.size(); i++) {
			SimPowerVm vm = (SimPowerVm) vmlist.get(i);
			double usedMips = toRemove.get(i).getMips();
			double leftMips = vm.getMips() - usedMips;
			List<SimCloudlet> toPlaced = new ArrayList<SimCloudlet>();
			toPlaced.add(toRemove.get(i));
			Set<Integer> areaEmptyOrSearched = new HashSet<Integer>();
			int leftMipsSize = areaList.size();
			boolean full = false;
			int bestFitArea = 0;
			int usedRam = toRemove.get(i).getRam();
			long usedIo = toRemove.get(i).getIo();
			long usedBw = toRemove.get(i).getBw();
			while ((usedMips < vm.getMips() * (stopUtilization - TOLERANCE)
					&& usedRam < vm.getRam() * (stopUtilization)
					&& usedIo < vm.getIo() * (stopUtilization)
					&& usedBw < vm.getBw() * (stopUtilization))
					&& areaEmptyOrSearched.size() < leftMipsSize) {
				double upperMips = vm.getMips() * (stopUtilization + TOLERANCE) - usedMips;
				int leftArea = getAreaByMips(upperMips);
				List<SimCloudlet> cloudletArea = null;
				//System.out.println("upperMips "+upperMips);
				//System.out.println("Vm#"+vm.getId() + " size()" +
				//areaEmptyOrSearched.size() + " " + leftMipsSize + " leftArea"+ leftArea);

				if (cpuNeighborCloudletList.containsKey(leftArea)) {
					bestFitArea = leftArea;
					leftMipsSize = areaList.indexOf(getAreaByMips(upperMips)) + 1;
				} else {
					for (int j = leftArea - 1; j > 0; j--) {
						if (cpuNeighborCloudletList.containsKey(j)) {
							bestFitArea = j;
							leftMipsSize = areaList.indexOf(j) + 1;
							break;
						}
					}
				}
				cloudletArea = cpuNeighborCloudletList.get(bestFitArea);

				SimCloudlet candidate = null;
				boolean found = false;
				if (cloudletArea != null) {
					for (SimCloudlet sc : cloudletArea) {
						System.out.println("SC#" + sc.getCloudletId() + " Mips:" + sc.getMips() + " Ram:" + sc.getRam()
								+ " Io:" + sc.getIo());
						if (checkResouceConstraint(vm, toPlaced, sc)) {
							candidate = sc;
							toPlaced.add(sc);
							usedMips += sc.getMips();
							leftMips -= sc.getMips();
							usedRam += sc.getRam();
							usedIo += sc.getIo();
							usedBw += sc.getBw();
							assign.put(sc.getCloudletId(), vm.getId());
							cpuNeighborCloudletList.get(bestFitArea).remove(sc);
							// addCloudletFinishedTime(sc, clock);
							// insertClockList(sc, clock);
							// System.out.println("Remove:Sc#" +
							// sc.getCloudletId() + " Vm#" + sc.getVmId() + " "
							// +
							// cpuNeighborCloudletList.get(bestFitArea).remove(sc));//
							// cpuNeighborCloudletList.get(bestFitArea).remove(sc);
							areaEmptyOrSearched.clear();
							leftMipsSize = areaList.indexOf(getAreaByMips(leftMips)) + 1;
							if (leftMipsSize == 0) {
								for (int j = getAreaByMips(leftMips) - 1; j > 0; j--) {
									if (areaList.contains(j)) {
										leftMipsSize = areaList.indexOf(j) + 1;
										break;
									}
								}
							}
							found = true;
							System.out.println("Vm# " + vm.getId() + " found! area:" + bestFitArea + " Cloudlet #"
									+ sc.getCloudletId());
							//System.out.println("usedMips " + usedMips + " usedRam" + usedRam + " usedIo " + usedIo
							//		+ " usedBw" + usedBw);
							//System.out.println("leftMipsSize " + leftMipsSize + " areaEmptyOrSearched "
							//		+ areaEmptyOrSearched.size());
							if (usedMips >= vm.getMips() * (stopUtilization - TOLERANCE)
									|| usedRam >= vm.getRam() * (stopUtilization)
									|| usedIo >= vm.getIo() * (stopUtilization)
									|| usedBw >= vm.getBw() * (stopUtilization))
								full = true;
							break;
						}
					}
				}

				if (cloudletArea == null || !found) {
					areaEmptyOrSearched.add(bestFitArea);
					int bestFitIndex = areaList.indexOf(bestFitArea);
					//System.out.println("Vm# " + vm.getId() + " " + found + " BestFitIndex: " + bestFitIndex);
					int[] randomArea = randomArray(0, bestFitIndex - 1, bestFitIndex);
					//System.out.print("bestFitIndex " + bestFitIndex + "  " + bestFitArea + "\nRandomArea :");
					if (randomArea != null) {
						/*for (int r : randomArea) {
							System.out.print(r + " ");
						}*/
						// System.out.println("randomArea Length " +
						// randomArea.length);
						for (int areaIndex : randomArea) {
							cloudletArea = cpuNeighborCloudletList.get(areaList.get(areaIndex));
							if (cloudletArea != null) {
								// System.out.println("Random Round " +
								// areaIndex + " Size : " + cloudletArea.size()
								// + " leftMips: " + leftMips);
								for (SimCloudlet sc : cloudletArea) {
									//System.out.println("!SC#" + sc.getCloudletId() + " Mips:" + sc.getMips() + " Ram:"
										//	+ sc.getRam() + " Io:" + sc.getIo());
									if (checkResouceConstraint(vm, toPlaced, sc)) {
										candidate = sc;
										toPlaced.add(sc);
										usedMips += sc.getMips();
										leftMips -= sc.getMips();
										usedRam += sc.getRam();
										usedIo += sc.getIo();
										usedBw += sc.getBw();
										assign.put(sc.getCloudletId(), vm.getId());
										cpuNeighborCloudletList.get(areaList.get(areaIndex)).remove(sc);
										// System.out.println("RandomRemove:SC"
										// + sc.getCloudletId() + " Vm#"
										// + sc.getVmId() + " "
										// +
										// cpuNeighborCloudletList.get(areaList.get(areaIndex)).remove(sc));
										areaEmptyOrSearched.clear();
										leftMipsSize = areaList.indexOf(getAreaByMips(leftMips)) + 1;
										if (leftMipsSize == 0) {
											for (int j = getAreaByMips(leftMips) - 1; j > 0; j--) {
												if (areaList.contains(j)) {
													leftMipsSize = areaList.indexOf(j) + 1;
													break;
												}
											}
										}
										found = true;
										System.out.println("Random " + areaIndex + " Vm# " + vm.getId()
												+ " found! area:" + bestFitArea + " Cloudlet #" + sc.getCloudletId());
										//System.out.println("usedMips " + usedMips + " usedRam " + usedRam + "usedIo "
										//		+ usedIo + " usedBw " + usedBw);
										// System.out.println("leftMipsSize " +
										// leftMipsSize + " areaEmptyOrSearched
										// "
										// + areaEmptyOrSearched.size());
										// System.out.println("usedIo >= Io*
										// stopUtilization:"+ (usedIo >=
										// vm.getIo()
										// * stopUtilization));
										if (usedMips >= vm.getMips() * (stopUtilization - TOLERANCE)
												|| usedRam >= vm.getRam() * (stopUtilization)
												|| usedIo >= vm.getIo() * (stopUtilization)
												|| usedBw >= vm.getBw() * (stopUtilization))
											full = true;
										// System.out.println("usedIo >= Io*
										// stopUtilization:"+ (usedIo >=
										// vm.getIo()
										// * stopUtilization)+" full:"+full);
										break;
									}
								}
								if (!found) {
									areaEmptyOrSearched.add(areaList.get(areaIndex));
									System.out.println((leftMipsSize == areaEmptyOrSearched.size())
											+ "Not Found! leftMipsSize " + leftMipsSize + " areaEmptyOrSearched "
											+ areaEmptyOrSearched.size());
								} else {
									// cpuNeighborCloudletList.get(areaList.get(areaIndex)).remove(candidate);
									break;
								}
							} else {
								areaEmptyOrSearched.add(areaList.get(areaIndex));
								System.out.println((leftMipsSize == areaEmptyOrSearched.size()) + "Empty! leftMipsSize "
										+ leftMipsSize + " areaEmptyOrSearched " + areaEmptyOrSearched.size());
							}
						}
					}
				}

				if (full || areaEmptyOrSearched.size() == leftMipsSize) {
					double totalMips = 0;
					int totalRam = 0;
					long totalIo = 0;
					long totalBw = 0;
					for (SimCloudlet c : toPlaced) {
						System.out.println("waittingList Remove SC#" + c.getCloudletId() + " Vm#" + c.getVmId() + " "
								+ waittingList.remove(c));
						// c.setVmId(vm.getId());
						totalMips += c.getMips();
						totalRam += c.getRam();
						totalIo += c.getIo();
						totalBw += c.getBw();
						//System.out.println("Vm Configuration: Mips: " + vm.getMips() + " Ram:" + vm.getRam() + " Io:"+ vm.getIo());
						System.out.println("Full VM #" + vm.getId() + " :" + c.getCloudletId() + " Mips:" + totalMips
								+ " Ram:" + totalRam + " Io:" + totalIo + " Bw:" + totalBw);
						addCloudletFinishedTime(c, clock);
						insertClockList(c, clock);
					}

					assignCloudlet(toPlaced, vm.getId(), usedMips, usedRam, usedIo, usedBw);

					break;
				}

			}

		}

		while (waittingList.size() != 0) {
			clock += clockInterval;
			List<SimCloudlet> finished = updateCloudlet(clock);
			List<Vm> toAssign = new ArrayList<Vm>();
			/*for (SimCloudlet c : finished) {
				System.out.println(clock + ": Cloudlet #" + c.getCloudletId() + "finished Vm #"
						+ cloudletVmMap.get(c.getCloudletId()));
			}*/
			for (SimCloudlet sc : finished) {
				toAssign.add(VmList.getById(vmlist, cloudletVmMap.get(sc.getCloudletId())));
			}
			for (Vm vm : toAssign) {
				//System.out.println(clock + ": VM# "+vm.getId()+" assignCloudletsToVmInProcess");
				assignCloudletsToVmInProcess(null, vm, clock);
			}
			/*if (clock > 2500){
				System.out.println("Time Out "+clock);
				System.exit(1);
			}*/
			//System.out.println(waittingList.size() + " clock :" + clock+" finished:"+this.finished);
			
		}

		/*for (Integer id : cloudletVmMap.keySet()) {
			System.out.println("Rts: Cl# " + id + " to Vm #" + cloudletVmMap.get(id));
		}*/
		// System.exit(1);
		int[] result = new int[cloudletlist.size()];
		for (int i = 0; i < cloudletlist.size(); i++) {
			int cloudletId = cloudletlist.get(i).getCloudletId();
			result[i] = cloudletVmMap.get(cloudletId);
			// if (assign.get(cloudletId) != null)
			// result[i] = assign.get(cloudletId);
			// else
			// result[i] = -1;
		}

		return result;
	}

	public Map<Integer, Integer> assignCloudletsToVmInProcess(List<SimCloudlet> cloudletlist, Vm vm,
			double currentTime) {
		// System.out.println("Assin InProcess VM#" + vm.getId() + " " +
		// waittingList.size());
		if (waittingList == null || waittingList.size() == 0)
			return null;
		/*if (vmAssignTime.get(vm.getId()) != null)
			System.out.println("Vm#" + vm.getId() + " CurTime:" + currentTime + " " + vmAssignTime.get(vm.getId()) + " "
					+ (currentTime > vmAssignTime.get(vm.getId())) + " "
					+ (currentTime == vmAssignTime.get(vm.getId())));*/
		if (vmAssignTime.get(vm.getId()) == null || currentTime > vmAssignTime.get(vm.getId()))
			vmAssignTime.put(vm.getId(), currentTime);
		else if (vmAssignTime.get(vm.getId()) == currentTime)
			return null;

		Map<Integer, Integer> assign = new HashMap<Integer, Integer>();
		if (cloudletlist != null && cloudletlist.size() > 0) {
			waittingList.addAll(cloudletlist);
			double newMinMips = minMips;
			for (SimCloudlet sc : cloudletlist) {
				if (sc.getMips() < newMinMips)
					newMinMips = sc.getMips();
			}
			if (newMinMips < minMips) {
				minMips = newMinMips;
				minMips--;
			}

			// 任务优先度降序排列
			Collections.sort(waittingList, new Comparator<SimCloudlet>() {
				public int compare(SimCloudlet sc0, SimCloudlet sc1) {
					double priority0 = getCloudletPriroity(sc0);
					double priority1 = getCloudletPriroity(sc1);
					if (priority0 < priority1)
						return 1;
					else if (priority0 == priority1) {
						return 0;
					} else
						return -1;
				}
			});

			cpuNeighborCloudletList.clear();
			for (SimCloudlet sc : waittingList) {
				int area = getAreaByMips(sc.getMips());
				List<SimCloudlet> areaCloudletList = cpuNeighborCloudletList.get(area);
				if (areaCloudletList == null) {
					areaCloudletList = new ArrayList<SimCloudlet>();
					areaCloudletList.add(sc);
				}
				cpuNeighborCloudletList.put(area, areaCloudletList);
			}

		}

		double occupiedMips = vmUsedMips.get(vm.getId());
		int occupiedRam = vmUsedRam.get(vm.getId());
		long occupiedIo = vmUsedIo.get(vm.getId());
		long occupiedBw = vmUsedBw.get(vm.getId());
		/*
		 * for (ResCloudlet c : executing) { occupiedMips += ((SimCloudlet)
		 * c.getCloudlet()).getMips(); occupiedRam += ((SimCloudlet)
		 * c.getCloudlet()).getRam(); occupiedIo += ((SimCloudlet)
		 * c.getCloudlet()).getIo(); occupiedBw += ((SimCloudlet)
		 * c.getCloudlet()).getBw(); }
		 */
		double availableMips = vm.getMips() - occupiedMips;
		int availableRam = vm.getRam() - occupiedRam;
		long availableIo = ((SimPowerVm) vm).getIo() - occupiedIo;
		long availableBw = vm.getBw() - occupiedBw;
		double leftMips = availableMips;
		List<SimCloudlet> toPlaced = new ArrayList<SimCloudlet>();
		Set<Integer> areaEmptyOrSearched = new HashSet<Integer>();
		int leftMipsSize = areaList.size();
		double usedMips = 0;
		int usedRam = 0;
		long usedIo = 0;
		long usedBw = 0;
		int bestFitArea = 0;

		boolean full = false;
		while (usedMips + occupiedMips < vm.getMips() * (stopUtilization - TOLERANCE)
				&& usedRam + occupiedRam < vm.getRam() * (stopUtilization)
				&& usedIo + occupiedIo < ((SimPowerVm) vm).getIo() * (stopUtilization)
				&& usedBw + occupiedBw < vm.getBw() * (stopUtilization)
				&& areaEmptyOrSearched.size() < leftMipsSize) {
			double upperMips = vm.getMips() * (stopUtilization + TOLERANCE) - usedMips;
			int leftArea = getAreaByMips(upperMips);
			// System.out.println("leftArea " + leftArea);
			List<SimCloudlet> cloudletArea = null;
			if (cpuNeighborCloudletList.containsKey(leftArea)) {
				bestFitArea = leftArea;
				leftMipsSize = areaList.indexOf(getAreaByMips(upperMips)) + 1;
			} else {
				for (int j = leftArea - 1; j > 0; j--) {
					if (cpuNeighborCloudletList.containsKey(j)) {
						bestFitArea = j;
						leftMipsSize = areaList.indexOf(j) + 1;
						break;
					}
				}
			}

			cloudletArea = cpuNeighborCloudletList.get(bestFitArea);
			SimCloudlet candidate = null;
			boolean found = false;
			if (cloudletArea != null) {
				for (SimCloudlet sc : cloudletArea) {
					//System.out.println("!SC#" + sc.getCloudletId() + " Mips:" + sc.getMips() + " Ram:" + sc.getRam()
					//		+ " Io:" + sc.getIo());
					if (checkResouceConstraint((SimPowerVm) vm, toPlaced, occupiedMips, occupiedRam, occupiedIo,
							occupiedBw, sc)) {
						candidate = sc;
						toPlaced.add(sc);
						usedMips += sc.getMips();
						leftMips -= sc.getMips();
						usedRam += sc.getRam();
						usedIo += sc.getIo();
						usedBw += sc.getBw();
						cpuNeighborCloudletList.get(bestFitArea).remove(sc);
						// System.out.println("As Remove:SC #" +
						// sc.getCloudletId() + " Vm#" + sc.getVmId()
						// +
						// cpuNeighborCloudletList.get(bestFitArea).remove(sc));
						assign.put(sc.getCloudletId(), vm.getId());
						areaEmptyOrSearched.clear();
						leftMipsSize = areaList.indexOf(getAreaByMips(leftMips)) + 1;
						if (leftMipsSize == 0) {
							for (int j = getAreaByMips(leftMips) - 1; j > 0; j--) {
								if (areaList.contains(j)) {
									leftMipsSize = areaList.indexOf(j) + 1;
									break;
								}
							}
						}
						found = true;
						if (usedMips + occupiedMips >= vm.getMips() * (stopUtilization - TOLERANCE)
								|| usedRam + occupiedRam >= vm.getRam() * (stopUtilization)
								|| usedIo + occupiedIo >= ((SimPowerVm) vm).getIo() * (stopUtilization)
								|| usedBw + occupiedBw >= vm.getBw() * (stopUtilization))
							full = true;
						break;
					}
				}
			}

			if (cloudletArea == null | !found) {
				areaEmptyOrSearched.add(bestFitArea);
				int bestFitIndex = areaList.indexOf(bestFitArea);
				//System.out.println("Vm# " + vm.getId() + " " + found + " BestFitIndex: " + bestFitIndex);
				int[] randomArea = randomArray(0, bestFitIndex - 1, bestFitIndex);
				//System.out.println("\n[" + bestFitIndex + "]  " + bestFitArea + "\nrandomArea :");
				/*for (int r : randomArea) {
					System.out.print(r + " ");
				}*/
				// System.out.println("randomArea Length " + randomArea.length);
				for (int areaIndex : randomArea) {
					cloudletArea = cpuNeighborCloudletList.get(areaList.get(areaIndex));
					if (cloudletArea != null) {
						for (SimCloudlet sc : cloudletArea) {
							if (checkResouceConstraint((SimPowerVm) vm, toPlaced, occupiedMips, occupiedRam, occupiedIo,
									occupiedBw, sc)) {
								candidate = sc;
								toPlaced.add(sc);
								usedMips += sc.getMips();
								leftMips -= sc.getMips();
								usedRam += sc.getRam();
								usedIo += sc.getIo();
								usedBw += sc.getBw();
								cpuNeighborCloudletList.get(areaList.get(areaIndex)).remove(sc);
								// System.out.println("AsRandomRemove:SC #" +
								// sc.getCloudletId() + " Vm#" + sc.getVmId()
								// + " from Area#" + areaList.get(areaIndex) + "
								// "
								// +
								// cpuNeighborCloudletList.get(areaList.get(areaIndex)).remove(sc));
								assign.put(sc.getCloudletId(), vm.getId());
								areaEmptyOrSearched.clear();
								leftMipsSize = areaList.indexOf(getAreaByMips(leftMips)) + 1;
								if (leftMipsSize == 0) {
									for (int j = getAreaByMips(leftMips) - 1; j > 0; j--) {
										if (areaList.contains(j)) {
											leftMipsSize = areaList.indexOf(j) + 1;
											break;
										}
									}
								}
								found = true;
								if (usedMips + occupiedMips >= vm.getMips() * (stopUtilization - TOLERANCE)
										|| usedRam + occupiedRam >= vm.getRam() * (stopUtilization - TOLERANCE)
										|| usedIo + occupiedIo >= ((SimPowerVm) vm).getIo()
												* (stopUtilization - TOLERANCE)
										|| usedBw + occupiedBw >= vm.getBw() * (stopUtilization - TOLERANCE))
									full = true;
								break;
							}
						}
						if (!found) {
							areaEmptyOrSearched.add(areaList.get(areaIndex));
							// System.out.println(
							// (leftMipsSize == areaEmptyOrSearched.size()) + "
							// As Not Found! leftMipsSize "
							// + leftMipsSize + " areaEmptyOrSearched " +
							// areaEmptyOrSearched.size());
						} else {
							break;
						}
					} else {
						areaEmptyOrSearched.add(areaList.get(areaIndex));
						System.out.println((leftMipsSize == areaEmptyOrSearched.size()) + "As Empty! leftMipsSize "
								+ leftMipsSize + " areaEmptyOrSearched " + areaEmptyOrSearched.size());
					}
				}
			}

			if (full || areaEmptyOrSearched.size() == leftMipsSize) {
				System.out.println("AS EmptyOrSearched.== leftMipsSize " + toPlaced.size()+" toPlace "+toPlaced.size());
				double total = 0;
				for (SimCloudlet c : toPlaced) {
					waittingList.remove(c);
					//System.out.println("AS waitList Remove SC#" + c.getVmId() + " " + waittingList.remove(c));
					total += c.getMips();
					System.out.println("InProcessFull VM#" + vm.getId() + " :" + c.getCloudletId() + " Mips:" + total);
					addCloudletFinishedTime(c, currentTime);
					insertClockList(c, currentTime);
				}
				assignCloudlet(toPlaced, vm.getId(), usedMips, usedRam, usedIo, usedBw);
				break;
			}
		}

		/*for (Integer scId : assign.keySet()) {
			System.out.println("assignRs sc#" + scId + " vm #" + assign.get(scId));
		}*/
		return assign;
	}

	private boolean checkResouceConstraint(SimPowerVm vm, List<SimCloudlet> placed, SimCloudlet candidate) {
		double accumulatedMips = 0.0;
		double accumulatedRam = 0.0;
		double accumulatedBw = 0.0;
		double accumulatedIo = 0.0;
		for (SimCloudlet cloudlet : placed) {
			//System.out.print(" #" + cloudlet.getCloudletId());
			accumulatedMips += cloudlet.getMips();
			accumulatedRam += cloudlet.getRam();
			accumulatedBw += cloudlet.getBw();
			accumulatedIo += cloudlet.getIo();
		}
		//System.out.println("\n  checkResouceConstraint VM " + vm.getId() + " Mips " + vm.getMips() + " Ram "
		//		+ vm.getRam() + " Io " + vm.getIo());
		//System.out.println("accMips" + accumulatedMips + " accRam " + accumulatedRam + " accIo " + accumulatedIo);
		//System.out.println("candidate#" + candidate.getCloudletId() + " Mips" + candidate.getMips() + " Ram "
		//		+ candidate.getRam() + " Io " + candidate.getIo());
		accumulatedMips += candidate.getMips();
		if (accumulatedMips > vm.getMips() * (stopUtilization + TOLERANCE))
			return false;
		accumulatedRam += candidate.getRam();
		if (accumulatedRam > vm.getRam() * (stopUtilization))
			return false;
		accumulatedBw += candidate.getBw();
		if (accumulatedBw > vm.getBw() * (stopUtilization))
			return false;
		accumulatedIo += candidate.getIo();
		if (accumulatedIo > vm.getIo() * (stopUtilization))
			return false;
		return true;

	}

	private boolean checkResouceConstraint(SimPowerVm vm, List<SimCloudlet> placed, double occupiedMips,
			int occupiedRam, long occupiedIo, long occupiedBw, SimCloudlet candidate) {
		double accumulatedMips = 0.0;
		double accumulatedRam = 0.0;
		double accumulatedBw = 0.0;
		double accumulatedIo = 0.0;
		for (SimCloudlet cloudlet : placed) {
			// System.out.print(" #" + cloudlet.getCloudletId());
			accumulatedMips += cloudlet.getMips();
			accumulatedRam += cloudlet.getRam();
			accumulatedBw += cloudlet.getBw();
			accumulatedIo += cloudlet.getIo();
		}
		//System.out.println("checkResouceConstraint Occpy VM " + vm.getId() + " Mips " + vm.getMips() + " Ram "
		//		+ vm.getRam() + " Io " + vm.getIo());
		//System.out.println("accMips" + accumulatedMips + " accRam " + accumulatedRam + " accIo " + accumulatedIo);
		//System.out.println("candidate#" + candidate.getCloudletId() + " Mips" + candidate.getMips() + " Ram "
		//		+ candidate.getRam() + " Io " + candidate.getIo());
		accumulatedMips += candidate.getMips();
		if (accumulatedMips + occupiedMips > vm.getMips() * (stopUtilization + TOLERANCE))
			return false;
		accumulatedRam += candidate.getRam();
		if (accumulatedRam + occupiedMips > vm.getRam() * (stopUtilization))
			return false;
		accumulatedBw += candidate.getBw();
		if (accumulatedBw + occupiedMips > vm.getBw() * (stopUtilization ))
			return false;
		accumulatedIo += candidate.getIo();
		if (accumulatedIo + occupiedMips > vm.getIo() * (stopUtilization))
			return false;
		return true;

	}

	private int getAreaByMips(double mips) {
		return (int) Math.ceil((mips - minMips) / cpuAreaInterval);
	}

	public static int[] randomArray(int min, int max, int n) {
		int len = max - min + 1;
		if (max < min || n > len) {
			return null;
		}

		// 初始化给定范围的待选数组
		int[] source = new int[len];
		for (int i = min; i < min + len; i++) {
			source[i - min] = i;
		}
		if (len == 1)
			return source;
		int[] result = new int[n];
		Random rd = new Random();
		int index = 0;
		for (int i = 0; i < result.length; i++) {
			// 待选数组0到(len-2)随机一个下标
			index = Math.abs(rd.nextInt() % len--);
			// 将随机到的数放入结果集
			result[i] = source[index];
			// 将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换
			source[index] = source[len];
		}
		return result;
	}

	private int random(int min, int max) {
		Random random = new Random();
		return random.nextInt(max) % (max - min + 1) + min;
	}

	private double getCloudletPriroity(SimCloudlet c) {
		double priority = TIME_WEIGHT * c.getCloudletLength() / c.getAverageMips() / NORMAL_TIME;
		priority += CPU_WEIGHT * c.getAverageMips() / NORMAL_CPU + RAM_WEIGHT * c.getRam() / NORMAL_RAM;
		priority += IO_WEIGHT * c.getIo() / NORMAL_IO + BW_WEIGHT * c.getBw() / NORMAL_BW;
		return priority;
	}

	private int binarySearch(List<Integer> areaList, int area) {
		int low = 0;
		int high = areaList.size() - 1;
		int mid;
		while (low <= high) {
			mid = (low + high) / 2;
			if (areaList.get(mid) == area) {
				return high + 1;
			} else if (areaList.get(high) < area) {
				low = mid + 1;
			} else {
				high = mid - 1;
			}
		}
		return -1;
	}

	private int binarySearch(List<SimCloudlet> cloudletList, double mips) {
		int low = 0;
		int high = cloudletList.size() - 1;
		int mid;
		while (low <= high) {
			mid = (low + high) / 2;
			if (cloudletList.get(mid).getMips() == (long) mips) {
				return high + 1;
			} else if (cloudletList.get(high).getMips() < mips) {
				low = mid + 1;
			} else {
				high = mid - 1;
			}
		}
		return -1;
	}

	public Map<Integer, List<SimCloudlet>> getCpuNeighborCloudletList() {
		return cpuNeighborCloudletList;
	}

	public void setCpuNeighborCloudletList(Map<Integer, List<SimCloudlet>> cpuNeighborCloudletList) {
		this.cpuNeighborCloudletList = cpuNeighborCloudletList;
	}

	public int getCpuAreaInterval() {
		return cpuAreaInterval;
	}

	public void setCpuAreaInterval(int cpuAreaInterval) {
		this.cpuAreaInterval = cpuAreaInterval;
	}

	public List<SimCloudlet> getWaittingList() {
		return waittingList;
	}

	public void setWaittingList(List<SimCloudlet> waittingList) {
		this.waittingList = waittingList;
	}

	public double getStopUtilization() {
		return stopUtilization;
	}

	public void setStopUtilization(double stopUtilization) {
		this.stopUtilization = stopUtilization;
	}

	public double getClockInterval() {
		return clockInterval;
	}

	public void setClockInterval(double clockInterval) {
		this.clockInterval = clockInterval;
	}

	public Map<Integer, Double> getVmUsedMips() {
		return vmUsedMips;
	}

	public void setVmUsedMips(Map<Integer, Double> vmUsedMips) {
		this.vmUsedMips = vmUsedMips;
	}

	public Map<Integer, Integer> getVmUsedRam() {
		return vmUsedRam;
	}

	public void setVmUsedRam(Map<Integer, Integer> vmUsedRam) {
		this.vmUsedRam = vmUsedRam;
	}

	public Map<Integer, Long> getVmUsedIo() {
		return vmUsedIo;
	}

	public void setVmUsedIo(Map<Integer, Long> vmUsedIo) {
		this.vmUsedIo = vmUsedIo;
	}

	public Map<Integer, Long> getVmUsedBw() {
		return vmUsedBw;
	}

	public void setVmUsedBw(Map<Integer, Long> vmUsedBw) {
		this.vmUsedBw = vmUsedBw;
	}

	public Map<Integer, List<SimCloudlet>> getCurrentAssignedCloudletList() {
		return currentAssignedCloudletList;
	}

	public void setCurrentAssignedCloudletList(Map<Integer, List<SimCloudlet>> currentAssignedCloudletList) {
		this.currentAssignedCloudletList = currentAssignedCloudletList;
	}

	/*
	 * public Map<Integer, List<SimCloudlet>> getAssignedResult() { return
	 * AssignedResult; }
	 * 
	 * public void setAssignedResult(Map<Integer, List<SimCloudlet>>
	 * assignedResult) { AssignedResult = assignedResult; }
	 */

}
