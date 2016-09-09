package com.smartfarm.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.smartfarm.util.Common;
import com.smartfarm.util.HttpUtil;
import com.smartfarm.util.Protocol;

public class InterveneHttp {

	private static int LOADING = 0x00;
	private static int SUCCESS = 0x01;
	private static int FAIL = 0x02;

	private String code;
	private String startTime;
	private String endTime;
	private String protocolKey;
	private String value;
	private String upper;
	private String lower;
	private MyListener myListener = null;

	public interface MyListener {

		public void loading();

		public void success();

		public void fail();

	}

	public InterveneHttp() {

	}

	public void request(String code, String startTime, String endTime, String protocolKey,
			String value, String upper, String lower, MyListener myListener) {

		this.code = code;
		this.startTime = startTime;
		this.endTime = endTime;
		this.protocolKey = protocolKey;
		this.value = value;
		this.upper = upper;
		this.lower = lower;
		this.myListener = myListener;

		Thread thread = new Thread(new Runnable() {
			public void run() {
				handler.sendEmptyMessage(LOADING);
				try {
					post();

				} catch (IOException e) {
					handler.sendEmptyMessage(FAIL);					
					e.printStackTrace();
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

					myListener.success();

				}

				if (msg.what == FAIL) {
					myListener.fail();
				}
			}
		}
	};

	private String getJSON() throws Exception {

		return "{\"token\":\"" + Common.token + "\",\"code\":\"" + code
				+ "\",\"startTime\":\"" + startTime + "\",\"endTime\":\""
				+ endTime + "\",\"protocolKey\":\"" + protocolKey + "\",\"value\":\"" + value
				+ "\",\"upperValue\":\"" + upper + "\",\"lowerValue\":\""
				+ lower + "\"}";

	}

	private void post() throws Exception {
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		String json = getJSON();
		Log.e("json", json);
		pairs.add(new BasicNameValuePair("data", json));
		String reponse = HttpUtil.httpPost(new DefaultHttpClient(),
				Protocol.INTERVENE_URL, pairs, "");
		if (reponse.contains("ok"))
			handler.sendEmptyMessage(SUCCESS);
		else
			handler.sendEmptyMessage(FAIL);
	}
}
