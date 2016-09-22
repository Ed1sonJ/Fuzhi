package com.smartfarm.util;

import android.os.CountDownTimer;

import com.smartfarm.mqtt.PublishWC;
import com.smartfarm.mqtt.PublishWC.MyListeners;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 浇水的封装类
 */
public class  WaterRoom {
	private WaterInterface waterInterface = null;
	private PublishWC publishWC = null;		//发布浇水信息
	Map<String,TimeCount> timeCountMap = new HashMap<String, WaterRoom.TimeCount>();	//统计各个水龙头的定时时间
	
	public WaterRoom(WaterInterface waterInterface){
		this.waterInterface = waterInterface;
		publishWC = new PublishWC();
	}

	//浇水接口
	public interface WaterInterface {
		void waterFailed();
		void watering(String code);
		void waterTimeOut(String code);
	}

	//发布浇水信息，code+controller，000021001/c/shc/1，这个就是topic，message：浇水的时间
	public void waterOn(final String code,final String controller,final String clientId,final String message){

//		if(!isNumeric(message)){
//			return;
//		}
//
		publishWC.request(new MyListeners() {
			@Override
			public void success(String arrivedMessage) {
				System.out.println("收到发布浇水信息："+arrivedMessage);
			}
			
			@Override
			public void loading() {
				System.out.println("正在连接MQTT网关");
			}

			@Override
			public void fail() {
				waterInterface.waterFailed();
				System.out.println("MQTT网关连接失败");
			}
			@Override
			public void hasPublish() {
				if(Integer.parseInt(message) != 0){
					//倒计时
					TimeCount timeCount = new TimeCount(code,
							controller,clientId,Integer.parseInt(message)*1000, 1000);
					timeCount.start();
					timeCountMap.put(code, timeCount);
					waterInterface.watering(code);
				}else{
					waterInterface.waterTimeOut(code);
				}
				System.out.println("成功发布消息："+code+"开水："+message+"/s");
			}

			@Override
			public void connectLostException() {
				System.out.println("尝试重连时发生exception");
			}
		}, code+controller, clientId, message);
	}

	//关闭浇水
	public void waterOffByBtn(String code,String controller,String clientId){
		//timeCountMap统计各个水龙头的定时的时间，倒计时集合
		if(timeCountMap.containsKey(code) && timeCountMap.get(code) != null){
			timeCountMap.get(code).cancel();
			timeCountMap.get(code).onFinish();
			timeCountMap.remove(code);
		}else{
			//waterOn(code,controller,clientId,"0");
			waterOff(code,controller,clientId,"0");
		}
	}

	//关水
	private void waterOff(String code,String controller,String clientId,String off){
		waterOn(code, controller, clientId, off);
	}
	
	//计时，CountDownTimer倒计时
	private class TimeCount extends CountDownTimer{
		private String code = null;
		private String controller;
		private String clientId;

		public TimeCount(String code,String controller,String clientId,long millisInFuture, long countDownInterval) {
			//第一个参数表示总时间，第二个参数表示间隔时间。
			//假设(10000, 1000)，意思就是每隔一秒会回调一次方法onTick，
			//然后10秒之后会回调onFinish方法。
			super(millisInFuture, countDownInterval);
			this.code = code;
			this.controller = controller;
			this.clientId = clientId;
		}
		//每隔一段时间就回调一次方法onTick
		@Override
		public void onTick(long millisUntilFinished) {			
		}

		@Override
		public void onFinish() {	
			waterOff(code,controller,clientId,"0");
		}
		
	}

	//判断是否是数字
	private  boolean isNumeric(String str){
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if(!isNum.matches()){
			return false;
		}
		return true;
	}

}
