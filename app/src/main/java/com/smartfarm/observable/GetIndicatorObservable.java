package com.smartfarm.observable;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartfarm.bean.TypeBean;
import com.smartfarm.util.Common;
import com.smartfarm.util.Protocol;

import java.net.URLEncoder;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
//手机客户端向云端服务器请求,指定农业设施控制方案包含指标类型
public class GetIndicatorObservable {

	private static String getPayload(String code){
		try {
			return "data="+ URLEncoder.encode("{\"token\":\"" + Common.token + "\",\"code\":\"" + code + "\"}",
					"UTF-8");
		} catch (Exception e) {
			Log.e("GetIndicatorObservavle", e.getMessage().toString());
		}
		return "";
	}
	public static Observable<List<TypeBean>> createObservable(String code){
		Observable<String> httpObservable = HttpObservable.createObservable(
				Protocol.GETINDICATORTYPES_URL, getPayload(code));
		Observable<List<TypeBean>> getIndicatorObservable = httpObservable
				.map(new Func1<String, List<TypeBean>>() {
			@Override
			public List<TypeBean> call(String response) {
				Log.d("response", response);
				List<TypeBean> typeBean;
				if(response.contains("name")){
					Gson gson = new Gson();
					try {
						typeBean = gson.fromJson(response, new TypeToken<List<TypeBean>>() {}.getType());
						return typeBean;
					} catch (Exception e) {
						return null;
					}
				}else{
					return null;
				}
			}
		});
		return getIndicatorObservable;
	}
}
