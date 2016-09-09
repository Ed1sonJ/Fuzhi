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

public class GetCodeHttp {

	private static int LOADING = 0x00;
	private static int SUCCESS = 0x01;
	private static int FAIL = 0x02;

	private String phone;

	private MyListener myListener = null;

	public interface MyListener {

		public void loading();

		public void success();

		public void fail();

	}

	public GetCodeHttp() {
		// TODO Auto-generated constructor stub
	}

	public void request(String phone, MyListener myListener) {
		this.phone = phone;
		this.myListener = myListener;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				handler.sendEmptyMessage(LOADING);
				try {
					post();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					handler.sendEmptyMessage(FAIL);
				} catch (IOException e) {
					e.printStackTrace();
					handler.sendEmptyMessage(FAIL);
				}
			}
		});
		thread.start();
	}

	/**
	 * handler处理
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if (myListener != null) {
				if (msg.what == LOADING) {
					myListener.loading();
				}

				if (msg.what == SUCCESS) {
					myListener.success();
				}

				if (msg.what == FAIL) {
					myListener.fail();
				}
			}

		}
	};

	private void post() throws ClientProtocolException, IOException {
		// 设置请求数据
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();// BasicNameValuePair是键值对，用来存放post请求的参数，前面一个键，后面一个值
		pairs.add(new BasicNameValuePair("data", getJSON()));
		// 获得数据，HttpUtil是自己定义的一个类，用于获取网页内容
		String response = HttpUtil.httpPost(new DefaultHttpClient(),
				Protocol.GETCODE_URL, pairs, "");
		if (isResponseOK(response)) {
			handler.sendEmptyMessage(SUCCESS);
		} else {
			handler.sendEmptyMessage(FAIL);
		}
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

	private String getJSON() {
		return "{\"phone\":\"" + phone + "\"}";
	}
}
