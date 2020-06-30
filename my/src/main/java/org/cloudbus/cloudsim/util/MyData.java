package org.cloudbus.cloudsim.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyData {


    private String OUTPUTDIR;
    private String expData;
    private double energyConsumption;
    private double executionTime;

    private int cls;
    private double alpha;



    private boolean lastFlag;


    private BufferedWriter bufferedWriter;



    private String filename = "";

    public MyData(String dir){
        try {
            setOUTPUTDIR(dir);
            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
            setFilename(format.format(new Date().getTime()));
            System.out.println(getFilename());
            setBufferedWriter((new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(
                                    getOUTPUTDIR()+getFilename()),"UTF-8")
            )));



        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception o) {
            //
        }
    }


    public void calculateAverage(double energyMat[][], double exeTimeMat[][], double alphaArr[]){

        StringBuilder str = new StringBuilder();
        str.append("Average:\n");
        for(int i = 0 ; i < alphaArr.length; i++){
            str.append("\tα: "+alphaArr[i]+", avgEnergy: "+ mean(energyMat[i]) + ", avgExeTime: "+
                    mean(exeTimeMat[i])+".\n");
        }


        recToLocal(str.toString());

    }


    public double mean(double alphaRow[]){
        double sum = 0;
        for (int i = 0 ; i < alphaRow.length; i++){
            if(alphaRow[i] == 0.0){
                break;

            }
            sum += alphaRow[i];
        }
        return sum/(alphaRow.length - 1);
    }

    public void recToLocal(String str) {

        BufferedWriter bw = getBufferedWriter();
        try {
            bw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void close() {
        try {
            getBufferedWriter().flush();
            getBufferedWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String formatter(){
        StringBuilder str = new StringBuilder();

        if(cls >= 0){
            str.append("cls: "+ getCls()+"\n");
        }
        str.append("\t"+"α: "+ getAlpha()+", energy consumption is: "+ getEnergyConsumption()+
                ", execution time is: "+ getExecutionTime()+".\n");


        if (isLastFlag()) {
            str.append("\n");
        }
        return str.toString();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }

    public void setBufferedWriter(BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
    }

    public String getOUTPUTDIR() {
        return OUTPUTDIR;
    }

    public void setOUTPUTDIR(String OUTPUTDIR) {
        this.OUTPUTDIR = OUTPUTDIR;
    }


    public boolean isLastFlag() {
        return lastFlag;
    }

    public void setLastFlag(boolean lastFlag) {
        this.lastFlag = lastFlag;
    }


    public String getExpData() {
        return expData;
    }

    public void setExpData(String expData) {
        this.expData = expData;
    }

    public double getEnergyConsumption() {
        return energyConsumption;
    }

    public void setEnergyConsumption(double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    public int getCls() {
        return cls;
    }

    public void setCls(int cls) {
        this.cls = cls;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }



}
