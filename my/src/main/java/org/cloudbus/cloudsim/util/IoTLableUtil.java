package org.cloudbus.cloudsim.util;

import org.cloudbus.cloudsim.SimProgressCloudlet;
import org.cloudbus.cloudsim.entity.CloudletFromFileBean;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

public class IoTLableUtil {

    private static final String original_fileName = "Cloudlet_8000_24";

    private static String originFileDir ;

    static {
        try {
            originFileDir = IoTLableUtil.class.getClassLoader().getResource("Output")
                        .toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static String destFileDir;

    static {
        try {
            destFileDir = IoTLableUtil.class.getClassLoader().getResource("LabelledOutput")
                        .toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static double getK() {
        return k;
    }

    //表示 n * k 个 任务为IoT
    private static final double k = 0.3;

    //理论上来说，这个方法历史上只会被调用一次
    public static void addLabel(int cloudletNum){

        //获取要加入label的行号
        LinkedList<Integer> lines = selectLines(cloudletNum);


        //先获取文件内容，每一行形成一个bean，并添加标签 ,返回该bean的链表
        LinkedList<CloudletFromFileBean> contents = getContent(originFileDir,
                original_fileName, cloudletNum, lines);


        //添加完标签后，还是写到新文件为好
        writeContent(destFileDir, original_fileName, contents);

    }

    private static void writeContent(String destFileDir, String original_fileName,
                                     LinkedList<CloudletFromFileBean> contents){

        BufferedWriter bw = null;

        StringBuilder builder ;

        try {
            bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(destFileDir + "/" + original_fileName)));

            for (int i = 0; i < contents.size(); i++){
                builder = new StringBuilder();
                CloudletFromFileBean bean = contents.get(i);
                builder.append(bean.getLabel()).append('\t').append(bean.getLength()).append('\t')
                        .append(bean.getMips()).append('\t').append(bean.getRam()).append('\t')
                        .append(bean.getIo()).append('\t').append(bean.getBw());
                bw.write(builder.toString());
                bw.newLine();

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(bw != null){
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    //读取文件
    private static LinkedList<CloudletFromFileBean> getContent(String originFileDir, String original_fileName, int cloudletNum,
                                                               LinkedList<Integer> lines){
        LinkedList<CloudletFromFileBean> contents = new LinkedList<>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(originFileDir + "/" + original_fileName)));

            CloudletFromFileBean bean;
            long bw = 0;
            for(int i =0 ; i< cloudletNum; i++){

                String line = br.readLine();

                if (line != null) {
                    String[] data = line.split("\t");
                    Long length = new Long(data[0]);
                    Long mips = new Long(data[1]);
                    Long ram = new Long(data[2]);
                    Long io = new Long(data[3]);


                    bean = new CloudletFromFileBean("Common", (double)length, (double)mips, (double)ram,
                            (double)io, (double)bw);

                    //如果当前行数刚好是被选中的就设置为IoT
                    if(lines.contains(i)){
                        bean.setLabel("IoT");
                    }

                    contents.add(bean);
                }

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(br!=null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return contents;
    }

    //随机选取某些行作为IoT任务
    //决定哪些任务是IoT也不能太随便，应该设置一个k值控制整体IoT任务数量
    //同样地，需要把本次运行的哪些任务是IoT记录起来。
    //n 个任务数量
    private static LinkedList<Integer> selectLines(int n){

        LinkedList<Integer> selected = new LinkedList<>();

        //遍历某些行，按照一定概率以及k来控制当前行是不是要设置为IoT
        //比如将1-n顺序的数字打乱顺序，随机放到一个数据容器中，然后按照n / k 作为每次前进步数。将命中的步数设置为IoT
        //随机生成1-n的不重复n个数字
        int step = (int)(1 / k);


        int ind = 0;
        List<Integer> tmp = new ArrayList<>();
        for(int i = 0; i < n; i++){
            int ran = (int)(Math.random()*n);


            while (tmp.contains(ran)){
                ran = (int)(Math.random()*n);
            }

            tmp.add(ran);
        }


        Iterator<Integer> iterator = tmp.iterator();

        int cnt = n;
        int cur = 0;
        while (cnt > 0){
            int kk = step;

            while (iterator.hasNext() && kk > 0){
                cur = iterator.next();
                kk --;

            }

            cnt -= step;

            selected.add(cur);

        }


        return selected;
    }



}
