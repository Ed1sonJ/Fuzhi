package com.smartfarm.view;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.Utils;
import com.smartfarm.activity.R;

import java.util.List;

public class MyMarkerView extends MarkerView{
	private TextView tvContent;
	private TextView tvDate;
	private List<String> xValues;
	private String date = "";
	
	public MyMarkerView(Context context, int layoutResource,List<String> xValues) {
		super(context, layoutResource);
		tvContent = (TextView) findViewById(R.id.tvContent);
		tvDate = (TextView) findViewById(R.id.tvDate);
		this.xValues = xValues;
	}
	//����Ҫ���Ƶ� MarkerView ��x���ƫ��λ�á�
	@Override
	public int getXOffset() {
		return 0;
	}
	//����Ҫ���Ƶ� MarkerView ��y���ƫ��λ�á�
	@Override
	public int getYOffset() {
		return 0;
	}
	//ÿ�� MarkerView �ػ�˷������ᱻ���ã���������ʾ������
	@Override
	public void refreshContent(Entry e, int arg1) {
		
        if(e.getXIndex() < xValues.size()){
        	date = xValues.get(e.getXIndex());
        }
		
        if (e instanceof CandleEntry) {

            CandleEntry ce = (CandleEntry) e;

            tvContent.setText("" + Utils.formatNumber(ce.getHigh(), 0, true));
            
        } else {

            tvContent.setText("" + Utils.formatNumber(e.getVal(), 0, true));
        }
        tvDate.setText(date);
	}
	

}
