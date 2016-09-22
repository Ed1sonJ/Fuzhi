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
        void Fertilizing(String equipmentCode,String controller);
        void FertilizeTimeOut(String equipmentCode,String controller);
    }

    public FertilizeRoom(FertilizeInterface mFertilizeInterface) {
        this.mFertilizeInterface = mFertilizeInterface;
        mPublishWC = new PublishWC();
    }

    /**
     * ����ʩ����Ϣ,ʶ��ǰ���豸Ϊ equipmentCode+controller -> 003001/c/fc/1
     * @param equipmentCode
     * @param controller ʩ������controller��־��ʶ��fc��shc�Ȳ�ͬ�豸
     * @param clientId �ͻ��˵�Ψһ��־
     * @param message ʩ�ʵ�ʱ��
     */
    public void FertilizeOn(final String equipmentCode,final String controller,final String clientId,final String message){
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
                    TimeCount timeCount = new TimeCount(equipmentCode,
                            controller,clientId,Integer.parseInt(message)*1000, 1000);
                    timeCount.start();
                    timeCountMap.put(equipmentCode, timeCount);
                    //�ص����豸����
                    mFertilizeInterface.Fertilizing(equipmentCode,controller);
                }else{
                    //�ص����豸����
                    mFertilizeInterface.FertilizeTimeOut(equipmentCode,controller);
                }
                Log.d("gzfuzhi","�ɹ�������Ϣ��"+equipmentCode+"���ϣ�"+message+"/s");
            }

            @Override
            public void connectLostException() {

            }
        },equipmentCode + controller,clientId,message);
    }

    /**
     * �˹�����ر�ʩ��
     * @param equipmentCode
     * @param controller
     * @param clientId
     */
    public void FertilizeOffByBtn(String equipmentCode,String controller,String clientId){
        //timeCountMapͳ�Ƹ���ˮ��ͷ�Ķ�ʱ��ʱ�䣬����ʱ����
        if (timeCountMap.containsKey(equipmentCode)&&timeCountMap.get(equipmentCode)!=null){
            timeCountMap.get(equipmentCode).cancel();
            timeCountMap.get(equipmentCode).onFinish();
            timeCountMap.remove(equipmentCode);
        }else{
            FertilizeOff(equipmentCode,controller,clientId,"0");
        }
    }

    /**
     * �ر�ʩ��
     * @param equipmentCode
     * @param controller
     * @param clientId
     * @param off
     */
    public void FertilizeOff(String equipmentCode ,String controller ,String clientId , String off){
        FertilizeOn(equipmentCode , controller,clientId,off);
    }

    /**
     * ���ڼ�ʱ���࣬����ʩ�ʵ�ʱ��
     */
    private class TimeCount extends CountDownTimer{
        private String mEquipmentCode;
        private String mController;
        private String mClientId;

        public TimeCount(String equipmentCode ,String controller,String clientId,long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            this.mEquipmentCode = equipmentCode;
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
            FertilizeOff(mEquipmentCode,mController,mClientId,"0");
        }
    }
}
