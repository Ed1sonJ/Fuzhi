package com.smartfarm.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.smartfarm.model.Equipment;

import java.util.ArrayList;

public class LocationActivity extends Activity {

	private TextView tvEquipmentName;
	private TextView tvEquipmentCode;
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private CardView mCardView;
	private TextView noLocationText;
	private ArrayList<String> equipmentCodes = new ArrayList<>();
	private ArrayList<Double> listLongitude = new ArrayList<>();
	private ArrayList<Double> listLatitude = new ArrayList<>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_location);
		Init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	private void Init() {
		tvEquipmentName = (TextView) findViewById(R.id.location_equiment_name);
		tvEquipmentCode = (TextView) findViewById(R.id.location_equiment_code);
		mMapView = (MapView) findViewById(R.id.BaiduMapViewLocation);
		mBaiduMap = mMapView.getMap();
		mCardView = (CardView)findViewById(R.id.BaiduMapViewCardView);
		noLocationText = (TextView)findViewById(R.id.no_location_text);
		getInfoFromLastActivity();
		setTitleInfo();
		if(!listLongitude.isEmpty() && !listLatitude.isEmpty()){
			hideNoLocationText();
			SetLocation(listLongitude.get(0), listLatitude.get(0),equipmentCodes.get(0));
			SetMarker(listLongitude, listLatitude,equipmentCodes);
		}else{
			showNoLocationText();
			return;
		}
	}

	//��ȡactivity��������Intent
	private void getInfoFromLastActivity(){
		Intent intent = getIntent();
		ArrayList<String> equipmentCodesTmp = intent.getStringArrayListExtra("equipmentCode");
		ArrayList<String> longitudeTmp = intent.getStringArrayListExtra("longitude");
		ArrayList<String> latitudeTmp = intent.getStringArrayListExtra("latitude");
		for (int i = 0; i < longitudeTmp.size(); i++) {
			if(!(Double.parseDouble(longitudeTmp.get(i)) == 0 && Double.parseDouble(latitudeTmp.get(i)) == 0)){
				equipmentCodes.add(equipmentCodesTmp.get(i));
				listLongitude.add(Double.parseDouble(longitudeTmp.get(i)));
				listLatitude.add(Double.parseDouble(latitudeTmp.get(i)));
			}
		}
	}

	//���ñ�����Ϣ
	private void setTitleInfo(){
		if (equipmentCodes.size() > 0)
			tvEquipmentName.setText(Equipment.getEquipmentName(this,equipmentCodes.get(0)));
	}

	//û�ж�λ����
	private void showNoLocationText(){
		mCardView.setVisibility(View.GONE);
		noLocationText.setVisibility(View.VISIBLE);
	}

	//�ж�λ����
	private void hideNoLocationText(){
		mCardView.setVisibility(View.VISIBLE);
		noLocationText.setVisibility(View.GONE);
	}

	//ȡ��һ����λ�����ڶ�λͼ�㶨λ
	private void SetLocation(double longitude,double latitude,String text){
		// ������λͼ��  
		mBaiduMap.setMyLocationEnabled(true);  
		// ���춨λ����  
		MyLocationData locData = new MyLocationData.Builder()  
		    // �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360  
		    .direction(100).latitude(latitude)  
		    .longitude(longitude).build();  
		// ���ö�λ����  
		mBaiduMap.setMyLocationData(locData);  
		// ���ö�λͼ������ã���λģʽ���Ƿ���������Ϣ���û��Զ��嶨λͼ�꣩  
		BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory  
		    .fromResource(R.drawable.icon_geo);
		MyLocationConfiguration config = new MyLocationConfiguration(
				com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING,
				false, mCurrentMarker);
		mBaiduMap.setMyLocationConfigeration(config);
		LatLng point = new LatLng(latitude, longitude);  
        OverlayOptions ooText = new TextOptions().bgColor(0xAAFFFF00)
                .fontSize(24).fontColor(0xFFFF00FF).text(text).rotate(-30)
                .position(new LatLng(latitude+0.0002,longitude+0.0002));
        mBaiduMap.addOverlay(ooText);
		// ������Ҫ��λͼ��ʱ�رն�λͼ��  
		//mBaiduMap.setMyLocationEnabled(false);
	}

	//���ݶ�λ������ʾ���
	private void SetMarker(ArrayList<Double> listLongitude,ArrayList<Double> listLatitude,ArrayList<String>equipmentCode){
		for (int i = 1; i < listLatitude.size(); i++) {
			//����Maker�����  
			LatLng point = new LatLng(listLatitude.get(i), listLongitude.get(i));  
			//����Markerͼ��  
			BitmapDescriptor bitmap = BitmapDescriptorFactory  
			    .fromResource(R.drawable.icon_geo);
			//����MarkerOption�������ڵ�ͼ�����Marker  
			OverlayOptions option = new MarkerOptions()  
			    .position(point)  
			    .icon(bitmap);  
	        OverlayOptions ooText = new TextOptions().bgColor(0xAAFFFF00)
	                .fontSize(24).fontColor(0xFFFF00FF).text(equipmentCode.get(i)).rotate(-30)
	                .position(new LatLng(listLatitude.get(i)+0.0002, listLongitude.get(i)+0.0002));
			//�ڵ�ͼ�����Marker������ʾ  
			((MarkerOptions) option).animateType(MarkerAnimateType.grow);
			mBaiduMap.addOverlay(option);
			mBaiduMap.addOverlay(ooText);
		}
	}
}
