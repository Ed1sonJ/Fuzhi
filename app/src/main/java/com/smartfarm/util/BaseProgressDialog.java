package com.smartfarm.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;

public class BaseProgressDialog {
	private Activity activity;
	private ProgressDialog progressDialog;

	public BaseProgressDialog(Activity ctx) {
		activity = ctx;
		progressDialog = new ProgressDialog(ctx);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("正在更新实时数据");
	}

	public void setMessage(String msg) {
		progressDialog.setMessage(msg);
	}

	public void show() {
		if (!activity.isFinishing())
			progressDialog.show();
	}

	public void dismiss() {
		progressDialog.dismiss();
	}

	public void setProgressStyle(int style) {
		progressDialog.setProgressStyle(style);
	}

	public void setProgress(int value) {
		progressDialog.setProgress(value);
	}

	public void setOnCancelListener(OnCancelListener listener) {
		progressDialog.setOnCancelListener(listener);
	}
}
