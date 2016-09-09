package com.smartfarm.update;

public interface CheckUpdateCallback {
	public void onCheckingUpdate();
	public void onCheckUpdateFinished(int currentVersion, int lastestVersion, String url, String date, String note);
}
