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
import com.smartfarm.util.MqttClientUtil;

public class GetRealTimeDataLQS {
	private MemoryPersistence persistence = new MemoryPersistence();
	private MqttClient client;
	private MqttConnectOptions options;
	private ScheduledExecutorService scheduler;

	private static int LOADING = 0x00;// ����Ψһ��ʶ
	private static int SUCCESS = 0x01;// �ɹ����ص�Ψһ��ʶ
	private static int FAIL = 0x02;// ����ʧ�ܵ�Ψһ��ʶ

	private String code = "000016001";
	private String myTopic = code + "/s/lqs/1";
	private List<Float> data = new ArrayList<Float>();// data��ʵʱ��ػ�ù�ǿ������
	private List<String> time = new ArrayList<String>();

	private MyListenerlqs myListener = null;

	public interface MyListenerlqs {// �Զ���һ���ӿڣ���߷�����������

		public void loading();

		public void success(List<String> time, List<Float> data);

		public void fail();

	}

	// �������ķ�������code������һ��MyListener�����ӿ�
	public void request(MyListenerlqs myListener) {

		this.myListener = myListener;

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
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
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
					"LQS", persistence);
			// �������������Ӷ���
			options = new MqttConnectOptions();
			options.setCleanSession(false);
			options.setUserName(MqttClientUtil.USERNAME);
			options.setPassword(MqttClientUtil.PASSWORD.toCharArray());
			options.setConnectionTimeout(1000);
			options.setKeepAliveInterval(2000);

			System.out
					.println("Connecting to broker: " + MqttClientUtil.BROKER);
			// ���ͻ��˶�������һ���ص�����
			client.setCallback(new MqttCallback() {
				@Override
				public void messageArrived(MqttTopic topic, MqttMessage messsage)
						throws Exception {

					String getMessage = messsage.toString();

					// ��õ�ǰʱ�䣬�����뼯����
					time.add(CalendarUtil.getCurrentSystemYMDHMS());
					// ��õ�ǰ�ӷ������õ�������
					data.add(Float.parseFloat(getMessage));
					int dataSize = data.size();
//					if(dataSize>9){
//						for (int i = 0; i < 5; i++) {
//							data.remove(i);
//							time.remove(i);
//						}
//					}
					if(dataSize>6){
						data.remove(0);
						time.remove(0);
					}
					if (getMessage != null) {
						handler.sendEmptyMessage(SUCCESS);
					} else
						handler.sendEmptyMessage(FAIL);
					Log.e("ʵʱ���ʱ�����", data.toString());
					Log.e("ʱ��", time.toString());

				}

				// publish���ִ�е�����
				@Override
				public void deliveryComplete(MqttDeliveryToken arg0) {
					// TODO Auto-generated method stub
					System.out.println("deliveryComplete---------"
							+ arg0.isComplete());
				}

				// ���Ӷ�ʧ��һ�����������������
				@Override
				public void connectionLost(Throwable arg0) {
					// TODO Auto-generated method stub
					System.out.println("connectionLost--------");
					try {
						client.connect(options);// ����broker
						client.subscribe(myTopic);// ������ص�������Ϣ
					} catch (MqttSecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
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
