package com.videogo.asynctask;

import android.content.Context;

import com.videogo.exception.BaseException;
import com.videogo.openapi.EZFavoriteSquareVideo;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZSquareVideo;
import com.videogo.openapi.EzvizAPI;
import com.videogo.openapi.bean.resp.SquareVideoInfo;

/**
 * 
 * 收藏及取消收藏
 */
public class CollectTask extends AsyncTaskBase<Void, Void, Boolean>{
	private Context context;
	private OnDataNotifyListener onDataNotifyListener;
	private EZFavoriteSquareVideo videoInfoFavorite;
	private EZSquareVideo videoInfo;
	private EZOpenSDK mEZSdk = null;
	
	public interface OnDataNotifyListener {
		public void onDataNotify(boolean result);
	}
	
	public CollectTask(Context context, EZSquareVideo squareVideoInfo, EZFavoriteSquareVideo favoriteVideo, OnDataNotifyListener onDataNotifyListener) {
		super(context);
		this.context = context;
		this.onDataNotifyListener = onDataNotifyListener;
		this.videoInfo = squareVideoInfo;
		mEZSdk = EZOpenSDK.getInstance();
	}
	
	boolean isCollected = false;
	@Override
	protected Boolean realDoInBackground(Void... params) throws BaseException {
		boolean result = false;
		
		if (isCollected) {
			isCollected = !isCollected;
			//result = mEZSdk.cancelFavorite(videoInfo.getFavoriteId());
		} else {
			mEZSdk.saveFavorite(videoInfo.getSquareId());
		}
		return result;
	}

	@Override
	protected void realOnPostExecute(Boolean result) {
		onDataNotifyListener.onDataNotify(result);
	}
	
	@Override
	protected void onError(int mErrorCode) {
		onDataNotifyListener.onDataNotify(false);
	}
}
