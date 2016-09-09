package com.smartfarm.observable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import rx.Observable;
import rx.functions.Func1;

import com.smartfarm.util.Common;
import com.smartfarm.util.Protocol;

public class DeleteEquipmentObservable {

    private static String getPayload(String code) {
        try {
            return "data=" + URLEncoder.encode("{\"token\":\"" + Common.token + "\",\"code\":\"" + code + "\"}", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Observable<Boolean> create(final String code) {
        return HttpObservable.createObservable(Protocol.UNBIND, getPayload(code))
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String response) {
                        if (response.contains("ok")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
    }
}
