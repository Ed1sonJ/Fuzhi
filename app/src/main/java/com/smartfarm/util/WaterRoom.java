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
		void watering(String equipmentCode,String controller);
		void waterTimeOut(String equipmentCode,String controller);
	}

	//������ˮ��Ϣ��code+controller��000021001/c/shc/1���������topic��message����ˮ��ʱ��
	public void waterOn(final String equipmentCode,final String controller,final String clientId,final String message){

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
					TimeCount timeCount = new TimeCount(equipmentCode,
							controller,clientId,Integer.parseInt(message)*1000, 1000);
					timeCount.start();
					timeCountMap.put(equipmentCode, timeCount);
					waterInterface.watering(equipmentCode,controller);
				}else{
					waterInterface.waterTimeOut(equipmentCode,controller);
				}
				System.out.println("�ɹ�������Ϣ��"+equipmentCode+"��ˮ��"+message+"/s");
			}

			@Override
			public void connectLostException() {
				System.out.println("��������ʱ����exception");
			}
		}, equipmentCode+controller, clientId, message);
	}

	//�رս�ˮ
	public void waterOffByBtn(String equipmentCode,String controller,String clientId){
		//timeCountMapͳ�Ƹ���ˮ��ͷ�Ķ�ʱ��ʱ�䣬����ʱ����
		if(timeCountMap.containsKey(equipmentCode) && timeCountMap.get(equipmentCode) != null){
			timeCountMap.get(equipmentCode).cancel();
			timeCountMap.get(equipmentCode).onFinish();
			timeCountMap.remove(equipmentCode);
		}else{
			//waterOn(code,controller,clientId,"0");
			waterOff(equipmentCode,controller,clientId,"0");
		}
	}

	//��ˮ
	private void waterOff(String equipmentCode,String controller,String clientId,String off){
		waterOn(equipmentCode, controller, clientId, off);
	}
	
	//��ʱ��CountDownTimer����ʱ
	private class TimeCount extends CountDownTimer{
		private String mEquipmentCode = null;
		private String mController;
		private String mClientId;

		public TimeCount(String equipmentCode,String controller,String clientId,long millisInFuture, long countDownInterval) {
			//��һ��������ʾ��ʱ�䣬�ڶ���������ʾ���ʱ�䡣
			//����(10000, 1000)����˼����ÿ��һ���ص�һ�η���onTick��
			//Ȼ��10��֮���ص�onFinish������
			super(millisInFuture, countDownInterval);
			this.mEquipmentCode = equipmentCode;
			this.mController = controller;
			this.mClientId = clientId;
		}
		//ÿ��һ��ʱ��ͻص�һ�η���onTick
		@Override
		public void onTick(long millisUntilFinished) {			
		}

		@Override
		public void onFinish() {	
			waterOff(mEquipmentCode,mController,mClientId,"0");
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
