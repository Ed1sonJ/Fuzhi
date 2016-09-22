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
 * ʩ�ʵķ�װ��
 */

public class FertilizeRoom {
    //���ڽ�����ص����豸���ƽ���
    private FertilizeInterface mFertilizeInterface = null;
    //����ʩ����Ϣ
    private PublishWC mPublishWC = null;
    //ͳ�Ƹ���ʩ��װ�õĶ�ʱʱ��
    Map<String,TimeCount> timeCountMap = new HashMap<>();

    /**
     * ʩ�ʽӿ�
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
     * ����ʩ����Ϣ
     * @param code �豸��
     * @param controller ʩ������controller��־
     * @param clientId �ͻ��˵�Ψһ��־
     * @param message ʩ�ʵ�ʱ��
     */
    public void FertilizeOn(final String code,final String controller,final String clientId,final String message){
        mPublishWC.request(new PublishWC.MyListeners() {
            @Override
            public void loading() {
                Log.d("gzfuzhi","��������MQTT����");
            }

            @Override
            public void success(String arrivedMessage) {
                Log.d("gzfuzhi","�յ���ʩ����Ϣ"+arrivedMessage);
            }

            @Override
            public void fail() {
                //���ýӿڻص�ʧ����Ϣ
                mFertilizeInterface.FertilizeFailed();
                Log.d("gzfuzhi","MQTT��������ʧ��");
            }

            @Override
            public void hasPublish() {
                //�Ѿ������ص�
                if(Integer.parseInt(message) != 0){
                    //����ʱ
                    TimeCount timeCount = new TimeCount(code,
                            controller,clientId,Integer.parseInt(message)*1000, 1000);
                    timeCount.start();
                    timeCountMap.put(code, timeCount);
                    //�ص����豸����
                    mFertilizeInterface.Fertilizing(code);
                }else{
                    //�ص����豸����
                    mFertilizeInterface.FertilizeTimeOut(code);
                }
                Log.d("gzfuzhi","�ɹ�������Ϣ��"+code+"���ϣ�"+message+"/s");
            }

            @Override
            public void connectLostException() {

            }
        },code + controller,clientId,message);
    }

    /**
     * �˹�����ر�ʩ��
     * @param code
     * @param controller
     * @param clientId
     */
    public void FertilizeOffByBtn(String code,String controller,String clientId){
        //timeCountMapͳ�Ƹ���ˮ��ͷ�Ķ�ʱ��ʱ�䣬����ʱ����
        if (timeCountMap.containsKey(code)&&timeCountMap.get(code)!=null){
            timeCountMap.get(code).cancel();
            timeCountMap.get(code).onFinish();
            timeCountMap.remove(code);
        }else{
            FertilizeOff(code,controller,clientId,"0");
        }
    }

    /**
     * �ر�ʩ��
     * @param code
     * @param controller
     * @param clientId
     * @param off
     */
    public void FertilizeOff(String code ,String controller ,String clientId , String off){
        FertilizeOn(code , controller,clientId,off);
    }

    /**
     * ���ڼ�ʱ���࣬����ʩ�ʵ�ʱ��
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
         * ÿcountDownInterval�봥��һ��onTick()
         * @param millisUntilFinished
         */
        @Override
        public void onTick(long millisUntilFinished) {

        }

        /**
         * millisInFuture������onFinish()
         */
        @Override
        public void onFinish() {
            //��������ʱ���ùر�ʩ��
            FertilizeOff(mCode,mController,mClientId,"0");
        }
    }
}
