package com.smartfarm.fragmentUtil;

import android.app.Activity;
import android.util.Log;

import com.smartfarm.bean.TypeBean;
import com.smartfarm.observable.GetIndicatorObservable;
import com.smartfarm.observable.InterveneObservable;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.ToastUtil;
import com.videogo.universalimageloader.utils.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.schedulers.Schedulers;

public class UploadAndDownloadScheme {
    private  Activity activity = null;
    private SchemeListener schemeListener = null;
    private BaseProgressDialog dialog = null;
    public UploadAndDownloadScheme(Activity activity){
        this.activity = activity;
        dialog = new BaseProgressDialog(activity);
    }
    public interface SchemeListener{
        void result(Map<String, ArrayList<String>> result);
        void noIndicator();
    }

    /**
     * ��ȡIndicator��RxJava
     * @param code
     * @param listener
     */
    public  void getIndicatorType(String code, final SchemeListener listener){
        this.schemeListener = listener;
        GetIndicatorObservable.createObservable(code).
                subscribeOn(Schedulers.newThread()).
                subscribe(new Subscriber<List<TypeBean>>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setMessage("����������Ϣ");
                                dialog.show();
                            }
                        });
                    }

                    @Override
                    public void onCompleted() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("getIndicatorObservable", "OnCompleted");
                                dialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable arg0) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showShort(activity, "��������ʧ��");
                            }
                        });
                    }

                    @Override
                    public void onNext(final List<TypeBean> list) {
                       activity.runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               if (list != null && list.size() != 0) {
//                                    invalidIndicator();
                                   getProtocolKeyAndName(list);
                               } else {
                                   schemeListener.noIndicator();
//                                    noneInvalidIndicator();
//                                    ToastUtil.showLong(activity, "���豸û�пɸ�Ԥѡ��");
                               }
                           }
                       });
                    }
                });
    }

    /**
     * ��list������key��name
     * protocolKey:lc,protocolName:��ǿ������
     * protocolKey:lqc,protocolName:���ʿ�����
     * protocolKey:tc,protocolName:�¶ȿ�����
     * protocolKey:hc,protocolName:ʪ�ȿ�����
     * protocolKey:co2c,protocolName:������̼������
     * protocolKey:shc,protocolName:����ʪ�ȿ�����
     * @param list
     */
    private void getProtocolKeyAndName(List<TypeBean> list){
        Map<String,ArrayList<String>> protocols = new HashMap<>();
        ArrayList<String> protocolKeys = new ArrayList<>();
        ArrayList<String> protocolNames = new ArrayList<>();
        for (TypeBean t : list) {
            protocolKeys.add(t.protocolKey);
            protocolNames.add(t.name);
            Log.i("gzfuzhi","protocolKey:"+t.protocolKey+",protocolName:"+t.name);
        }
        protocols.put("protocolKeys",protocolKeys);
        protocols.put("protocolNames",protocolNames);
        schemeListener.result(protocols);
    }
    //intervene��ֹ,��Ԥ,��Ϊ���ƣ�����app����
    public void interveneObservable(String code, String startTime, String endTime, String protocolKey,
                                     String value, String upper, String lower){
        InterveneObservable.create(code, startTime, endTime, protocolKey, value, upper, lower).
                subscribeOn(Schedulers.newThread()).
                subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setMessage("�����ϴ�����");
                                dialog.show();
                            }
                        });
                    }

                    @Override
                    public void onCompleted() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable arg0) {
                       activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                ToastUtil.showShort(activity, "�����ϴ�ʧ��");
                            }
                        });
                    }

                    @Override
                    public void onNext(final Boolean arg0) {
                       activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (arg0 == true) {
                                    ToastUtil.showShort(activity, "�����ϴ��ɹ�");
                                } else {
                                    ToastUtil.showShort(activity, "������Ϣ����");
                                }
                            }
                        });
                    }
                });
    }
}
