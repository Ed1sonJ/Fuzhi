package com.smartfarm.observable;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartfarm.bean.ConfigBean;
import com.smartfarm.util.Common;
import com.smartfarm.util.Protocol;

import java.net.URLEncoder;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by shawn on 2016/1/11.
 */
public class GetConfigObservable {
    private static String getPayload(String code){
        try {
            String data = "data="+ URLEncoder.encode("{\"token\":\"" + Common.token + "\",\"code\":\"" + code + "\"}",
                    "UTF-8");
            System.out.println("我在这里帮你调试"+data);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public static Observable<List<ConfigBean>> createObservable(String code){
        Observable<String> httpObservable = HttpObservable.createObservable(
                Protocol.GETCONFIG, getPayload(code));
        Observable<List<ConfigBean>> getConfigObservable = httpObservable
                .map(new Func1<String, List<ConfigBean>>() {
                    @Override
                    public List<ConfigBean> call(String response) {
                        System.out.println("我在这里帮你调试"+response);
                        List<ConfigBean> configBean;
                        if(!response.isEmpty()){
                            Gson gson = new Gson();
                            try {
                                configBean = gson.fromJson(response, new TypeToken<List<ConfigBean>>() {}.getType());
                                return configBean;
                            } catch (Exception e) {
                                return null;
                            }
                        }else{
                            return null;
                        }
                    }
                });
        return getConfigObservable;
    }
}
