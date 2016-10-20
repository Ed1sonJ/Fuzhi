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
import android.util.Log;

import com.smartfarm.util.HttpUtil;
import com.smartfarm.util.Protocol;

public class FindPasswordHttp {
	
	private static int LOADING = 0x00;
	private static int SUCCESS = 0x01;
	private static int FAIL = 0x02;

	private String phone;
	private String find_password_status;

	private MyListener myListener = null;

	public interface MyListener {

		public void loading();

		public void success(String find_password_status);

		public void fail();

	}

	public FindPasswordHttp() {
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
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();

	}

	/**
	 * handler����
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if (myListener != null) {
				if (msg.what == LOADING) {
					myListener.loading();
				}

				if (msg.what == SUCCESS) {
					myListener.success(find_password_status);
				}

				if (msg.what == FAIL) {
					myListener.fail();
				}
			}

		}
	};

	private void post() throws ClientProtocolException, IOException {
		// ������������
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();// BasicNameValuePair�Ǽ�ֵ�ԣ��������post����Ĳ�����ǰ��һ����������һ��ֵ
		pairs.add(new BasicNameValuePair("data", getJSON()));
		// ������ݣ�HttpUtil���Լ������һ���࣬���ڻ�ȡ��ҳ����
		String reponse = HttpUtil.httpPost(new DefaultHttpClient(),
				Protocol.FINDPASSWROD_URL, pairs, "");
		Log.e("�һ�����", reponse);
		if (reponse.contains("errmsg")) {
			parseJSON(reponse);// parseJSON���Լ�����ķ���
			 handler.sendEmptyMessage(SUCCESS);
		} else
			handler.sendEmptyMessage(FAIL);
		
	}



	private String getJSON() {

		return "{\"phone\":\"" + phone +"\"}";

	}
	
	
	private void parseJSON(String json) {
		try {
			JSONObject jsonObject = new JSONObject(json);
			find_password_status = jsonObject.getString("errmsg");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
