package com.smartfarm.application;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.smartfarm.event.GlobalEvent;
import com.tencent.bugly.crashreport.CrashReport;
import com.videogo.openapi.EZOpenSDK;

public class CrashReportApplication extends Application {
    public static String APP_KEY = "d553cd512a8840168f079aab76cc3cae";
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(getApplicationContext());
        CrashReport.initCrashReport(getApplicationContext(), "900054705", false);
        //≥ı ºªØ
        EZOpenSDK.initLib(this, APP_KEY, "");
        createEventBus();
    }

    protected void createEventBus() {
        GlobalEvent.init();
    }
}
