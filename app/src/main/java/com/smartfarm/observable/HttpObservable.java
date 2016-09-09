package com.smartfarm.observable;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

public class HttpObservable {
	public static String postAndResponse(String address, String payload)
			throws Exception {
		Log.d("gzfuzhi", Thread.currentThread().getName());
		URL url = new URL(address);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(60000);
		conn.setReadTimeout(60000);
		conn.setDoOutput(true);
		BufferedWriter ou = new BufferedWriter(new OutputStreamWriter(
				conn.getOutputStream()));
		ou.write(payload);
		ou.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		StringBuffer response = new StringBuffer();
		String line = in.readLine();
		while (line != null) {
			response.append(line);
			line = in.readLine();
		}
		in.close();
		conn.disconnect();
		return response.toString();
	}

	public static Observable<String> createObservable(final String address,
			final String payload) {
		Observable<String> observable = Observable
				.create(new OnSubscribe<String>() {
					@Override
					public void call(Subscriber<? super String> subscriber) {
						try {
							String response = postAndResponse(address, payload);
							subscriber.onNext(response);
							subscriber.onCompleted();
						} catch (Exception e) {
							subscriber.onError(e);
						}
					}
				});
		return observable;
	}
}
