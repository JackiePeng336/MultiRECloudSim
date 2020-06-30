package org.cloudbus.cloudsim;

public class SimProgressCloudletIoT extends SimProgressCloudlet {

    private String label ;

    public SimProgressCloudletIoT(int cloudletId, String label,
                                  long cloudletLength, int pesNumber,
                                  long cloudletFileSize, long cloudletOutputSize,
                                  long mips, int ram, long io, long bw,
                                  UtilizationModel utilizationModelCpu,
                                  UtilizationModel utilizationModelRam,
                                  UtilizationModel utilizationModelIo,
                                  UtilizationModel utilizationModelBw) {
        super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, mips, ram, io, bw, utilizationModelCpu, utilizationModelRam, utilizationModelIo, utilizationModelBw);

        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
