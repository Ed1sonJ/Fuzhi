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

public class GetCrlSchemaDataHttp {

	private static int LOADING = 0x00;
	private static int SUCCESS = 0x01;
	private static int FAIL = 0x02;

	private String code;
	private List<ChartBean> list = new ArrayList<ChartBean>();

	private MyListener myListener = null;

	public interface MyListener {

		public void loading();

		public void success(List<ChartBean> list);

		public void fail();

	}

	public GetCrlSchemaDataHttp() {

	}

	public void request(String code, MyListener myListener) {

		this.code = code;
		this.myListener = myListener;

		// 多线程
		Thread thread = new Thread(new Runnable() {

			public void run() {

				handler.sendEmptyMessage(LOADING);

				try {
					post();

				} catch (IOException e) {
					e.printStackTrace();
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
					myListener.success(list);
				}

				if (msg.what == FAIL) {
					myListener.fail();
				}
			}

		}
	};

	private void post() throws ClientProtocolException, IOException {
		// 设置请求数据
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		pairs.add(new BasicNameValuePair("data", getJSON()));

		// 获得数据
		String reponse = HttpUtil.httpPost(new DefaultHttpClient(),
				Protocol.GETCRLSCHEMADATA_URL, pairs, "");

		Log.e("方案数据", reponse);

		parseJSON(reponse);

		if (reponse.contains("schema")) {
			parseJSON(reponse);
			handler.sendEmptyMessage(SUCCESS);
		} else
			handler.sendEmptyMessage(FAIL);

	}

	private String getJSON() {

		return "{\"token\":\"" + Common.token + "\",\"code\":\"" + code + "\"}";

	}

	private void parseJSON(String json) {
		Gson gson = new Gson();
		list = gson.fromJson(json, new TypeToken<List<ChartBean>>() {
		}.getType());

	}

}
