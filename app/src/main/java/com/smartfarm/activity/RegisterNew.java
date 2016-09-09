package com.smartfarm.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;

import com.smartfarm.http.LoginHttp;
import com.smartfarm.http.RegisterHttp;
import com.smartfarm.observable.LoginObservable;
import com.smartfarm.observable.RegisterObservable;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.Common;
import com.smartfarm.util.Config;
import com.smartfarm.util.ToastUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Subscriber;
import rx.schedulers.Schedulers;

public class RegisterNew extends Activity {

	private EditText et_phone;
	private EditText et_password;
	private EditText et_name;
	private Button loginBtn;

	private Config config;
	private String phone;
	private String password;
	private String name;
	private BaseProgressDialog progressDialog;

	private RegisterHttp registerHttp = new RegisterHttp();
	private LoginHttp loginHttp = new LoginHttp();

	private boolean phoneOK = false, passwordOK = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_new);
		config = new Config(getApplication());
		findViewById();
		initView();
	}

	private void findViewById() {
		et_phone = (EditText) findViewById(R.id.et_phone);
		et_phone.clearFocus();
		et_name = (EditText) findViewById(R.id.et_name);
		et_name.clearFocus();
		et_password = (EditText) findViewById(R.id.et_password);
		et_password.requestFocus();
		loginBtn = (Button) findViewById(R.id.loginBtn);
	}

	private void getData() {
		phone = et_phone.getText().toString();
		name = et_name.getText().toString();
		password = et_password.getText().toString();
	}

	private void initView() {
		Intent intent = getIntent();
		phone = intent.getExtras().getString("phoneNumber");
		et_phone.setText(phone);

		progressDialog = new BaseProgressDialog(RegisterNew.this);
		progressDialog.setMessage("ÕýÔÚ×¢²á");

		et_password.setOnFocusChangeListener(new TextChecker("password"));

		loginBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				et_password.clearFocus();
				getData();
				if (passwordOK) {
					getRegisterObservable(name, phone, phone, password);
				}
			}
		});
	}

	class TextChecker implements OnFocusChangeListener {
		private String fieldName;

		public TextChecker(String fieldName) {
			this.fieldName = fieldName;
		}

		private boolean checkPwd(String text) {
			Pattern p = Pattern.compile("^[0-9A-z]{6,12}$");
			Matcher m = p.matcher(text);
			return m.matches();
		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			EditText view = (EditText) v;
			if (hasFocus == true) {
				view.setTextColor(Color.BLACK);
				return;
			}
			String text = view.getText().toString();
			if (fieldName.equals("password")) {
				if (!checkPwd(text)) {
					ToastUtil.showShort(getApplicationContext(),
							"ÃÜÂëÒªÎª6µ½12Î»µÄÊý×Ö£¬×ÖÄ¸»òÓ¢ÎÄ·ûºÅ");
					view.setTextColor(Color.RED);
					passwordOK = false;
				} else {
					passwordOK = true;
				}
			}
		}
	}
	
	private void getRegisterObservable(String name,final String username,final String phone,final String password){
		RegisterObservable.create(name, username, phone, password).subscribeOn(Schedulers.newThread())
		.subscribe(new Subscriber<Boolean>() {
			@Override
			public void onStart() {
				super.onStart();
				runOnUiThread(new Runnable() {			
					@Override
					public void run() {
						progressDialog.show();
					}
				});
			}

			@Override
			public void onCompleted() {
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						progressDialog.dismiss();
					}
				});
			}

			@Override
			public void onError(Throwable e) {
				e.printStackTrace();
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						progressDialog.dismiss();
						ToastUtil.showShort(RegisterNew.this,
								"×¢²áÊ§°Ü");
					}
				});
			}

			@Override
			public void onNext(final Boolean response) {
				runOnUiThread(new Runnable() {				
					@Override
					public void run() {
						if(response == true){
							progressDialog.dismiss();
							ToastUtil.showShort(RegisterNew.this,
									"×¢²á³É¹¦");
							getLoginObservable(phone, password);
						}else{
							progressDialog.dismiss();
							ToastUtil.showShort(RegisterNew.this,
									"×¢²áÊ§°Ü");
						}
					}
				});
			}
		});
	}
	
	private void getLoginObservable(final String username, final String password){
		LoginObservable.create(username, password).subscribeOn(Schedulers.newThread()).subscribe(
				new Subscriber <Map<String,String>>() {
					@Override
					public void onCompleted() {
						Log.d("RegisterNew", "onCompleted");
					}

					@Override
					public void onError(Throwable e) {
						runOnUiThread(new Runnable() {					
							@Override
							public void run() {
								config.clear();
								Intent loginIntent = new Intent(RegisterNew.this,
										Login.class);
								startActivity(loginIntent);
								RegisterNew.this.finish();
							}
						});
					}

					@Override
					public void onNext(final Map<String, String> login) {
						runOnUiThread(new Runnable() {						
							@Override
							public void run() {
								if(login != null){
									config.setToken(login.get("token"));
									config.setUsername(username);
									config.setPassword(password);
									Common.setToken(login.get("token"));
									Common.setUsername(username);
									Intent intent = new Intent(RegisterNew.this,
											MainActivityNew.class);
									startActivity(intent);
									finish();
								}else{
									config.clear();
									ToastUtil.showShort(RegisterNew.this, "µÇÂ½Ê§°Ü£¬ÇëÖØÐÂµÇÂ½");
									Intent loginIntent = new Intent(RegisterNew.this,
											Login.class);
									startActivity(loginIntent);
									RegisterNew.this.finish();
								}
							}
						});
					}
				});
	}
}
