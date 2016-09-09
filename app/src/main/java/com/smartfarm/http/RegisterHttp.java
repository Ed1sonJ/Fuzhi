package com.smartfarm.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.smartfarm.util.HttpUtil;
import com.smartfarm.util.Protocol;

public class RegisterHttp {

	private static int LOADING = 0x00;
	private static int SUCCESS = 0x01;
	private static int FAIL = 0x02;

	private String name;
	private String username;
	private String phone;
	private String password;
	private String token;

	private MyListener myListener = null;

	public interface MyListener {

		public void loading();

		public void success(String token);

		public void fail();

	}

	public void request(String name, String username, String phone,
			String password, MyListener myListener) {
		this.name = name;
		this.username = username;
		this.phone = phone;
		this.password = password;
		this.myListener = myListener;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				handler.sendEmptyMessage(LOADING);
				try {
					post();
				} catch (ClientProtocolException e) {
					handler.sendEmptyMessage(FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					handler.sendEmptyMessage(FAIL);
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	/**
	 * handler¥¶¿Ì
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (myListener != null) {
				if (msg.what == LOADING) {
					myListener.loading();
				}
				if (msg.what == SUCCESS) {
					myListener.success(token);
				}
				if (msg.what == FAIL) {
					myListener.fail();
				}
			}
		}
	};

	private void post() throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		pairs.add(new BasicNameValuePair("data", getJSON()));
		String response = HttpUtil.httpPost(new DefaultHttpClient(),
				Protocol.REGISTER_URL, pairs, "");
		if (isResponseOK(response)) {
			parseJSON(response);
			handler.sendEmptyMessage(SUCCESS);
		} else {
			handler.sendEmptyMessage(FAIL);
		}
	}

	private String getJSON() {
		return "{\"name\":\"" + name + "\",\"username\":\"" + username
				+ "\",\"phone\":\"" + phone + "\",\"password\":\"" + password
				+ "\"}";
	}

	private boolean isResponseOK(String response) {
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

	private void parseJSON(String json) {
		try {
			JSONObject jsonObject = new JSONObject(json);
			token = jsonObject.getString("token");
		} catch (JSONException e) {
			RegisterHttp.this.myListener.fail();
			e.printStackTrace();
		}
	}
}
