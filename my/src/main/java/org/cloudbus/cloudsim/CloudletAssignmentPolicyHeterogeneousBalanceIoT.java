package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.power.SimPowerHost;
import org.cloudbus.cloudsim.power.SimPowerVm;

import java.util.*;

public class CloudletAssignmentPolicyHeterogeneousBalanceIoT extends CloudletAssignmentPolicy{




    //核心就是又一个k要 决定 IoT总体全局优先级。
    // 记住我们的计算是按照排名来的，而不是按照优先度来的。
    //理论上来说，k越小，说明越多的IoT任务排名靠前，对于IoT越友好。
    //k是有区间的，也就是所有IoT都在最前面时候，k最小。而所有IoT都在最后面的时候，k最大。
    //所以这个类里面必须有一个方法，根据当前任务总量和IoT任务总量统计出k的范围，如果传进来的k不对就直接抛出异常结束分配
    private double k = 0.3;

    private double kMin;
    private double kMax;
    //替换时候的步数大小
    private int step = 2;

    private double totalPri;
    private double totalIotPri;
    //
    //以vm的id作为key
    private Map<Integer, Float> cpuLoadIndicator; // 任务-CPU负载指标
    private Map<Integer, Float> ramLoadIndicator; // 任务-内存负载指标
    private Map<Integer, Float> ioLoadIndicator; // 任务-IO负载指标
    private Map<Integer, Float> bwLoadIndicator; // 任务-带宽负载指标
    private final static int NORMAL_LENGTH = 1000; // 任务长度参考标准值
    private final static int NORMAL_MIPS = 900; // 任务MIPS参考标准值
    private final static int NORMAL_RAM = 768; // 任务内存参考标准值
    private final static int NORAML_IO = 83; // 任务IO参考标准值
    private final static int NORMAL_BW = 10000; // 任务带宽参考标准值
    private final static int CPU = 0; // CPU密集型
    private final static int RAM = 1; // 内存密集型
    private final static int IO = 2; // IO密集型
    private final static int BW = 3; // 带宽密集型




    private double TIME_WEIGHT = 0.7;
    private double CPU_WEIGHT = 0.3;


    private final static double RAM_WEIGHT = 0.1;
    private final static double IO_WEIGHT = 0.1;
    private final static double BW_WEIGHT = 0.0;
    private final static double REFERENCE_TIME = 30;
    private final static double REFERENCE_CPU = 900;
    private final static double REFERENCE_RAM = 768;
    private final static double REFERENCE_IO = 30;
    private final static double REFERENCE_BW = 1000;
    private List<Vm> vmList; // 待接收任务的虚拟机列表
    private static Map<Integer, Double> vmCpuLoadLine = new HashMap<Integer, Double>();
    private static Map<Integer, Integer> vmRamLoadLine = new HashMap<Integer, Integer>();
    private static Map<Integer, Long> vmIoLoadLine = new HashMap<Integer, Long>();
    private static Map<Integer, Long> vmBwLoadLine = new HashMap<Integer, Long>();

    public CloudletAssignmentPolicyHeterogeneousBalanceIoT(List<? extends SimPowerHost> hostList,
                                                           double k, int iotNum, int cloudletNum) {
        //
        setCpuLoadIndicator(new HashMap<Integer, Float>());
        setRamLoadIndicator(new HashMap<Integer, Float>());
        setIoLoadIndicator(new HashMap<Integer, Float>());
        setBwLoadIndicator(new HashMap<Integer, Float>());
        setVmList(new ArrayList<Vm>());

        setK(k);

        setTotalPri(0.0);
        setTotalIotPri(0.0);

        countKRange(iotNum, cloudletNum);
        //这边肯定要修改，改成添加vm的资源数据，而不是主机的。
       /* int vmnum = 0;
        for (SimPowerHost host : hostList) {

            for (Vm vm : host.getVmList()){
                vmnum++;
                vmCpuLoadLine.put(vm.getId(), vm.getMips() * vm.getNumberOfPes());
                vmRamLoadLine.put(vm.getId(), vm.getRam());
                vmIoLoadLine.put(vm.getId(), ((SimPowerVm)vm).getIo());
                vmBwLoadLine.put(host.getId(), host.getBw());
            }


        }

        System.out.println("顺便统计一下虚拟机数量是否相符" + vmnum);*/
    }
    private double getCloudletPriroity(SimCloudlet c) {
        double priority = TIME_WEIGHT * c.getCloudletLength() / c.getAverageMips() / REFERENCE_TIME;
        priority += CPU_WEIGHT * c.getAverageMips() / REFERENCE_CPU;// +
        //System.out.println("I am in the policy and my weight is: "+ TIME_WEIGHT +" "+CPU_WEIGHT);
        // RAM_WEIGHT
        // *
        // c.getRam()
        // /
        // REFERENCE_RAM;
        // priority += IO_WEIGHT * c.getIo() / REFERENCE_IO + BW_WEIGHT *
        // c.getBw() / REFERENCE_BW;
        //double priority = (double)c.getCloudletLength()/(double)c.getMaxMips();
        return priority;
    }

    /**
     *
     * 这边也要修改，Collections.sort完了之后要进一步调整，让IoT满足k
     *
     * 为了让这个函数能够辨识cloudlet的标签，需要将SimPrgressCloudlet进一步改造，就添加一个标签而已
     *
     * */
    @Override
    public int[] assignCloudletsToVm(List<SimCloudlet> cloudletlist, List<Vm> vmlist) {
        addNewVmList(vmlist);

        if(getK() < getkMin() || getK() > getkMax()){
            try {
                throw new Exception("-----------K 设置得太大或者太小了！！-------------");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //按照优先度从大到小排序
        Collections.sort(cloudletlist, new Comparator<SimCloudlet>() {
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



        //还要再根据k再调整一次每个任务在队列中的位置
        adjustAgainForK(cloudletlist);


        int n = cloudletlist.size();
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            if (cloudletlist.get(i).getVmId() != -1)
                result[i] = cloudletlist.get(i).getVmId();
            else {
                int type = getCloudletIntensiveType(cloudletlist.get(i));
                result[i] = findMinLoadVmByType(type);
                // Log.printLine("findMinLoadVmByType Type " + type + "
                // result[i]" + result[i]);
                // printVmAllLoad();
                addLoadForVm(VmList.getById(vmlist, result[i]), cloudletlist.get(i));
            }
        }

        return result;
    }


    /**
     * 从队列头部开始，按照步数step来依次替换。边替换边检查k。
     *   如果一开始就小于k就不用循环。否则就一直变换，直到小于k为止。
     *
     *
     * */
    private void adjustAgainForK(List<SimCloudlet> cloudletlist) {
        SimProgressCloudletIoT cloudletIoT;

        if(getK() > getkMax() || getK() < getkMin()){
            System.out.println("--------------------K 无效值-------------------");
            return;
        }

        //从队列头部开始，按照步数step来依次替换。边替换边检查k。
        //  如果一开始就小于k就不用循环。否则就一直变换，直到小于k为止。
        int i= 0, j = i;

        int begin = 0;
        int end = cloudletlist.size() - 1;
        while (!checkK(cloudletlist, i, j)){

            // 从前往后 找到第一个待交换的Common

            while (((SimProgressCloudletIoT)cloudletlist.get(begin)).getLabel().equals("IoT")){
                begin += step;
            }

            // 从后往前 找到第一个待交换的IoT

            while (((SimProgressCloudletIoT)cloudletlist.get(end)).getLabel().equals("Common")){
                end -= step;
            }

            //出现极端情况，就要重新设置步数了,并且i跟j都调整为0，忽略这次的改变
            if(begin >= end){
                setStep(getStep() - 1);
                i = j = 0;
                begin = 0;
                end = cloudletlist.size() - 1;
                continue;
            }
            Collections.swap(cloudletlist, begin, end);
            i = begin;
            j = end;
        }



    }

    /**
     * 每次调整都是一个的，如果每次都遍历就很没必要了。应该搞一个缓存，存储上次的结果，交换过后，只需要
     * 知道交换的是哪两个位置i和j，然后 原缓存值 - i和j的差的绝对值
     * @param
     * @return 如果已经小于k就返回true。
     * */
    private boolean checkK(List<SimCloudlet> cloudletlist, int i, int j){


        //如果是第一次算的话就没办法，只能从头开始遍历
        if(Math.abs(0.0 - getTotalIotPri()) < 0.01){

            double pri = 0.0;

            int ind = 1;
            for(SimCloudlet cloudlet : cloudletlist){
                SimProgressCloudletIoT cloudletIoT = (SimProgressCloudletIoT) cloudlet;
                if(cloudletIoT.getLabel().equals("IoT")){

                    pri += ind;
                }
                ind++;
            }

            setTotalIotPri(pri);

        }
        else {

            setTotalIotPri(getTotalIotPri() - Math.abs(i - j));

        }

        return getTotalIotPri() / getTotalPri() < getK();
    }


    /**
     * 根据IoT数量和总数量，计算出k的合理范围
     *
     *
     * @param iotNum
     * @param cloudletNum*/
    private void countKRange(int iotNum, int cloudletNum){

        double numerator = 0.0;
        double denominator = 0.0;

        for (int i =1; i<= cloudletNum; i++){
            denominator += i;
        }
        //调用这个类得构造函数已经运行了一遍这个了
        setTotalPri(denominator);

        for (int i = 1; i<= iotNum; i++) {
            numerator += i;
        }
        setkMin(numerator / denominator);
        numerator = 0.0;
        for (int i = cloudletNum; i>= cloudletNum - iotNum; i--) {
            numerator += i;
        }
        setkMax(numerator / denominator);
    }



    public static int getCloudletIntensiveType(SimCloudlet cloudlet) { // 判断任务是哪种密集型
        double[] type = new double[4];
        type[CPU] = (double) cloudlet.getAverageMips() / (double) NORMAL_MIPS;
        type[RAM] = (double) cloudlet.getRam() / (double) NORMAL_RAM;
        type[IO] = (double) cloudlet.getIo() / (double) NORAML_IO;
        type[BW] = (double) cloudlet.getBw() / (double) NORMAL_BW;
        // Log.printLine("getCloudletIntensiveType "+type[CPU]+" "+type[RAM]+"
        // "+type[IO]+" "+type[BW]);
        double max = 0.0;
        int maxType = 0;
        for (int i = 0; i < 4; i++) {
            if (type[i] > max) {
                max = type[i];
                maxType = i;
            }
        }
        return maxType;
    }

    private int findMinLoadVmByType(int type) { // 根据密集型找该负载最小的虚拟机
        Map<Integer, Float> loadIndicator = null;
        switch (type) {
            case CPU:
                loadIndicator = getCpuLoadIndicator();//得到映射
                break;
            case RAM:
                loadIndicator = getRamLoadIndicator();
                break;
            case IO:
                loadIndicator = getIoLoadIndicator();
                break;
            case BW:
                loadIndicator = getBwLoadIndicator();
                break;
        }

        float minLoad = Float.MAX_VALUE;
        int id = 0;
        for (Integer vmid : loadIndicator.keySet()) {//vmid就是获取这个映射里面的VM的ID
            if (getVmLoadByIndicator(loadIndicator, vmid) < minLoad) {
                minLoad = getVmLoadByIndicator(loadIndicator, vmid);
                id = vmid;
            }
        }
        return id;
    }

    private void initLoadIndicator(List<Vm> vmlist) { // 初始化虚拟机负载指标
        if (vmlist == null)
            return;
        for (Vm vm : vmlist) {
            getCpuLoadIndicator().put(vm.getId(), (float) 0.0);
            getRamLoadIndicator().put(vm.getId(), (float) 0.0);
            getIoLoadIndicator().put(vm.getId(), (float) 0.0);
            getBwLoadIndicator().put(vm.getId(), (float) 0.0);

            vmCpuLoadLine.put(vm.getId(), vm.getMips() * vm.getNumberOfPes());
            vmRamLoadLine.put(vm.getId(), vm.getRam());
            vmIoLoadLine.put(vm.getId(), ((SimPowerVm)vm).getIo());
            vmBwLoadLine.put(vm.getId(), vm.getBw());
        }

    }

    private void addNewVmList(List<Vm> vmlist) { // 若有新的虚拟机则添加
        List<Vm> newVmList = new ArrayList<Vm>();
        if (!getVmList().containsAll(vmlist)) {
            newVmList.addAll(vmlist);
        } else {
            for (Vm vm : vmlist) {
                if (!getVmList().contains(vm))
                    newVmList.add(vm);
            }
        }

        getVmList().addAll(newVmList);
        initLoadIndicator(newVmList);
    }

    private float getVmLoadByIndicator(Map<Integer, Float> indicator, int vmid) { // 根据指标和虚拟机Id返回负载
        if (indicator.containsKey(vmid))
            return indicator.get(vmid);
        else
            return (float) 0.0;
    }

    /**
     * 除以主机的资源量出现在这里。。
     * */
    private void addLoadForVm(Vm vm, SimCloudlet cloudlet) { // 根据任务给虚拟机添加负载
        float cpuLoad = getCpuLoadForVm(vm);
        float ramLoad = getRamLoadForVm(vm);
        float ioLoad = getIoLoadForVm(vm);
        float bwLoad = getBwLoadForVm(vm);
        float weight = (float) (cloudlet.getCloudletLength() / cloudlet.getAverageMips());// /
        // cloudlet.getAverageMips());//cloudlet.getAverageMips());//NORMAL_LENGTH;
        // //
        // 长度是权重

        getCpuLoadIndicator().put(vm.getId(),
                (float) (cpuLoad + weight * cloudlet.getAverageMips() / getVmCpuLoadLineById(vm.getId())));

        getRamLoadIndicator().put(vm.getId(),
                (float) (ramLoad + weight * cloudlet.getRam() / getVmRamLoadLineById(vm.getId())));

        getIoLoadIndicator().put(vm.getId(),
                (float) (ioLoad + weight * cloudlet.getIo() / getVmIoLoadLineById(vm.getId())));

        getBwLoadIndicator().put(vm.getId(),
                (float) (bwLoad + weight * cloudlet.getBw() / getVmBwLoadLineById(vm.getId())));
    }

    public void removeLoadForVm(Vm vm, SimCloudlet cloudlet) {// 根据任务给虚拟机减少负载
        float cpuLoad = getCpuLoadForVm(vm);
        float ramLoad = getRamLoadForVm(vm);
        float ioLoad = getIoLoadForVm(vm);
        float bwLoad = getBwLoadForVm(vm);
        float weight = (float) (cloudlet.getCloudletLength() / cloudlet.getAverageMips());// cloudlet.getAverageMips());
        // //
        // NORMAL_LENGTH;
        getCpuLoadIndicator().put(vm.getId(),
                (float) (cpuLoad - weight * cloudlet.getAverageMips() / getVmCpuLoadLineById(vm.getHost().getId())));
        getRamLoadIndicator().put(vm.getId(),
                (float) (ramLoad - weight * cloudlet.getRam() / getVmRamLoadLineById(vm.getHost().getId())));
        getIoLoadIndicator().put(vm.getId(),
                (float) (ioLoad - weight * cloudlet.getIo() / getVmIoLoadLineById(vm.getHost().getId())));
        getBwLoadIndicator().put(vm.getId(),
                (float) (bwLoad - weight * cloudlet.getBw() / getVmBwLoadLineById(vm.getHost().getId())));
    }

    public void printVmAllLoad() {
        Log.printLine("printVmAllLoad");
        for (Integer vmid : getCpuLoadIndicator().keySet()) {
            Log.printLine("Vm#" + vmid + " " + getCpuLoadIndicator().get(vmid) + " " + getRamLoadIndicator().get(vmid)
                    + " " + getIoLoadIndicator().get(vmid) + " " + getBwLoadIndicator().get(vmid));
        }
    }

    public void printVmCpuLoad() {
        for (Integer vmid : getCpuLoadIndicator().keySet()) {
            Log.printLine("Vm#" + vmid + " " + getCpuLoadIndicator().get(vmid));
        }
    }

    public void printRamCpuLoad() {
        for (Integer vmid : getRamLoadIndicator().keySet()) {
            Log.printLine("Vm#" + vmid + " " + getRamLoadIndicator().get(vmid));
        }
    }

    public void printIoCpuLoad() {
        for (Integer vmid : getIoLoadIndicator().keySet()) {
            Log.printLine("Vm#" + vmid + " " + getIoLoadIndicator().get(vmid));
        }
    }

    public void printBwCpuLoad() {
        for (Integer vmid : getBwLoadIndicator().keySet()) {
            Log.printLine("Vm#" + vmid + " " + getBwLoadIndicator().get(vmid));
        }
    }

    public double getK() {
        return k;
    }

    public void setK(double k) {
        this.k = k;
    }

    public double getTIME_WEIGHT() {
        return TIME_WEIGHT;
    }

    public void setTIME_WEIGHT(double TIME_WEIGHT) {
        this.TIME_WEIGHT = TIME_WEIGHT;
    }

    public double getCPU_WEIGHT() {
        return CPU_WEIGHT;
    }

    public void setCPU_WEIGHT(double CPU_WEIGHT) {
        this.CPU_WEIGHT = CPU_WEIGHT;
    }


    private float getCpuLoadForVm(Vm vm) { // 获取Vm的CPU负载
        if (getCpuLoadIndicator().containsKey(vm.getId()))
            return getCpuLoadIndicator().get(vm.getId());
        else
            return (float) 0.0;
    }

    private float getRamLoadForVm(Vm vm) { // 获取Vm的内存负载
        if (getRamLoadIndicator().containsKey(vm.getId()))
            return getRamLoadIndicator().get(vm.getId());
        else
            return (float) 0.0;
    }

    private float getIoLoadForVm(Vm vm) { // 获取Vm的IO负载
        if (getIoLoadIndicator().containsKey(vm.getId()))
            return getIoLoadIndicator().get(vm.getId());
        else
            return (float) 0.0;
    }

    private float getBwLoadForVm(Vm vm) { // 获取Vm的带宽负载
        if (getBwLoadIndicator().containsKey(vm.getId()))
            return getBwLoadIndicator().get(vm.getId());
        else
            return (float) 0.0;
    }

    private Double getVmCpuLoadLineById(int id) {
        return vmCpuLoadLine.get(id);
    }

    private Integer getVmRamLoadLineById(int id) {
        return vmRamLoadLine.get(id);
    }

    private Long getVmIoLoadLineById(int id) {
        return vmIoLoadLine.get(id);
    }

    private Long getVmBwLoadLineById(int id) {
        return vmBwLoadLine.get(id);
    }

    public Map<Integer, Float> getCpuLoadIndicator() {
        return cpuLoadIndicator;
    }

    public void setCpuLoadIndicator(Map<Integer, Float> cpuLoadIndicator) {
        this.cpuLoadIndicator = cpuLoadIndicator;
    }

    public Map<Integer, Float> getRamLoadIndicator() {
        return ramLoadIndicator;
    }

    public void setRamLoadIndicator(Map<Integer, Float> ramLoadIndicator) {
        this.ramLoadIndicator = ramLoadIndicator;
    }

    public Map<Integer, Float> getIoLoadIndicator() {
        return ioLoadIndicator;
    }

    public void setIoLoadIndicator(Map<Integer, Float> ioLoadIndicator) {
        this.ioLoadIndicator = ioLoadIndicator;
    }

    public Map<Integer, Float> getBwLoadIndicator() {
        return bwLoadIndicator;
    }

    public void setBwLoadIndicator(Map<Integer, Float> bwLoadIndicator) {
        this.bwLoadIndicator = bwLoadIndicator;
    }

    public List<Vm> getVmList() {
        return vmList;
    }

    public void setVmList(List<Vm> vmlist) {
        this.vmList = vmlist;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public double getTotalPri() {
        return totalPri;
    }

    public void setTotalPri(double totalPri) {
        this.totalPri = totalPri;
    }

    public double getkMin() {
        return kMin;
    }

    public void setkMin(double kMin) {
        this.kMin = kMin;
    }

    public double getkMax() {
        return kMax;
    }

    public void setkMax(double kMax) {
        this.kMax = kMax;
    }

    public double getTotalIotPri() {
        return totalIotPri;
    }

    public void setTotalIotPri(double totalIotPri) {
        this.totalIotPri = totalIotPri;
    }
}
