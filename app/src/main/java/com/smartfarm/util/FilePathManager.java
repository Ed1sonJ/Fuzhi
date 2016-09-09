package com.smartfarm.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class FilePathManager {
	private static final String APP_FOLDER_NAME = "SmartFarm";
	private static FilePathManager instance;
	private File appFolderFile;
	private Context ctx;

	public static synchronized FilePathManager getInstance(Context paramContext) {
		if (instance == null)
			instance = new FilePathManager(paramContext);
		return instance;
	}
	//创建路径，如:/mnt/sdcard/mp3/
	private FilePathManager(Context context) {
		ctx = context;
		appFolderFile = new File(Environment.getExternalStorageDirectory(),APP_FOLDER_NAME);
		if (!appFolderFile.getParentFile().exists()) {
			appFolderFile.getParentFile().mkdirs();//如果文件不存在就创建文件
		}
	}

	private void checkStorageStateOrDie() throws Exception {
		if (!"mounted".equals(Environment.getExternalStorageState())) {
			throw new Exception("Error found no sdcard.");
		}
		if (!this.appFolderFile.exists()) {
			if (!this.appFolderFile.mkdir()) {
				throw new Exception("Error can't make app folder.");
			}
		}
	}
	//创建文件，如/mnt/sdcard/mp3/hear.mp3
	public File getFile(String filename) throws Exception {
		//checkStorageStateOrDie();
		return new File(appFolderFile, filename);
	}
}
