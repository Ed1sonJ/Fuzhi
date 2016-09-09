package com.smartfarm.crash;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.util.Log;

public class CrashHandler implements UncaughtExceptionHandler {

    private static CrashHandler instance;  //�������ã������������ɵ����ģ���Ϊ����һ��Ӧ�ó�������ֻ��Ҫһ��UncaughtExceptionHandlerʵ��
    
    private CrashHandler(){}
   
    public synchronized static CrashHandler getInstance(){  //ͬ�����������ⵥ�����̻߳����³����쳣
        if (instance == null){
            instance = new CrashHandler();
        }
        return instance;
    }
   
    public void init(Context ctx){  //��ʼ�����ѵ�ǰ�������ó�UncaughtExceptionHandler������
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		//����δ������쳣����ʱ���ͻ����������
        Log.e("uncaughtException", "thread: " + thread+ " name: "
        		+ thread.getName() + " id: " + thread.getId() + "exception: "
                + ex);
             String threadName = thread.getName();
             if ("PublishWC".equals(threadName)){
                   Log.d("uncaughtException", "PublishWC");
             }else {
              }
	}

}
