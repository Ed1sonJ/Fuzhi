package com.smartfarm.update;

/**
 * 用于检测更新的回调接口
 */
public interface CheckUpdateCallback {
	void onCheckingUpdate();
	void onCheckUpdateFinished(int currentVersion, int lastestVersion, String url, String date, String note);
}
