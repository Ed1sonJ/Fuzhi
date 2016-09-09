package com.smartfarm.observable;

import android.util.Log;

import com.smartfarm.util.Base64Util;
import com.smartfarm.util.DesUtil;
import com.smartfarm.util.Protocol;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;

public class LoginObservable {
	private static String getToken(String username,String password){
		String token = null;
		String data = username + "#" + password;
		try {
			//加密
			token = Base64Util.encode(DesUtil.encrypt(data.getBytes() , Protocol.key));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return token;
	}
	private static String getPayload(String userName,String passWord){
		//获得加密后的String
		String token = getToken(userName, passWord);
		Log.d("token", token);
		try {
			//URL加密，解码是要对应编码utf-8
			return "data="+URLEncoder.encode("{\"token\":\"" + token + "\"}", "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	//map变换：就是把事件对象转换成自己目的事件对象，即把String转成Map<String,String>
	public static Observable<Map<String,String>> create(final String username,final String password){
		return HttpObservable.createObservable(Protocol.LOGIN_URL, getPayload(username, password))
				.map(new Func1<String,Map<String,String>>(){
					@Override
					public Map<String,String> call(String response) {
						if(response.contains("role") && response.contains("name")){
							Map<String,String> hasLogin = new HashMap<String, String>();
							hasLogin.put("token", getToken(username, password));
							return hasLogin;
						}else{
							return null;
						}
					}				
				});
	}
}
