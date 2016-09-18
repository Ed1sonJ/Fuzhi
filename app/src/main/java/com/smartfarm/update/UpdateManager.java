package com.smartfarm.update;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartfarm.bean.UpdateBean;
import com.smartfarm.util.BaseProgressDialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateManager {
	private static UpdateManager instance;

	private Activity context;

//	static public synchronized UpdateManager getInstance(Activity ctx) {
//		if (instance == null) {
//			instance = new UpdateManager(ctx);
//		}
//		return instance;
//	}

	private UpdateManager(Activity ctx) {
		context = ctx;
	}

	public static UpdateManager getInstance(Activity context){
		//提高效率
		if(instance == null){
			synchronized (UpdateManager.class){
				if(instance == null){
					instance = new UpdateManager( context );
				}
			}
		}
		return instance;
	}

	/**
	 * 检测更新的方法，检测服务器存在的update.json文件
	 * @param callback
     */
	public void checkUpdate(final CheckUpdateCallback callback) {
		final int currentVersion = getCurrentVersion();
		new Thread(new Runnable() {
			@Override
			public void run() {
				callback.onCheckingUpdate();
				StringBuffer sb = new StringBuffer();
				try {
					URL url = new URL("http://app.gzfuzhi.com/downloads/update.json");
					HttpURLConnection urlConnection = (HttpURLConnection) url
							.openConnection();
					int length = urlConnection.getContentLength();
					if (length > 0) {
						byte[] buff = new byte[length];
						InputStream in = new BufferedInputStream(urlConnection
								.getInputStream());
						int count = 0;
						while (count < length) {
							count += in.read(buff);
						}
						//将读入的buff转换成String类型
						String strJson = new String(buff);
						Gson gson = new Gson();
						UpdateBean updateBean=gson.fromJson(strJson, new TypeToken<UpdateBean>() {}.getType());
						int lastestVersion = updateBean.getVersion();
						String apkUrl = updateBean.getUrl();
						String date = updateBean.getDate();
						String note = updateBean.getNote();
						callback.onCheckUpdateFinished(currentVersion,
								lastestVersion, apkUrl, date, note);
					}

				} catch (Exception e) {
					e.printStackTrace();
					callback.onCheckUpdateFinished(currentVersion, -1, "", "",
							"");
				}
			}
		}).start();
	}

	/**
	 * 获取当前的版本号
	 * @return
     */
	public int getCurrentVersion() {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pInfo.versionCode;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 获取当前的版本名
	 * @return
     */
	public String getCurrentVersionName() {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pInfo.versionName;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "0.0.1";
		}
	}

	/**
	 * 连接url下载apk
	 * @param url
     */
	public void downloadAndInstall(String url) {
		DownloadAndInstallTask task = new DownloadAndInstallTask();
		task.execute(url);
	}

	private class DownloadAndInstallTask extends
			AsyncTask<String, Integer, Boolean> {

		private File apkFile = new File(context.getExternalCacheDir(),
				"SmartFarm_tmp.apk");
		private boolean downloadFinished = false;
		private BaseProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			progressDialog = new BaseProgressDialog(context);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.show();
		};

		@Override
		protected Boolean doInBackground(String... urls) {
			try {
				URL url;
				url = new URL(urls[0]);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				int len = connection.getContentLength();
				if (len > 0) {
					BufferedInputStream bin = new BufferedInputStream(
							connection.getInputStream());
					FileOutputStream fout = new FileOutputStream(apkFile);
					byte[] buffer = new byte[1024];
					int count = 0;
					int progress = 0;
					while ((count = bin.read(buffer)) != -1) {
						//写进文件里面
						fout.write(buffer, 0, count);
						progress += count;
						//更新进度条
						publishProgress((int) (1.0f * progress / len * 100));
					}
					bin.close();
					connection.disconnect();
					fout.close();
					downloadFinished = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return downloadFinished;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressDialog.setProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(Boolean finished) {
			progressDialog.dismiss();
			if (finished) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(apkFile),
						"application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		}
	}
}
