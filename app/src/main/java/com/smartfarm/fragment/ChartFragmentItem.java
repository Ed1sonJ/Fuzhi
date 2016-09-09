package com.smartfarm.fragment;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.smartfarm.activity.R;
import com.smartfarm.view.MyMarkerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChartFragmentItem extends RelativeLayout {

	private Context context;
	private View view;
	private LineChart lineChart;
	private TextView chartValueName;
	private String name = "";
	//单位
	private String unit = "";
	private boolean showAnimation = true;
	
	
	public ChartFragmentItem(Context context) {
		super(context);
		this.context = context;
		init();
	}

	private void init() {
		setContentView();
		findViewById();
		setChartView(lineChart);
		// loadData(sensorCode, startDate, endDate);
	}

	public void setContentView() {
		LayoutInflater inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.fragment_chart_item, null);
		addView(view);
	}

	private void findViewById() {
		lineChart = (LineChart) view.findViewById(R.id.line_chart);
		chartValueName = (TextView) view.findViewById(R.id.chart_value_name);
	}
	//x轴(XAxis)，y轴(YAxis)，表头(Lenged)，标识(MarkerView).
	private void setChartView(LineChart lineChart) {
		// 如果没有数据的时候，会显示这个，类似listview的emtpyview
		lineChart.setNoDataText(" ");
		//画格子
		lineChart.setDrawGridBackground(false);
		//Legend表头，设置表头不可用
		lineChart.getLegend().setEnabled(false);
		//数据描述字体的颜色
		lineChart.setDescriptionColor(context.getResources().getColor(
				R.color.black));
		//数据描述字体的大小
		lineChart.setDescriptionTextSize(14);
		// 设置是否可以触摸
		lineChart.setTouchEnabled(true);
		// 是否可以拖拽
        lineChart.setDragEnabled(false);
		//是否可以缩放
        lineChart.setScaleEnabled(false);
 
        //硬件加速
		lineChart.setHardwareAccelerationEnabled(true);

		//x轴样式设置
		XAxis xAxis = lineChart.getXAxis();
		//设置是否显示横轴表格
		xAxis.setDrawGridLines(true);
		xAxis.setGridColor(Color.BLACK);
		xAxis.setAxisLineColor(Color.BLACK);
		xAxis.setTextColor(Color.BLACK);
		xAxis.setTextSize(14);
		// 设置x轴在底部显示
		xAxis.setPosition(XAxisPosition.BOTTOM);
		//定制X轴起点和终点Label不能超出屏幕。
		xAxis.setAvoidFirstLastClipping(true);

		//Y轴样式设置
		YAxis leftAxis = lineChart.getAxisLeft();
		leftAxis.setGridColor(Color.BLACK);
		leftAxis.setDrawGridLines(false);
		leftAxis.setAxisLineColor(Color.BLACK);
		leftAxis.setTextColor(Color.BLACK);
		leftAxis.setTextSize(14);
		//设置右侧Y轴
		YAxis rightAxis = lineChart.getAxisRight();
		rightAxis.setEnabled(false);
	}

	private LineData generateLineData(Map<String,List<String>> cache1,List<Entry> cache2){
		name = cache1.get("name").get(0);
		unit = cache1.get("unit").get(0);

		List<String> xValues = cache1.get("time");
		//Entry,LineChart开源库中的一个class有mVal，mXIndex属性
		List<Entry> dataValues = cache2;
		//MarkerView表示，当达到什么情况就会出现一个view,要显示的这个view就是R.layout.custom_marker_view
        MyMarkerView mv = new MyMarkerView(context, R.layout.custom_marker_view,xValues);
        lineChart.setMarkerView(mv);
		
//		List<String> lowSchema = cache1.get("lowSchema");
//		List<String> upSchema = cache1.get("upSchema");
//		YAxis leftAxis = lineChart.getAxisLeft();
//		if(lowSchema.get(0) != null && lowSchema.get(0) != ""){
//			LimitLine low = new LimitLine(Float.parseFloat(lowSchema.get(0)), "下限值");
//			leftAxis.addLimitLine(low);
//		}
//		if(upSchema.get(0) != null && upSchema.get(0) != ""){
//			LimitLine max = new LimitLine(Float.parseFloat(upSchema.get(0)),"上限值");
//			leftAxis.addLimitLine(max);
//		}

		ArrayList<LineDataSet> lineDataSets = new ArrayList<LineDataSet>();
		//曲线样式设置，y轴的数据集合，数据类型List<Entry>
		LineDataSet dataSet = new LineDataSet(dataValues, "");
		dataSet.setColor(context.getResources().getColor(R.color.blue_100));
		dataSet.setLineWidth(1.5f);
		dataSet.setDrawCircles(false);
		////设置是否显示点值
		dataSet.setDrawValues(false);
		//设置曲线样式为cubie
		dataSet.setDrawCubic(true);
		////置曲线顺滑度
		dataSet.setCubicIntensity(0.1f);
		lineDataSets.add(dataSet);
		//数据集x轴与y轴
		LineData lineData = new LineData(xValues, lineDataSets);
		return lineData;
	}
	
	public void showLineChart(Map<String,List<String>> cache1,List<Entry> cache2,String Date) {
		LineData lineData = generateLineData(cache1, cache2);
		lineChart.setData(lineData);		
		if (showAnimation) {
			////从X轴进入的动画，2s,效果Easing.EasingOption.EaseInOutQuad
			lineChart.animateX(2000, Easing.EasingOption.EaseInOutQuad);
			showAnimation = false;
		}
		//标题
		chartValueName.setText(name+"/"+Date);
		//单位
		lineChart.setDescription("单位:" + unit);
	}
	
	public void showEmptyText() {
		chartValueName.setText("没有历史数据");
	}
}
