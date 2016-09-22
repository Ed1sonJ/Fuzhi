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
//本协议采取MQTT发布/订阅模式通讯，通信双方各自订阅需要的信息，并发布需要发布的信息。
//实现了mqtt的连接、断开、发布消息的方法。
public class PublishWC {

	private static int LOADING = 0x00;// 加载唯一标识
	private static int SUCCESS = 0x01;// 成功加载的唯一标识
	private static int FAIL = 0x02;// 加载失败的唯一标识
	private static int PUBLISHSUCCESS = 0x03;//发布成功
	private static int CONNECTLOSTEXCEPTION = 0x04;//连接失败

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
		// 常见和设置连接对象
		options = new MqttConnectOptions();
		//设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
		options.setCleanSession(false);
		//设置连接的用户名
		options.setUserName(MqttClientUtil.USERNAME);
		//设置连接的密码
		options.setPassword(MqttClientUtil.PASSWORD.toCharArray());
		// 设置超时时间 单位为秒
		options.setConnectionTimeout(10);
		// 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
		options.setKeepAliveInterval(20);
	}

	public interface MyListeners {// 自定义一个接口

		void loading();

		void success(String arrivedMessage);

		void fail();

		void hasPublish();

		void connectLostException();

	}
	//用一个handler接收各种信息，再实现相关的接口再做处理
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

	// 获得请求的方法，带code参数和一个MyListener监听接口，发布浇水信息
	//Topic格式：鉴权token/上位机编号/alarm
	//例如：1233425sdfsdfdsfdsfdsfdsg/0000010000000/alarm
	public void request(MyListeners myListener,String topic,String clientId,String message) {

		this.myListener = myListener;
		this.myTopic = topic;
		this.clientId = clientId;
		//getBytes()得到一个操作系统默认的编码格式的字节数组
		this.message = new MqttMessage(message.getBytes());

		// 多线程
		Thread thread = new Thread(new Runnable() {

			public void run() {
				init();// 初始化，设置请求连接
				//startReconnect();// 开始连接
				connect();
			}
		});

		thread.setName("PublishWC");
		thread.start();
	}

	public void init() {
		try {
			// 创建一个MQTT客户端对象
			//new MqttClient(host, "test",new MemoryPersistence());
			//host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，
			// MemoryPersistence设置clientid的保存形式，默认为以内存保存
			client = new MqttClient(MqttClientUtil.BROKER,
					clientId, persistence);
			System.out.println("Connecting to broker: " + MqttClientUtil.BROKER);
			// 给客户端对象设置一个回调方法
			//我们可以使用回调来实现消息到达的处理、连接断开、发送完毕的处理。
			client.setCallback(new MqttCallback() {
				@Override
				public void messageArrived(MqttTopic topic, MqttMessage messsage)
						throws Exception {
					//接收到的信息
					arrivedMessage = messsage.toString();

					if (arrivedMessage != null) {
						handler.sendEmptyMessage(SUCCESS);
					} else{
						handler.sendEmptyMessage(FAIL);
					}

				}

				// publish后会执行到这里
				@Override
				public void deliveryComplete(MqttDeliveryToken arg0) {
					System.out.println("deliveryComplete---------"+ arg0.isComplete());
					disConnect();
					handler.sendEmptyMessage(PUBLISHSUCCESS);
				}

				// 连接丢失后，一般在这里面进行重连
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
			// 创建MQTT连接
			client.connect(options);
			// 发送消息

			client.subscribe(myTopic);// 订阅相关主题信息
			topic = client.getTopic(myTopic);
			message.setQos(qos);
			topic.publish(message);
			handler.sendEmptyMessage(LOADING);// 发送空消息
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
