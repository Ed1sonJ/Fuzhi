/* 
 * @ProjectName VideoGoJar
 * @Copyright HangZhou Hikvision System Technology Co.,Ltd. All Right Reserved
 * 
 * @FileName EzvizApplication.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2014-7-12
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo;

import android.app.Application;

import com.videogo.constant.Config;
import com.videogo.openapi.EZOpenSDK;
/**
 * 自定义应用
 *
 * @author xiaxingsuo
 */
public class EzvizApplication extends Application {
	// 开放平台申请的APP key & secret key
	// open
    public static String APP_KEY = "d553cd512a8840168f079aab76cc3cae"; // 2015/10/29


	@Override
	public void onCreate() {
		super.onCreate();

        Config.LOGGING = true;
        EZOpenSDK.initLib(this, APP_KEY, "");

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
	}
}
