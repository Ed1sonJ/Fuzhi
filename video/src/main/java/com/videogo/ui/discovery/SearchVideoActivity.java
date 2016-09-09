package com.videogo.ui.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.videogo.open.R;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZCheckFavoriteSquareVideo;
import com.videogo.openapi.EZFavoriteSquareVideo;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZSquareVideo;
//import com.videogo.openapi.EzvizAPI;
import com.videogo.openapi.bean.req.SearchSquareVideoInfo;
//import com.videogo.openapi.bean.resp.FavoriteInfo;
import com.videogo.openapi.bean.resp.SquareVideoInfo;
import com.videogo.widget.TitleBar;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

public class SearchVideoActivity extends FragmentActivity implements SearchVideoFragment.IOnSearchClick, SquareVideoListFragment.OnDataProcess {
	private SearchSquareVideoInfo searchSquareVideoInfo;
	private SearchVideoFragment searchFragment;
	private SquareVideoListFragment searchResultFragment;
	private TitleBar titleBar;
	private EZOpenSDK mEZSdk = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_square_video_search);
		titleBar = (TitleBar) findViewById(R.id.title_bar);
		titleBar.setTitle(R.string.search);
//		titleBar.addRightTextButton(getResources().getString(R.string.cities), new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				FragmentManager fm = getSupportFragmentManager();
//				CityConfigFragment ccf = new CityConfigFragment();
//				ccf.show(fm, "city_config_fragment");
//			}
//		});
		searchFragment = (SearchVideoFragment) getSupportFragmentManager().findFragmentById(R.id.searchFragment);
		searchFragment.setOnSearchClick(this);
		mEZSdk = EZOpenSDK.getInstance();

	}
	
	@Override
	public void onSearch(SearchSquareVideoInfo searchSquareVideoInfo) {
		this.searchSquareVideoInfo = searchSquareVideoInfo;
		if (searchResultFragment == null) {
			searchResultFragment = new SquareVideoListFragment();
		}
		searchResultFragment.setOnDataProcess(this);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.container, searchResultFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	@Override
	public List<EZSquareVideo> loadMore(int pageStart, int pageSize, Object...objects)
			throws BaseException {
		if (searchSquareVideoInfo != null) {
			searchSquareVideoInfo.setPageSize(pageSize);
			searchSquareVideoInfo.setPageStart(pageStart);
			List<EZSquareVideo> result = mEZSdk.getSquareVideoList(searchSquareVideoInfo.getChannel(), 0, 10);
			List<String> squareIds = new ArrayList<String>();
			if (result != null) {
				StringBuilder sb = new StringBuilder();
				for (EZSquareVideo videoInfo : result) {
					squareIds.add(videoInfo.getSquareId());
				}
				List<EZCheckFavoriteSquareVideo> favoriteInfoList = mEZSdk.checkFavorite(squareIds);
				HashMap<String, Boolean> isCollected = new HashMap<String, Boolean>();
				for (EZCheckFavoriteSquareVideo favoriteInfo : favoriteInfoList) {
					isCollected.put(favoriteInfo.getSquareId(), true);
				}
				for (EZSquareVideo videoInfo :result) {
					if (isCollected.get(videoInfo.getSquareId()) != null && isCollected.get(videoInfo.getSquareId())) {
//						videoInfo.setCollected(true);
					}
				}
			}
			return result;
		} else {
			return null;
		}
	}

	@Override
	public List<EZFavoriteSquareVideo> loadMoreFavorite(int pageStart,
			int pageSize, Object... objects) throws BaseException {
		// TODO Auto-generated method stub
		return null;
	}
}
