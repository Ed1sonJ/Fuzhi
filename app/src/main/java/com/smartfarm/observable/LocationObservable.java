package com.smartfarm.observable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import rx.Observable;
import rx.functions.Func1;
import android.util.Log;

import com.smartfarm.util.Common;
import com.smartfarm.util.Protocol;

public class LocationObservable {
	private static String getPayload(String content,String longitude,String latitude){
		try {
			return "data="+URLEncoder.encode("{\"token\":\"" + Common.token + "\",\"content\":\"" + content 
					+ "\",\"longitude\":\"" + longitude + "\",\"latitude\":\"" + latitude + "\"}","UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static Observable<Boolean> create(String content,String longitude,String latitude){
		return HttpObservable.createObservable(Protocol.LOCATION, getPayload(content,longitude,latitude))
				.map(new Func1<String,Boolean>(){
					@Override
					public Boolean call(String response) {
						if(response.contains("ok")){
							return true;
						}else{
							return false;
						}
					}				
				});
	}
}
