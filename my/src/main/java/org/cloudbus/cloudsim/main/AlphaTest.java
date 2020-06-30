package org.cloudbus.cloudsim.main;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.allocator.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.*;
import org.cloudbus.cloudsim.power.model.PowerModelIoSimple;
import org.cloudbus.cloudsim.power.model.PowerModelRamSimple;
import org.cloudbus.cloudsim.power.models.*;
import org.cloudbus.cloudsim.provisioners.*;
import org.cloudbus.cloudsim.util.MyData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class AlphaTest {


    private static double _alpha[] = {0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
    private static int cls = 1;
    private static int numUser = 1;
    private static int schedulingInterval = 5;

    private static final int NUM_CLOUDLET = 560;
    private static final int NUM_VM = 560; // 主机数目
    private static final int NUM_HOST = 100; // 主机数目
    private static int[] hostMips = { 1843, 3067, 2048, 2500 };//数组，不同类型主机的mips
    private static int[] hostRam = { 4096, 12288, 6144, 4096 };//数组，不同类型主机的Ram
    private static int[] hostIo = { 300, 500, 400, 300 };//数组，不同类型主机的Io
    private static int[] vms = { 4, 6, 3, 2 };//数组，不同类型主机的虚拟机的个数

    private static final int POLICY_ONE = 0;
    private static final int POLICY_TWO = 1;
    private static final int POLICY_THREE = 2;
    //private static final int CLOUDLET_ASSIGN
    private static final int POLICY_FOUR = 4;
    private static final int POLICY_FIVE = 5;
    //private static final int CLOUDLET_ASSIGN_POLICY_TIME_BALANCE = 6;
    private static final int POLICY_SIX = 6;//trade-offs
    private static final int ALLOCATION_POLICY = POLICY_FIVE; //表示选择哪一个策略

    private static final String filename = "Cloudlet_8000_24";
    //"5" represent the employed assignment policy
    private static final String OUTPUTDIR = Helper.projectPath + "/";

    //if wanna record data this time
    private static final boolean recOrNot = true;


    private static double energyMat[][] = new double[_alpha.length + 1][cls + 1];
    private static double exeTimeMat[][] = new double[_alpha.length + 1][cls + 1];
    public static void main(String[] args) {

        MyData myData = new MyData(OUTPUTDIR);
        for(int i = 0 ; i < cls; i++){
            int clsFlag = i;


            for(int j = 0 ; j < _alpha.length; j++){

                if(j == 0){
                    myData.setCls(clsFlag);
                }
                else {
                    myData.setCls(-1);
                }
                if(j == _alpha.length - 1){
                    myData.setLastFlag(true);
                }
                else {
                    myData.setLastFlag(false);
                }

                myData.setAlpha(_alpha[j]);
                long begin = System.currentTimeMillis();

                try {
                    Log.setDisabled(true);
                    Calendar calendar = Calendar.getInstance();

                    CloudSim.init(numUser, calendar, false);

                    SimPowerDatacenter datacenter = createDatacenter(schedulingInterval, ALLOCATION_POLICY);


                    //I create a new method in Helper.java for facilitating the alpha test
                    SimDatacenterBroker broker = Helper.createBrokerForAlphaTest(ALLOCATION_POLICY, datacenter.getHostList(), _alpha[j]);

                    int brokerId = broker.getId();
                    List<SimPowerVm> vmList = new ArrayList<>();
                    vmList = createVmByHostType(brokerId, NUM_VM);

                    broker.submitVmList(vmList);

                    List<SimProgressCloudlet> cloudletList = Helper.createCloudletsByData(brokerId, NUM_CLOUDLET, filename);

                    broker.submitCloudletList(cloudletList);
                    Helper.init(NUM_HOST);
                    double lastClock = CloudSim.startSimulation();

                    CloudSim.stopSimulation();

                    energyMat[j][i] = datacenter.getPower()/3600;
                    exeTimeMat[j][i] = lastClock;
                    myData.setEnergyConsumption(datacenter.getPower()/3600);
                    myData.setExecutionTime(lastClock);
                    if (recOrNot) {
                        //the recorded data in single line is tagged as energy consumption & execution time, respectively
                        myData.recToLocal(myData.formatter());
                    }

                    Helper.printResults(datacenter,vmList,lastClock,"AlphaTest");

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }


        }
        myData.calculateAverage(energyMat, exeTimeMat, _alpha);
        myData.close();

    }




    private static List<SimPowerVm> createVmByHostType(int userId, int numHost) {
        List<SimPowerVm> vmList = new ArrayList<>();



        long size = 10000; // image size (MB)
        int ram = 512;// 1024;// 2048; // vm memory (MB)
        int mips = 1024;
        long bw = 1000;
        long io = 60;
        int pesNumber = 1; // number of cpus
        String vmm = "Xen"; // VMM name
        SimPowerVm vm = null;

        CloudletCpuAllocator cpuAllocator = null;
        int id = 0;

        int ind = 0;
        for (int i = 0; i < numHost; i++) {
            int type = i % vms.length;
            for (int j = 0; j < vms[type]; j++) {
                if (ind >= NUM_VM){
                    break;
                }
                ind++;
                vm = new SimPowerVm(id++, userId, hostMips[type], pesNumber, hostRam[type]/vms[type], hostIo[type]/vms[type], bw, size, 1, vmm,
                        new SimCloudletSchedulerDynamicWorkload(mips,
                                pesNumber, new CloudletCpuAllocatorReservation(mips),
                                new CloudletRamAllocatorSimple(ram), // CloudletRamAllocatorSimple
                                new CloudletIoAllocatorSimple(io), // CloudletIoAllocatorSimple
                                new CloudletBwAllocatorSimple(bw)),
                        5); // CloudletBwAllocatorSimple

                vmList.add(vm);
            }
        }


        return vmList;
    }

    private static SimPowerDatacenter createDatacenter(double schedulingInterval, int policy) throws Exception {

        String name = "datacenter_0";

        String architecture = "x86";
        String os = "linux";
        String vmm = "Xen";
        List<PowerHost> hostList = new ArrayList<>();
        double staticPercent = 0.01;

        for (int i = 0; i < NUM_HOST; i++) {
            int id = i;
            int type = i%vms.length;
            List<Pe> peList = new ArrayList<>();
            for(int j = 0; j < vms[j]; j++){
                peList.add(new Pe(j, new PeProvisionerSimple(hostMips[type])));
            }
            RamProvisioner ramProvisioner = new SimRamProvisionerSimple(hostRam[type]);
            BwProvisioner bwProvisioner = new SimBwProvisionerSimple(10000);
            SimIoProvisioner ioProvisioner = new SimIoProvisionerSimple(hostIo[type]);
            long storage = 1000000;

            VmScheduler vmScheduler = new VmSchedulerTimeSharedOverSubscription(peList);
            PowerModel powerModelCpu = new PowerModelSpecPowerIbmX3550XeonX5675();
            PowerModel powerModelRam = new PowerModelRamSimple(hostRam[type]);
            PowerModel powerModelIO = new PowerModelIoSimple(hostIo[type]);
            PowerModel powerModelBw = new PowerModelCubic(100, staticPercent);
            PowerHost host = new SimPowerHostReservation(id, ramProvisioner,
                    bwProvisioner, ioProvisioner, storage, peList, vmScheduler,powerModelCpu, powerModelRam,
                    powerModelIO, powerModelBw);
            hostList.add(host);
        }

        double timeZone = 10.0;
        double costPerSec = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.02;

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(architecture,os,vmm,hostList,timeZone,
                costPerSec,costPerMem,costPerStorage,costPerBw);
        VmAllocationPolicy vmAllocationPolicy = new PowerVmAllocationPolicySimple(hostList);
        List<Storage> storageList = new LinkedList<Storage>();

        return new SimPowerDatacenter(name, characteristics, vmAllocationPolicy,
                storageList, schedulingInterval);
    }
}
