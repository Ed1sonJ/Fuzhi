package com.videogo.ui.discovery;

import java.util.List;

import com.videogo.open.R;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZFavoriteSquareVideo;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZSquareVideo;
import com.videogo.widget.TitleBar;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class MyCollectActivity extends FragmentActivity implements SquareVideoListFragment.OnDataProcess {
    private TitleBar titleBar;
    private EZOpenSDK mEZSdk = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_collect);
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setTitle(R.string.my_collect);
		if (savedInstanceState != null) {
			return;
		}
		SquareVideoListFragment fragment = SquareVideoListFragment.newInstance(null, true);
		fragment.setOnDataProcess(this);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.container, fragment);
		transaction.commit();
		mEZSdk = EZOpenSDK.getInstance();
	}

	@Override
	public List<EZSquareVideo> loadMore(int pageStart, int pageSize,
			Object... objects) throws BaseException {
//		List<SquareVideoInfo> result = EzvizAPI.getInstance().getSquareVideoFavorite(pageStart, pageSize);
//		if (result != null) {
//			for (SquareVideoInfo videoInfo : result) {
//				videoInfo.setCollected(true);
//			}
//		}
//		return result;
		return null;
	}

	@Override
	public List<EZFavoriteSquareVideo> loadMoreFavorite(int pageStart,
			int pageSize, Object... objects) throws BaseException {
		List<EZFavoriteSquareVideo> result = mEZSdk.getFavoriteSquareVideoList(pageStart, pageSize);
		if (result != null) {
			for (EZFavoriteSquareVideo videoInfo : result) {
//				videoInfo.setCollected(true);
			}
		}
		return result;

	}
}
