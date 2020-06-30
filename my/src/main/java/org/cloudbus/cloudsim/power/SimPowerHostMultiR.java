package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.allocator.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.*;
import org.cloudbus.cloudsim.util.Constants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SimPowerHostMultiR extends SimPowerHost {
    public SimPowerHostMultiR(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, SimIoProvisioner ioProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModelCpu, PowerModel powerModelRam, PowerModel powerModelIO, PowerModel powerModelBw) {
        super(id, ramProvisioner, bwProvisioner, ioProvisioner, storage, peList, vmScheduler, powerModelCpu, powerModelRam, powerModelIO, powerModelBw);
    }


    List<SimHostStateHistoryEntry> simStateHistory = new LinkedList<>();
    @Override
    public double updateVmsProcessing(double currentTime) {
        boolean recover = false;
        // 本来可以输出
        if(!Log.isDisabled()){
            // 但是这边不让输出
            if(!Constants.updateVmsProcessingInHost){
                Log.disable();
                recover = true;
            }
        }else {
            if(Constants.updateVmsProcessingInHost){
                Log.enable();
                recover = true;
            }
        }
        double nextTime = Double.MAX_VALUE;
        CloudletCpuAllocator cpuAllocator;
        CloudletRamAllocator ramAllocator;
        CloudletIoAllocator ioAllocator;

        List<Double> reqMipsList ;
        List<Pe> peList;

        double usedMips = 0.0;
        double usedRam = 0.0;
        double usedIo = 0.0;

        double availMips = 0.0;

        double hostTotalReqMips = 0.0;
        double hostTotalReqRam = 0.0;
        double hostTotalReqIo = 0.0;

        double vmTotalReqMips =0.0;
        double vmTotalAllocatedMips = 0.0;

        double vmTotalReqRam = 0;
        double vmTotalAllocatedRam ;

        double vmTotalReqIo = 0;
        double vmTotalAllocatedIo;

        List<ResCloudlet> cloudletExecList;
        List<ResCloudlet> cloudletWaitingList;

        Cloudlet _cloudlet;
        SimCloudletSchedulerDynamicWorkload scheduler;

        //就算是覆盖，也别忘了调用原函数中的某些方法完成本该完成的工作。比如这边要继续更新虚拟机进度
        for(Vm vm: getVmList()){
            double time = vm.updateVmProcessing(currentTime, getVmScheduler()
                    .getAllocatedMipsForVm(vm));

            if(time > 0.0 && time < nextTime){
                nextTime = time;
            }
        }

        //肯定是这边设置利用率出问题了，出现了利用率大于1的状况
        //因为是在Host里面的updateVmProcessing，为了后面的添加主机的历史记录考虑必须设置前一时段的资源情况
        setPreviousUtilizationMips(getUtilizationMips());
        setPreviousUtilizationRam(getUtilizationRam());
        setPreviousUtilizationIO(getUtilizationIO());

        //果然，之前忘记了将mips清零。清空，类似更下面的Provisioner的deallocate
        setUtilizationMips(0);

        //每一轮update vm都会先清除原来的资源
        //vm --> cloudlet
        for(Vm vm: getVmList()){

            getVmScheduler().deallocatePesForVm(vm);

            scheduler = (SimCloudletSchedulerDynamicWorkload)
                    vm.getCloudletScheduler();

            cpuAllocator = scheduler.getCpuAllocator();
            ramAllocator = scheduler.getRamAllocator();
            ioAllocator = scheduler.getIoAllocator();

            ((CloudletCpuAllocatorSimple) cpuAllocator).resetMipsTable();
            ((CloudletRamAllocatorSimple) ramAllocator).resetRamTable();
            ((CloudletIoAllocatorSimple)ioAllocator).resetIoTable();
        }

        // host --> vm
        getRamProvisioner().deallocateRamForAllVms();
        getIoProvisioner().deallocateIoForAllVms();
        getBwProvisioner().deallocateBwForAllVms();
        //都回收了资源，准备开始新一轮资源分配
        for(Vm vm : getVmList()){

            //之所以是list，是因为每个vm有n个pe，list的大小就是pe的数量。
            reqMipsList = vm.getCurrentRequestedMips();
            getVmScheduler().allocatePesForVm(vm, reqMipsList);

            scheduler = (SimCloudletSchedulerDynamicWorkload) vm.getCloudletScheduler();

            Log.printLine(
                    "AllocatePesForVm " + vm.getId() + " " + ((SimPowerVm) vm).getCurrentRequestedMips() + " Scheduler "
                            + scheduler.getCurrentRequestedMips().toString() + " " + scheduler.getTotalCurrentMips() + " " + scheduler.getCurrentMipsShare().toString());

            //追加CPU资源
            cpuAllocator = scheduler.getCpuAllocator();
            //上面删完pe后，可用的总mips当然 等于 初始化分配mips量
            //vmscheduler返回的list是对应vm上每个pe的mips量
            availMips = scheduler.getTotalAvailableMips(getVmScheduler().getAllocatedMipsForVm(vm));
            //@1：available才是真正分配到的结果。虽然这边是任务的，但是也能拿来参考。
            cpuAllocator.setAvailableMips(availMips);

            // 反映的是getVmScheduler().allocatePesForVm(vm, reqMips)这边
            if(getVmScheduler().getAllocatedMipsForVm(vm) != null){
                Log.printLine("Vm #" + vm.getId() + " Current getAllocatedMips:"
                        + getVmScheduler().getAllocatedMipsForVm(vm).toString()
                        + " RequestMips:"
                        + ((SimPowerVm) vm).getCurrentRequestedMips().toString());
            }

            //追加ram、io、bw资源给资源供给模块
            cloudletExecList = scheduler.getCloudletExecList();

            vmTotalReqRam = 0;
            vmTotalReqIo = 0;
            for(ResCloudlet cloudlet : cloudletExecList){

                _cloudlet = cloudlet.getCloudlet();
                vmTotalReqRam += ((SimCloudlet)_cloudlet).getCurrentRequestedRam(currentTime);
                vmTotalReqIo += ((SimCloudlet)_cloudlet).getCurrentRequestedIo(currentTime);

                Log.printLine("SimPowerHost Exe scl#" + _cloudlet.getCloudletId()
                        + " Ram:" + vmTotalReqRam + " Io:" + vmTotalReqIo);
            }

            getRamProvisioner().allocateRamForVm(vm, (int) vmTotalReqRam);
            getIoProvisioner().allocateIoForVm((SimPowerVm) vm, (long) vmTotalReqIo);
        }

        //上面只是分配给了vm资源，接下来要给vm追加    任务等待队列    中的一些资源
        //
        for(Vm vm: getVmList()){

            scheduler = (SimCloudletSchedulerDynamicWorkload) vm.getCloudletScheduler();

            cloudletWaitingList = scheduler.getCloudletWaitingList();

            for(ResCloudlet cloudlet : cloudletWaitingList){

                _cloudlet = cloudlet.getCloudlet();

                /**
                 *  检查waitinglist的任务是否可以运行用cloudlet的资源请求最大值。
                 *  但是，追加资源用的是当前请求值
                 * */
                if(isCloudletRunnable((SimCloudlet)_cloudlet, vm)){
                    addResourcesToVm((SimCloudlet)_cloudlet, vm, currentTime);
                }
            }
        }

        for(Vm vm : getVmList()){
            vmTotalReqMips = vm.getCurrentRequestedTotalMips();
            vmTotalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);

            //注意这边计算利用率是getMips，众所周知vm的getMips只是其中一个pe的量。
            //好在本实验都是使用的一个vm对应一个pe的思想。
            //这也侧面反映cloudsim的设计是有问题的。
            Log.formatLine(
                    "%.2f: [Host #"
                            + getId()
                            + "] Total allocated MIPS for VM #"
                            + vm.getId()
                            + " (Host #"
                            + vm.getHost().getId()
                            + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
                    CloudSim.clock(), vmTotalAllocatedMips, vmTotalReqMips,
                    vm.getMips(), vmTotalReqMips / vm.getMips() * 100);


            peList = getVmScheduler().getPeList();
            StringBuilder pesString = new StringBuilder();
            if(peList != null){
                for(Pe pe : peList){
                    pesString.append(String.format(" PE #" + pe.getId()
                            + ": %.2f.", pe.getPeProvisioner()
                            .getTotalAllocatedMipsForVm(vm)));
                }
                //还输出这个是为了跟上面那个对比下数值
                Log.formatLine("%.2f: [Host #" + getId() + "] MIPS for VM #"
                                + vm.getId() + " by PEs (" + getNumberOfPes() + " * "
                                + getVmScheduler().getPeCapacity() + ")." + pesString,
                        CloudSim.clock());
            }

            //检查当前的vm是不是刚从其他host迁移进来的，并输出
            if(getVmsMigratingIn().contains(vm)){
                Log.formatLine(
                        "%.2f: [Host #" + getId() + "] VM #" + vm.getId()
                                + " is being migrated to Host #" + getId(),
                        CloudSim.clock());
            }
            //如果不是
            else {
                if (vmTotalAllocatedMips + 0.1 < vmTotalReqMips) {
                    //从这个log输出就能发现，为什么有些vm历史记录会是-1了，就是因为资源不够了
                    Log.formatLine("%.2f: [Host #" + getId()
                            + "] Under allocated MIPS for VM #" + vm.getId()
                            + ": %.2f", CloudSim.clock(), vmTotalReqMips
                            - vmTotalAllocatedMips);
                }

                /**
                 *
                 * 给  VM   添加历史记录
                 * ------------------------重写这个方法的意义从这边开始------------------------
                 * ------------------------很重要：给历史记录增加多资源记录-----------------------
                 * ------------------------很重要：给历史记录增加多资源记录-----------------------
                 *
                 * */
                //给  VM   添加历史记录
                //------------------------重写这个方法的意义从这边开始------------------------
                //vmTotalReqMips = 0;

                vmTotalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);
                vmTotalAllocatedRam = vm.getCurrentAllocatedRam() ;
                vmTotalAllocatedIo = ((SimPowerVm)vm).getCurrentAllocatedIo();


                vmTotalReqRam = vm.getCurrentRequestedRam();
                vmTotalReqIo = ((SimPowerVm)vm).getCurrentRequestedIo();

                ((SimPowerVm)vm).addStateHistoryEntry(currentTime,
                        vmTotalAllocatedMips, vmTotalReqMips, 0,
                        vmTotalAllocatedRam, vmTotalReqRam, 0,
                        vmTotalAllocatedIo, vmTotalReqIo, usedIo,
                        0, 0, 0,
                        (vm.isInMigration()&&!getVmsMigratingIn().contains(vm)));

                //------------------------很重要：给历史记录增加多资源记录-----------------------

                /**
                 *
                 * ------------------------很重要：给历史记录增加多资源记录-----------------------
                 * ------------------------很重要：给历史记录增加多资源记录-----------------------
                 * ------------------------很重要：给历史记录增加多资源记录-----------------------
                 * */

                //如果当前vm准备迁移出去了，那么会性能下降，这边固定设置性能下降为10%

                if (vm.isInMigration()) {
                    Log.formatLine(
                            "%.2f: [Host #" + getId() + "] VM #" + vm.getId()
                                    + " is in migration", CloudSim.clock());
                    vmTotalAllocatedMips /= 0.9; // performance degradation due to
                    // migration - 10%
                }
            }
            // 不同于下面的setUtilizationXXX。因为UtilizationMips不是百分比，而是绝对值的使用数值
            setUtilizationMips(getUtilizationMips() + vmTotalAllocatedMips);
            hostTotalReqMips += vmTotalReqMips;
            hostTotalReqRam += vmTotalReqRam;
            hostTotalReqIo += vmTotalReqIo;
        }

        // UtilizationRAM都是百分比不同于上面的setUtilizationMips。
        // 没什么好纠结的，Of 方法只是会帮你先统计一下资源利用率而已。
        setUtilizationRam(getUtilizationOfRam());
        setUtilizationIO(getUtilizationOfIO());
        setUtilizationBw(getUtilizationOfBw());

        /**
         *  添加主机的历史记录
         *  有些主机刚刚变为active，但是分配资源为0，所以一样要使用(getUtilizationMips() > 0)而不能一律由active判定
         * */
        addStateHistoryEntry(currentTime, getUtilizationMips(),
                hostTotalReqMips, getRamProvisioner().getUsedRam(), hostTotalReqRam,
                getIoProvisioner().getUsedIo(), hostTotalReqIo,
                (getUtilizationMips() > 0));

        if(recover){
            if(Log.isDisabled()){
                Log.enable();
            }else {
                Log.disable();
            }
        }

        return nextTime;
    }


    @Override
    protected void addResourcesToVm(SimCloudlet scl, Vm vm, double currentTime) {
        // 这一步清空其实可有可无
        getVmScheduler().deallocatePesForVm(vm); // 清空PEs分配
        SimCloudletSchedulerDynamicWorkload scheduler = ((SimCloudletSchedulerDynamicWorkload) vm
                .getCloudletScheduler());
        scheduler.addCurrentRequestedMips(scl
                .getCurrentRequestedMips(currentTime)); //增加当前需求的Mips
        //Log.printLine("cloudlet #" + scl.getCloudletId()
        //		+ " Add After: Vm Current RequestMips:"
        //		+ ((SimPowerVm) vm).getCurrentRequestedMips().toString());
        getVmScheduler().allocatePesForVm(vm,
                ((SimPowerVm) vm).getCurrentRequestedMips()); // 主机分配Mips给Vm

        CloudletCpuAllocatorSimple cpuAllocator = (CloudletCpuAllocatorSimple) scheduler
                .getCpuAllocator();
        cpuAllocator.setAvailableMips(scheduler
                .getTotalAvailableMips(getVmScheduler().getAllocatedMipsForVm(
                        vm))); //重新确定被分配的MIPS

        //Log.printLine("addResources Vm allocate Mips:"
        //		+ getVmScheduler().getAllocatedMipsForVm(vm));
        Log.printLine("//VM#" + vm.getId() + " cloudlet#"
                + scl.getCloudletId() + " asks for more Reasources. mips:"
                + vm.getCurrentRequestedTotalMips());


        //追加内存，IO，带宽资源. 可以不先清空资源
        ((SimRamProvisionerSimple) getRamProvisioner()).addRamForVm(vm,
                (int) (scl.getRam()*scl.getUtilizationOfRam(currentTime)));
        ((SimIoProvisionerSimple) getIoProvisioner()).addIoForVm((SimPowerVm) vm,
                (long) (scl.getIo()*scl.getUtilizationOfIO(currentTime)));
    }

    public void addStateHistoryEntry(double time, double allocatedMips, double requestedMips,
                                     double allocatedRam, double requestedRam, double allocatedIo,
                                     double requestedIo,
                                     boolean isActive) {

        SimHostStateHistoryEntry newState = new SimHostStateHistoryEntry(
                time,
                allocatedMips, requestedMips,
                allocatedRam, requestedRam,
                allocatedIo, requestedIo,
                isActive);
        if (!getSimStateHistory().isEmpty()) {
            SimHostStateHistoryEntry previousState = getSimStateHistory().get(getSimStateHistory().size() - 1);
            if (previousState.getTime() == time) {
                getSimStateHistory().set(getSimStateHistory().size() - 1, newState);
                return;
            }
        }
        getSimStateHistory().add(newState);


        HostStateHistoryEntry hnewState = new HostStateHistoryEntry(
                time,
                allocatedMips,
                requestedMips,
                isActive);
        if (!getStateHistory().isEmpty()) {
            HostStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
            if (previousState.getTime() == time) {
                getStateHistory().set(getStateHistory().size() - 1, hnewState);
                return;
            }
        }
        getStateHistory().add(hnewState);


    }

    public List<SimHostStateHistoryEntry> getSimStateHistory() {
        return simStateHistory;
    }

    public void setSimStateHistory(List<SimHostStateHistoryEntry> stateHistory) {
        this.simStateHistory = stateHistory;
    }
}
