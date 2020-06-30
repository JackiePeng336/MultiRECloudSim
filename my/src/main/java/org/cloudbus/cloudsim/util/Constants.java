package org.cloudbus.cloudsim.util;

public class Constants {

    /**
     * SFF---PowerVmAllocationPolicySimple。简单顺序放置
     * */
    public static final int SFF = 0;
    /**
     * PFF---VmAllocationPolicySimple。考虑PE的非简单顺序放置
     * */
    public static final int PFF = 1;

    /**
     * RMS
     * */
    public static final int RMS = 2;
    /**
     * MAD
     * */
    public static final int MMEE  = 3;
    /**
     *
     * EAGLE.MMEA跟RMS差别不大，只需要在DT算法的一些地方加些条件判断即可
     * */
    public static final int MMEA = 4;

    public static final double HOST_IDLE_POWER = 0.02 * 3600;

    /**
     * Cloudlet长度
     * */
    public static int CLOUDLET_LENGTH = 200000;

    public static int HOST_NUM = 100;//(int) (ACTIVE_HOST_NUM * 1.25);

    public static int VM_NUM = 560;

    public static final int MMVM = 5;

    /**
     *
     * 可以通过这个控制VM运行时间,或者说，整个仿真程序的运行时间
     */
    public static int CLOUDLET_NUM = VM_NUM;


    public final static double SIMULATION_LIMIT = 2000;//36 * 60 * 60;
    /**
     *  虚拟机的历史负载长度
     *
     * */
    public static int HISTORY_LENGTH = (int) (SIMULATION_LIMIT * 1.2);

    public static final double SafetyParam = 0.1;
    /**
     *  动态调整..由于I/O负载的利用率太小了。基本不会出现位于ad里面的状况
     *      把r0调成0就能看出来有没有因为过载而产生的迁移了
     * */
    public static double r0 = 0.18;

    /**
     *
     *  动态调整
     * */
    public static double R0 = 0.8;

    /**
     *
     * */
    public static int LOOK_FORWARD = 5;

    /**
     *  回顾过去N个点
     *  预测未来M个点,只测试三个：20，50
     * */
    public static int LOOK_BACK = 5;

    public static final int VM_ALLOCATION_POLICY = SFF;

    /**
     * 防止分配精度溢出
     * */
    public static final double VALUE_OVERFLOW = 0.0;

    /**
     *  50 to 0.8
     *  100 to 0.6
     *  施加在MadArr那边和getPUS
     * */
    public static final double ACCURACY_DEGRADATION = 0;


    /**
     *  唯独主机的低载阈值是固定的
     * */
    public static double UNDER_UTILIZED_THR = 0.10;

    /**
     *
     *  Policies comparisons
     *      MMEE: MAD+MC+Energy-efficiency
     *      MMEA:
     * **/
    public static double SafetyParam_MMEE = 0.1;


    /**
     *  每次optimize能迁移几个虚拟机。
     *  放置的时候都是没考虑其他虚拟机迁进来之后会少的量
     * */

    public static final double RAM_OFFSET = 0.00;
    public static final double IO_OFFSET = 0.00;

    public static final double SCHEDULING_INTERVAL = 1;

    /**
     *  设置初始时候活跃Host的数量
     * */
    public static final int ACTIVE_HOST_NUM = 70;




    public static final double OFFSET_TRIGGER = 0.7;
    public static final double REDUCE_TO = 0.8;
    public static final double MIGRATING_IN_DEGRADATION = 0.9;
    /**
     *
     * */
    public final static int HOST_BW		 = 1000000000; // in Kbps, 10000 Mbps, 1Gbps


    /***/
    public static int PROGRESS_ROW = (int) (1.5 * HISTORY_LENGTH);


    /**
     *  指定r0跟R0是否动态变化
     * */
    public static final int RDynamic = 0;
    public static final int RStatic = 1;
    public static int RStatus = RDynamic;
    // Mips Weight For Combined R Dynamic
    public static final double MipsWFCRD = 0.93;
    // Ram Weight For Combined R Dynamic
    public static final double RamWFCRD = 0.93;
    // Io Weight For Combined R Dynamic
    public static final double IoWFCRD = 0.1;




    /**
     *  资源维度
     * */
    public static final int Dimensions = 3;

    /**
     *  被论文Energy efficient virtual machine placement algorithm with balanced and
     *  improved resource utilization in a data center中的所启发的创新点
     *      Definition 5
     *          0 表示采用平均值
     *          1 表示采用xMAD,代码中的xMAD指的都是加强版的
     * */
    public static final int OverallStrategy = 0;

    /**
     *  被论文Energy efficient virtual machine placement algorithm with balanced and
     *  improved resource utilization in a data center中的所启发的创新点
     *      Definition 5
     *          0 表示采用论文中的方案
     *          1 表示取与向量(1,1,...,1)的距离
     *
     * */
    public static int DistanceStrategy = OverallStrategy;


    /**
     *  资源依据为 当前 或 预测 或 近期平均值
     *          0       1       2
     * */
    public static final int ResourceCur = 0;
    public static final int ResourcePredict = 1;
    public static final int ResourceMean = 2;
    //public static final int ResourceMAD = 3;
    public static int ResourceBase = ResourcePredict;


    /**
     *  阈值部分
     *
     * */
    public static final int PolicyStatic = 0;
    public static final int PolicyxMad = 1;
    public static final int PolicyLmsReg = 2;
    public static int PolicyCoreParamBasedOnUt = PolicyxMad;

    // 过载判断是否只看单个MIPS情况，还是综合所有资源。
    // 只看单个MIPS
    public static final int OverUtThrOnSingle = 0;
    // 参考多种资源的阈值
    public static final int OverUtThrOnMulti = 1;
    public static int OverUtThrBase = OverUtThrOnMulti;

    /**
     *  日志方面的设置
     * */
    public static final boolean updateVmsProcessingInHost = false;
    public static final boolean updateCloudetProcessingWithoutSchedulingFutureEventsForceInAliDC = false;
    public static final boolean allocateForSimCloudletInAllocator = false;

//    日志设置示例
//
//    boolean recover = false;
//    // 本来可以输出
//        if(!Log.isDisabled()){
//        // 但是这边不让输出
//        if(!Constants.updateVmsProcessingInHost){
//            Log.disable();
//            recover = true;
//        }
//    }else {
//        if(Constants.updateVmsProcessingInHost){
//            Log.enable();
//            recover = true;
//        }
//    }
//    ........
//    if(recover){
//        if(Log.isDisabled()){
//            Log.enable();
//        }else {
//            Log.disable();
//        }
//    }



    /**
     *  VM选择策略部分
     * */
    public static final int StopCondOnNotOverUt = 0;
    //public static final int StopCondOnXXX = 1;
    // 过载主机上在迁出一个VM之后可能不会马上脱离过载范围，但是又不能一直迁移。
    // 所以需要一个循环终止条件。
    public static int StopCondOnGettingVmsToMigrateFromHosts = StopCondOnNotOverUt;

    /**
     *  为R0跟r0专门开辟的安全系数
     *  SPM = space partition model
     * */
    public static double SafetyParam_SPM = 0.5;


    public static int getTimePoint(double curtime){
        int time_point = (int) (curtime / Constants.SCHEDULING_INTERVAL);
        return time_point;

    }
}
