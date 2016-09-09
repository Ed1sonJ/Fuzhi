package com.smartfarm.observable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

public class LoadImageObservable {
	public static Observable<Void> createObservable(final String address,
			final File imgFile) {
		Observable<Void> observable = Observable
				.create(new OnSubscribe<Void>() {
					@Override
					public void call(Subscriber<? super Void> subscriber) {
						URL url;
						try {
							url = new URL(address);
							HttpURLConnection conn = (HttpURLConnection) url
									.openConnection();
							conn.setInstanceFollowRedirects(true);
							conn.setConnectTimeout(15000);
							conn.setReadTimeout(15000);
							InputStream is = conn.getInputStream();
							BufferedInputStream bi = new BufferedInputStream(is);
							BufferedOutputStream bo = new BufferedOutputStream(
									new FileOutputStream(imgFile));
							byte[] buff = new byte[4096];
							int len = 0;
							while ((len = bi.read(buff)) > 0) {
								bo.write(buff, 0, len);
							}
							bi.close();
							bo.close();
							conn.disconnect();
							subscriber.onNext(null);
							subscriber.onCompleted();
						} catch (Exception e) {
							subscriber.onError(e);
						}
					}
				});
		return observable;
	}
}
