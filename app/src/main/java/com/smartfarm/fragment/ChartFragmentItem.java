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
	//��λ
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
	//x��(XAxis)��y��(YAxis)����ͷ(Lenged)����ʶ(MarkerView).
	private void setChartView(LineChart lineChart) {
		// ���û�����ݵ�ʱ�򣬻���ʾ���������listview��emtpyview
		lineChart.setNoDataText(" ");
		//������
		lineChart.setDrawGridBackground(false);
		//Legend��ͷ�����ñ�ͷ������
		lineChart.getLegend().setEnabled(false);
		//���������������ɫ
		lineChart.setDescriptionColor(context.getResources().getColor(
				R.color.black));
		//������������Ĵ�С
		lineChart.setDescriptionTextSize(14);
		// �����Ƿ���Դ���
		lineChart.setTouchEnabled(true);
		// �Ƿ������ק
        lineChart.setDragEnabled(false);
		//�Ƿ��������
        lineChart.setScaleEnabled(false);
 
        //Ӳ������
		lineChart.setHardwareAccelerationEnabled(true);

		//x����ʽ����
		XAxis xAxis = lineChart.getXAxis();
		//�����Ƿ���ʾ������
		xAxis.setDrawGridLines(true);
		xAxis.setGridColor(Color.BLACK);
		xAxis.setAxisLineColor(Color.BLACK);
		xAxis.setTextColor(Color.BLACK);
		xAxis.setTextSize(14);
		// ����x���ڵײ���ʾ
		xAxis.setPosition(XAxisPosition.BOTTOM);
		//����X�������յ�Label���ܳ�����Ļ��
		xAxis.setAvoidFirstLastClipping(true);

		//Y����ʽ����
		YAxis leftAxis = lineChart.getAxisLeft();
		leftAxis.setGridColor(Color.BLACK);
		leftAxis.setDrawGridLines(false);
		leftAxis.setAxisLineColor(Color.BLACK);
		leftAxis.setTextColor(Color.BLACK);
		leftAxis.setTextSize(14);
		//�����Ҳ�Y��
		YAxis rightAxis = lineChart.getAxisRight();
		rightAxis.setEnabled(false);
	}

	private LineData generateLineData(Map<String,List<String>> cache1,List<Entry> cache2){
		name = cache1.get("name").get(0);
		unit = cache1.get("unit").get(0);

		List<String> xValues = cache1.get("time");
		//Entry,LineChart��Դ���е�һ��class��mVal��mXIndex����
		List<Entry> dataValues = cache2;
		//MarkerView��ʾ�����ﵽʲô����ͻ����һ��view,Ҫ��ʾ�����view����R.layout.custom_marker_view
        MyMarkerView mv = new MyMarkerView(context, R.layout.custom_marker_view,xValues);
        lineChart.setMarkerView(mv);
		
//		List<String> lowSchema = cache1.get("lowSchema");
//		List<String> upSchema = cache1.get("upSchema");
//		YAxis leftAxis = lineChart.getAxisLeft();
//		if(lowSchema.get(0) != null && lowSchema.get(0) != ""){
//			LimitLine low = new LimitLine(Float.parseFloat(lowSchema.get(0)), "����ֵ");
//			leftAxis.addLimitLine(low);
//		}
//		if(upSchema.get(0) != null && upSchema.get(0) != ""){
//			LimitLine max = new LimitLine(Float.parseFloat(upSchema.get(0)),"����ֵ");
//			leftAxis.addLimitLine(max);
//		}

		ArrayList<LineDataSet> lineDataSets = new ArrayList<LineDataSet>();
		//������ʽ���ã�y������ݼ��ϣ���������List<Entry>
		LineDataSet dataSet = new LineDataSet(dataValues, "");
		dataSet.setColor(context.getResources().getColor(R.color.blue_100));
		dataSet.setLineWidth(1.5f);
		dataSet.setDrawCircles(false);
		////�����Ƿ���ʾ��ֵ
		dataSet.setDrawValues(false);
		//����������ʽΪcubie
		dataSet.setDrawCubic(true);
		////������˳����
		dataSet.setCubicIntensity(0.1f);
		lineDataSets.add(dataSet);
		//���ݼ�x����y��
		LineData lineData = new LineData(xValues, lineDataSets);
		return lineData;
	}
	
	public void showLineChart(Map<String,List<String>> cache1,List<Entry> cache2,String Date) {
		LineData lineData = generateLineData(cache1, cache2);
		lineChart.setData(lineData);		
		if (showAnimation) {
			////��X�����Ķ�����2s,Ч��Easing.EasingOption.EaseInOutQuad
			lineChart.animateX(2000, Easing.EasingOption.EaseInOutQuad);
			showAnimation = false;
		}
		//����
		chartValueName.setText(name+"/"+Date);
		//��λ
		lineChart.setDescription("��λ:" + unit);
	}
	
	public void showEmptyText() {
		chartValueName.setText("û����ʷ����");
	}
}
