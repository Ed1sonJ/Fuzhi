package com.smartfarm.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartfarm.bean.ChartBean;
import com.smartfarm.util.Common;
import com.smartfarm.util.HttpUtil;
import com.smartfarm.util.Protocol;

public class GetHistoryDataHttp {

	private static int LOADING = 0x00;
	private static int SUCCESS = 0x01;
	private static int FAIL = 0x02;

	private String code;
	private ChartBean list = new ChartBean();
	private MyListener myListener;
	private String startDate;
	private String endDate;
	private String json;
	private String reponse;

	public interface MyListener {
		public void loading();

		public void success(ChartBean list);

		public void fail(String response);
	}

	public synchronized void request(String code, String startDate,
			String endDate, MyListener myListener) {

		this.code = code;
		this.startDate = startDate;
		this.endDate = endDate;
		this.myListener = myListener;

		Thread thread = new Thread(new Runnable() {
			public void run() {
				handler.sendEmptyMessage(LOADING);
				try {
					post();
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendEmptyMessage(FAIL);
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
					myListener.success(list);
				}
				if (msg.what == FAIL) {
					myListener.fail(reponse);
				}
			}
		}
	};

	private void post() throws Exception {
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		json = getJSON();
		Log.d("JSON", json);
		pairs.add(new BasicNameValuePair("data", json));
		reponse = HttpUtil.httpPost(new DefaultHttpClient(),
				Protocol.GETHISTORYDATA_URL, pairs, "");
		if (reponse.contains("data")) {
			parseJSON(reponse);
			handler.sendEmptyMessage(SUCCESS);
		} else {
			handler.sendEmptyMessage(FAIL);
		}
	}

	private String getJSON() {
		return "{\"token\":\"" + Common.token + "\",\"code\":\"" + code
				+ "\",\"startDate\":\"" + startDate + "\",\"endDate\":\""
				+ endDate + "\"}";
	}

	private void parseJSON(String json) {
		Gson gson = new Gson();
		list = gson.fromJson(json, new TypeToken<ChartBean>() {
		}.getType());
	}
}
