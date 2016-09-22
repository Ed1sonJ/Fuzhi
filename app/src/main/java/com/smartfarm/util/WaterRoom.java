package com.smartfarm.util;

import android.os.CountDownTimer;

import com.smartfarm.mqtt.PublishWC;
import com.smartfarm.mqtt.PublishWC.MyListeners;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ��ˮ�ķ�װ��
 */
public class  WaterRoom {
	private WaterInterface waterInterface = null;
	private PublishWC publishWC = null;		//������ˮ��Ϣ
	Map<String,TimeCount> timeCountMap = new HashMap<String, WaterRoom.TimeCount>();	//ͳ�Ƹ���ˮ��ͷ�Ķ�ʱʱ��
	
	public WaterRoom(WaterInterface waterInterface){
		this.waterInterface = waterInterface;
		publishWC = new PublishWC();
	}

	//��ˮ�ӿ�
	public interface WaterInterface {
		void waterFailed();
		void watering(String code);
		void waterTimeOut(String code);
	}

	//������ˮ��Ϣ��code+controller��000021001/c/shc/1���������topic��message����ˮ��ʱ��
	public void waterOn(final String code,final String controller,final String clientId,final String message){

//		if(!isNumeric(message)){
//			return;
//		}
//
		publishWC.request(new MyListeners() {
			@Override
			public void success(String arrivedMessage) {
				System.out.println("�յ�������ˮ��Ϣ��"+arrivedMessage);
			}
			
			@Override
			public void loading() {
				System.out.println("��������MQTT����");
			}

			@Override
			public void fail() {
				waterInterface.waterFailed();
				System.out.println("MQTT��������ʧ��");
			}
			@Override
			public void hasPublish() {
				if(Integer.parseInt(message) != 0){
					//����ʱ
					TimeCount timeCount = new TimeCount(code,
							controller,clientId,Integer.parseInt(message)*1000, 1000);
					timeCount.start();
					timeCountMap.put(code, timeCount);
					waterInterface.watering(code);
				}else{
					waterInterface.waterTimeOut(code);
				}
				System.out.println("�ɹ�������Ϣ��"+code+"��ˮ��"+message+"/s");
			}

			@Override
			public void connectLostException() {
				System.out.println("��������ʱ����exception");
			}
		}, code+controller, clientId, message);
	}

	//�رս�ˮ
	public void waterOffByBtn(String code,String controller,String clientId){
		//timeCountMapͳ�Ƹ���ˮ��ͷ�Ķ�ʱ��ʱ�䣬����ʱ����
		if(timeCountMap.containsKey(code) && timeCountMap.get(code) != null){
			timeCountMap.get(code).cancel();
			timeCountMap.get(code).onFinish();
			timeCountMap.remove(code);
		}else{
			//waterOn(code,controller,clientId,"0");
			waterOff(code,controller,clientId,"0");
		}
	}

	//��ˮ
	private void waterOff(String code,String controller,String clientId,String off){
		waterOn(code, controller, clientId, off);
	}
	
	//��ʱ��CountDownTimer����ʱ
	private class TimeCount extends CountDownTimer{
		private String code = null;
		private String controller;
		private String clientId;

		public TimeCount(String code,String controller,String clientId,long millisInFuture, long countDownInterval) {
			//��һ��������ʾ��ʱ�䣬�ڶ���������ʾ���ʱ�䡣
			//����(10000, 1000)����˼����ÿ��һ���ص�һ�η���onTick��
			//Ȼ��10��֮���ص�onFinish������
			super(millisInFuture, countDownInterval);
			this.code = code;
			this.controller = controller;
			this.clientId = clientId;
		}
		//ÿ��һ��ʱ��ͻص�һ�η���onTick
		@Override
		public void onTick(long millisUntilFinished) {			
		}

		@Override
		public void onFinish() {	
			waterOff(code,controller,clientId,"0");
		}
		
	}

	//�ж��Ƿ�������
	private  boolean isNumeric(String str){
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if(!isNum.matches()){
			return false;
		}
		return true;
	}

}
