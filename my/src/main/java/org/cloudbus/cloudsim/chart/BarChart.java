package org.cloudbus.cloudsim.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

public class BarChart {

    private static String FONT = "TimesRoman";

    //private static ArrayList<XYSeriesCollection> dataset;

    private static String title = "";

    private static String xLabel = "";

    private static String yLabel = "";


    private static ArrayList<Dataset> datasets = new ArrayList<>();

    private static Dataset dataset = null;
    /**
     * 最多MAX个柱体
     * */
    private static int MAX = 100;

    public static void draw() {
        //得到数据集
        /*XYSeriesCollection dataset = getCpuDataset();
        datasets.add(dataset);

        dataset = getRamDataset();

        datasets.add(dataset);*/
        //绘制图表
        JFreeChart chart = getJFreeChart(getTitle(), getxLabel(), getyLabel(), null,
                PlotOrientation.VERTICAL, true, true, true);
        doEnCoding(chart, getDatasets());




        //ChartFrame 继承 java.swing包  ：图形化
        ChartFrame frame = new ChartFrame(getTitle(), chart);
        //可见属性
        frame.setVisible(true);
        frame.pack();
    }
    /**
     * 生成图表
     * @param title			主标题
     * @param categoryAxisLabel	轴标题
     * @param valueAxisLabel	Y轴标题
     * @param dataset		图表需要的数据 	CategoryDataset接口
     * @param orientation	 	图标的方法(水平或垂直)(PlotOrientation类中常量)
     * @param legend		是否显示图例
     * @param tooltips		是否显示工具提示
     * @param urls			是否产生RUL连接
     * @return
     */
    private static JFreeChart getJFreeChart(String title, String categoryAxisLabel, String valueAxisLabel,
                                            CategoryDataset dataset, PlotOrientation orientation,
                                            Boolean legend, Boolean tooltips, Boolean urls){
        JFreeChart chart = ChartFactory.createBarChart3D(title, categoryAxisLabel, valueAxisLabel,
                dataset, orientation, legend, tooltips, urls);
        return chart;
    }


    public static void clear(){
        getDatasets().clear();
        setDataset(null);
    }


    /**
     * 动态增加数据源。注意，数据源必须提前处理好，形成最简单的Map<K, V>作为提交。
     * 每次增加数据源只能用于增加单条。不可同时增加多条。
     * K要么是Double，要么是Integer。
     * 作为一条数据源，只能管到自身的数据系列的key，而不能掌控title、xLabel和yLabel。
     *
     *
     * 不过barchart不像linechart一个序列对应一个key，barchart是一个柱体对应一个key,也就是单体数字对应一个key，
     * 而且key的添加是在dataset的setValue方法中设置的。
     * @return
     */
    public static <K, V> void addDataset(String key, Map<K, V> map) {

        ArrayList<Dataset> dataList = getDatasets();
        DefaultCategoryDataset dataset = getDataset();
        //new DefaultCategoryDataset();
        if (map instanceof HashMap) {

            //遍历,比如不同任务的等待时间
            Set<Map.Entry<K, V>> set = map.entrySet();
            Iterator iterator = set.iterator();

            int ind = 0;
            //dataset = new DefaultCategoryDataset();

            while (iterator.hasNext()){
                Map.Entry entry = (Map.Entry) iterator.next();

                double d = (double)(entry.getValue());
                int i = (int) entry.getKey();
                dataset.addValue((double)entry.getValue(), key, ""+(int)entry.getKey());
                //dataset.addValue((double)entry.getValue(), key, ""+(int)entry.getKey()+1);
                //dataset.addValue(400, key, ""+1);

                ind++;
                if(ind > getMAX()){
                    break;
                }
                //dataset.add(new Double(entry.getKey().toString()).doubleValue() , (double)entry.getValue());
//                iterator.next();
//                dataset.addValue(100, "北京", "苹果");
//
//                dataset.addValue(200, "北京", "梨子");
//
//                dataset.addValue(300, "北京", "葡萄");
//
//                dataset.addValue(400, "北京", "香蕉");
//
//                dataset.addValue(500, "北京", "荔枝");


            }

            setDataset(dataset);
            if (dataList.isEmpty()) {
                dataList.add(dataset);
            }
            else {
                dataList.clear();
                dataList.add(dataset);
            }
        }
    }

    public static void addDataset(String key, List<? extends Object> list) {

        ArrayList<Dataset> dataList = getDatasets();
        DefaultCategoryDataset dataset = getDataset();
        //new DefaultCategoryDataset();
        //遍历,比如不同任务的等待时间


        int ind = 0;
        //dataset = new DefaultCategoryDataset();

        for (Object o : list){

            double d = (Double) o;
            dataset.addValue(d, key, (list.indexOf(o) + 1) +"");

            ind++;
            if(ind > getMAX()){
                break;
            }
            //dataset.add(new Double(entry.getKey().toString()).doubleValue() , (double)entry.getValue());

        }

        setDataset(dataset);
        if (dataList.isEmpty()) {
            dataList.add(dataset);
        }
        else {
            dataList.clear();
            dataList.add(dataset);
        }
    }

    //限定为单例模式
    private static DefaultCategoryDataset getDataset() {
        if(BarChart.dataset == null){
            return new DefaultCategoryDataset();
        }
        return (DefaultCategoryDataset) BarChart.dataset;
    }

    /**
     * 处理字符集问题 以及 工具类的使用
     * @param chart 图标对象
     */
    private static void doEnCoding(JFreeChart chart, ArrayList<Dataset> datasets){
        //获取图标对象：
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        //获取X轴
        CategoryAxis3D domainAxis = (CategoryAxis3D) plot.getDomainAxis();
        //获取Y轴
        NumberAxis3D numberAxis3D = (NumberAxis3D) plot.getRangeAxis();

        //处理主标题乱码: 需要new一个Font对象，设置字体、样式、大小
        chart.getTitle().setFont(new Font(getFONT(), Font.BOLD, 18));
        //处理子标题乱码：
        chart.getLegend().setItemFont(new Font(getFONT(), Font.BOLD, 18));
        //处理X轴乱码：
        domainAxis.setTickLabelFont(new Font(getFONT(), Font.BOLD, 12)); //x轴上
        domainAxis.setLabelFont(new Font(getFONT(), Font.BOLD, 15)); 	 //x轴外
        //处理Y轴乱码：
        numberAxis3D.setTickLabelFont(new Font(getFONT(), Font.BOLD, 12));
        numberAxis3D.setLabelFont(new Font(getFONT(), Font.BOLD, 15));

        //处理Y轴刻度
        //自动刻度
        numberAxis3D.setAutoTickUnitSelection(true);
        //设置刻度
//        NumberTickUnit size = new NumberTickUnit(1);
//        numberAxis3D.setTickUnit(size);

        BarRenderer3D renderer = null;

        int ind = 0;
        for(Dataset d: getDatasets()){


            plot.setDataset(ind, (CategoryDataset) d);
            //处理柱状图宽度
            renderer = new BarRenderer3D();

            //处理Y轴刻度
            //自动刻度
            //numberAxis.setAutoTickUnitSelection(true);
            //设置刻度
        /*NumberTickUnit size = new NumberTickUnit(1);
        numberAxis.setTickUnit(size);*/

            //获取绘图区域对象
            //LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();

            renderer.setMaximumBarWidth(0.08);
            //处理柱状图上的标识数字以及设置大小字体
            renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            renderer.setBaseItemLabelsVisible(true);
            renderer.setBaseItemLabelFont(new Font(getFONT(), Font.BOLD, 10));



            plot.setRenderer(ind++, renderer);
        }





    }

    public static void setDataset(Dataset dataset) {
        BarChart.dataset = dataset;
    }

    public static int getMAX() {
        return MAX;
    }

    public static void setMAX(int MAX) {
        BarChart.MAX = MAX;
    }

    public static String getFONT() {
        return FONT;
    }

    public static void setFONT(String FONT) {
        BarChart.FONT = FONT;
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String title) {
        BarChart.title = title;
    }

    public static String getxLabel() {
        return xLabel;
    }

    public static void setxLabel(String xLabel) {
        BarChart.xLabel = xLabel;
    }

    public static String getyLabel() {
        return yLabel;
    }

    public static void setyLabel(String yLabel) {
        BarChart.yLabel = yLabel;
    }

    public static ArrayList<Dataset> getDatasets() {
        return datasets;
    }

    public static void setDatasets(ArrayList<Dataset> datasets) {
        BarChart.datasets = datasets;
    }
}
