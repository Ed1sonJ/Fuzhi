package com.smartfarm.mqtt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.ibm.micro.client.mqttv3.MqttCallback;
import com.ibm.micro.client.mqttv3.MqttClient;
import com.ibm.micro.client.mqttv3.MqttConnectOptions;
import com.ibm.micro.client.mqttv3.MqttDeliveryToken;
import com.ibm.micro.client.mqttv3.MqttException;
import com.ibm.micro.client.mqttv3.MqttMessage;
import com.ibm.micro.client.mqttv3.MqttSecurityException;
import com.ibm.micro.client.mqttv3.MqttTopic;
import com.ibm.micro.client.mqttv3.internal.MemoryPersistence;
import com.smartfarm.bean.TabBean1;
public class GetAlarmMqtt {
	private static int LOADING = 0x00;
	private static int SUCCESS = 0x01;
	private static int FAIL = 0x02;

	private String page;
	private String size;
	private String topic;
	private TabBean1 mBean1;
	private List<TabBean1> list = new ArrayList<TabBean1>();

	private MqttClient client;
	private MqttConnectOptions options;
	private ScheduledExecutorService scheduler;

	private MyListenner myListener = null;

	public interface MyListenner {

		public void loading();

		public void success(List<TabBean1> list);

		public void fail();

	}

	public void request(String page, String size, String topic,
			MyListenner myListener) {
		this.page = page;
		this.size = size;
		this.topic = topic;
		this.myListener = myListener;

		// 多线程
		Thread thread = new Thread(new Runnable() {

			public void run() {
				// TODO 自动生成的方法存根

				handler.sendEmptyMessage(LOADING);// 发送加载信号
				init();// 初始化连接设置
				startReconnect();// 开始连接
			}
		});
		thread.start();// 启动线程
	}

	/**
	 * handler处理
	 */
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			if (myListener != null) {
				if (msg.what == LOADING) {
					myListener.loading();
				}

				if (msg.what == SUCCESS) {
//					try {
//						client.subscribe(topic,1);
//					} catch (MqttSecurityException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (MqttException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					myListener.success(list);
				}
				if (msg.what == FAIL) {
					myListener.fail();
				}
			}

		}
	};

	private void init() {
		try {
			client = new MqttClient("tcp://broker.gzfuzhi.com:1883", "mqtt_test", new MemoryPersistence());
			options = new MqttConnectOptions();
			options.setCleanSession(true);
			options.setConnectionTimeout(1000);
			options.setKeepAliveInterval(2000);
			client.setCallback(new MqttCallback() {
				@Override
				public void messageArrived(MqttTopic topic, MqttMessage message)
						throws Exception {
					
					System.out.println("messageArrived----------");
					
					Log.e("报警数据", message.toString());
					mBean1 = new TabBean1();
					Gson gson = new Gson();
					mBean1 = gson.fromJson(message.toString(), TabBean1.class);

					list.add(mBean1);
					handler.sendEmptyMessage(SUCCESS);
					int listsize = list.size();
					
					if (listsize > 6) {
						list.remove(0);
						Log.e("NextListsize", listsize+"");
					}
				}

				// publish后会执行到这里
				@Override
				public void deliveryComplete(MqttDeliveryToken arg0) {
					// TODO Auto-generated method stub
					System.out.println("deliveryComplete---------"
							+ arg0.isComplete());
				}

				// 连接丢失后，一般在这里面进行重连
				public void connectionLost(Throwable arg0) {
					System.out.println("connectionLost--------");
					try {
						client.connect(options);// 连接broker
						client.subscribe(topic,1);// 订阅相关的主题信息
					} catch (MqttSecurityException e) {
						e.printStackTrace();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (MqttException me) {
			// TODO Auto-generated catch block
			me.printStackTrace();

		}
	}

	private void startReconnect() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (!client.isConnected()) {
					connect();
				}
			}
		}, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
	}

	private void connect() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					client.connect(options);
					client.subscribe(topic,1);
					handler.sendEmptyMessage(SUCCESS);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendEmptyMessage(FAIL);
				}
			}
		}).start();
	}
	
}
