package com.smartfarm.update;

/**
 * ���ڼ����µĻص��ӿ�
 */
public interface CheckUpdateCallback {
	void onCheckingUpdate();
	void onCheckUpdateFinished(int currentVersion, int lastestVersion, String url, String date, String note);
}
