package com.smartfarm.fragmentUtil;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.smartfarm.observable.GetLocationObservable;
import com.smartfarm.observable.LocationObservable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.schedulers.Schedulers;

public class UploadAndDownloadGPS {

	public LocationClient mLocationClient = null;
	private String scanResult = "";
	GPSLisaner gpsLisaner;
	public BDLocationListener myListener = new MyLocationListener();

	public interface GPSLisaner{
		void uploadGPSsuccess(Map<String,ArrayList<String>> result);
		void uploadGPSfailure();
		void uploadGPSError();
	}
	
	public void getLocationAndSend(LocationClient locationClient, String scanResult) {
		this.scanResult = scanResult;
		mLocationClient = locationClient;
		mLocationClient.registerLocationListener(myListener); // ע���������
		initLocation();
		//��ʼ��λ
		mLocationClient.start();
	}

	private void initLocation() {
		//LocationClientOption���ö�λ��ʽ�������Ƿ����û��棬ʹ��gps��ʱ������
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);
		option.setCoorType("bd09ll");  //�ٶȣ�bd09ll������wgs84
		int span = 1000;
		// ���ö�ʱ��λ��ʱ��������λ����
		option.setScanSpan(span);
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setIsNeedLocationDescribe(true);
		option.setIsNeedLocationPoiList(true);
		option.setIgnoreKillProcess(true);
		mLocationClient.setLocOption(option);
	}
	
	public class MyLocationListener implements BDLocationListener {
		//�����첽���صĵĶ�λ���
        @Override
        public void onReceiveLocation(BDLocation location) {
            sendLocation(location.getLatitude(), location.getLongitude());
            Log.e("location", location.getLatitude()+"/"+location.getLongitude());
        }
	}

	private void sendLocation(double latitude, double longitude) {
		LocationObservable.create(scanResult, longitude + "", latitude + "")
				.subscribeOn(Schedulers.newThread())
				.subscribe(new Subscriber<Boolean>() {
					public void onCompleted() {
						Log.d("sendLocation", "sendLocationOnCompleted");
					}
					public void onError(Throwable arg0) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								System.out.println("�ϴ���λ���ݳ���");
							}
						}).start();
					}

					@Override
					public void onNext(final Boolean response) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								if (response == true){
									System.out.println("�ϴ���λ���ݳɹ�");
								}
								else{
									System.out.println("�ϴ���λ����ʧ��");
								}
							}
						}).start();
					}
				});
	}
	//��ȡλ��
	public void getLocationObservable(List<String> codeList, final GPSLisaner gpsLisaner){
		this.gpsLisaner = gpsLisaner;
		GetLocationObservable.createObservable(codeList).
			subscribeOn(Schedulers.newThread()).
			subscribe(new Subscriber<String>() {	
				@Override
				public void onStart() {
					super.onStart();
				}
				@Override
				public void onCompleted() {
				}
				@Override
				public void onError(Throwable arg0) {
					new Thread(new Runnable() {					
						@Override
						public void run() {
							gpsLisaner.uploadGPSError();
						}
					}).start();
				}
				@Override
				public void onNext(final String response) {
					new Thread(new Runnable() {					
						@Override
						public void run() {

							if(response != null && !response.equals("")){
								gpsLisaner.uploadGPSsuccess(convertLocationsToList(response));
							}else{
								gpsLisaner.uploadGPSfailure();
							}
						}
					}).start();
				}
			});
	}
	
	private Map<String,ArrayList<String>> convertLocationsToList(String locations){
		try {
				locations = locations.substring(locations.indexOf(":")+2);
				int commaIndex = locations.indexOf(",");
				boolean isLogitude = true;
				ArrayList<String> longitude = new ArrayList<String>();
				ArrayList<String> latitude = new ArrayList<String>();
				while(commaIndex != -1){
					if(isLogitude){
						longitude.add(locations.substring(0,commaIndex));
						locations = locations.substring(commaIndex+1);
						commaIndex = locations.indexOf(",");
						isLogitude = false;
					}else{
						latitude.add(locations.substring(0,commaIndex));
						locations = locations.substring(commaIndex+1);
						commaIndex = locations.indexOf(",");
						isLogitude = true;
					}
				}
				if(!isLogitude){
					latitude.add(locations.substring(0,locations.length()-2));
				}
				if(longitude.size() != latitude.size()){
					return null;
				}
				Map<String,ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
				map.put("longitude", longitude);
				map.put("latitude", latitude);
				return map;
		} catch (Exception e) {
				e.printStackTrace();
				return null;
		}
	}
}
