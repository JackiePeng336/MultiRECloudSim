package org.cloudbus.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.main.Helper;

import sun.util.logging.resources.logging;

public class SimDatacenterBroker extends DatacenterBroker {

	private CloudletAssignmentPolicy cloudletAssignPolicy; //任务分配策略
	public int createdVms;
	public SimDatacenterBroker(String name,
			CloudletAssignmentPolicy cloudletAssignPolicy) throws Exception {
		super(name);
		setCloudletAssignPolicy(cloudletAssignPolicy);
	}

	@Override
	protected void submitCloudlets() {
		createdVms = getVmsCreatedList().size();
		int[] assign = getCloudletAssignPolicy().assignCloudletsToVm(
				this.<SimCloudlet>getCloudletList(), getVmsCreatedList());
		for (int i = 0; i < getCloudletList().size(); i++) {
			Cloudlet cloudlet = getCloudletList().get(i);


			/*System.out.println(CloudSim.clock() + ": " + getName()
			+ ": Sending cloudlet " + cloudlet.getCloudletId()
			+ " to VM #" + assign[i]);*/


			//Log.printLine(CloudSim.clock() + ": " + getName()
			//		+ ": Sending cloudlet " + cloudlet.getCloudletId()
			//		+ " to VM #" + assign[i]);
			//the binding process is implemented here directly. the original function named bindCloudletToVm
			// in the father class datacenterbroker is therefore not used in the project.
			cloudlet.setVmId(assign[i]);
			if(getVmsToDatacentersMap().get(assign[i]) == null){
				continue;
			}
			sendNow(getVmsToDatacentersMap().get(assign[i]),
					CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			getCloudletSubmittedList().add(cloudlet);
		}
		//System.out.println("Testing Cloudlet Length!!!!!!");

		/*for (SimProgressCloudlet sc:this.<SimProgressCloudlet>getCloudletList()) {
			System.out.println(sc.getCloudletLength()+"    "+sc.getMaxMips()+"    "+sc.getMips());
		}*/

		// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
	}

	@Override
	protected void processCloudletReturn(SimEvent ev) {
		SimCloudlet cloudlet = (SimCloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet "
				+ cloudlet.getCloudletId() + " received");
		cloudletsSubmitted--;
		Helper.vmFininshedTime.put(cloudlet.getVmId(),CloudSim.clock());
		if (getCloudletAssignPolicy() instanceof CloudletAssignmentPolicyBalance) {
			Vm vm = VmList.getById(getVmList(), cloudlet.getVmId());
			((CloudletAssignmentPolicyBalance) getCloudletAssignPolicy())
					.removeLoadForVm(vm, cloudlet);
		}
		if (getCloudletAssignPolicy() instanceof CloudletAssignmentPolicyTimeGreedy) {
			Vm vm = VmList.getById(getVmList(), cloudlet.getVmId());
			((CloudletAssignmentPolicyTimeGreedy) getCloudletAssignPolicy())
					.removeLoadForVm(vm, cloudlet);
		}
		
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printLine(CloudSim.clock() + ": " + getName()
					+ ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

		}
	}

	public <T extends Vm> List<T> getCreatedVmList(){
		return this.getCreatedVmList();
	}
	
	public CloudletAssignmentPolicy getCloudletAssignPolicy() {
		return cloudletAssignPolicy;
	}

	public void setCloudletAssignPolicy(
			CloudletAssignmentPolicy cloudletAssignPolicy) {
		this.cloudletAssignPolicy = cloudletAssignPolicy;
	}

}
