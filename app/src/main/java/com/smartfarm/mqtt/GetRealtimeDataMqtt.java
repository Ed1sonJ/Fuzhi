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

	private static int LOADING = 0x00;// ����Ψһ��ʶ
	private static int SUCCESS = 0x01;// �ɹ����ص�Ψһ��ʶ
	private static int FAIL = 0x02;// ����ʧ�ܵ�Ψһ��ʶ 
	
	private String myTopic;
	private String code;
	private List<Float> data = new ArrayList<Float>();// data��ʵʱ��ػ�ù�ǿ������
	private List<String> time = new ArrayList<String>();
	
	
	private MyListener myListener = null;

	public static interface MyListener {// �Զ���һ���ӿڣ���߷�����������

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
		// ���߳�
		Thread thread = new Thread(new Runnable() {

			public void run() {
				handler.sendEmptyMessage(LOADING);// ���Ϳ���Ϣ
				init();// ��ʼ����������������
				startReconnect();// ��ʼ����
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
			// ����һ��MQTT�ͻ��˶���
			client = new MqttClient(MqttClientUtil.BROKER,
					myTopic, persistence);
			// �������������Ӷ���
			options = new MqttConnectOptions();
			options.setCleanSession(false);
			options.setUserName(config.getUsername());
			options.setPassword(config.getPassword().toCharArray());
			options.setConnectionTimeout(1000);
			options.setKeepAliveInterval(2000);

			System.out
					.println("Connecting to broker: " + MqttClientUtil.BROKER);
			// ���ͻ��˶�������һ���ص�����
			client.setCallback(new MqttCallback() {
				@Override
				public void messageArrived(MqttTopic topic, MqttMessage messsage)
						throws Exception {
					Log.e("��������", "!!!!!!!!!!!!!!");

					String getMessage = messsage.toString();

					// ��õ�ǰʱ�䣬�����뼯����
					time.add(CalendarUtil.getCurrentSystemYMDHMS());
					// ��õ�ǰ�ӷ������õ�������
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

				// publish���ִ�е�����
				@Override
				public void deliveryComplete(MqttDeliveryToken arg0) {
					System.out.println("deliveryComplete---------"
							+ arg0.isComplete());
				}

				// ���Ӷ�ʧ��һ�����������������
				@Override
				public void connectionLost(Throwable arg0) {
					System.out.println("connectionLost--------");
					try {
						client.connect(options);// ����broker
						client.subscribe(myTopic);// ������ص�������Ϣ
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
					client.subscribe(myTopic);// �������������Ϣ
					handler.sendEmptyMessage(SUCCESS);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendEmptyMessage(FAIL);
				}
			}
		}).start();
	}
}