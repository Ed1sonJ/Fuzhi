package com.smartfarm.application;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.smartfarm.crash.CrashHandler;
import com.smartfarm.event.GlobalEvent;
import com.videogo.openapi.EZOpenSDK;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formUri = "http://app.gzfuzhi.com:8222/py/error_report.py", logcatArguments = {
        "-t", "50", "ActivityManager:I", "gzfuzhi:V", "*:S"}, socketTimeout = 20000)
public class CrashReportApplication extends Application {
    public static String APP_KEY = "d553cd512a8840168f079aab76cc3cae";
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(getApplicationContext());
        ACRA.init(this);
        CrashHandler handler = CrashHandler.getInstance();
        //handler.init(getApplicationContext()); //在Appliction里面设置异常处理器为UncaughtExceptionHandler处理器
        //初始化
        EZOpenSDK.initLib(this, APP_KEY, "");
        createEventBus();
    }

    protected void createEventBus() {
        GlobalEvent.init();
    }
}
