package com.smartfarm.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.smartfarm.http.ScanHttp;
import com.smartfarm.http.ScanHttp.MyListener;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.ToastUtil;
import com.videogo.scan.main.CaptureActivity;

public class QR extends Activity {
	private Button scanButton;
	private ScanHttp scanHttp = new ScanHttp();
	private String scanResult;
	private Intent intent;
	private String token;
	private BaseProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qrcode);
		initView();
	}

	private void initView() {
		progressDialog = new BaseProgressDialog(QR.this);
		progressDialog.setMessage("ÇëÉÔµÈ...");

		scanButton = (Button) findViewById(R.id.scanButton);
		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent startScan = new Intent(QR.this, CaptureActivity.class);
				startActivityForResult(startScan, 0);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			scanResult = data.getExtras().getString("result");
			intent = getIntent();
			token = intent.getStringExtra("token");
			startConnect();
		}
	}

	private void startConnect() {
		scanHttp.request(scanResult, token, new MyListener() {
			@Override
			public void success() {
				progressDialog.dismiss();
				ToastUtil.showLong(QR.this, "É¨Âë³É¹¦£¡£¡£¡");
				Intent intent = new Intent(QR.this, Login.class);
				startActivity(intent);
				finish();
			}

			@Override
			public void loading() {
				progressDialog.show();
			}

			@Override
			public void fail() {
				progressDialog.dismiss();
				ToastUtil.showLong(QR.this, "É¨ÂëÊ§°Ü£¡£¡£¡");
			}
		});
	}
}
