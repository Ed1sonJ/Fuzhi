package com.smartfarm.mqtt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ibm.micro.client.mqttv3.MqttCallback;
import com.ibm.micro.client.mqttv3.MqttClient;
import com.ibm.micro.client.mqttv3.MqttConnectOptions;
import com.ibm.micro.client.mqttv3.MqttDeliveryToken;
import com.ibm.micro.client.mqttv3.MqttException;
import com.ibm.micro.client.mqttv3.MqttMessage;
import com.ibm.micro.client.mqttv3.MqttSecurityException;
import com.ibm.micro.client.mqttv3.MqttTopic;
import com.ibm.micro.client.mqttv3.internal.MemoryPersistence;
import com.smartfarm.util.CalendarUtil;

import com.smartfarm.util.Config;
import com.smartfarm.util.MqttClientUtil;
import com.smartfarm.util.TopicType;


public class GetRealtimeDataMqtt {
	private MemoryPersistence persistence = new MemoryPersistence();
	private MqttClient client;
	private MqttConnectOptions options;
	private ScheduledExecutorService scheduler;
	private Config config;

	private static int LOADING = 0x00;// 加载唯一标识
	private static int SUCCESS = 0x01;// 成功加载的唯一标识
	private static int FAIL = 0x02;// 加载失败的唯一标识 
	
	private String myTopic;
	private String code;
	private List<Float> data = new ArrayList<Float>();// data是实时监控获得光强的数据
	private List<String> time = new ArrayList<String>();
	
	
	private MyListener myListener = null;

	public static interface MyListener {// 自定义一个接口，里边放有三个方法

		public void loading();

		public void success(List<String> time, List<Float> data);

		public void fail();

	}
	public GetRealtimeDataMqtt(TopicType topicType, MyListener myListener) {
		this.myListener = myListener;
		switch (topicType) {
		case GetRealtimeDataLS:
			this.myTopic = "000016001/s/ls/1";
			break;
		case GetRealtimeDataLQS:
			this.myTopic = "000016001/s/lqs/1";
			break;
		case GetRealtimeDataTS:
			this.myTopic = "000016001/s/ts/1";
			break;
		case GetRealtimeDataHS:
			this.myTopic = "000016001/s/hs/1";
			break;
		case GetRealtimeDataCO2S:
			this.myTopic = "000016001/s/co2s/1";
			break;
		}
//	case GetRealtimeDataLS:
//		this.myTopic = code+"/s/ls/1";
//		break;
//	case GetRealtimeDataLQS:
//		this.myTopic = code+"/s/lqs/1";
//		break;
//	case GetRealtimeDataTS:
//		this.myTopic = code+"/s/ts/1";
//		break;
//	case GetRealtimeDataHS:
//		this.myTopic = code+"/s/hs/1";
//		break;
//	case GetRealtimeDataCO2S:
//		this.myTopic = code+"/s/co2s/1";
//		break;
//	}
		// 多线程
		Thread thread = new Thread(new Runnable() {

			public void run() {
				handler.sendEmptyMessage(LOADING);// 发送空消息
				init();// 初始化，设置请求连接
				startReconnect();// 开始连接
			}
		});

		thread.start();
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			if (myListener != null) {
				if (msg.what == LOADING) {
					myListener.loading();
				}

				if (msg.what == SUCCESS) {
					try {
						client.subscribe(myTopic);
					} catch (MqttSecurityException e) {
						e.printStackTrace();
					} catch (MqttException e) {
						e.printStackTrace();
					}
					myListener.success(time, data);
				}
				if (msg.what == FAIL) {
					myListener.fail();
				}
			}
		}
	};

	public void init() {
		try {
			// 创建一个MQTT客户端对象
			client = new MqttClient(MqttClientUtil.BROKER,
					myTopic, persistence);
			// 常见和设置连接对象
			options = new MqttConnectOptions();
			options.setCleanSession(false);
			options.setUserName(config.getUsername());
			options.setPassword(config.getPassword().toCharArray());
			options.setConnectionTimeout(1000);
			options.setKeepAliveInterval(2000);

			System.out
					.println("Connecting to broker: " + MqttClientUtil.BROKER);
			// 给客户端对象设置一个回调方法
			client.setCallback(new MqttCallback() {
				@Override
				public void messageArrived(MqttTopic topic, MqttMessage messsage)
						throws Exception {
					Log.e("数据来啦", "!!!!!!!!!!!!!!");

					String getMessage = messsage.toString();

					// 获得当前时间，并加入集合中
					time.add(CalendarUtil.getCurrentSystemYMDHMS());
					// 获得当前从服务器得到的数据
					data.add(Float.parseFloat(getMessage));
					int dataSize = data.size();
					if(dataSize>6){
						data.remove(0);
						time.remove(0);
					}
					if (getMessage != null) {
						handler.sendEmptyMessage(SUCCESS);
					} else
						handler.sendEmptyMessage(FAIL);

				}

				// publish后会执行到这里
				@Override
				public void deliveryComplete(MqttDeliveryToken arg0) {
					System.out.println("deliveryComplete---------"
							+ arg0.isComplete());
				}

				// 连接丢失后，一般在这里面进行重连
				@Override
				public void connectionLost(Throwable arg0) {
					System.out.println("connectionLost--------");
					try {
						client.connect(options);// 连接broker
						client.subscribe(myTopic);// 订阅相关的主题信息
					} catch (MqttSecurityException e) {
						e.printStackTrace();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (MqttException me) {
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
					client.subscribe(myTopic);// 订阅相关主题信息
					handler.sendEmptyMessage(SUCCESS);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendEmptyMessage(FAIL);
				}
			}
		}).start();
	}
}
