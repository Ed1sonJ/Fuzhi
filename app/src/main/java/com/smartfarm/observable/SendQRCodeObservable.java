package com.smartfarm.observable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.smartfarm.util.Protocol;

import rx.Observable;
import rx.functions.Func1;

public class SendQRCodeObservable {
    private static String getPayload(String qrCode, String token) {
        try {
            return "data="
                    + URLEncoder.encode("{\"content\":\"" + qrCode
                    + "\",\"token\":\"" + token + "\"}", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Observable<Boolean> create(String qrCode, String token) {
        return HttpObservable.createObservable(Protocol.SCANCODE_URL,
                getPayload(qrCode, token)).map(new Func1<String, Boolean>() {
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
