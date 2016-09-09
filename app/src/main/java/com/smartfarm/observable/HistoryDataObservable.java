package com.smartfarm.observable;

import java.net.URLEncoder;

import rx.Observable;
import rx.functions.Func1;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartfarm.bean.ChartBean;
import com.smartfarm.util.Common;
import com.smartfarm.util.Protocol;

public class HistoryDataObservable {
	private static String getPayload(String code,String startDate,String endDate){
		try {
			return "data="+ URLEncoder.encode("{\"token\":\"" + Common.token + "\",\"code\":\"" + code
					+ "\",\"startDate\":\"" + startDate + "\",\"endDate\":\""
					+ endDate + "\"}","UTF-8");
		} catch (Exception e) {
			Log.e("HistoryDataObservavle", e.getMessage().toString());
		}
		return "";
	}
	public static Observable<ChartBean> createObservable(String code,String startDate,String endDate){
		Observable<String> httpObservable = HttpObservable.createObservable(
				Protocol.GETHISTORYDATA_URL, getPayload(code,startDate,endDate));
		Observable<ChartBean> historyDataObservable = httpObservable
				.map(new Func1<String, ChartBean>() {
			@Override
			public ChartBean call(String response) {
				Log.d("response", response);
				ChartBean chartBean;
				if(response.contains("data")){
					Gson gson = new Gson();
					try {
						chartBean = gson.fromJson(response, new TypeToken<ChartBean>() {}.getType());
						return chartBean;
					} catch (Exception e) {
						return null;
					}
				}else{
					return null;
				}
			}
		});
		return historyDataObservable;
	}
}
