package com.videogo.ui.message;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.videogo.RootActivity;
import com.videogo.alarm.AlarmLogInfoEx;
import com.videogo.alarm.AlarmLogInfoManager;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.ErrorCode;
import com.videogo.open.R;
import com.videogo.openapi.EZAlarmInfo;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.ui.remoteplayback.EZRemotePlayBackActivity;
import com.videogo.universalimageloader.core.DisplayImageOptions;
import com.videogo.universalimageloader.core.ImageLoader;
import com.videogo.universalimageloader.core.assist.FailReason;
import com.videogo.universalimageloader.core.assist.FailReason.FailType;
import com.videogo.universalimageloader.core.download.DecryptFileInfo;
import com.videogo.universalimageloader.core.listener.ImageLoadingListener;
import com.videogo.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.DevPwdUtil;
import com.videogo.util.LocalInfo;
import com.videogo.util.MD5Util;
import com.videogo.util.Utils;
import com.videogo.widget.TitleBar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class EZMessageImageActivity2 extends RootActivity implements View.OnClickListener/* mj implements GetDeviceOpSmsCodeListener */{

    private static final long HIDE_BAR_DELAY = 2000;
    private static final int MSG_HIDE_BAR = 1;
    public static final int ERROR_WEB_NO_ERROR = 100000; // /< 没有错误
    public static final int ERROR_WEB_NO_DATA = 100000 - 2; // /< 数据为空或不存在

    /** 标题栏 */
    private TitleBar mTitleBar;
    /** 标题栏菜单按钮 */
    private CompoundButton mTitleMenuButton;

    /** 标题栏菜单Layout */
    private ViewGroup mMenuLayout;
    /** 标题栏菜单——实时视频 */
    private TextView mMenuPlayView;
    /** 标题栏菜单——下载图片 */
    private TextView mMenuDownloadView;
    /** 标题栏菜单——分享图片 */
    private TextView mMenuShareView;

    /** 底部栏 */
    private ViewGroup mBottomBar;
    /** 底部栏——消息类型 */
    private TextView mMessageTypeView;
    /** 底部栏——消息时间 */
    private TextView mMessageTimeView;
    /** 底部栏——消息来源 */
    private TextView mMessageFromView;
    /** 底部栏——消息录像 */
    private Button mVideoButton;

    /** 图片 */
//    private ZoomGallery mGallery;

    private Dialog mSafePasswordDialog;

//    private ImageAdapter mAdapter;
//    private ShareDialog mShareDialog;
//    private OpenService mOpenService;
    private LocalInfo mLocalInfo;
//    private MyImageDownloader mImageDownloader;
    private ImageLoader mImageLoader;
    private Handler mHandler;
//    private MessageCtrl mMessageCtrl;

    /** 报警消息管理 */
    private AlarmLogInfoManager mAlarmLogInfoManager;
    private List<EZAlarmInfo> mAlarmList;

    private int mDataType = Constant.MESSAGE_LIST;
    private int mCurrentIndex = -1;
    private boolean mCurrentBarVisibility = true;
    private boolean mDataChanged;
    private boolean mNoMoreData;
    private long mLastLoadTime;

    private boolean mIsDecrypt = false;
    private ImageView mAlarmImageView;
    private EZAlarmInfo mAlarmInfo;
    private EZCameraInfo mCameraInfo = null;
    /** 异步任务管理（防止重复） */
    private GetAlarmMessageTask mSingleTask;
    private SparseArray<CheckAlarmInfoTask> mCheckTasks = new SparseArray<CheckAlarmInfoTask>();
    private String mEntryptPwd = "";
    private String mPwd = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        ((CustomApplication) getApplication()).addSingleActivity(EZMessageImageActivity2.class.getName(), this);
        // 页面统计
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ez_message_image_page);

        findViews();
        initData();
        initTitleBar();
        initViews();
        setListner();
    }

    /**
     * 控件关联
     */
    private void findViews() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);

        mMenuLayout = (ViewGroup) findViewById(R.id.menu_layout);
        mMenuPlayView = (TextView) findViewById(R.id.menu_play);
        //mMenuDownloadView = (TextView) findViewById(R.id.menu_download);
        //mMenuShareView = (TextView) findViewById(R.id.menu_share);

        mBottomBar = (ViewGroup) findViewById(R.id.bottom_bar);
        mMessageTypeView = (TextView) findViewById(R.id.message_type);
        mMessageTimeView = (TextView) findViewById(R.id.message_time);
        mMessageFromView = (TextView) findViewById(R.id.message_from);
        mVideoButton = (Button) findViewById(R.id.video_button);
        mAlarmImageView = (ImageView) findViewById(R.id.alarm_image);

//        mGallery = (ZoomGallery) findViewById(R.id.gallery);
    }

    /**
     * 初始化数据
     */
    private void initData() {
//        mMessageCtrl = MessageCtrl.getInstance();
        mAlarmLogInfoManager = AlarmLogInfoManager.getInstance();

        mDataType = getIntent().getIntExtra("com.videogo.EXTRA_FLAG", Constant.MESSAGE_LIST);
        EZAlarmInfo alarmTemp = getIntent().getParcelableExtra(IntentConsts.EXTRA_ALARM_INFO);
        mCameraInfo = getIntent().getParcelableExtra(IntentConsts.EXTRA_CAMERA_INFO);
        mAlarmInfo = getIntent().getParcelableExtra(IntentConsts.EXTRA_ALARM_INFO);
        mAlarmList = new ArrayList<EZAlarmInfo>();

        if (mDataType == Constant.MESSAGE_LIST) {
            //mAlarmList = mAlarmLogInfoManager.getAlarmLogList();
            if(alarmTemp != null)
                mAlarmList.add(alarmTemp);
        } else {
            mAlarmList = getIntent().getParcelableArrayListExtra("com.videogo.EXTRA_ALARM_LIST");
        }

        if (alarmTemp != null) {
            EZAlarmInfo alarm;
            if (TextUtils.isEmpty(alarmTemp.getAlarmId())) {
                for (int i = 0; i < mAlarmList.size(); i++) {
                    alarm = mAlarmList.get(i);
                    if (alarm.getChannelNo() == alarmTemp.getChannelNo()
                            && alarm.getAlarmType() == alarmTemp.getAlarmType()
                            && alarm.getAlarmStartTime().equals(alarmTemp.getAlarmStartTime())) {
                        mCurrentIndex = i;
                        break;
                    }
                }

            } else {
                for (int i = 0; i < mAlarmList.size(); i++) {
                    alarm = mAlarmList.get(i);
                    if (alarm.getAlarmId().equals(alarmTemp.getAlarmId())) {
                        mCurrentIndex = i;
                        break;
                    }
                }
            }
        }

        if (mCurrentIndex == -1) {
            finish();
        }

        mHandler = new MyHandler(this);
        mLocalInfo = LocalInfo.getInstance();
        mImageLoader = ImageLoader.getInstance();
//        mImageDownloader = new MyImageDownloader(this);
    }

    /**
     * 初始化标题栏
     */
    private void initTitleBar() {
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        mTitleBar.setTitle(R.string.ez_event_message_detail);
    }

    /**
     * 初始化控件
     */
    private void initViews() {
//        mAdapter = new ImageAdapter();
//        mGallery.setAdapter(mAdapter);
//        mGallery.setFlingEnable(false);
//        if (mCurrentIndex >= 0) {
//            mGallery.setSelection(mCurrentIndex);
            setupAlarmInfo(mCurrentIndex, mAlarmList.get(mCurrentIndex));
            mAlarmImageView.setOnClickListener(this);
            loadImage(mAlarmInfo, mAlarmImageView);
            
//        }
    }

    /**
     * 设置监听
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setListner() {
        OnClickListener clickListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCurrentIndex < mAlarmList.size()) {
                    EZAlarmInfo alarm = mAlarmList.get(mCurrentIndex);

                    int i = v.getId();
                    if (i == R.id.video_button) {//                            RemotePlayBackUtils.goToMessageVideoActivity(EZMessageImageActivity2.this, alarm, true);
//                            overridePendingTransition(R.anim.window_anim_slide_in_right, R.anim.window_anim_fade_out);
                        EZAlarmInfo alarmTemp = getIntent().getParcelableExtra(IntentConsts.EXTRA_ALARM_INFO);
//                            if (relAlarm != null && relAlarm.getEnumAlarmType() == AlarmType.DETECTOR_IPC_LINK)
//                                alarmInfo = relAlarm;

                        Intent intent = new Intent(EZMessageImageActivity2.this, EZRemotePlayBackActivity.class);
                        intent.putExtra(IntentConsts.EXTRA_ALARM_TIME, alarmTemp.getAlarmStartTime());
                        intent.putExtra(IntentConsts.EXTRA_ALARM_PRE_TIME, alarmTemp.getPreTime());
                        intent.putExtra(IntentConsts.EXTRA_ALARM_DELAY_TIME, alarmTemp.getDelayTime());
                        intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, mCameraInfo);
//                            intent.putExtra(IntentConsts.EXTRA_DEVICE_ID, alarmTemp.getDeviceSerial());
                        intent.putExtra(IntentConsts.EXTRA_CHANNEL_NO, alarmTemp.getChannelNo());
                        intent.putExtra(IntentConsts.EXTRA_ACTIVITY_NAME, alarmTemp.getClass().getName());
//                            intent.putExtra(IntentConsts.EXTRA_NETWORK_TIP, networkTip);
                        startActivity(intent);

                    }
                }
            }

        };

        mVideoButton.setOnClickListener(clickListener);
        mMenuPlayView.setOnClickListener(clickListener);
        //mMenuDownloadView.setOnClickListener(clickListener);
        //mMenuShareView.setOnClickListener(clickListener);
//        mGallery.setOnClickListener(clickListener);

//        mGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                mCurrentIndex = position;
//                if (mCurrentIndex < mAlarmList.size()) {
//                    HikStat.onEvent(EZMessageImageActivity2.this, HikAction.ACTION_MESSAGE_slide);
//                    AlarmLogInfoEx alarm = mAlarmList.get(mCurrentIndex);
//                    setupAlarmInfo(mCurrentIndex, alarm);
//                    if (mAdapter.isEncrypted(position)) {
//                        AlarmType alarmType = alarm.getEnumAlarmType();
//                        AlarmLogInfo relAlarm = alarm.getRelationAlarms();
//                        if (relAlarm != null && relAlarm.getEnumAlarmType() == AlarmType.DETECTOR_IPC_LINK) {
////                            showInputSafePassword(relAlarm);
//                        } else if (alarmType.hasCamera()) {
////                            showInputSafePassword(alarm);
//                        }
//                    }
//                } else
//                    setBarVisibility(false);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });

        // mImageView.setOnActionListener(new OnActionListener() {
        //
        // @Override
        // public void onDoubleClick(View v) {
        // setBarVisibility(false);
        // }
        //
        // @Override
        // public void onDrag(View v) {
        // }
        //
        // @Override
        // public void onZoom(View v) {
        // setBarVisibility(false);
        // }
        // });

        mTitleBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mBottomBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    private void setupAlarmInfo(int position, EZAlarmInfo alarm) {
        // 消息类型
        AlarmType alarmType = AlarmType.BODY_ALARM;//alarm.getEnumAlarmType();
        mMessageTypeView.setText( getString(alarmType.getTextResId()));
        // 消息来源
        mMessageFromView.setText(getText(R.string.from) + alarm.getAlarmName());
        // 消息时间
        mMessageTimeView.setText(alarm.getAlarmStartTime());

        setButtonEnable(alarm);
        setBarVisibility(mCurrentBarVisibility);

        if (alarm.getIsRead() == 0 && mCheckTasks.get(position) == null) {
            CheckAlarmInfoTask task = new CheckAlarmInfoTask(position);
            mCheckTasks.append(position, task);
            task.execute(alarm);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideBarDelay();
//        if (mIsDecrypt && mAdapter != null) {
//            mAdapter.notifyDataSetChanged();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(MSG_HIDE_BAR);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        hideBarDelay();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

            // 消息底部栏变化
            LayoutParams layoutParams = (LayoutParams) mMessageTypeView.getLayoutParams();

            layoutParams = (LayoutParams) mMessageTimeView.getLayoutParams();
            layoutParams.topMargin = 0;
            layoutParams.leftMargin = Utils.dip2px(this, 15);
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.addRule(RelativeLayout.BELOW, 0);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.message_type);

            layoutParams = (LayoutParams) mMessageFromView.getLayoutParams();
            layoutParams.topMargin = 0;
            layoutParams.leftMargin = Utils.dip2px(this, 15);
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.addRule(RelativeLayout.BELOW, 0);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.message_time);
            mMessageFromView.setSingleLine(true);
            mMessageFromView.setEllipsize(TruncateAt.END);

//            mVideoButton.setBackgroundResource(R.drawable.full_video_button_selector);
//            mVideoButton.setTextColor(getResources().getColorStateList(R.color.message_full_video_button_selector));
            layoutParams = (LayoutParams) mVideoButton.getLayoutParams();
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.height = LayoutParams.WRAP_CONTENT;

            mBottomBar.setPadding(mBottomBar.getPaddingLeft(), mBottomBar.getPaddingTop(),
                    mBottomBar.getPaddingRight(), Utils.dip2px(this, 10));

            hideBarDelay();

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            // 消息底部栏变化
            LayoutParams layoutParams = (LayoutParams) mMessageTypeView.getLayoutParams();

            layoutParams = (LayoutParams) mMessageTimeView.getLayoutParams();
            layoutParams.topMargin = Utils.dip2px(this, 3);
            layoutParams.leftMargin = 0;
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.addRule(RelativeLayout.BELOW, R.id.message_type);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);

            layoutParams = (LayoutParams) mMessageFromView.getLayoutParams();
            layoutParams.topMargin = Utils.dip2px(this, 3);
            layoutParams.leftMargin = 0;
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.addRule(RelativeLayout.BELOW, R.id.message_time);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            mMessageFromView.setSingleLine(false);
            mMessageFromView.setEllipsize(null);

            mVideoButton.setBackgroundResource(R.drawable.login_btn_selector);
//            mVideoButton.setTextColor(getResources().getColorStateList(R.color.message_video_button_selector));
            layoutParams = (LayoutParams) mVideoButton.getLayoutParams();
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.height = Utils.dip2px(this, 39);

            mBottomBar.setPadding(mBottomBar.getPaddingLeft(), mBottomBar.getPaddingTop(),
                    mBottomBar.getPaddingRight(), Utils.dip2px(this, 30));

            mHandler.removeMessages(MSG_HIDE_BAR);
        }

//        if (mShareDialog != null)
//            mShareDialog.onOrientationChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (mOpenService != null)
//            mOpenService.loadOnActivityResult(requestCode, resultCode, data);
    }

    private void setButtonEnable(EZAlarmInfo alarm) {
        AlarmType alarmType = AlarmType.BODY_ALARM;
//        AlarmLogInfo relAlarm = alarm.getRelationAlarms();

        mVideoButton.setEnabled(true);
    }

    private void setVideoButtonEnable(EZAlarmInfo alarm) {/*
        String deviceSerial = alarm.getDeviceSerial();

        DeviceInfoEx deviceInfoEx = DeviceManager.getInstance().getDeviceInfoExById(deviceSerial);
        CameraInfoEx cameraInfoEx = CameraManager.getInstance().getAddedCamera(deviceSerial);

        if (deviceInfoEx == null || cameraInfoEx == null) {
            // 设备已删除
            mMenuPlayView.setEnabled(false);
            mVideoButton.setEnabled(false);
        } else {
            boolean status = deviceInfoEx.isOnline();
            if (status) {
                mMenuPlayView.setEnabled(true);
                mVideoButton.setEnabled(true);
            } else {
                DeviceInfoEx deviceInfoBelong = DeviceManager.getInstance().getDeviceInfoExById(
                        deviceInfoEx.getBelongSerial());

                if ((VideoGoNetSDK.getInstance().getUserCloudStatus() && deviceInfoEx.getSupportCloud() == 1)
                        || (deviceInfoBelong != null && deviceInfoBelong.isOnline() && (deviceInfoBelong.getModelType() == DeviceInfoEx.TYPE_R1 || deviceInfoBelong
                                .getModelType() == DeviceInfoEx.TYPE_N1))) {
                    mVideoButton.setEnabled(true);
                    mMenuPlayView.setEnabled(false);
                } else {
                    mVideoButton.setEnabled(false);
                    mMenuPlayView.setEnabled(false);
                }
            }

//            if (!TextUtils.isEmpty(alarm.getAlarmLogId())
//                    && (deviceInfoEx.getCloudServiceStatus() == DeviceConsts.OFF
//                            || deviceInfoEx.getCloudType() != DeviceConsts.CLOUD_TYPE_YS || (alarm.getRecState() & 1) == 0)
//                    && Utils.getN1orR1(deviceInfoEx.getBelongSerial()) == null && (alarm.getRecState() & 1 << 2) == 0) {
//                mVideoButton.setEnabled(false);
//            }
        }
    */}

    private void toggleBarVisibility() {
        boolean visible = mTitleBar.getVisibility() == View.VISIBLE;
        setBarVisibility(!visible);
        mCurrentBarVisibility = !visible;
    }

    private void setBarVisibility(boolean visible) {
        mTitleBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        mBottomBar.setVisibility(visible ? View.VISIBLE : View.GONE);

        if (visible) {
            hideBarDelay();
        } else {
            mHandler.removeMessages(MSG_HIDE_BAR);
        }
    }

    private void hideBarDelay() {
        mHandler.removeMessages(MSG_HIDE_BAR);
        if (mTitleBar.getVisibility() == View.VISIBLE
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mHandler.sendEmptyMessageDelayed(MSG_HIDE_BAR, HIDE_BAR_DELAY);
        }
    }

    private Uri insertImageDatabase(String deviceId, String filePath, String thumbnailFilePath, int type) {/*
        ContentValues values = new ContentValues(7);

        values.put(ImageColumns.CAMERA_ID, deviceId);
        values.put(ImageColumns.DEVICE_ID, deviceId);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, 0, 0, 0);

        values.put(ImageColumns.OSD_TIME, calendar.getTimeInMillis());

        values.put(ImageColumns.TYPE, type);
        values.put(ImageColumns.FILE_PATH, filePath);
        values.put(ImageColumns.THUMB_PATH, thumbnailFilePath);

        String userName = mLocalInfo != null ? mLocalInfo.getRealUserName() : "";
        values.put(ImageColumns.USER, userName);

        try {
            return getContentResolver().insert(ImageColumns.CONTENT_URI, values);
        } catch (Exception ex) {
            return null;
        }
    */
        return null;
        }

    // 处理密码错误
    private void showInputSafePassword(final EZAlarmInfo alarm) {
        closeSafePasswordDialog();

        // 从布局中加载视图
        final View passwordErrorLayout = getLayoutInflater().inflate(R.layout.password_error_layout, null);
        final EditText passwordInput = (EditText) passwordErrorLayout.findViewById(R.id.new_password);
        passwordInput.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constant.PSW_MAX_LENGTH)});

        passwordErrorLayout.findViewById(R.id.message2).setVisibility(View.GONE);
        TextView message1 = (TextView) passwordErrorLayout.findViewById(R.id.message1);
        if (mIsDecrypt /*&& device != null *//*&&device.isDecryptPassword()*/) {
          message1.setText((mIsDecrypt) ? R.string.message_encrypt_inputpsw_tip_title
                    : R.string.message_encrypt_inputpsw_tip_title);
        } else {
            message1.setText(R.string.message_encrypt_inputpsw_tip_title);
            if (/*device != null && device.getSupportRemoteAuthRandcode() == 1*/true) {
                TextView decrypt = (TextView) passwordErrorLayout.findViewById(/*R.id.decrypt*/0);
//                decrypt.setText(R.string.decrypt_via_sms_verification_code);
                decrypt.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
//                        new GetDeviceOpSmsCodeTask(EZMessageImageActivity2.this, EZMessageImageActivity2.this).execute();
                    }

                });
                decrypt.setVisibility(View.VISIBLE);
            }
        }

        // 使用布局中的视图创建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(passwordErrorLayout)
                .setPositiveButton(R.string.cancel, null)
                .setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 使用新密码
                        String newPassword = passwordInput.getText().toString();
                        if (/*device != null*/true) {
                        	mPwd = newPassword;
                        	DevPwdUtil.savePwd("463222275", mPwd);
                            if (!TextUtils.isEmpty(mEntryptPwd)
                                    && mEntryptPwd.equals(
                                            MD5Util.getMD5String(MD5Util.getMD5String(newPassword)))) {
                                mPwd = newPassword;
//                                DevPwdUtil.savePwd(EZMessageImageActivity2.this, device.getDeviceID(), newPassword,
//                                        mLocalInfo.getRealUserName(), device.getSupportChangeSafePasswd());
                            }
//                            device.setCloudSafeModePasswd(newPassword);
                        }
                        
                        if (tryDecrypt(alarm)) {
//                            mAdapter.notifyDataSetChanged();
                        	loadImage(mAlarmInfo, mAlarmImageView);
                        } else {
                            new AlertDialog.Builder(EZMessageImageActivity2.this).setMessage(R.string.common_passwd_error)
                                    .setPositiveButton(R.string.cancel, null)
                                    .setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            showInputSafePassword(alarm);
                                        }
                                    }).show();
                        }
                    }
                });
        if (!(mIsDecrypt/* && device != null *//*&& device.isDecryptPassword()*/)) {
            builder.setTitle(R.string.realplay_encrypt_password_error_title);
        }
        mSafePasswordDialog = builder.create();

        mSafePasswordDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mSafePasswordDialog.show();

        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(passwordInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void closeSafePasswordDialog() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (mSafePasswordDialog != null && mSafePasswordDialog.isShowing() && !isFinishing()) {
            mSafePasswordDialog.dismiss();
        }
        mSafePasswordDialog = null;
    }

    private boolean tryDecrypt(EZAlarmInfo alarm) {
        /*try {
            DisplayImageOptions options = new DisplayImageOptions.Builder().extraForDownloader(
                    new DecryptFileInfo(alarm.getDeviceSerial(), alarm.getCheckSum())).build();
            File source = mImageLoader.getDiskCache().get(alarm.getAlarmPicUrl());
            InputStream is = mImageDownloader.getStream(Scheme.DECRYPT.wrap(source.getAbsolutePath()), options);
            if (is != null)
                return true;
        } catch (IOException e) {
        }
        return false;*/
    	return true;
    }

//    private class ImageAdapter extends BaseAdapter implements OnClickListener {
//
//        private class ViewHolder {
//            ViewGroup layout;
//            ImageView image;
//            LoadingTextView progress;
//            ImageButton unlock;
//        }
//
//        private SparseBooleanArray mEncryptedArray = new SparseBooleanArray();
//
//        @Override
//        public int getCount() {
//            if (mDataType == Constant.MESSAGE_LIST)
//                return mAlarmList.size() + (mNoMoreData ? 0 : 1);
//            else
//                return mAlarmList.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            if (position < mAlarmList.size())
//                return mAlarmList.get(position);
//            else
//                return null;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public int getViewTypeCount() {
//            return 2;
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            return getItem(position) == null ? 1 : 0;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            final ViewHolder viewHolder;
//            int viewType = getItemViewType(position);
//
//            if (convertView == null) {
//                convertView = getLayoutInflater().inflate(R.layout.message_image_item, parent, false);
//                viewHolder = new ViewHolder();
//                viewHolder.layout = (ViewGroup) convertView;
//                viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
//                viewHolder.progress = (LoadingTextView) convertView.findViewById(R.id.progress);
//                viewHolder.unlock = (ImageButton) convertView.findViewById(R.id.unlock_button);
//
//                // 防止获得缓存
//                viewHolder.image.setDrawingCacheEnabled(false);
//                viewHolder.image.setWillNotCacheDrawing(true);
//                viewHolder.unlock.setOnClickListener(this);
//
//                if (viewType == 1) {
//                    viewHolder.image.setVisibility(View.GONE);
//                    viewHolder.progress.setVisibility(View.VISIBLE);
//                } else {
//                    viewHolder.image.setVisibility(View.VISIBLE);
//                }
//
//                convertView.setTag(R.id.tag_key_zoom_imageview, viewHolder.image);
//                convertView.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewHolder) convertView.getTag();
//            }
//
//            final AlarmLogInfoEx alarmLogInfo = (AlarmLogInfoEx) getItem(position);
//            if (viewType == 1) {
//                if (mAlarmList != null && mAlarmList.size() > 0) {
//                    String lastTime = mAlarmList.get(mAlarmList.size() - 1).getAlarmStartTime();
//                    viewHolder.progress.setText(R.string.loading_more);
//                    if (mSingleTask == null)
//                        new GetAlarmMessageTask().execute(lastTime);
//                }
//            } else {
//                AlarmType alarmType = alarmLogInfo.getEnumAlarmType();
//
//                convertView.setTag(R.id.tag_key_bitmap, null);
//
//                viewHolder.unlock.setTag(R.id.tag_key_position, position);
//                viewHolder.unlock.setVisibility(View.GONE);
//                viewHolder.image.setTag(R.id.tag_key_position, position);
//
//                mEncryptedArray.append(position, false);
//
//                AlarmLogInfo relAlarm = alarmLogInfo.getRelationAlarms();
//                // 消息图片
//                if (relAlarm != null && relAlarm.getEnumAlarmType() == AlarmType.DETECTOR_IPC_LINK) {
//                    loadImage(viewHolder, relAlarm);
//                } else if (alarmType.hasCamera()) {
//                    loadImage(viewHolder, alarmLogInfo);
//                } else {
//                    mImageLoader.cancelDisplayTask(viewHolder.image);
//                    viewHolder.image.setImageResource(alarmType.getDetailDrawableResId());
//                    viewHolder.progress.setVisibility(View.GONE);
//                }
//            }
//            return convertView;
//        }
//
//        @Override
//        public void onClick(View v) {
//            int position = (Integer) v.getTag(R.id.tag_key_position);
//
//            unlock(position);
//        }
//
//        private void unlock(int position) {/*
//            AlarmLogInfoEx alarmLogInfo = (AlarmLogInfoEx) getItem(position);
//            AlarmType alarmType = alarmLogInfo.getEnumAlarmType();
//            AlarmLogInfo relAlarm = alarmLogInfo.getRelationAlarms();
//            if (relAlarm != null && relAlarm.getEnumAlarmType() == AlarmType.DETECTOR_IPC_LINK) {
//                showInputSafePassword(relAlarm);
//            } else if (alarmType.hasCamera()) {
//                showInputSafePassword(alarmLogInfo);
//            }
//        */}
//
//        private void loadImage(final ViewHolder viewHolder, AlarmLogInfo alarmLogInfo) {
//            viewHolder.image.setImageBitmap(null);
//            DisplayImageOptions options = new DisplayImageOptions.Builder()
//                    .cacheInMemory(true)
//                    .cacheOnDisk(true)
//                    .needDecrypt(alarmLogInfo.getAlarmEncryption())
//                    .showImageOnDecryptFail(R.drawable.lock_bg)
//                    .showImageOnFail(R.drawable.load_failed_bg)
//                    .extraForDownloader(new DecryptFileInfo(alarmLogInfo.getDeviceSerial(), alarmLogInfo.getCheckSum()))
//                    .build();
//
//            mImageLoader.displayImage(alarmLogInfo.getAlarmPicUrl(), viewHolder.image, options,
//                    new ImageLoadingListener() {
//                        @Override
//                        public void onLoadingStarted(String imageUri, View view) {
//                            viewHolder.progress.setText(0 + "%");
//                            viewHolder.progress.setVisibility(View.VISIBLE);
//                        }
//
//                        @Override
//                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {/*
//                            int position = (Integer) view.getTag(R.id.tag_key_position);
//                            if (!isFinishing() && failReason.getType() == FailType.DECRYPT_ERROR) {
//                                mEncryptedArray.append(position, true);
//                                viewHolder.unlock.setVisibility(View.VISIBLE);
//                                if (mIsDecrypt) {
//                                    unlock(position);
//                                    mIsDecrypt = false;
//                                }
//                            }
//                            viewHolder.progress.setVisibility(View.GONE);
//                        */}
//
//                        @Override
//                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                            // int position = (Integer) view.getTag(R.id.tag_key_position);
//
//                            viewHolder.layout.setTag(R.id.tag_key_bitmap, loadedImage);
//                            viewHolder.progress.setVisibility(View.GONE);
//                        }
//
//                        @Override
//                        public void onLoadingCancelled(String imageUri, View view) {
//                            viewHolder.progress.setVisibility(View.GONE);
//                        }
//
//                    }, new ImageLoadingProgressListener() {
//
//                        @Override
//                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
//                            viewHolder.progress.setText((current * 100 / total) + "%");
//                            viewHolder.progress.setVisibility(View.VISIBLE);
//                        }
//                    });
//        }
//
//        public boolean isEncrypted(int position) {
//            return mEncryptedArray.get(position, false);
//        }
//    }
    
	  @Override
	  public void onClick(View v) {
//		  if(mIsDecrypt)
//			  unlock(0);
	  }

	  private void unlock(int position) {
		  EZAlarmInfo alarmLogInfo = this.mAlarmInfo;
		
		  showInputSafePassword(alarmLogInfo);
	  }

	  private void loadImage(EZAlarmInfo alarmInfo, final ImageView imageView) {
		  imageView.setImageBitmap(null);
	  DisplayImageOptions options = new DisplayImageOptions.Builder()
	          .cacheInMemory(true)
	          .cacheOnDisk(true)
	          .needDecrypt(alarmInfo.getAlarmEncryption())
	          .showImageOnDecryptFail(R.drawable.lock_bg)
	          .showImageOnFail(R.drawable.load_failed_bg)
	          .extraForDownloader(new DecryptFileInfo("463222275", MD5Util.getMD5String(MD5Util.getMD5String(mPwd))))
	          .build();
	
	  mImageLoader.displayImage(alarmInfo.getAlarmPicUrl(), imageView, options,
	          new ImageLoadingListener() {
	              @Override
	              public void onLoadingStarted(String imageUri, View view) {
//	                  viewHolder.progress.setText(0 + "%");
//	                  viewHolder.progress.setVisibility(View.VISIBLE);
	              }
	
	              @Override
	              public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
	                  if (!isFinishing() && failReason.getType() == FailType.DECRYPT_ERROR) {
//	                      mEncryptedArray.append(position, true);
//	                      viewHolder.unlock.setVisibility(View.VISIBLE);
	                      if (mIsDecrypt) {
	                          unlock(0);
	                          mIsDecrypt = false;
	                      }
	                      mIsDecrypt = true;
	                  }
//	                  viewHolder.progress.setVisibility(View.GONE);
	              }
	
	              @Override
	              public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
	                  // int position = (Integer) view.getTag(R.id.tag_key_position);
	
//	                  viewHolder.layout.setTag(R.id.tag_key_bitmap, loadedImage);
//	                  viewHolder.progress.setVisibility(View.GONE);
//	            	  Drawable drawable = new Drawable();
//	            	  BitmapDrawable bd = new BitmapDrawable(loadedImage);
//	            	  FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(loadedImage.getWidth(), loadedImage.getHeight());
//	            	  imageView.setLayoutParams(lp);
	            	  imageView.setImageBitmap(loadedImage);
	              }
	
	              @Override
	              public void onLoadingCancelled(String imageUri, View view) {
//	                  viewHolder.progress.setVisibility(View.GONE);
	              }

	          }, new ImageLoadingProgressListener() {
	
	              @Override
	              public void onProgressUpdate(String imageUri, View view, int current, int total) {
//	                  viewHolder.progress.setText((current * 100 / total) + "%");
//	                  viewHolder.progress.setVisibility(View.VISIBLE);
	              }
	          });
	}

    @Override
    public void finish() {
        Intent data = new Intent();
        if (mDataType != Constant.MESSAGE_LIST) {
//            data.putParcelableArrayListExtra(IntentConsts.EXTRA_ALARM_LIST, (ArrayList<AlarmLogInfoEx>) mAlarmList);
        }
//mj        data.putExtra(IntentConsts.EXTRA_LAST_LOAD_TIME, mLastLoadTime);
        setResult(/*mDataChanged ? RESULT_OK : */RESULT_CANCELED, data);
        super.finish();
    }

    private static class MyHandler extends Handler {
        WeakReference<EZMessageImageActivity2> mActivity;

        private MyHandler(EZMessageImageActivity2 activity) {
            mActivity = new WeakReference<EZMessageImageActivity2>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity == null || mActivity.get() == null) {
                return;
            }
            final EZMessageImageActivity2 activity = mActivity.get();
            switch (msg.what) {
                case MSG_HIDE_BAR:
                    // 动画效果
//                    Animation animationTop = AnimationUtils.loadAnimation(activity, R.anim.slide_out_top);
//                    animationTop.setDuration(800);
//                    animationTop.setInterpolator(new LinearInterpolator());
//                    Animation animationBottom = AnimationUtils.loadAnimation(activity, R.anim.slide_out_bottom);
//                    animationBottom.setDuration(800);
//                    animationBottom.setInterpolator(new LinearInterpolator());
//
//                    animationTop.setAnimationListener(new AnimationListener() {
//
//                        @Override
//                        public void onAnimationStart(Animation animation) {
//                        }
//
//                        @Override
//                        public void onAnimationRepeat(Animation animation) {
//                        }
//
//                        @Override
//                        public void onAnimationEnd(Animation animation) {
//                            activity.setBarVisibility(false);
//                            activity.mCurrentBarVisibility = false;
//                        }
//                    });
//
//                    activity.mTitleBar.startAnimation(animationTop);
//                    activity.mMenuLayout.startAnimation(animationTop);
//                    activity.mBottomBar.startAnimation(animationBottom);
                    break;
            }
        }
    };

    /**
     * 获取事件消息任务
     */
    private class GetAlarmMessageTask extends AsyncTask<String, Void, List<AlarmLogInfoEx>> {

        private int mErrorCode = ERROR_WEB_NO_ERROR;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSingleTask = this;
        }

        @Override
        protected List<AlarmLogInfoEx> doInBackground(String... params) {
//            HikStat.onEvent(EZMessageImageActivity2.this, HikAction.EM_loadMore);

            if (!ConnectionDetector.isNetworkAvailable(EZMessageImageActivity2.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }

            return null;
//            try {
//                List<AlarmLogInfoEx> result = mMessageCtrl.getAlarmInfoList(params[0]);
//                try {
//                    mMessageCtrl.fetchUnreadMsgCount(EZMessageImageActivity2.this);
//                } catch (VideoGoNetSDKException e) {
//
//                }
//
//                if (result.size() == 0)
//                    mErrorCode = ErrorCode.ERROR_WEB_NO_DATA;
//
//                return result;
//
//            } catch (VideoGoNetSDKException e) {
//                mErrorCode = e.getErrorCode();
//                if (mErrorCode == ErrorCode.ERROR_WEB_NO_DATA)
//                    return new ArrayList<AlarmLogInfoEx>();
//                else
//                    return null;
//            }
        }

        @Override
        protected void onPostExecute(List<AlarmLogInfoEx> result) {
            super.onPostExecute(result);
            mSingleTask = null;

            if (result != null) {
//                mAlarmLogInfoManager.insertAlarmLog(result);
//                mAdapter.notifyDataSetChanged();

                if (mCurrentIndex < mAlarmList.size())
                    setupAlarmInfo(mCurrentIndex, mAlarmList.get(mCurrentIndex));

                mLastLoadTime = System.currentTimeMillis();
            }

            if (mErrorCode == ERROR_WEB_NO_ERROR)
                mDataChanged = true;
            else
                onError(mErrorCode);
        }

        protected void onError(int errorCode) {
            switch (errorCode) {
                case ERROR_WEB_NO_DATA:
                    if (mAlarmList.size() > 0) {
                        // 没有更多
                        mNoMoreData = true;
                        showToast(R.string.xlistview_footer_no_more);
//                        mAdapter.notifyDataSetChanged();
                    }
                    break;

//                case ErrorCode.ERROR_WEB_SESSION_ERROR:
//                    ActivityUtils.handleSessionException(EZMessageImageActivity2.this);
//                    break;
//
//                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
//                    ActivityUtils.handleHardwareError(EZMessageImageActivity2.this, null);
//                    break;

                case ErrorCode.ERROR_WEB_SERVER_EXCEPTION:
                    showError(getText(R.string.message_refresh_fail_server_exception));
                    break;

                case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                    showError(getText(R.string.message_refresh_fail_network_exception));
                    break;

                default:
                    showError(getText(R.string.get_message_fail_service_exception) + " (" + errorCode + ')');
                    break;
            }
        }

        private void showError(CharSequence text) {
            showToast(text);
        }
    }

    /**
     * 设置消息已读任务
     */
    private class CheckAlarmInfoTask extends AsyncTask<EZAlarmInfo, Void, Boolean> {

        private int mErrorCode = ERROR_WEB_NO_ERROR;
        private AlarmLogInfoEx info;
        private int position;

        public CheckAlarmInfoTask(int position) {
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(EZAlarmInfo... params) {
            if (!ConnectionDetector.isNetworkAvailable(EZMessageImageActivity2.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return false;
            }

            return true;
//            try {
//                info = params[0];
//                if (TextUtils.isEmpty(info.getAlarmLogId()))
//                    mMessageCtrl.setAlarmInfoChecked(info.getDeviceSerial(), info.getChannelNo(), info.getAlarmType(),
//                            info.getAlarmStartTime());
//                else
//                    mMessageCtrl.setAlarmInfoChecked(info.getAlarmLogId());
//                info.setCheckState(1);
//                return true;
//
//            } catch (VideoGoNetSDKException e) {
//                mErrorCode = e.getErrorCode();
//                return false;
//            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mCheckTasks.remove(position);

            if (result) {
//                mAlarmLogInfoManager.removeAlarmLogInfoFromNotifier(info);
//                mMessageCtrl.decreaseUnreadCameraMessageCount(EZMessageImageActivity2.this);
            }

            if (mErrorCode == ERROR_WEB_NO_ERROR) {
                if (mDataType != Constant.MESSAGE_LIST)
                    mDataChanged = true;
            } else
                onError(mErrorCode);
        }

        protected void onError(int errorCode) {
            switch (errorCode) {
//                case ErrorCode.ERROR_WEB_SESSION_ERROR:
//                    ActivityUtils.handleSessionException(EZMessageImageActivity2.this);
//                    break;
//
//                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
//                    ActivityUtils.handleHardwareError(EZMessageImageActivity2.this, null);
//                    break;

                default:
                    break;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.videogo.devicemgt.GetDeviceOpSmsCodeTask.GetDeviceOpSmsCodeListener#
     * onGetDeviceOpSmsCodeSuccess()
     */
//    @Override
    public void onGetDeviceOpSmsCodeSuccess() {
//        closeSafePasswordDialog();
//        mIsDecrypt = true;
//        AlarmLogInfoEx alarm = mAlarmList.get(mCurrentIndex);
//        DeviceInfoEx device = DeviceManager.getInstance().getDeviceInfoExById(alarm.getDeviceSerial());
//        if (device != null) {
//            device.setDecryptPassword(false);
//        }
//        startActivity((new Intent(this, DecryptViaSmsVerifyActivity.class).putExtra(IntentConsts.EXTRA_DEVICE_ID,
//                alarm.getDeviceSerial())));
//        overridePendingTransition(R.anim.fade_up, R.anim.alpha_fake_fade);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.videogo.devicemgt.GetDeviceOpSmsCodeTask.GetDeviceOpSmsCodeListener#onGetDeviceOpSmsCodeFail
     * (int)
     */
//    @Override
    public void onGetDeviceOpSmsCodeFail(int errorCode) {
//        showToast(R.string.register_get_verify_code_fail, errorCode);
    }
}