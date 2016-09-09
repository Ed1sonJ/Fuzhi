package com.videogo.ui.discovery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.videogo.universalimageloader.core.ImageLoader;
import com.videogo.universalimageloader.core.listener.PauseOnScrollListener;
import com.videogo.open.R;
import com.videogo.asynctask.AsyncTaskBase;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZCheckFavoriteSquareVideo;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZSquareChannel;
import com.videogo.openapi.EZSquareVideo;
import com.videogo.openapi.EZFavoriteSquareVideo;
import com.videogo.openapi.bean.req.GetSquareVideoInfoList;
import com.videogo.openapi.bean.resp.SquareVideoInfo;
import com.videogo.ui.realplay.EZRealPlayActivity;
import com.videogo.util.Utils;
import com.videogo.widget.PullToRefreshFooter;
import com.videogo.widget.PullToRefreshHeader;
import com.videogo.widget.pulltorefresh.IPullToRefresh.Mode;
import com.videogo.widget.pulltorefresh.IPullToRefresh.OnRefreshListener;
import com.videogo.widget.pulltorefresh.LoadingLayout;
import com.videogo.widget.pulltorefresh.PullToRefreshBase;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.LoadingLayoutCreator;
import com.videogo.widget.pulltorefresh.PullToRefreshGridView;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.Orientation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

public class SquareVideoListFragment extends Fragment {
	private PullToRefreshGridView mRefreshGridView;
	private GridView mGridView;
	private EZSquareChannel mSquareChannel;
	private SquareVideoAdapter mAdapter;
	private FavoriteSquareVideoAdapter mFavoriteAdapter;

	private TextView emptyView;
	private int pageStart = 0;
	private static final int pageSize = 20;
	private GetSquareVideoListTask getSquareVideoListTask;
	private GetFavoriteSquareVideoListTask getFavoSquareVideoListTask;
	private EZOpenSDK mEZSdk = null;
	private boolean isMyFavorite = false;
	
	public interface EXTRA_KEY {
		String COLUMN_INFO = "columnInfo";
	}

	public interface OnDataProcess {
		List<EZSquareVideo> loadMore(int pageStart, int pageSize, Object...objects) throws BaseException;
		List<EZFavoriteSquareVideo> loadMoreFavorite(int pageStart, int pageSize, Object...objects) throws BaseException;
	}
	
	private OnDataProcess onDataProcess;
	
	public void setOnDataProcess(OnDataProcess onDataProcess) {
		this.onDataProcess = onDataProcess;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			mSquareChannel = bundle.getParcelable(EXTRA_KEY.COLUMN_INFO);
			isMyFavorite = bundle.getBoolean("isMyFavorite");
		}
		mEZSdk = EZOpenSDK.getInstance();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	public static SquareVideoListFragment newInstance(EZSquareChannel channelInfo, boolean isMyFavorite) {
		SquareVideoListFragment fragment = new SquareVideoListFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(SquareVideoListFragment.EXTRA_KEY.COLUMN_INFO, channelInfo);
		bundle.putBoolean("isMyFavorite", isMyFavorite);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(isMyFavorite)
			return onCreateViewFavorite(inflater, container, savedInstanceState);

		mRefreshGridView = (PullToRefreshGridView) inflater.inflate(R.layout.fragment_square_video_list, container, false);
		mRefreshGridView.setLoadingLayoutCreator(new LoadingLayoutCreator() {
			@Override
			public LoadingLayout create(Context context, boolean headerOrFooter,
					Orientation orientation) {
				if (headerOrFooter) {
					return new PullToRefreshHeader(getActivity());
				} else {
					return new PullToRefreshFooter(getActivity(), PullToRefreshFooter.Style.EMPTY_NO_MORE);
				}
			}
		});
		mRefreshGridView.setOnRefreshListener(new OnRefreshListener<GridView>() {
			@Override
			public void onRefresh(PullToRefreshBase<GridView> refreshView,
					boolean headerOrFooter) {
					getSquareVideoListTask = new GetSquareVideoListTask(headerOrFooter);
					getSquareVideoListTask.execute();
			}
		});
		mRefreshGridView.setMode(Mode.BOTH);
		
		mGridView = mRefreshGridView.getRefreshableView();
		mAdapter = new SquareVideoAdapter(getActivity());
		mGridView.setAdapter(mAdapter);
		mGridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SquareVideoInfo videoInfo = (SquareVideoInfo) parent.getAdapter().getItem(position);
				if(videoInfo != null && !TextUtils.isEmpty(videoInfo.getPlayUrl())) {
                    Intent intent = new Intent(getActivity(), EZRealPlayActivity.class);
                    intent.putExtra(IntentConsts.EXTRA_RTSP_URL, videoInfo.getPlayUrl());
                    startActivity(intent);
				}
			}
		});
		emptyView = new TextView(getActivity());
		emptyView.setText(R.string.refresh_empty_hint);
		emptyView.setGravity(Gravity.CENTER);
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mRefreshGridView.setRefreshing();
		return mRefreshGridView;
	}

	public View onCreateViewFavorite(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRefreshGridView = (PullToRefreshGridView) inflater.inflate(R.layout.fragment_square_video_list, container, false);
		mRefreshGridView.setLoadingLayoutCreator(new LoadingLayoutCreator() {
			@Override
			public LoadingLayout create(Context context, boolean headerOrFooter,
					Orientation orientation) {
				if (headerOrFooter) {
					return new PullToRefreshHeader(getActivity());
				} else {
					return new PullToRefreshFooter(getActivity(), PullToRefreshFooter.Style.EMPTY_NO_MORE);
				}
			}
		});
		mRefreshGridView.setOnRefreshListener(new OnRefreshListener<GridView>() {
			@Override
			public void onRefresh(PullToRefreshBase<GridView> refreshView,
					boolean headerOrFooter) {
					getFavoSquareVideoListTask = new GetFavoriteSquareVideoListTask(headerOrFooter);
					getFavoSquareVideoListTask.execute();
			}
		});
		mRefreshGridView.setMode(Mode.BOTH);
		
		mGridView = mRefreshGridView.getRefreshableView();
		mFavoriteAdapter = new FavoriteSquareVideoAdapter(getActivity());
		mGridView.setAdapter(mFavoriteAdapter);
		mGridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				EZFavoriteSquareVideo videoInfo = (EZFavoriteSquareVideo) parent.getAdapter().getItem(position);
				if(videoInfo != null && !TextUtils.isEmpty(videoInfo.getSquarePlayUrl())) {
                    Intent intent = new Intent(getActivity(), EZRealPlayActivity.class);
                    intent.putExtra(IntentConsts.EXTRA_RTSP_URL, videoInfo.getSquarePlayUrl());
                    startActivity(intent);
				}
			}
		});
		emptyView = new TextView(getActivity());
		emptyView.setText(R.string.refresh_empty_hint);
		emptyView.setGravity(Gravity.CENTER);
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mRefreshGridView.setRefreshing();
		return mRefreshGridView;
	}
    @Override
    public void onDestroyView() {
        mRefreshGridView = null;
        if(mAdapter != null)
        	mAdapter.clear();
        mAdapter = null;
        if(mFavoriteAdapter != null)
        	mFavoriteAdapter.clear();
        mFavoriteAdapter = null;
        mGridView = null;
        emptyView = null;
        if(getSquareVideoListTask != null) {
        	getSquareVideoListTask.cancel(true);
        	getSquareVideoListTask = null;
        }
        super.onDestroyView();
    }

    private class GetSquareVideoListTask extends AsyncTaskBase<Void, Void, List<EZSquareVideo>> {
		private boolean mHeaderOrFooter;
		
		public GetSquareVideoListTask(boolean headerOrFooter) {
			super(getActivity());
			this.mHeaderOrFooter = headerOrFooter;
		}
		
		@Override
		protected void realOnCancelled() {
			super.realOnCancelled();
		}
		
		@Override
		protected void onError(int errorCode) {
			mRefreshGridView.onRefreshComplete();
			switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                	mEZSdk.openLoginPage();
                    break;
            default:
                String hintText = TextUtils.isEmpty(msg) ? getString(R.string.refresh_fail_hint) : msg;
                if(mAdapter.getCount() == 0) {
                    emptyView.setText(hintText + errorCode);
                	mRefreshGridView.setEmptyView(emptyView);
                } else {
                    Utils.showToast(getActivity(), hintText + errorCode);
                }
                break;
           }
		}

		@Override
		protected List<EZSquareVideo> realDoInBackground(Void... params)
				throws BaseException {
			if (onDataProcess != null) {   //需要别的加载方式。非SquareColumnInfo加载
				if (mHeaderOrFooter) {
					pageStart = 0;
				} else {
					pageStart = mAdapter.getCount() / pageSize;
				}
				if(isMyFavorite) {
					//return onDataProcess.loadMoreFavorite(pageStart, pageSize);
				}
				return onDataProcess.loadMore(pageStart, pageSize);
			}
			
			
			if (mSquareChannel == null) {
				return null;
			}
			GetSquareVideoInfoList getSquareVideoInfoList = new GetSquareVideoInfoList();
			getSquareVideoInfoList.setPageSize(pageSize);
			if (mHeaderOrFooter) {
				getSquareVideoInfoList.setPageStart(0);
			} else {
				getSquareVideoInfoList.setPageStart(mAdapter.getCount() / pageSize);
			}
			getSquareVideoInfoList.setChannel(Integer.valueOf(mSquareChannel.getChannelId()));
            if (isCancelled()) {  //如果取消了尽早退出
                return null;
            }
			List<EZSquareVideo> result = mEZSdk.getSquareVideoList(mSquareChannel.getChannelId(), 0, pageSize);
			List<String> squareIds = new ArrayList<String>();
			if (result != null) {
				for (EZSquareVideo videoInfo : result) {
					squareIds.add(videoInfo.getSquareId());
				}
                if (isCancelled()) { //如果取消了尽早退出
                    return null;
                }
				List<EZCheckFavoriteSquareVideo> favoriteInfoList = mEZSdk.checkFavorite(squareIds);
				HashMap<String, Boolean> isCollected = new HashMap<String, Boolean>();
				for (EZCheckFavoriteSquareVideo favoriteInfo : favoriteInfoList) {
					isCollected.put(favoriteInfo.getSquareId(), true);
				}
                if (isCollected != null && isCollected.size() != 0) {
                    for (EZSquareVideo videoInfo : result) {
                        String squareId = String.valueOf(videoInfo.getSquareId());
                        if (isCollected.get(squareId) != null && isCollected.get(squareId)) {
//                            videoInfo.setCollected(true);
                        }
                    }
                }
			}
			return result;
		}

		@Override
		protected void realOnPostExecute(List<EZSquareVideo> result) {
			mRefreshGridView.onRefreshComplete();
			if (result != null) {
				if (mHeaderOrFooter) {
					CharSequence dateText = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date());
					for (LoadingLayout layout : mRefreshGridView.getLoadingLayoutProxy(true, false).getLayouts()) {
						((PullToRefreshHeader) layout).setLastRefreshTime(":" + dateText);
					}
					mRefreshGridView.setFooterRefreshEnabled(true);
					mAdapter.clear();
				}
				if (result.size() < pageSize) {
					mRefreshGridView.setFooterRefreshEnabled(false);
				} else {
					mRefreshGridView.setFooterRefreshEnabled(true);
				}
				mAdapter.appendData(result);
			}
			if (mAdapter.getCount() == 0) {
				mRefreshGridView.setEmptyView(emptyView);
			}
		}
	}

    private class GetFavoriteSquareVideoListTask extends AsyncTaskBase<Void, Void, List<EZFavoriteSquareVideo>> {
		private boolean mHeaderOrFooter;
		
		public GetFavoriteSquareVideoListTask(boolean headerOrFooter) {
			super(getActivity());
			this.mHeaderOrFooter = headerOrFooter;
		}
		
		@Override
		protected void realOnCancelled() {
			super.realOnCancelled();
		}
		
		@Override
		protected void onError(int errorCode) {
			mRefreshGridView.onRefreshComplete();
			switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                	mEZSdk.openLoginPage();
                    break;
            default:
                String hintText = TextUtils.isEmpty(msg) ? getString(R.string.refresh_fail_hint) : msg;
                if(mFavoriteAdapter.getCount() == 0) {
                    emptyView.setText(hintText + errorCode);
                	mRefreshGridView.setEmptyView(emptyView);
                } else {
                    Utils.showToast(getActivity(), hintText + errorCode);
                }
                break;
           }
		}

		@Override
		protected List<EZFavoriteSquareVideo> realDoInBackground(Void... params)
				throws BaseException {
			if (onDataProcess != null) {   //需要别的加载方式。非SquareColumnInfo加载
				if (mHeaderOrFooter) {
					pageStart = 0;
				} else {
					pageStart = mFavoriteAdapter.getCount() / pageSize;
				}
				if(isMyFavorite) {
					//return onDataProcess.loadMoreFavorite(pageStart, pageSize);
				}
				return onDataProcess.loadMoreFavorite(pageStart, pageSize);
			}
			
			if (mSquareChannel == null) {
				return null;
			}
			GetSquareVideoInfoList getSquareVideoInfoList = new GetSquareVideoInfoList();
			getSquareVideoInfoList.setPageSize(pageSize);
			if (mHeaderOrFooter) {
				getSquareVideoInfoList.setPageStart(0);
			} else {
				getSquareVideoInfoList.setPageStart(mFavoriteAdapter.getCount() / pageSize);
			}
			getSquareVideoInfoList.setChannel(Integer.valueOf(mSquareChannel.getChannelId()));
            if (isCancelled()) {  //如果取消了尽早退出
                return null;
            }
			List<EZFavoriteSquareVideo> result = mEZSdk.getFavoriteSquareVideoList(0, pageSize);
			List<String> favoriteIds = new ArrayList<String>();
			if (result != null) {
				for (EZFavoriteSquareVideo videoInfo : result) {
					favoriteIds.add(videoInfo.getFavoriteId());
				}
                if (isCancelled()) { //如果取消了尽早退出
                    return null;
                }
				List<EZCheckFavoriteSquareVideo> favoriteInfoList = mEZSdk.checkFavorite(favoriteIds);
				HashMap<String, Boolean> isCollected = new HashMap<String, Boolean>();
				for (EZCheckFavoriteSquareVideo favoriteInfo : favoriteInfoList) {
					isCollected.put(favoriteInfo.getSquareId(), true);
				}
                if (isCollected != null && isCollected.size() != 0) {
                    for (EZFavoriteSquareVideo videoInfo : result) {
                        String squareId = String.valueOf(videoInfo.getSquareId());
                        if (isCollected.get(squareId) != null && isCollected.get(squareId)) {
//                            videoInfo.setCollected(true);
                        }
                    }
                }
			}
			return result;
		}

		@Override
		protected void realOnPostExecute(List<EZFavoriteSquareVideo> result) {
			mRefreshGridView.onRefreshComplete();
			if (result != null) {
				if (mHeaderOrFooter) {
					CharSequence dateText = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date());
					for (LoadingLayout layout : mRefreshGridView.getLoadingLayoutProxy(true, false).getLayouts()) {
						((PullToRefreshHeader) layout).setLastRefreshTime(":" + dateText);
					}
					mRefreshGridView.setFooterRefreshEnabled(true);
					mFavoriteAdapter.clear();
				}
				if (result.size() < pageSize) {
					mRefreshGridView.setFooterRefreshEnabled(false);
				} else {
					mRefreshGridView.setFooterRefreshEnabled(true);
				}
				mFavoriteAdapter.appendData(result);
			}
			if (mFavoriteAdapter.getCount() == 0) {
				mRefreshGridView.setEmptyView(emptyView);
			}
		}
	}
}
