package com.smartfarm.observable;

import com.smartfarm.util.Common;
import com.smartfarm.util.Protocol;

import java.net.URLEncoder;
import java.util.List;

import rx.Observable;

public class GetLocationObservable {

	private static String getPayload(List<String> codeList){
		String codes = convertListToStrings(codeList);
		try {
			return "data="+ URLEncoder.encode("{\"token\":\"" + Common.token + "\",\"code\":\"" + codes + "\"}",
					"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private static String convertListToStrings(List<String> codeList){
		String strings = "";
		for (int i = 0; i < codeList.size() - 1; i++) {
			strings += codeList.get(i)+",";
		}
		if(codeList.size() - 1 >= 0)
			strings += codeList.get(codeList.size() - 1);
		return strings;
	}
	
	public static Observable<String> createObservable(List<String> codeList){
		Observable<String> httpObservable = HttpObservable.createObservable(
				Protocol.GETLOCATION, getPayload(codeList));

		System.out.println(Protocol.GETLOCATION);
		System.out.println(getPayload(codeList));

		return httpObservable;
	}
}
