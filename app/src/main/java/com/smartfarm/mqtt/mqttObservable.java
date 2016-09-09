package com.smartfarm.mqtt;

import rx.Observable;
import rx.Subscriber;
import rx.Observable.OnSubscribe;

import com.ibm.micro.client.mqttv3.MqttCallback;
import com.ibm.micro.client.mqttv3.MqttClient;
import com.ibm.micro.client.mqttv3.MqttConnectOptions;
import com.ibm.micro.client.mqttv3.MqttDeliveryToken;
import com.ibm.micro.client.mqttv3.MqttException;
import com.ibm.micro.client.mqttv3.MqttMessage;
import com.ibm.micro.client.mqttv3.MqttTopic;
import com.ibm.micro.client.mqttv3.internal.MemoryPersistence;
import com.smartfarm.util.MqttClientUtil;

public class mqttObservable{
	public static String Publish(String topic,String clientId,String message){		
		MemoryPersistence persistence = new MemoryPersistence();
		try {
			// 创建一个MQTT客户端对象
			final MqttClient client = new MqttClient(MqttClientUtil.BROKER,clientId, persistence);
			System.out.println("Connecting to broker: "+MqttClientUtil.BROKER);
			client.setCallback(new MqttCallback() {		
				@Override
				public void messageArrived(MqttTopic arg0, MqttMessage messsage)
						throws Exception {
					System.out.println("--------messageArrived---------"+ messsage.toString());		
				}
				
				@Override
				public void deliveryComplete(MqttDeliveryToken arg0) {
					System.out.println("--------deliveryComplete---------"+ arg0.isComplete());
				}
				
				@Override
				public void connectionLost(Throwable arg0) {
					System.out.println("--------connectionLost--------");		
				}
			});
			// 常见和设置连接对象
			MqttConnectOptions options = new MqttConnectOptions();
			options.setCleanSession(true);
			options.setUserName(MqttClientUtil.USERNAME);
			options.setPassword(MqttClientUtil.PASSWORD.toCharArray());
			options.setConnectionTimeout(10);
			options.setKeepAliveInterval(20);
			//连接
			client.connect(options);
			System.out.println("Connected");
			System.out.println("Publishing message: "+message);
			MqttTopic mqttTopic = client.getTopic(topic);
			MqttMessage mqttMessage = new MqttMessage(message.getBytes());
			mqttMessage.setQos(2);
			mqttTopic.publish(mqttMessage);
			System.out.println("Message published");
			client.disconnect();
			System.out.println("Disconnected");			
		} catch (MqttException e) {
			e.printStackTrace();
		}
		return message;
	}
	
	public static Observable<String> createObservable(final String topic,
			final String clientId,final String message) {
		Observable<String> observable = Observable
				.create(new OnSubscribe<String>() {
					@Override
					public void call(Subscriber<? super String> subscriber) {
						try {
							String response = Publish(topic,clientId,message);
							subscriber.onNext(response);
							subscriber.onCompleted();
						} catch (Exception e) {
							subscriber.onError(e);
						}
					}
				});
		return observable;
	}
}	

