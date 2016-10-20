package com.smartfarm.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.smartfarm.fixleak.IMMLeaks;
import com.smartfarm.observable.LoginObservable;
import com.smartfarm.util.Common;
import com.smartfarm.util.Config;
import com.smartfarm.util.ToastUtil;

import java.util.Map;

import rx.Subscriber;
import rx.schedulers.Schedulers;
public class AutoLogin extends Activity {

	private Config config;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_autologin);
		init();
		login();
	}

	private void init() {
		config = new Config(this);
	}

	private void login() {
		if (config.hasLogin()) {
			autoLogin();
		} else {
			Intent intent = new Intent(AutoLogin.this, Login.class);
			startActivity(intent);
			finish();
		}
	}

	private void autoLogin() {// ³¢ÊÔ×Ô¶¯Á¬½Ó
		String name = config.getUsername();
		String pwd = config.getPassword();
		getLoginObservable(name, pwd);
	}

	
	private void getLoginObservable(final String username, final String password){
		LoginObservable.create(username, password).subscribeOn(Schedulers.newThread()).subscribe(
				new Subscriber <Map<String,String>>() {
					@Override
					public void onCompleted() {
						Log.d("AutoLogin", "onCompleted");
					}

					@Override
					public void onError(Throwable e) {
						runOnUiThread(new Runnable() {					
							@Override
							public void run() {
								config.clear();
								ToastUtil.showShort(AutoLogin.this, "µÇÂ¼Ê§°Ü£¬ÇëÖØÐÂµÇÂ½");
								Intent intent = new Intent(AutoLogin.this, Login.class);
								startActivity(intent);
								finish();
							}
						});
					}

					@Override
					public void onNext(final Map<String, String> login) {
						runOnUiThread(new Runnable() {						
							@Override
							public void run() {
								if(login != null){
									Common.setToken(login.get("token"));
									Common.setUsername(username);
									Intent intent = new Intent(AutoLogin.this,
											MainActivityNew.class);
									startActivity(intent);
									finish();
								}else{
									ToastUtil.showShort(AutoLogin.this, "µÇÂ½Ê§°Ü£¬ÇëÖØÐÂµÇÂ½");
									Intent intent = new Intent(AutoLogin.this, Login.class);
									startActivity(intent);
									finish();
								}
							}
						});
					}
				});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			moveTaskToBack(false);		
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		super.onStop();
		IMMLeaks.fixFocusedViewLeak(getApplication());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
