/* 
 * @ProjectName ezviz-openapi-android-demo
 * @Copyright HangZhou Hikvision System Technology Co.,Ltd. All Right Reserved
 * 
 * @FileName DeviceDiscoverActivity.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2015-5-13
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.devicelist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.hikvision.wifi.configuration.BaseUtil;
import com.hikvision.wifi.configuration.DeviceDiscoveryListener;
import com.hikvision.wifi.configuration.DeviceInfo;
import com.hikvision.wifi.configuration.DeviceInfo.DevceState;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.open.R;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EzvizAPI;
import com.videogo.openapi.bean.resp.CameraInfo;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.SecurityEditText;
import com.videogo.widget.TitleBar;
import com.videogo.widget.WaitDialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

/**
 * 在此对类做相应的描述
 * @author chenxingyf1
 * @data 2015-5-13
 */
public class EZDevDiscActivity extends Activity implements OnClickListener {
    private static final String TAG = "EZDeviceDiscoverActivity";

    private static final long OVERTIME_CONNECT_WIFI_REGIST = 60 * 1000;

    private String wifiPassword = "";

    private String wifiSSID = "";

    private String maskIpAddress;

    private Timer overTimeTimer;

    private MulticastLock multicastLock;
    
    private Map<String, DeviceDiscoverInfo> discoverMap;
    
    private EZOpenSDK mEZOpenSDK = null;
    
    //UI
    private TitleBar mTitleBar;
    private TextView mDiscoverTv;
    private Button mRetryBtn;
    private ListView mDeviceDiscoverListView;
    private DeviceDiscoverAdapter mDeviceDiscoverAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_discover_page);
        init();
        findViews();
        mEZOpenSDK.startConfigWifi(this, wifiSSID, wifiPassword, deviceDiscoveryListener);
    }

    private void init() {
        // 唤醒，常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        wifiPassword = intent.getStringExtra(IntentConsts.EXTRA_WIFI_PASSWORD);
        wifiSSID = intent.getStringExtra(IntentConsts.EXTRA_WIFI_SSID);
        LogUtil.debugLog(TAG, "wifiPassword = " + wifiPassword + ",wifiSSID = " + wifiSSID);

        // 一键安装管理器
        maskIpAddress = BaseUtil.getMaskIpAddress(getApplicationContext());
        LogUtil.debugLog(TAG, wifiSSID + " " + wifiPassword + " " + maskIpAddress);
        
        discoverMap = new HashMap<String, DeviceDiscoverInfo>();
        
        mEZOpenSDK = EZOpenSDK.getInstance();
    }

    /**
     * 这里对方法做描述
     * 
     * @see
     * @since V1.0
     */
    private void findViews() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.auto_wifi_title_add_device);
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });
        mDiscoverTv = (TextView) findViewById(R.id.discover_tv);
        mRetryBtn = (Button) findViewById(R.id.retry_btn);
        mDeviceDiscoverListView = (ListView) findViewById(R.id.discover_list_lv);
        mDeviceDiscoverAdapter = new DeviceDiscoverAdapter(this);
        mDeviceDiscoverAdapter.setOnClickListener(this);
        mDeviceDiscoverListView.setAdapter(mDeviceDiscoverAdapter);
    }
    
    private DeviceDiscoveryListener deviceDiscoveryListener = new DeviceDiscoveryListener() {
        @Override
        public void onDeviceLost(DeviceInfo deviceInfo) {
        }

        @Override
        public void onDeviceFound(DeviceInfo deviceInfo) {
            Message msg = new Message();
            msg.what = 0;
            msg.obj = deviceInfo;
            defiveFindHandler.sendMessage(msg);
        }

		@Override
		public void onError(String arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}
    };
    
    private Handler defiveFindHandler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                DeviceInfo deviceInfo = (DeviceInfo) msg.obj;
                if (deviceInfo == null || deviceInfo.getState() == null 
                || TextUtils.isEmpty(deviceInfo.getSerialNo())) {
                    LogUtil.debugLog(TAG, "接收到无效的bonjour信息 为空");
                    return;
                }

                if (DevceState.WIFI == deviceInfo.getState() || DevceState.REPORT == deviceInfo.getState()) {                    
                    DeviceDiscoverInfo deviceDiscoverInfo = discoverMap.get(deviceInfo.getSerialNo());
                    if(deviceDiscoverInfo == null) {
                        deviceDiscoverInfo = new DeviceDiscoverInfo();
                        deviceDiscoverInfo.deviceID = deviceInfo.getSerialNo();
                        deviceDiscoverInfo.deviceName = deviceInfo.getType()+"("+deviceDiscoverInfo.deviceID+")";
                        discoverMap.put(deviceDiscoverInfo.deviceID, deviceDiscoverInfo);
                        mDeviceDiscoverAdapter.addItem(deviceDiscoverInfo);
                        if (DevceState.REPORT == deviceInfo.getState()) { 
                            new GetCamersInfoTask().execute(deviceDiscoverInfo);
                        }
                    }
                    deviceDiscoverInfo.isWifiConnected = true;
                    if(!TextUtils.isEmpty(deviceInfo.getIp())) {
                        deviceDiscoverInfo.localIP = deviceInfo.getIp();
                    }
                    mDeviceDiscoverAdapter.notifyDataSetChanged();
                } else if (DevceState.PLAT == deviceInfo.getState()) {
                    DeviceDiscoverInfo deviceDiscoverInfo = discoverMap.get(deviceInfo.getSerialNo());
                    if(deviceDiscoverInfo == null) {
                        deviceDiscoverInfo = new DeviceDiscoverInfo();
                        deviceDiscoverInfo.deviceID = deviceInfo.getSerialNo();
                        deviceDiscoverInfo.deviceName = deviceInfo.getType()+"("+deviceDiscoverInfo.deviceID+")";
                        discoverMap.put(deviceDiscoverInfo.deviceID, deviceDiscoverInfo);
                        mDeviceDiscoverAdapter.addItem(deviceDiscoverInfo);
                        new GetCamersInfoTask().execute(deviceDiscoverInfo);
                    }
                    deviceDiscoverInfo.isWifiConnected = true;
                    deviceDiscoverInfo.isPlatConnected = true;
                    if(!TextUtils.isEmpty(deviceInfo.getIp())) {
                        deviceDiscoverInfo.localIP = deviceInfo.getIp();
                    }
                    mDeviceDiscoverAdapter.notifyDataSetChanged();
                }
            }
        };
    };
    
    private void acquireWifiLock() {
        if(multicastLock != null) {
            return;
        }
        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("videogo_multicate_lock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();
    }
    
    private void releaseWifiLock() {
        if (multicastLock != null) {
            multicastLock.release();
            multicastLock = null;
        }
    }

    /**
     * 开始连接wifi状态,发送bonjour信息,配置超时信息
     * 
     * @see
     * @since V1.8.2
     */
    private void startBonjour() {
        mDiscoverTv.setText(R.string.discovering_device);
        mRetryBtn.setVisibility(View.GONE);
        
        acquireWifiLock();
        // 检测
    }

    /**
     * 停止连接wifi，注册设备
     * 
     * @see
     * @since V1.8.2
     */
    private synchronized void stopBonjourOnThread() {
        releaseWifiLock();
        // 停止配置，停止bonjour服务
        new Thread(new Runnable() {
            @Override
            public void run() {
                stopConfigAndBonjour();
            }
        }).start();
        LogUtil.debugLog(TAG, "stopBonjourOnThread");
    }

    /**
     * 这里对方法做描述
     *
     *            ture just stop Config
     * @see
     * @since V1.8.2
     */
    private synchronized void stopConfigAndBonjour() {
    }

    /**
     * 停止连接wifi，注册设备
     * 
     * @see
     * @since V1.8.2
     */
    private void stopBonjour() {
        releaseWifiLock();
        // 停止配置，停止bonjour服务
        stopConfigAndBonjour();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.retry_btn) {
            startBonjour();

        } else if (i == R.id.add_btn) {
            openAddDeviceDialog((String) v.getTag());

        } else if (i == R.id.local_realplay_btn) {
            openLocalRealPlay((DeviceDiscoverInfo) v.getTag());

        } else {
        }
    }

    private void openLocalRealPlay(DeviceDiscoverInfo deviceDiscoverInfo) {


    }
    
    /**
     * 弹出对话确认是否退出
     * 
     * @see
     * @since V1.8.2
     */
    private void showConfirmDialog() {
        new AlertDialog.Builder(this).setMessage(R.string.auto_wifi_dialog_connecting_msg)
                .setPositiveButton(R.string.update_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).setNegativeButton(R.string.wait, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    /**
     * 关闭超时
     * 
     * @see
     * @since V1.8.2
     */
    private void cancelOvertimeTimer() {
        if (overTimeTimer != null) {
            overTimeTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelOvertimeTimer();
        mEZOpenSDK.stopConfigWiFi();
        //stopBonjourOnThread();
    }
    
    /**
     * 获取设备信息任务
     */
    private class GetCamersInfoTask extends AsyncTask<DeviceDiscoverInfo, Void, CameraInfo> {  
        private DeviceDiscoverInfo deviceDiscoverInfo = null;
        private int mErrorCode = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected CameraInfo doInBackground(DeviceDiscoverInfo... params) {
            if(EZDevDiscActivity.this.isFinishing()) {
                return null;
            }
            if (!ConnectionDetector.isNetworkAvailable(EZDevDiscActivity.this)) {
                return null;
            }
            
            deviceDiscoverInfo = params[0];
            try {
                return (CameraInfo)EzvizAPI.getInstance().getCameraInfo(1, deviceDiscoverInfo.deviceID);
            } catch (BaseException e) {
                e.printStackTrace();
                mErrorCode = e.getErrorCode();
                return null;
            }
        }

        @Override
        protected void onPostExecute(CameraInfo result) {
            super.onPostExecute(result);
            if(EZDevDiscActivity.this.isFinishing()) {
                return;
            }
            
            if (result != null) { 
                //设备已被自己添加
                deviceDiscoverInfo.cameraInfo = result;
                deviceDiscoverInfo.isAdded = false;
                mDeviceDiscoverAdapter.notifyDataSetChanged();
            } else {
                onError(mErrorCode);
            }
        }
        
        protected void onError(int errorCode) {
            LogUtil.debugLog(TAG, "GetCamersInfoTask onError:" + errorCode);
            switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                    EzvizAPI.getInstance().gotoLoginPage();
                    finish();
                    break;
                case ErrorCode.ERROR_WEB_DEVICE_NOTEXIT:
                case ErrorCode.ERROR_WEB_DEVICE_NOT_EXIT:
                case ErrorCode.ERROR_WEB_DIVICE_ONLINE_NOT_ADD:
                    deviceDiscoverInfo.isWifiConnected = true;
                    deviceDiscoverInfo.isPlatConnected = true;
                    mDeviceDiscoverAdapter.notifyDataSetChanged();
                    break;                     
                case ErrorCode.ERROR_WEB_DIVICE_ADDED:
                case ErrorCode.ERROR_WEB_DIVICE_ONLINE_ADDED:
                case ErrorCode.ERROR_WEB_DIVICE_OFFLINE_ADDED:
                    //设备已被别人添加 
                    deviceDiscoverInfo.isAdded = true;
                    mDeviceDiscoverAdapter.notifyDataSetChanged();
                    break;
                case ErrorCode.ERROR_WEB_DIVICE_NOT_ONLINE:
                case ErrorCode.ERROR_WEB_DIVICE_OFFLINE:
                default:
                    break;                     
            }
        }
    }
    
    private void openAddDeviceDialog(String serialNo) {
        if(TextUtils.isEmpty(serialNo)) {
            return;
        }
        final SecurityEditText editText = new SecurityEditText(EZDevDiscActivity.this);
        editText.setAction(SecurityEditText.ACTION_ADD_DEVICE, serialNo);
        editText.setHint(R.string.input_device_verify_code);
        new  AlertDialog.Builder(EZDevDiscActivity.this)  
        .setTitle(this.getString(R.string.add_device) + serialNo)   
        .setIcon(android.R.drawable.ic_dialog_info)   
        .setView(editText)  
        .setPositiveButton(R.string.certain, new DialogInterface.OnClickListener() {
    
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AddDeviceTask().execute(editText);
            }
            
        })   
        .setNegativeButton(R.string.cancel, null)
        .show(); 
    }
    
    private class AddDeviceTask extends AsyncTask<SecurityEditText, Void, Boolean> {
        private Dialog mWaitDialog;
        private int mErrorCode = 0;
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(EZDevDiscActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Boolean doInBackground(SecurityEditText... params) {
            if (!ConnectionDetector.isNetworkAvailable(EZDevDiscActivity.this)) {
                return null;
            }
            
            SecurityEditText editText = params[0];
            try {
                return editText.submitAction();
            } catch (BaseException e) {
                e.printStackTrace();
                mErrorCode = e.getErrorCode();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();

            if (result != null && result) {
                Utils.showToast(EZDevDiscActivity.this, R.string.add_device_success); 
            } else {
                Utils.showToast(EZDevDiscActivity.this, R.string.add_device_fail, mErrorCode);
            }
        }
    }
}
