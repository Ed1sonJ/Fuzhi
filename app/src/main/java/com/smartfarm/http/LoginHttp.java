package com.smartfarm.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smartfarm.util.Base64Util;
import com.smartfarm.util.DesUtil;
import com.smartfarm.util.HttpUtil;
import com.smartfarm.util.Protocol;

public class LoginHttp {

	private static int LOADING = 0x00;
	private static int SUCCESS = 0x01;
	private static int FAIL = 0x02;

	private String json;
	private String token;
	private String username;

	private MyListener myListener = null;

	public interface MyListener {

		public void loading();

		public void success(String token, String username);

		public void fail();

	}

	public void request(String username, String password, MyListener myListener) {
		this.myListener = myListener;
		this.username = username;
		try {
			toJSON(username, password);
		} catch (Exception e) {
			handler.sendEmptyMessage(FAIL);
			e.printStackTrace();
			return;
		}
		Thread thread = new Thread(new Runnable() {
			public void run() {
				handler.sendEmptyMessage(LOADING);
				try {
					post();
				} catch (Exception e) {
					handler.sendEmptyMessage(FAIL);
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (myListener != null) {
				if (msg.what == LOADING) {
					myListener.loading();
				}
				if (msg.what == SUCCESS) {
					myListener.success(token, username);
				}
				if (msg.what == FAIL) {
					myListener.fail();
				}
			}
		}
	};

	private void toJSON(String username, String password) throws Exception {
		String data = username + "#" + password;

		try {
			token = Base64Util.encode(DesUtil.encrypt(data.getBytes(),
					Protocol.key));// 根据键值进行des加密
			Log.d("token", token);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		token = Base64Util
				.encode(DesUtil.encrypt(data.getBytes(), Protocol.key));
		json = "{\"token\":\"" + token + "\"}";
	}

	private void post() throws Exception {
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		pairs.add(new BasicNameValuePair("data", json));
		String reponse = HttpUtil.httpPost(new DefaultHttpClient(),
				Protocol.LOGIN_URL, pairs, "");		
		Log.d("登录数据", reponse);
		if (reponse.contains("role")) {
			handler.sendEmptyMessage(SUCCESS);
		} else {
			handler.sendEmptyMessage(FAIL);
		}
	}
}
