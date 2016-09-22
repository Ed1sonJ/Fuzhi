package com.smartfarm.util;

import android.os.CountDownTimer;
import android.util.Log;

import com.facebook.common.logging.LoggingDelegate;
import com.smartfarm.mqtt.PublishWC;
import com.videogo.universalimageloader.utils.L;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hp on 2016/9/21.
 *
 * 施肥的封装类
 */

public class FertilizeRoom {
    //用于将结果回调到设备控制界面
    private FertilizeInterface mFertilizeInterface = null;
    //发布施肥信息
    private PublishWC mPublishWC = null;
    //统计各个施肥装置的定时时间
    Map<String,TimeCount> timeCountMap = new HashMap<>();

    /**
     * 施肥接口
     */
    public interface FertilizeInterface{
        void FertilizeFailed();
        void Fertilizing(String code);
        void FertilizeTimeOut(String code);
    }

    public FertilizeRoom(FertilizeInterface mFertilizeInterface) {
        this.mFertilizeInterface = mFertilizeInterface;
        mPublishWC = new PublishWC();
    }

    /**
     * 发布施肥信息
     * @param code 设备码
     * @param controller 施肥器的controller标志
     * @param clientId 客户端的唯一标志
     * @param message 施肥的时间
     */
    public void FertilizeOn(final String code,final String controller,final String clientId,final String message){
        mPublishWC.request(new PublishWC.MyListeners() {
            @Override
            public void loading() {
                Log.d("gzfuzhi","正在连接MQTT网关");
            }

            @Override
            public void success(String arrivedMessage) {
                Log.d("gzfuzhi","收到了施肥信息"+arrivedMessage);
            }

            @Override
            public void fail() {
                //利用接口回调失败信息
                mFertilizeInterface.FertilizeFailed();
                Log.d("gzfuzhi","MQTT网关连接失败");
            }

            @Override
            public void hasPublish() {
                //已经发布回调
                if(Integer.parseInt(message) != 0){
                    //倒计时
                    TimeCount timeCount = new TimeCount(code,
                            controller,clientId,Integer.parseInt(message)*1000, 1000);
                    timeCount.start();
                    timeCountMap.put(code, timeCount);
                    //回调到设备界面
                    mFertilizeInterface.Fertilizing(code);
                }else{
                    //回调到设备界面
                    mFertilizeInterface.FertilizeTimeOut(code);
                }
                Log.d("gzfuzhi","成功发布信息："+code+"肥料："+message+"/s");
            }

            @Override
            public void connectLostException() {

            }
        },code + controller,clientId,message);
    }

    /**
     * 人工点击关闭施肥
     * @param code
     * @param controller
     * @param clientId
     */
    public void FertilizeOffByBtn(String code,String controller,String clientId){
        //timeCountMap统计各个水龙头的定时的时间，倒计时集合
        if (timeCountMap.containsKey(code)&&timeCountMap.get(code)!=null){
            timeCountMap.get(code).cancel();
            timeCountMap.get(code).onFinish();
            timeCountMap.remove(code);
        }else{
            FertilizeOff(code,controller,clientId,"0");
        }
    }

    /**
     * 关闭施肥
     * @param code
     * @param controller
     * @param clientId
     * @param off
     */
    public void FertilizeOff(String code ,String controller ,String clientId , String off){
        FertilizeOn(code , controller,clientId,off);
    }

    /**
     * 用于计时的类，倒数施肥的时间
     */
    private class TimeCount extends CountDownTimer{
        private String mCode;
        private String mController;
        private String mClientId;

        public TimeCount(String code ,String controller,String clientId,long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            this.mCode = code;
            this.mController = controller;
            this.mClientId = clientId;
        }
        /**
         * 每countDownInterval秒触发一次onTick()
         * @param millisUntilFinished
         */
        @Override
        public void onTick(long millisUntilFinished) {

        }

        /**
         * millisInFuture秒后出发onFinish()
         */
        @Override
        public void onFinish() {
            //倒数结束时调用关闭施肥
            FertilizeOff(mCode,mController,mClientId,"0");
        }
    }
}
