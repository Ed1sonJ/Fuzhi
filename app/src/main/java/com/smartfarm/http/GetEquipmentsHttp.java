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
import com.smartfarm.bean.EquipmentBean;
import com.smartfarm.bean.TabBean0;
import com.smartfarm.util.Common;
import com.smartfarm.util.HttpUtil;
import com.smartfarm.util.Protocol;

public class GetEquipmentsHttp {

	private static int LOADING = 0x00;
	private static int SUCCESS = 0x01;
	private static int FAIL = 0x02;

	private List<EquipmentBean> list = new ArrayList<EquipmentBean>();

	private MyListener myListener = null;

	public interface MyListener {

		public void loading();

		public void success(List<EquipmentBean> list);

		public void fail();

	}

	public GetEquipmentsHttp() {
	}

	public void request(MyListener myListener) {
		this.myListener = myListener;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				handler.sendEmptyMessage(LOADING);
				try {
					post();
				} catch (IOException e) {
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
					myListener.success(list);
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
		String reponse = HttpUtil.httpPost(new DefaultHttpClient(),
				Protocol.GETEQUIPMENTS_URL, pairs, "");
		Log.d("GetEquipmentsHttp", getJSON());
		Log.d("GetEquipmentsHttp", reponse);
		if (reponse.contains("code")) {
			parseJSON(reponse);
			handler.sendEmptyMessage(SUCCESS);
		} else {
			handler.sendEmptyMessage(FAIL);
		}
	}

	private String getJSON() {
		return "{\"token\":\"" + Common.token + "\"}";
	}

	private void parseJSON(String json) {
		Gson gson = new Gson();
		list = gson.fromJson(json, new TypeToken<List<EquipmentBean>>() {
		}.getType());
	}
}
