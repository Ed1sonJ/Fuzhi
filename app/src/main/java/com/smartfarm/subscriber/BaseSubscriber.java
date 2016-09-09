package com.smartfarm.subscriber;

import android.util.Log;
import rx.Subscriber;

public class BaseSubscriber extends Subscriber<Object> {
	@Override
	public void onCompleted() {
		
	}

	@Override
	public void onError(Throwable e) {
		Log.d("gzfuzhi", e.toString());
	}

	@Override
	public void onNext(Object object) {
		
	}
}
