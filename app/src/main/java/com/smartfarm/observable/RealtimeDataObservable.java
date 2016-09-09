package com.smartfarm.observable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartfarm.bean.RealtimeDataBean;
import com.smartfarm.util.Protocol;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public class RealtimeDataObservable {
	private static String getPayload(String token, String code) {
		try {
			return "data="
					+ URLEncoder.encode("{\"token\":\"" + token + "\","
							+ "\"code\":" + "\"" + code + "\"}", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Observable<List<RealtimeDataBean>> createObservable(
			final String token, final String code) {
		Observable<String> httpObservable = HttpObservable.createObservable(
				Protocol.GET_REALTIME_DATA_URL, getPayload(token, code));
		Observable<List<RealtimeDataBean>> realtimeDataObservable = httpObservable
				.map(new Func1<String, List<RealtimeDataBean>>() {
					@Override
					public List<RealtimeDataBean> call(String response) {
						List<RealtimeDataBean> beans = new Gson().fromJson(
								response,
								new TypeToken<List<RealtimeDataBean>>() {
								}.getType());
						return beans;
					}
				});
		return realtimeDataObservable;
	}
}
