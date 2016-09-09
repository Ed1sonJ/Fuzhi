package com.smartfarm.observable;

import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import com.smartfarm.util.Protocol;

import rx.Observable;
import rx.functions.Func1;

public class RegisterObservable {
	private static String getLoadpay(String name,String username,String phone,String password){
		try {
			return "data="+URLEncoder.encode("{\"name\":\"" + name + "\",\"username\":\"" + username
					+ "\",\"phone\":\"" + phone + "\",\"password\":\"" + password
					+ "\"}","UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static Observable<Boolean> create(String name,String username,String phone,String password){
		Observable<Boolean> httpObservable = HttpObservable.createObservable(Protocol.REGISTER_URL, 
				getLoadpay(name, username, phone, password)).map(new Func1<String, Boolean>() {

					@Override
					public Boolean call(String response) {
						if (response == null)
							return false;
						JSONObject json;
						try {
							json = new JSONObject(response);
							if (json.get("errmsg").equals("ok"))
								return true;
							else
								return false;
						} catch (JSONException e) {
							e.printStackTrace();
							return false;
						}
					}
				});
		return httpObservable;
	}
}
