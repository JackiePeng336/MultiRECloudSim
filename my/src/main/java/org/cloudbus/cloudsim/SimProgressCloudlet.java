package org.cloudbus.cloudsim;


public class SimProgressCloudlet extends SimCloudlet {

	private long cloudletFinishedSoFar; //任务完成进度

	public SimProgressCloudlet(int cloudletId, long cloudletLength, int pesNumber,
			long cloudletFileSize, long cloudletOutputSize, long mips, int ram,
			long io, long bw, UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelIo,
			UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize,
				cloudletOutputSize, mips, ram,io,  bw, 
				utilizationModelCpu, utilizationModelRam,
				utilizationModelIo,utilizationModelBw);
	}
	
	/*@Override
	public double getCurrentRequestedMips(final double time) {
		// Log.printLine("getCurrentRequestedMips :"+ getUtilizationOfCpu(time)+
		// " mips:"+getMips());
		return (double)(long) (getUtilizationOfCpu() * getMips());
	}*/

	public void updateCloudletFinishedSoFar(long miLength) {	//更新任务进度
		cloudletFinishedSoFar += miLength;
	}

	public double getProgress() {	//获得任务进度百分比
		//Log.printLine("getProgress： Cloudlet"+getCloudletId()+"progress "+ (double)getCloudletFinishedSoFar() / (double)getCloudletLength());
		return (double)getCloudletFinishedSoFar() / (double)getCloudletLength();
	}

	@Override
	public double getUtilizationOfCpu(final double time) {	//根据任务进度百分比返回CPU利用率
		return getUtilizationModelCpu().getUtilization(getProgress());
	}

	@Override
	public double getUtilizationOfRam(final double time) {	//根据任务进度百分比返回内存利用率
		return getUtilizationModelRam().getUtilization(getProgress());
	}

	@Override
	public double getUtilizationOfBw(final double time) {	//根据任务进度百分比返回带宽利用率
		return getUtilizationModelBw().getUtilization(getProgress());
	}
	
	@Override
	public double getUtilizationOfIO(final double time) {	//根据任务进度百分比返回IO利用率
		return getUtilizationModelIo().getUtilization(getProgress());
	}

	public long getCloudletFinishedSoFar() {
		return cloudletFinishedSoFar;
	}

	public void setCloudletFinishedSoFar(long cloudletFinishedSoFar) {
		this.cloudletFinishedSoFar = cloudletFinishedSoFar;
	}

}
