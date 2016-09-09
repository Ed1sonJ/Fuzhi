package com.smartfarm.observable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import rx.Observable;
import rx.functions.Func1;
import android.util.Log;

import com.smartfarm.util.Common;
import com.smartfarm.util.Protocol;

public class InterveneObservable {

	private static String getPayload(String code, String startTime, String endTime, String protocolKey,
			String value, String upper, String lower){
		try {
			String data = "{\"token\":\"" + Common.token + "\",\"code\":\"" + code
					+ "\",\"startTime\":\"" + startTime + "\",\"endTime\":\""
					+ endTime + "\",\"protocolKey\":\"" + protocolKey + "\",\"value\":\"" + value
					+ "\",\"upperValue\":\"" + upper + "\",\"lowerValue\":\""
					+ lower + "\"}";
			System.out.println("人工干预上传信息："+data);
			return "data="+URLEncoder.encode(data,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static Observable<Boolean> create(String code, String startTime, String endTime, String protocolKey,
			String value, String upper, String lower){
		return HttpObservable.createObservable(Protocol.INTERVENE_URL, getPayload(code,
				startTime,endTime,protocolKey,value,upper,lower))
				.map(new Func1<String,Boolean>(){
					@Override
					public Boolean call(String response) {
						System.out.println("人工干预返回结果："+response);
						if(response.contains("ok")){
							return true;
						}else{
							return false;
						}
					}				
				});
	}
}
