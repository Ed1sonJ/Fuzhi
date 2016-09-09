package com.smartfarm.observable;

import java.net.URLEncoder;
import java.util.List;

import rx.Observable;
import android.util.Log;

import com.smartfarm.util.Common;
import com.smartfarm.util.Protocol;

public class HasIndicatorObservable {
	private static String getPayload(String code,String protocolKey){
		String content = "{\"token\":\"" + Common.token + "\",\"code\":\"" + code + 
				"\",\"protocolKey\":\"" + protocolKey +"\"}";
		try {
			return "data="+ URLEncoder.encode("{\"token\":\"" + Common.token + "\",\"code\":\"" + code + 
					"\",\"protocolKey\":\"" + protocolKey +"\"}",
					"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	
	public static Observable<String> createObservable(String code,String protocolKey){
		Observable<String> httpObservable = HttpObservable.createObservable(
				Protocol.HASINDICATORTYPE, getPayload(code,protocolKey));
		return httpObservable;
	}
}
