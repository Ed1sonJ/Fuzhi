package com.smartfarm.observable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartfarm.bean.EquipmentBean;
import com.smartfarm.util.Protocol;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public class EquipmentsListObservable {
	//加密
	private static String getPayload(String token) {
		try {
			return "data="
					+ URLEncoder.encode("{\"token\":\"" + token + "\"}",
							"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Observable<List<EquipmentBean>> createObservable(
			final String token) {
		System.out.println(token);
		Observable<String> httpObservable = HttpObservable.createObservable(
				Protocol.GETEQUIPMENTS_URL, getPayload(token));
		Observable<List<EquipmentBean>> equipmentsListObservable = httpObservable
				.map(new Func1<String, List<EquipmentBean>>() {
					@Override
					public List<EquipmentBean> call(String response) {
						if (response.contains("无已配置好的农业设施")) {
							return new ArrayList<EquipmentBean>();
						}
						//用gson解析，获取设备信息
						List<EquipmentBean> equipmentsList = new Gson()
								.fromJson(response,
										new TypeToken<List<EquipmentBean>>() {
										}.getType());
						return equipmentsList;
					}
				});
		return equipmentsListObservable;
	}
}
