package com.smartfarm.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.Handler;
import android.os.Message;

import com.smartfarm.util.Base64Util;
import com.smartfarm.util.Common;
import com.smartfarm.util.DesUtil;
import com.smartfarm.util.HttpUtil;
import com.smartfarm.util.Protocol;

public class PasswordHttp {

	private static int LOADING = 0x00;
	private static int SUCCESS = 0x01;
	private static int FAIL = 0x02;

	private String token;
	private String oldPassword;
	private String newPassword;
	private MyListener myListener = null;

	public interface MyListener {

		public void loading();

		public void success(String token);

		public void fail();

	}

	public PasswordHttp() {

	}

	public void request(String oldPassword, String newPassword,
			MyListener myListener) {

		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
		this.myListener = myListener;

		// 多线程
		Thread thread = new Thread(new Runnable() {

			public void run() {

				handler.sendEmptyMessage(LOADING);

				try {
					post();

				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
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
					toToken(Common.username, newPassword);
					myListener.success(token);

				}

				if (msg.what == FAIL) {
					myListener.fail();
				}
			}

		}
	};

	private String getJSON() throws Exception {

		return "{\"token\":\""
				+ Common.token
				+ "\",\"oldPassword\":\""
				+ Base64Util.encode(DesUtil.encrypt(oldPassword.getBytes(),
						Protocol.key))
				+ "\",\"newPassword\":\""
				+ Base64Util.encode(DesUtil.encrypt(newPassword.getBytes(),
						Protocol.key)) + "\"}";

	}

	private void toToken(String username, String password) {
		String data = username + "#" + password;

		try {
			token = Base64Util.encode(DesUtil.encrypt(data.getBytes(),
					Protocol.key));

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void post() throws Exception {
		// 设置请求数据
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		pairs.add(new BasicNameValuePair("data", getJSON()));

		// 获得数据
		String reponse = HttpUtil.httpPost(new DefaultHttpClient(),
				Protocol.CHANGEPASSWORD_URL, pairs, "");

		if (reponse.contains("ok"))
			handler.sendEmptyMessage(SUCCESS);
		else
			handler.sendEmptyMessage(FAIL);

	}
}
