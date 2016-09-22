package com.smartfarm.mqtt;

import android.os.Handler;
import android.os.Message;

import com.ibm.micro.client.mqttv3.MqttCallback;
import com.ibm.micro.client.mqttv3.MqttClient;
import com.ibm.micro.client.mqttv3.MqttConnectOptions;
import com.ibm.micro.client.mqttv3.MqttDeliveryToken;
import com.ibm.micro.client.mqttv3.MqttException;
import com.ibm.micro.client.mqttv3.MqttMessage;
import com.ibm.micro.client.mqttv3.MqttTopic;
import com.ibm.micro.client.mqttv3.internal.MemoryPersistence;
import com.smartfarm.util.MqttClientUtil;
//��Э���ȡMQTT����/����ģʽͨѶ��ͨ��˫�����Զ�����Ҫ����Ϣ����������Ҫ��������Ϣ��
//ʵ����mqtt�����ӡ��Ͽ���������Ϣ�ķ�����
public class PublishWC {

	private static int LOADING = 0x00;// ����Ψһ��ʶ
	private static int SUCCESS = 0x01;// �ɹ����ص�Ψһ��ʶ
	private static int FAIL = 0x02;// ����ʧ�ܵ�Ψһ��ʶ
	private static int PUBLISHSUCCESS = 0x03;//�����ɹ�
	private static int CONNECTLOSTEXCEPTION = 0x04;//����ʧ��

	private MemoryPersistence persistence = new MemoryPersistence();
	private MqttClient client;
	private MqttConnectOptions options;

	private String myTopic = "";
	private String clientId = "";
	private int qos = 2;
	private MqttMessage message;
	private MqttTopic topic;


	private String arrivedMessage;

	private MyListeners myListener = null;

	public PublishWC() {
		optionInit();
	}

	private void optionInit(){
		// �������������Ӷ���
		options = new MqttConnectOptions();
		//�����Ƿ����session,�����������Ϊfalse��ʾ�������ᱣ���ͻ��˵����Ӽ�¼����������Ϊtrue��ʾÿ�����ӵ������������µ��������
		options.setCleanSession(false);
		//�������ӵ��û���
		options.setUserName(MqttClientUtil.USERNAME);
		//�������ӵ�����
		options.setPassword(MqttClientUtil.PASSWORD.toCharArray());
		// ���ó�ʱʱ�� ��λΪ��
		options.setConnectionTimeout(10);
		// ���ûỰ����ʱ�� ��λΪ�� ��������ÿ��1.5*20���ʱ����ͻ��˷��͸���Ϣ�жϿͻ����Ƿ����ߣ������������û�������Ļ���
		options.setKeepAliveInterval(20);
	}

	public interface MyListeners {// �Զ���һ���ӿ�

		void loading();

		void success(String arrivedMessage);

		void fail();

		void hasPublish();

		void connectLostException();

	}
	//��һ��handler���ո�����Ϣ����ʵ����صĽӿ���������
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			if (myListener != null) {
				if (msg.what == LOADING) {
					myListener.loading();
				}

				if (msg.what == SUCCESS) {
					myListener.success(arrivedMessage);
				}

				if (msg.what == FAIL) {
					myListener.fail();
				}

				if (msg.what == PUBLISHSUCCESS) {
					myListener.hasPublish();
				}

				if (msg.what == CONNECTLOSTEXCEPTION) {
					myListener.connectLostException();
				}
			}
		}
	};

	// �������ķ�������code������һ��MyListener�����ӿڣ�������ˮ��Ϣ
	//Topic��ʽ����Ȩtoken/��λ�����/alarm
	//���磺1233425sdfsdfdsfdsfdsfdsg/0000010000000/alarm
	public void request(MyListeners myListener,String topic,String clientId,String message) {

		this.myListener = myListener;
		this.myTopic = topic;
		this.clientId = clientId;
		//getBytes()�õ�һ������ϵͳĬ�ϵı����ʽ���ֽ�����
		this.message = new MqttMessage(message.getBytes());

		// ���߳�
		Thread thread = new Thread(new Runnable() {

			public void run() {
				init();// ��ʼ����������������
				//startReconnect();// ��ʼ����
				connect();
			}
		});

		thread.setName("PublishWC");
		thread.start();
	}

	public void init() {
		try {
			// ����һ��MQTT�ͻ��˶���
			//new MqttClient(host, "test",new MemoryPersistence());
			//hostΪ��������testΪclientid������MQTT�Ŀͻ���ID��һ���Կͻ���Ψһ��ʶ����ʾ��
			// MemoryPersistence����clientid�ı�����ʽ��Ĭ��Ϊ���ڴ汣��
			client = new MqttClient(MqttClientUtil.BROKER,
					clientId, persistence);
			System.out.println("Connecting to broker: " + MqttClientUtil.BROKER);
			// ���ͻ��˶�������һ���ص�����
			//���ǿ���ʹ�ûص���ʵ����Ϣ����Ĵ������ӶϿ���������ϵĴ���
			client.setCallback(new MqttCallback() {
				@Override
				public void messageArrived(MqttTopic topic, MqttMessage messsage)
						throws Exception {
					//���յ�����Ϣ
					arrivedMessage = messsage.toString();

					if (arrivedMessage != null) {
						handler.sendEmptyMessage(SUCCESS);
					} else{
						handler.sendEmptyMessage(FAIL);
					}

				}

				// publish���ִ�е�����
				@Override
				public void deliveryComplete(MqttDeliveryToken arg0) {
					System.out.println("deliveryComplete---------"+ arg0.isComplete());
					disConnect();
					handler.sendEmptyMessage(PUBLISHSUCCESS);
				}

				// ���Ӷ�ʧ��һ�����������������
				@Override
				public void connectionLost(Throwable arg0) {
					System.out.println("--------connectionLost--------");

				}
			});
		} catch (MqttException me) {
			handler.sendEmptyMessage(FAIL);
			me.printStackTrace();

		}
	}

	private void connect() {
		try {
			// ����MQTT����
			client.connect(options);
			// ������Ϣ

			client.subscribe(myTopic);// �������������Ϣ
			topic = client.getTopic(myTopic);
			message.setQos(qos);
			topic.publish(message);
			handler.sendEmptyMessage(LOADING);// ���Ϳ���Ϣ
		} catch (Exception e) {
			e.printStackTrace();
			handler.sendEmptyMessage(FAIL);
		}
	}

	public void disConnect(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(client.isConnected()){
					try {
						client.disconnect();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}
