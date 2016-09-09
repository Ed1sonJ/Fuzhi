/* 
 * @ProjectName VideoGoJar
 * @Copyright HangZhou Hikvision System Technology Co.,Ltd. All Right Reserved
 * 
 * @FileName CameraListActivity.java
 * @Description 这里对文件进行描述
 * 
 * @author xia xingsuo
 * @data 2015-11-5
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.cameralist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.devicemgt.EZDeviceSettingActivity;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.open.R;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.remoteplayback.list.PlayBackListActivity;
import com.videogo.remoteplayback.list.RemoteListContant;
import com.videogo.scan.main.CaptureActivity;
import com.videogo.ui.discovery.SquareColumnActivity;
import com.videogo.ui.message.EZMessageActivity2;
import com.videogo.ui.realplay.EZRealPlayActivity;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.DateTimeUtil;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.PullToRefreshFooter;
import com.videogo.widget.PullToRefreshFooter.Style;
import com.videogo.widget.PullToRefreshHeader;
import com.videogo.widget.TitleBar;
import com.videogo.widget.WaitDialog;
import com.videogo.widget.pulltorefresh.IPullToRefresh.Mode;
import com.videogo.widget.pulltorefresh.IPullToRefresh.OnRefreshListener;
import com.videogo.widget.pulltorefresh.LoadingLayout;
import com.videogo.widget.pulltorefresh.PullToRefreshBase;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.LoadingLayoutCreator;
import com.videogo.widget.pulltorefresh.PullToRefreshBase.Orientation;
import com.videogo.widget.pulltorefresh.PullToRefreshListView;

import java.util.Date;
import java.util.List;

/**
 * 摄像头列表
 * @author xiaxingsuo
 * @data 2014-7-14
 */
public class EZCameraListActivity extends Activity implements View.OnClickListener  {
    protected static final String TAG = "CameraListActivity";
    /** 删除设备 */
    private final static int SHOW_DIALOG_DEL_DEVICE = 1;
    
    //private EzvizAPI mEzvizAPI = null;
    private BroadcastReceiver mReceiver = null;
    
    private TitleBar mTitleBar = null;
    private PullToRefreshListView mListView = null;
    private View mNoMoreView;
    private EZCameraListAdapter mAdapter = null;
    
    private LinearLayout mNoCameraTipLy = null;
    private LinearLayout mGetCameraFailTipLy = null;
    private TextView mCameraFailTipTv = null;
    
    private EZOpenSDK mEZOpenSDK = null;
    private boolean bIsFromSetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cameralist_page);
        
        initData();
        initView();        
        Utils.clearAllNotification(this);
    }

    private void initView() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.cameras_txt);
		// initPopupMenu();
		mTitleBar.addRightButton(R.drawable.my_add,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(EZCameraListActivity.this, CaptureActivity.class);
						startActivity(intent);
					}
				});

		ImageView iv = new ImageView(this);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(5, 5);
		iv.setLayoutParams(lp);
		iv.setImageDrawable(getResources().getDrawable(R.drawable.user));
		iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popLogoutDialog();
			}
		});
		mTitleBar.addLeftView(iv);

        mNoMoreView = getLayoutInflater().inflate(R.layout.no_device_more_footer, null);
        
        mAdapter = new EZCameraListAdapter(this);
        mAdapter.setOnClickListener(new EZCameraListAdapter.OnClickListener() {

            @Override
            public void onPlayClick(BaseAdapter adapter, View view, int position) {
                EZCameraInfo cameraInfo = mAdapter.getItem(position);
                Intent intent = new Intent(EZCameraListActivity.this, EZRealPlayActivity.class);
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                startActivity(intent);
            }

         @Override
            public void onRemotePlayBackClick(BaseAdapter adapter, View view, int position) {
            	EZCameraInfo cameraInfo = mAdapter.getItem(position);
                Intent intent = new Intent(EZCameraListActivity.this, PlayBackListActivity.class);
                intent.putExtra(RemoteListContant.DEVICESERIAL_INTENT_KEY, cameraInfo.getDeviceSerial());
                intent.putExtra(RemoteListContant.CHANNELNO_INTENT_KEY, cameraInfo.getChannelNo());
                intent.putExtra(RemoteListContant.QUERY_DATE_INTENT_KEY, DateTimeUtil.getNow());
                intent.putExtra("com.vidego.CAMERAID", cameraInfo.getCameraId());
                intent.putExtra("com.vidego.CAMERAINFO", cameraInfo);
                intent.putExtra("com.videogo" + ".EXTRA_NETWORK_TIP", "0");
                startActivity(intent);
            }

            @Override
            public void onSetDeviceClick(BaseAdapter adapter, View view, int position) {
            	EZCameraInfo cameraInfo = mAdapter.getItem(position);
                Intent intent = new Intent(EZCameraListActivity.this, EZDeviceSettingActivity.class);
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                startActivity(intent);
                bIsFromSetting = true;
            }

            @Override
            public void onDeleteClick(BaseAdapter adapter, View view, int position) {
                showDialog(SHOW_DIALOG_DEL_DEVICE);
            }

            @Override
            public void onAlarmListClick(BaseAdapter adapter, View view, int position) {
            	EZCameraInfo cameraInfo = mAdapter.getItem(position);
                Intent intent = new Intent(EZCameraListActivity.this, EZMessageActivity2.class);
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                startActivity(intent);


//                Thread th = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            EZTransferMessageInfo info = EZOpenSDK.getInstance().getTransferMessageInfo("3c3e10b2-5d17-4a24-bd2a-645de00774f3");
//                        } catch (BaseException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//
//                th.start();


            }

            @Override
            public void onDevicePictureClick(BaseAdapter adapter, View view, int position) {}

            @Override
            public void onDeviceVideoClick(BaseAdapter adapter, View view, int position) {}

			@Override
			public void onDeviceDefenceClick(BaseAdapter adapter, View view,
					int position) {}
            
        });
        mListView = (PullToRefreshListView) findViewById(R.id.camera_listview);
        mListView.setLoadingLayoutCreator(new LoadingLayoutCreator() {

            @Override
            public LoadingLayout create(Context context, boolean headerOrFooter, Orientation orientation) {
                if (headerOrFooter)
                    return new PullToRefreshHeader(context);
                else
                    return new PullToRefreshFooter(context, Style.EMPTY_NO_MORE);
            }
        });
        mListView.setMode(Mode.BOTH);
        mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView, boolean headerOrFooter) {
                getCameraInfoList(headerOrFooter);
            }
        });                
        mListView.getRefreshableView().addFooterView(mNoMoreView);
        mListView.setAdapter(mAdapter);
        mListView.getRefreshableView().removeFooterView(mNoMoreView);
        
        mNoCameraTipLy = (LinearLayout) findViewById(R.id.no_camera_tip_ly);
        mGetCameraFailTipLy = (LinearLayout) findViewById(R.id.get_camera_fail_tip_ly);
        mCameraFailTipTv = (TextView) findViewById(R.id.get_camera_list_fail_tv);
    }
    
    private void initData() {
        mEZOpenSDK = EZOpenSDK.getInstance();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                LogUtil.debugLog(TAG, "onReceive:" + action);
                if (action.equals(Constant.ADD_DEVICE_SUCCESS_ACTION)) {
                    refreshButtonClicked();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ADD_DEVICE_SUCCESS_ACTION);
        registerReceiver(mReceiver, filter);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if( bIsFromSetting || (mAdapter != null && mAdapter.getCount() == 0)) {
            refreshButtonClicked();
            bIsFromSetting = false;
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if(mAdapter != null) {
            mAdapter.shutDownExecutorService();
            mAdapter.clearImageCache();
        }
    }
    
    /**
     * 从服务器获取最新事件消息
     */
    private void getCameraInfoList(boolean headerOrFooter) {
        if(this.isFinishing()) {
            return;
        }
        new GetCamersInfoListTask(headerOrFooter).execute();
    }
    
    /**
     * 获取事件消息任务
     */
    private class GetCamersInfoListTask extends AsyncTask<Void, Void, List<EZCameraInfo>> {
        private boolean mHeaderOrFooter;
        private int mErrorCode = 0;

        public GetCamersInfoListTask(boolean headerOrFooter) {
            mHeaderOrFooter = headerOrFooter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mListView.setFooterRefreshEnabled(true);
            mListView.getRefreshableView().removeFooterView(mNoMoreView);
        }

        @Override
        protected List<EZCameraInfo> doInBackground(Void... params) {
            if(EZCameraListActivity.this.isFinishing()) {
                return null;
            }
            if (!ConnectionDetector.isNetworkAvailable(EZCameraListActivity.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }

            try {
                List<EZCameraInfo> result = null;
                if(mHeaderOrFooter) {
                    result = mEZOpenSDK.getCameraList(0, 20);
                } else {
                    result = mEZOpenSDK.getCameraList(mAdapter.getCount()/20, 20);
                }
                
                return result;

            } catch (BaseException e) {
                mErrorCode = e.getErrorCode();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<EZCameraInfo> result) {
            super.onPostExecute(result);
            mListView.onRefreshComplete();
            if(EZCameraListActivity.this.isFinishing()) {
                return;
            }
            
            if (result != null) {               
                if (mHeaderOrFooter) {
                    CharSequence dateText = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date());
                    for (LoadingLayout layout : mListView.getLoadingLayoutProxy(true, false).getLayouts()) {
                        ((PullToRefreshHeader) layout).setLastRefreshTime(":" + dateText);
                    }
                    mAdapter.clearItem();
                }
                if(mAdapter.getCount() == 0 && result.size() == 0) {
                    mListView.setVisibility(View.GONE);
                    mNoCameraTipLy.setVisibility(View.VISIBLE);
                    mGetCameraFailTipLy.setVisibility(View.GONE);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                 } else if(result.size() < 10){
                    mListView.setFooterRefreshEnabled(false);
                    mListView.getRefreshableView().addFooterView(mNoMoreView);
                } else if(mHeaderOrFooter) {
                    mListView.setFooterRefreshEnabled(true);
                    mListView.getRefreshableView().removeFooterView(mNoMoreView);
                }
                addCameraList(result);
                mAdapter.notifyDataSetChanged();
            }

            if (mErrorCode != 0) {
                onError(mErrorCode);
            }
        }

        protected void onError(int errorCode) {
            switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                    mEZOpenSDK.openLoginPage();
                    break;
                default:
                    if(mAdapter.getCount() == 0) {
                        mListView.setVisibility(View.GONE);
                        mNoCameraTipLy.setVisibility(View.GONE);
                        mCameraFailTipTv.setText(Utils.getErrorTip(EZCameraListActivity.this, R.string.get_camera_list_fail,  errorCode));
                        mGetCameraFailTipLy.setVisibility(View.VISIBLE);
                    } else {
                        Utils.showToast(EZCameraListActivity.this, R.string.get_camera_list_fail,  errorCode);
                    }
                    break;
            }
        }
    }
            
    private void addCameraList(List<EZCameraInfo> result) {
        int count = result.size();
        EZCameraInfo item = null;
        for (int i = 0; i < count; i++) {
           item = result.get(i);
           mAdapter.addItem(item);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.camera_list_refresh_btn || i == R.id.no_camera_tip_ly) {
            refreshButtonClicked();

        } else if (i == R.id.camera_list_gc_ly) {
            Intent intent = new Intent(EZCameraListActivity.this, SquareColumnActivity.class);
            startActivity(intent);

        } else {
        }
    }
    
    /**
     * 刷新点击
     */
    private void refreshButtonClicked() {
        mListView.setVisibility(View.VISIBLE);
        mNoCameraTipLy.setVisibility(View.GONE);
        mGetCameraFailTipLy.setVisibility(View.GONE);
        mListView.setMode(Mode.BOTH);
        mListView.setRefreshing();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case SHOW_DIALOG_DEL_DEVICE:
                break;
        }
        return dialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.update_exit).setIcon(R.drawable.exit_selector);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (dialog != null) {
            removeDialog(id);
            TextView tv = (TextView) dialog.findViewById(android.R.id.message);
            tv.setGravity(Gravity.CENTER);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {// 得到被点击的item的itemId
            case 1:// 对应的ID就是在add方法中所设定的Id
                popLogoutDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 弹出登出对话框
     * 
     * @see
     * @since V1.0
     */
    private void popLogoutDialog() {
        Builder exitDialog = new AlertDialog.Builder(EZCameraListActivity.this);
        exitDialog.setTitle(R.string.exit);
        exitDialog.setMessage(R.string.exit_tip);
        exitDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new LogoutTask().execute();
            }
        });
        exitDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        exitDialog.show();
    }
    
    private class LogoutTask extends AsyncTask<Void, Void, Void> {
        private Dialog mWaitDialog;
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(EZCameraListActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mEZOpenSDK.logout();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();
            mEZOpenSDK.openLoginPage();
            finish();
        }
    }
            
}
