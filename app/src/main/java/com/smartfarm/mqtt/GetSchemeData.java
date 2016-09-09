package com.smartfarm.mqtt;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ibm.micro.client.mqttv3.MqttCallback;
import com.ibm.micro.client.mqttv3.MqttClient;
import com.ibm.micro.client.mqttv3.MqttConnectOptions;
import com.ibm.micro.client.mqttv3.MqttDeliveryToken;
import com.ibm.micro.client.mqttv3.MqttException;
import com.ibm.micro.client.mqttv3.MqttMessage;
import com.ibm.micro.client.mqttv3.MqttSecurityException;
import com.ibm.micro.client.mqttv3.MqttTopic;
import com.ibm.micro.client.mqttv3.internal.MemoryPersistence;
import com.smartfarm.util.MqttClientUtil;

public class GetSchemeData {

	private MemoryPersistence persistence = new MemoryPersistence();
	private MqttClient client;
	private MqttConnectOptions options;
	private ScheduledExecutorService scheduler;

	private String topic;
	private boolean haschange;
	
	public boolean isSchemeDataChanged(String topic){
		this.topic = topic;
		init();
		startReconnect();
		return haschange;
	}
	public void init(){
		try {
			// 创建一个MQTT客户端对象
			client = new MqttClient(MqttClientUtil.BROKER, MqttClientUtil.CLIENT_ID, persistence);
			// 常见和设置连接对象
			options = new MqttConnectOptions();
			options.setCleanSession(false); 
			options.setUserName(MqttClientUtil.USERNAME);
			options.setPassword(MqttClientUtil.PASSWORD.toCharArray());
			options.setConnectionTimeout(10); 
			options.setKeepAliveInterval(20);
			
			System.out.println("Connecting to broker: " + MqttClientUtil.BROKER);
			// 给客户端对象设置一个回调方法
			client.setCallback(new MqttCallback() {
				@Override
				public void messageArrived(MqttTopic topic, MqttMessage messsage)
						throws Exception {

					String getMessage = messsage.toString();
					
					if (getMessage!=null) {
						if("1".equals(getMessage)){
							haschange = true;
						}else{
							haschange = false;
						}
					} else
						System.out.println("");
				}

				// publish后会执行到这里
				@Override
				public void deliveryComplete(MqttDeliveryToken arg0) {
					// TODO Auto-generated method stub
					System.out.println("deliveryComplete---------"
							+ arg0.isComplete());
				}

				// 连接丢失后，一般在这里面进行重连
				@Override
				public void connectionLost(Throwable arg0) {
					// TODO Auto-generated method stub
					System.out.println("connectionLost--------");
					try {
						client.connect(options);// 连接broker
						client.subscribe(topic);// 订阅相关的主题信息
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
					client.subscribe(topic);// 订阅相关主题信息
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	
}
