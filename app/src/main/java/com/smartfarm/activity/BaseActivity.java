package com.smartfarm.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class BaseActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("gzfuzhi", this.toString() + " onCreate");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d("gzfuzhi", this.toString() + " onRestart");
	};

	@Override
	protected void onStart() {
		super.onStart();
		Log.d("gzfuzhi", this.toString() + " onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("gzfuzhi", this.toString() + " onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("gzfuzhi", this.toString() + " onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("gzfuzhi", this.toString() + " onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("gzfuzhi", this.toString() + " onDestroy");
	}
}
