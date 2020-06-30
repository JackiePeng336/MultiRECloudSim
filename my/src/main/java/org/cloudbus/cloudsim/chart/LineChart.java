package org.cloudbus.cloudsim.chart;

import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.*;
import java.util.List;

public class LineChart {


    private static String FONT = "TimesRoman";

    //private static ArrayList<XYSeriesCollection> dataset;

    private static String title = "";

    private static String xLabel = "";

    private static String yLabel = "";

    /**
     * 最多MAX条线条
     * */
    private static int MAX = 100;

    private static ArrayList<Dataset> datasets = new ArrayList<>();

    public static XYSeriesCollection collection ;

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
                                            XYSeriesCollection dataset, PlotOrientation orientation,
                                            Boolean legend, Boolean tooltips, Boolean urls){

        JFreeChart chart = ChartFactory.createXYLineChart(title, categoryAxisLabel, valueAxisLabel,
                dataset, orientation, legend, tooltips, urls);
        return chart;
    }

    public static void clear(){
        getDatasets().clear();
        setCollection(null);
    }

    /**
     * 同样地，要设计成只添加单条
     * List的下标index作为横坐标
     * */
    public static <K, V> void addDatasetsByList(String key, List<? extends Object> list){

        ArrayList<Dataset> datasetList = getDatasets();
        XYSeries series = null;
        XYSeriesCollection collection = getCollection();

        setCollection(collection);

        int ind = 0;
        series = new XYSeries(key);

        //比如遍历VM的CPU历史记录列表
        for (Object o : list) {

            //o其实是Double类型来的

            double num = (Double) o;


            //检测到异常值，直接退出
             if (num > 1.0) {
                return;
            }
            else {
                series.add(new Double(list.indexOf(o)).doubleValue() , num);
            }

            ind++;

        }


        collection.addSeries(series);


        if(datasetList.size() > 0){
            datasetList.clear();
            datasetList.add(collection);
        }
        else {
            datasetList.add(collection);
        }
        setDatasets(datasetList);
        //datasetList.add(collection);

    }

    //限定为单例模式
    private static XYSeriesCollection getCollection() {
        if(LineChart.collection == null){
            return new XYSeriesCollection();
        }
        return LineChart.collection;
    }

    /**
     *
     * 动态增加数据源。注意，数据源必须提前处理好，形成最简单的Map<K, V>作为提交。
     * 每次增加数据源只能用于增加单条。不可同时增加多条。
     * K要么是Double，要么是Integer。
     * 作为一条数据源，只能管到自身的数据系列的key，而不能掌控title、xLabel和yLabel。
     * */
    //必须Map<K, V>
    public static <K, V> void addDatasetsByMap(String key, Map<K, V> o){
        ArrayList<Dataset> datasetList = getDatasets();
        XYSeries series = null;
        XYSeriesCollection collection = null;



        if(o instanceof HashMap){


            //添加hostToSla这样的Map<Integer, Double>类型
            if(o.entrySet().iterator().next().getValue() instanceof Double){


                //遍历,比如不同任务的等待时间
                Set<Map.Entry<K, V>> set = o.entrySet();
                Iterator iterator = set.iterator();

                int ind = 0;
                series = new XYSeries(key);
                while (iterator.hasNext()){
                    Map.Entry entry = (Map.Entry) iterator.next();



                    series.add(new Double(entry.getKey().toString()).doubleValue() , (double)entry.getValue());

                    ind++;


                }
                collection = new XYSeriesCollection(series);

            }

            /**
             *
             * 下面这个add才是核心
             * */
            //series.add();

            datasetList.add(collection);
        }


    }

    public static ArrayList<Dataset> getDatasets(){



        return datasets;
    }


    /**
     * 返回图标需要的数据
     * @return
     */


    /**
     * 处理字符集问题 以及 工具类的使用
     * @param chart 图标对象
     * @param datasets
     */
    private static void doEnCoding(JFreeChart chart, ArrayList<Dataset> datasets){
        String font = FONT;

        //获取图表对象：
        XYPlot plot = (XYPlot) chart.getPlot();
        //获取X轴
        ValueAxis domainAxis = plot.getDomainAxis();
        //获取Y轴
        NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();

        domainAxis.setAutoRange(true);

        numberAxis.setAutoRangeIncludesZero(true);

        //处理主标题乱码: 需要new一个Font对象，设置字体、样式、大小
        chart.getTitle().setFont(new Font(getFONT(), Font.BOLD, 18));
        //处理子标题乱码：
        chart.getLegend().setItemFont(new Font(getFONT(), Font.BOLD, 18));
        //处理X轴乱码：
        domainAxis.setTickLabelFont(new Font(getFONT(), Font.BOLD, 12)); //x轴上
        domainAxis.setLabelFont(new Font(getFONT(), Font.BOLD, 15)); 	 //x轴外
        //处理Y轴乱码：
        numberAxis.setTickLabelFont(new Font(getFONT(), Font.BOLD, 12));
        numberAxis.setLabelFont(new Font(getFONT(), Font.BOLD, 15));

        //获取绘图区域对象
        StandardXYItemRenderer renderer ;

        int ind = 0;
        for(Dataset d: getDatasets()){


            plot.setDataset(ind, (XYSeriesCollection) d);
            renderer = new StandardXYItemRenderer();
            //处理Y轴刻度
            //自动刻度
            //numberAxis.setAutoTickUnitSelection(true);
            //设置刻度
        /*NumberTickUnit size = new NumberTickUnit(1);
        numberAxis.setTickUnit(size);*/

            //获取绘图区域对象
            //LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();

            //处理柱状图上的标识数字以及设置大小字体
            renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
            renderer.setBaseItemLabelsVisible(false);
            renderer.setBaseItemLabelFont(new Font(font, Font.BOLD, 10));
            //处理折点处的图形
            renderer.setSeriesShape(ind, new Rectangle(3,3));//Shape 形状
            renderer.setBaseShapesVisible(true);// setSeriesShapesVisible(ind, true);
            plot.setRenderer(ind++, renderer);
        }




    }

    public static void setCollection(XYSeriesCollection collection) {
        LineChart.collection = collection;
    }

    public static int getMAX() {
        return MAX;
    }

    public static void setMAX(int MAX) {
        LineChart.MAX = MAX;
    }

    public static String getFONT() {
        return FONT;
    }

    public static void setFONT(String FONT) {
        LineChart.FONT = FONT;
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String title) {
        LineChart.title = title;
    }

    public static String getxLabel() {
        return xLabel;
    }

    public static void setxLabel(String xLabel) {
        LineChart.xLabel = xLabel;
    }

    public static String getyLabel() {
        return yLabel;
    }

    public static void setyLabel(String yLabel) {
        LineChart.yLabel = yLabel;
    }

    public static void setDatasets(ArrayList<Dataset> datasets) {
        LineChart.datasets = datasets;
    }
}
