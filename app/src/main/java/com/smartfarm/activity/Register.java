package com.smartfarm.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.smartfarm.util.CommonTools;
import com.smartfarm.util.ToastUtil;


public class Register  extends Activity{
	
	private EditText mobile;
	private Button access_password;
	private CommonTools tools;
	private String get_password;
	private boolean flag = false;
	private ImageView iv_back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_register);
		findViewById();
		initView();
	}

	private void findViewById() {
		mobile = (EditText) findViewById(R.id.edit_mobile);
		access_password = (Button) findViewById(R.id.access_password);
		iv_back = (ImageView) findViewById(R.id.iv_back);
		
	}
	
	private void initView() {
		tools = new CommonTools();
		get_password = mobile.getText().toString();
		flag = tools.isMobileNO(get_password);
		if (flag == false) {
			ToastUtil.showShort(Register.this, "您输入的手机号不合法");
		}
		access_password.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				startActivity(new Intent(Register.this, RegisterBormalActivity.class));
				
			}
		});
		
		iv_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(Register.this, Login.class));
			}
		});
	}
}
