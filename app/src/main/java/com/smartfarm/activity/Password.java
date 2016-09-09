package com.smartfarm.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.smartfarm.http.PasswordHttp;
import com.smartfarm.http.PasswordHttp.MyListener;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.Config;
import com.smartfarm.util.ToastUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Password extends Activity {

	private RelativeLayout back;
	private Button ok;
	private EditText oldPassword;
	private EditText newPassword;
	private PasswordHttp passwordHttp = new PasswordHttp();
	private BaseProgressDialog customProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password);

		findViewById();
		initView();

	}

	private void findViewById() {
		ok = (Button) findViewById(R.id.okBtn);
		oldPassword = (EditText) findViewById(R.id.oldPassword);
		newPassword = (EditText) findViewById(R.id.newPassword);
		back = (RelativeLayout) findViewById(R.id.backR);

	}

	private boolean checkPwd(String text) {
		Pattern p = Pattern.compile("^[0-9A-z]{6,12}$");
		Matcher m = p.matcher(text);
		return m.matches();
	}
	
	private boolean checkInput(){
		return (checkPwd(oldPassword.getText().toString())&&
				checkPwd(newPassword.getText().toString()));
	}
	
	private void initView() {
		
		customProgressDialog = new BaseProgressDialog(Password.this);
		customProgressDialog.setMessage("修改密码。。。");
		
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//如果输入不合法返回
				if(!checkInput()){
					ToastUtil.showLong(Password.this, "输入格式不正确");
					return;
				}				
				passwordHttp.request(oldPassword.getText().toString(),
						newPassword.getText().toString(), new MyListener() {

							@Override
							public void success(String token) {
								customProgressDialog.dismiss();
								new Config(Password.this).setToken(token);
								ToastUtil.showShort(Password.this, "修改成功");
								Password.this.finish();

							}

							@Override
							public void loading() {
								customProgressDialog.show();
							}

							@Override
							public void fail() {
								customProgressDialog.dismiss();
								ToastUtil.showShort(Password.this, "修改失败");

							}
						});

			}
		});

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Password.this.finish();

			}
		});
	}

}
