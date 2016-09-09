package com.videogo.devicemgt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.videogo.RootActivity;
import com.videogo.camera.CameraInfoEx;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.device.DeviceInfoEx;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.HCNetSDKException;
import com.videogo.open.R;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceVersion;
import com.videogo.ui.cameralist.EZCameraListActivity;
import com.videogo.ui.util.ActivityUtils;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.widget.TitleBar;
import com.videogo.widget.WaitDialog;

public class EZDeviceSettingActivity extends RootActivity {
    private final String TAG = "EZDeviceSettingActivity";
    private static final int REQUEST_CODE_BAIDU = 1;

    /**
     * 设置安全模式
     */
    private static final int SHOW_DIALOG_SAFE_MODE = 0;
    /**
     * 关闭安全模式
     */
    private static final int SHOW_DIALOG_CLOSE_SAFE_MODE = 1;
    /**
     * 删除设备
     */
    private final static int SHOW_DIALOG_DEL_DEVICE = 3;
    /**
     * 设备下线上报
     */
    private final static int SHOW_DIALOG_OFFLINE_NOTIFY = 4;
    private final static int SHOW_DIALOG_WEB_SETTINGS_ENCRYPT = 6;
    private final static int SHOW_DIALOG_WEB_SETTINGS_DEFENCE = 7;
    /**
     * 标题栏
     */
    private TitleBar mTitleBar;
    /**
     * 设备基本信息
     */
    private ViewGroup mDeviceInfoLayout;
    /**
     * 设备名称
     */
    private TextView mDeviceNameView;
    /**
     * 设备类型+序列号
     */
    private TextView mDeviceTypeSnView;

    /**
     * 设备序列号
     */
    private ViewGroup mDeviceSNLayout;
    
    /**
     * 防护
     */
    private ViewGroup mDefenceLayout;
    /**
     * 防护
     */
    private TextView mDefenceView;
    /**
     * 防护状态
     */
    private TextView mDefenceStateView;

    /**
     * 防护计划父框架
     */
    private ViewGroup mDefencePlanParentLayout;
    /**
     * 防护计划箭头
     */
    private View mDefencePlanArrowView;

    /**
     * 存储状态
     */
    private ViewGroup mStorageLayout;
    /**
     * 存储提示
     */
    private View mStorageNoticeView;
    /**
     * 设备版本
     */
    private ViewGroup mVersionLayout;
    /**
     * 设备版本状态
     */
    private TextView mVersionView;
    /**
     * 设备版本最新
     */
    private View mVersionNewestView;
    /**
     * 版本提示
     */
    private View mVersionNoticeView;
    /**
     * 版本箭头
     */
    private View mVersionArrowView;

    /**
     * 视频图片加密父框架
     */
    private ViewGroup mEncryptParentLayout;
    /**
     * 视频图片加密切换按钮
     */
    private Button mEncryptButton;
    /**
     * 修改密码
     */
    private ViewGroup mModifyPasswordLayout;

    /* 设备删除 */
    private View mDeviceDeleteView;

    /**
     * 设备信息
     */
    private DeviceInfoEx mDevice;
    /**
     * 设备摄像头信息（如果是摄像头）
     */
    private CameraInfoEx mCamera;
    /**
     * 全局按钮监听
     */
    private OnClickListener mOnClickListener;

    private TextView mCurrentVersionTextView;
    private Button mDefenceToggleButton;
    private TextView mDeviceSerialTextView;
    private String mValidateCode;

    private EZOpenSDK mEZOpenSDK = null;
    private EZCameraInfo mEZCameraInfo = null;
    private EZDeviceVersion mDeviceVersion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 页面统计
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_setting_page);

        findViews();
        initData();
        initTitleBar();
        initViews();
    }

    /**
     * 控件关联
     */
    private void findViews() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);

        mDeviceInfoLayout = (ViewGroup) findViewById(R.id.device_info_layout);
        mDeviceNameView = (TextView) findViewById(R.id.device_name);
        mDeviceTypeSnView = (TextView) findViewById(R.id.device_type_sn);

        mDeviceSNLayout = (ViewGroup) findViewById(R.id.ez_device_serial_layout);

        mDefenceLayout = (ViewGroup) findViewById(R.id.defence_layout);
        mDefenceView = (TextView) findViewById(R.id.defence);
        mDefenceStateView = (TextView) findViewById(R.id.defence_state);

        mDefencePlanParentLayout = (ViewGroup) findViewById(R.id.defence_plan_parent_layout);
        mDefencePlanArrowView = findViewById(R.id.defence_plan_arrow);
        mDefenceToggleButton = (Button) findViewById(R.id.defence_toggle_button);


        mStorageLayout = (ViewGroup) findViewById(R.id.storage_layout);
        mStorageNoticeView = findViewById(R.id.storage_notice);
        mVersionLayout = (ViewGroup) findViewById(R.id.version_layout);
        mVersionView = (TextView) findViewById(R.id.version);
        mVersionNewestView = findViewById(R.id.version_newest);
        mVersionNoticeView = findViewById(R.id.version_notice);
        mVersionArrowView = findViewById(R.id.version_arrow);
        mCurrentVersionTextView = (TextView) findViewById(R.id.current_version);

        mEncryptParentLayout = (ViewGroup) findViewById(R.id.encrypt_parent_layout);
        mEncryptButton = (Button) findViewById(R.id.encrypt_button);
        mModifyPasswordLayout = (ViewGroup) findViewById(R.id.modify_password_layout);

        mDeviceDeleteView = findViewById(R.id.device_delete);
        mDeviceSerialTextView = (TextView) findViewById(R.id.ez_device_serial);
    }

    /**
     * 初始化数据
     */
    private void initData() {

        mEZOpenSDK = EZOpenSDK.getInstance();
        mEZCameraInfo = getIntent().getParcelableExtra(IntentConsts.EXTRA_CAMERA_INFO);

        if(mEZCameraInfo == null) {
        	showToast(R.string.device_have_not_added);
        	finish();
        }
    }

    /**
     * 初始化标题栏
     */
    private void initTitleBar() {
        mTitleBar.setTitle(R.string.ez_setting);
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        if (mEZCameraInfo != null) {
            mOnClickListener = new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent;
                    int i = v.getId();
                    if (i == R.id.device_info_layout) {
                        intent = new Intent(EZDeviceSettingActivity.this, ModifyDeviceNameActivity.class);
                        intent.putExtra("DEVICE_SERIAL", mEZCameraInfo.getDeviceSerial());
                        intent.putExtra("EZCAMERA_INFO", mEZCameraInfo);
                        startActivityForResult(intent, 0);

                    } else if (i == R.id.ez_device_serial_layout) {
                        try {
                            mEZOpenSDK.openCloudPage(mEZCameraInfo.getDeviceSerial());
                        } catch (BaseException e) {
                            e.printStackTrace();
                        }


                    } else if (i == R.id.defence_layout || i == R.id.defence_toggle_button) {
                        new SetDefenceTask().execute(!(mEZCameraInfo.getDefence() != 0));

                    } else if (i == R.id.defence_plan_button) {
                        setDefencePlanNew(false);

                    } else if (i == R.id.defence_plan_status_retry) {
                        setDefencePlanNew(false);

                    } else if (i == R.id.defence_plan_set_layout) {
                        if (mDefencePlanArrowView.getVisibility() == View.VISIBLE) {
                        }
                        setDefencePlanNew(false);

                    } else if (i == R.id.defence_plan_retry) {
                        setDefencePlanNew(false);

                    } else if (i == R.id.storage_layout) {
                    } else if (i == R.id.version_layout) {
                        intent = new Intent(EZDeviceSettingActivity.this, EZUpgradeDeviceActivity.class);
                        intent.putExtra("deviceSerial", mEZCameraInfo.getDeviceSerial());
                        startActivity(intent);

                    } else if (i == R.id.encrypt_button) {
                        gotoSetSafeMode();

                    } else if (i == R.id.modify_password_layout) {
                        gotoModifyPassword();

                    } else if (i == R.id.device_delete) {
                        showDialog(SHOW_DIALOG_DEL_DEVICE);

                    } else {
                    }
                }
            };

            new GetDeviceInfoTask().execute();

            // 防护计划设置
            setupSafeModePlan(true);

            mDeviceDeleteView.setOnClickListener(mOnClickListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mCloudStateHelper.onResume();
        setupDeviceInfo();
        setupParentLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mCloudStateHelper.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_BAIDU)
                setupBaiduDeviceInfo(true, true);
        }
    }

    private void setupDeviceInfo() {
        if (mEZCameraInfo != null) {
            // 设备图片部分
            // 设备名字部分
//            String typeSn = String.format("%s(%s)",
//                    TextUtils.isEmpty(mDeviceModel.getDisplay()) ? mDevice.getFullModel() : mDeviceModel.getDisplay(),
//                    mDevice.getDeviceID());
            String typeSn = mEZCameraInfo.getCameraName();
            mDeviceSerialTextView.setText(mEZCameraInfo.getDeviceSerial());

            mDeviceNameView.setText(mEZCameraInfo.getCameraName());
            if (typeSn.equals(mEZCameraInfo.getCameraName())) {
                mDeviceTypeSnView.setVisibility(View.GONE);
            } else {
                mDeviceTypeSnView.setText(typeSn);
                mDeviceTypeSnView.setVisibility(View.VISIBLE);
            }
            mDeviceInfoLayout.setOnClickListener(mOnClickListener);
            mDeviceSNLayout.setOnClickListener(mOnClickListener);

            mDefencePlanParentLayout.setVisibility(View.GONE);

            boolean bSupportDefence = true;
            if(bSupportDefence) {
            	mDefenceView.setText(R.string.detail_defend_c1_c2_f1);
                mDefenceStateView.setTextColor(getResources().getColorStateList(R.color.on_off_text_selector));
//                mDefenceStateView.setText(mDevice.isDefenceOn() ? R.string.on : R.string.off);
//                mDefenceStateView.setEnabled(mEZCameraInfo.getDefence() == 1);
                boolean isDefenceEnable = (mEZCameraInfo.getDefence() != 0);
                mDefenceToggleButton.setBackgroundResource(isDefenceEnable ? R.drawable.autologin_on
                        : R.drawable.autologin_off);
                mDefenceToggleButton.setOnClickListener(mOnClickListener);
            
				mDefenceLayout.setVisibility(View.VISIBLE);
//				mDefenceLayout.setTag(supportMode);
//				mDefenceLayout.setOnClickListener(mOnClickListener); // dont allow to click the list
           }

            // 存储状态部分

            {

                mStorageNoticeView.setVisibility(View.VISIBLE);

                // TODO
                mStorageLayout.setVisibility(View.VISIBLE);
                mStorageLayout.setOnClickListener(mOnClickListener);
            }

            // 版本部分
            if (mEZCameraInfo.getOnlineStatus() == 1 && mDeviceVersion != null) {
                boolean bHasUpgrade = (mDeviceVersion.getIsNeedUpgrade() != 0);
                mCurrentVersionTextView.setText(mDeviceVersion.getCurrentVersion());
                mVersionView.setText(mDeviceVersion.getNewestVersion());
                if (bHasUpgrade){
                    mVersionNewestView.setVisibility(View.VISIBLE);
                } else {
                    mVersionNewestView.setVisibility(View.GONE);
                }

//                bHasUpgrade = true;// TODO stub
                if(bHasUpgrade) {
                    mVersionNoticeView.setVisibility(View.VISIBLE);
                    mVersionArrowView.setVisibility(View.VISIBLE);
                    mVersionLayout.setOnClickListener(mOnClickListener);
                } else {
                    mVersionNoticeView.setVisibility(View.GONE);
                    mVersionArrowView.setVisibility(View.GONE);
                    mVersionLayout.setOnClickListener(null);
                }
                mVersionLayout.setVisibility(View.VISIBLE);
            } else {
                mVersionLayout.setVisibility(View.GONE);
            }

            // 视频图片加密部分
            boolean bSupportEncrypt = true;
            //if (mDevice.getSupportEncrypt() == DeviceConsts.NOT_SUPPORT || !mDevice.isOnline()) {
            if(!bSupportEncrypt) {
                mEncryptParentLayout.setVisibility(View.GONE);
            } else {
                mEncryptButton
                        .setBackgroundResource((mEZCameraInfo.getEncryptStatus() == 1) ? R.drawable.autologin_on
                                : R.drawable.autologin_off);
                mEncryptButton.setOnClickListener(mOnClickListener);

                mModifyPasswordLayout.setOnClickListener(mOnClickListener);
                boolean bSupportChangePwd = false;
//                if ((mEZCameraInfo.getEncryptStatus() != 1))
                       // || mDevice.getSupportChangeSafePasswd() == DeviceConsts.NOT_SUPPORT) {
                if(!bSupportChangePwd) {
                    mModifyPasswordLayout.setVisibility(View.GONE);
                } else {
                    mModifyPasswordLayout.setVisibility(View.VISIBLE);
                }

                mEncryptParentLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupParentLayout() {
        // 在线父框架
//        mOnlineParentLayout.setVisibility(mOnlineTimeLayout.getVisibility() & mOfflineNotifyLayout.getVisibility());
    }

    private void setupSafeModePlan(boolean fromServer) {}

    private void setDefencePlanNew(boolean visible) {
    }

    private void setupBaiduDeviceInfo(boolean fromServer, boolean reload) {}

    /**
     * 修改加密密码
     */
    private void gotoModifyPassword() {
    }

    private void gotoSetSafeMode() {
        if (mEZCameraInfo.getEncryptStatus() == 1) {
            // 如果开启，则关闭
            if (!isFinishing()) {
                showDialog(SHOW_DIALOG_CLOSE_SAFE_MODE);
            }
        } else {
            // 如果关闭，则开启
//            showDialog(SHOW_DIALOG_SAFE_MODE);
            openSafeMode();
        }
    }

    /**
     * 　　开启安全状态
     */
    private void openSafeMode() {
        // 首先判断有没有密码，如果有的话，就不需要设置了
//        if (TextUtils.isEmpty(mDevice.getEncryptPwd()) || mDevice.getEncryptPwd().equals("null")) {
//            Intent intent = new Intent(DeviceSettingActivity.this, DeviceEncryptPasswordActivity.class);
//            intent.putExtra("deviceID", mDevice.getDeviceID());
//            startActivity(intent);
//        } else {
//            new OpenEncryptTask().execute();
//        }
            new OpenEncryptTask().execute(true);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case SHOW_DIALOG_SAFE_MODE: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this).setPositiveButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setNegativeButton(R.string.certain, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openSafeMode();
                    }
                });

                {
                    builder.setMessage(getString(R.string.detail_safe_btn_tip));
                }

                dialog = builder.create();
            }
                break;
            case SHOW_DIALOG_CLOSE_SAFE_MODE: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this).setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setPositiveButton(R.string.certain, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        openVideoEncryptDialog();
                    }
                });

                builder.setMessage(getString(R.string.detail_safe_close_btn_tip));

                dialog = builder.create();
            	}
                break;

            case SHOW_DIALOG_DEL_DEVICE:
                dialog = new AlertDialog.Builder(this).setMessage(getString(R.string.detail_del_device_btn_tip))
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setPositiveButton(R.string.certain, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteDeviceTask().execute();
                            }
                        }).create();
                break;

            case SHOW_DIALOG_OFFLINE_NOTIFY:
                break;
            case SHOW_DIALOG_WEB_SETTINGS_ENCRYPT:
            {
                dialog = new AlertDialog.Builder(this).setMessage("该功能暂时只支持页面操作哦")
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setPositiveButton(R.string.certain, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mEZOpenSDK.openSettingDevicePage(mEZCameraInfo.getDeviceId());
                            }
                        }).create();
            }
            break;
            case SHOW_DIALOG_WEB_SETTINGS_DEFENCE:
            {
                dialog = new AlertDialog.Builder(this).setMessage("该功能暂时只支持页面操作哦")
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setPositiveButton(R.string.certain, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mEZOpenSDK.openSettingDevicePage(mEZCameraInfo.getDeviceId());
                            }
                        }).create();
            }
            break;
            default:
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (dialog != null) {
            removeDialog(id);
//            TextView tv = (TextView) dialog.findViewById(android.R.id.message);
//            tv.setGravity(Gravity.CENTER);
        }
    }

    private void openVideoEncryptDialog() {
        LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup smsVerifyView = (ViewGroup) mLayoutInflater.inflate(R.layout.device_video_encrypt_dialog, null, true);
        final EditText etSmsCode = (EditText) smsVerifyView.findViewById(R.id.ez_sms_code_et);

        new  AlertDialog.Builder(EZDeviceSettingActivity.this)  
        .setTitle(R.string.input_device_verify_code)
        .setIcon(android.R.drawable.ic_dialog_info)   
        .setView(smsVerifyView)
        .setPositiveButton(R.string.ez_dialog_btn_disable_video_encrypt, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	String sms = null;
            	if(etSmsCode != null) {
            		sms = etSmsCode.getEditableText().toString();
            	}
            	mValidateCode = sms;
            	if(!TextUtils.isEmpty(mValidateCode)) {
            		new OpenEncryptTask().execute(false); //disable video encryption
            	}
            }
            
        })   
        .setNegativeButton(R.string.cancel, null)
        .show();  
    }
    /**
     * 登陆设备因为密码错误的处理
     */
    private void showInputDevicePswDlg() {
        // 从布局中加载视图
        LayoutInflater factory = LayoutInflater.from(this);
        final View passwordErrorLayout = factory.inflate(R.layout.password_error_layout, null);
        final EditText newPassword = (EditText) passwordErrorLayout.findViewById(R.id.new_password);
        newPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constant.PSW_MAX_LENGTH)});

        final TextView message1 = (TextView) passwordErrorLayout.findViewById(R.id.message1);
        message1.setText(getString(R.string.realplay_password_error_message1));

        message1.setText(getString(R.string.realplay_verifycode_error_message0));

        // 使用布局中的视图创建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.camera_detail_verifycode_error_title);
        builder.setView(passwordErrorLayout);
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });

        builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 确定修改名称
                String newPwd = newPassword.getText().toString();
                savePswToLocal(newPwd);
                new DeviceLoginTask().execute();
            }
        });
        if (!isFinishing()) {
            Dialog dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.show();
        }
    }

    private void savePswToLocal(String password) {
        mDevice.setPassword(password);
        LocalInfo localInfo = LocalInfo.getInstance();
        if (localInfo != null) {
//            DevPwdUtil.savePwd(this, mDevice.getDeviceID(), password, localInfo.getRealUserName(),
//                    mDevice.getSupportChangeSafePasswd());
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(IntentConsts.EXTRA_DEVICE_INFO, mDevice);
        setResult(RESULT_OK, data);
        super.finish();
    }

    /**
     * 获取设备信息
     */
    private class GetDeviceInfoTask extends AsyncTask<Void, Void, Boolean> {

        private int mErrorCode = 0;

        @Override
        protected Boolean doInBackground(Void... params) {
        	try {
        		mDeviceVersion = mEZOpenSDK.getDeviceVersion(mEZCameraInfo.getDeviceSerial());
        		return true;
        	} catch (BaseException e) {
        		mErrorCode = e.getErrorCode();
        	}
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                setupDeviceInfo();
                setupParentLayout();
            } else {
                switch (mErrorCode) {
                    case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                        break;
                    case ErrorCode.ERROR_WEB_SESSION_ERROR:
                        ActivityUtils.handleSessionException(EZDeviceSettingActivity.this);
                        break;
                    case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                        ActivityUtils.handleHardwareError(EZDeviceSettingActivity.this, null);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 设备登录任务
     */
    private class DeviceLoginTask extends AsyncTask<Void, Void, Boolean> {

        private Dialog mWaitDialog;

        private int mErrorCode = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(EZDeviceSettingActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();

            if (result) {
//                Intent intent = new Intent(DeviceSettingActivity.this, DeviceWifiListActivity.class);
//                intent.putExtra("deviceId", mDevice.getDeviceID());
//                startActivity(intent);
            } else if (mErrorCode == HCNetSDKException.NET_DVR_PASSWORD_ERROR) {
                showInputDevicePswDlg();
            } else {
                showToast(R.string.device_wifi_set_no_in_subnet);
            }
        }
    }

    private class SetDefenceTask extends AsyncTask<Boolean, Void, Boolean> {
        private Dialog mWaitDialog;
        private int mErrorCode = 0;
        boolean bSetDefence;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mWaitDialog = new WaitDialog(EZDeviceSettingActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
		}

		@Override
		protected Boolean doInBackground(Boolean... params) {
			bSetDefence = (Boolean) params[0];
			Boolean result = false;
			try {
				result = mEZOpenSDK.setDefence(bSetDefence, mEZCameraInfo.getDeviceSerial());
			} catch (BaseException e) {
				mErrorCode = e.getErrorCode();
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mWaitDialog.dismiss();
			if(result) {
				mEZCameraInfo.setDefence(bSetDefence ? 1 : 0);
				setupDeviceInfo();
			} else {
				switch (mErrorCode) {
                case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                    showToast(R.string.encrypt_password_open_fail_networkexception);
                    break;
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                    ActivityUtils.handleSessionException(EZDeviceSettingActivity.this);
                    break;
                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                    ActivityUtils.handleHardwareError(EZDeviceSettingActivity.this, null);
                    break;
                default:
                    showToast(R.string.encrypt_password_open_fail, mErrorCode);
                    break;
				}
			}
		}

    }
    /**
     * 开启设备视频图片加密任务
     */
    private class OpenEncryptTask extends AsyncTask<Boolean, Void, Boolean> {
        private boolean bAction;
        private Dialog mWaitDialog;

        private int mErrorCode = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(EZDeviceSettingActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
        	boolean isEnableEncrypt = params[0];
            bAction = isEnableEncrypt;
            try {
            	EZOpenSDK.getInstance().setDeviceEncryptStatusEx(isEnableEncrypt, mEZCameraInfo.getDeviceSerial(), mValidateCode);

                return true;
            } catch (BaseException e) {
                mErrorCode = e.getErrorCode();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();

            if (result) {
                showToast(R.string.encrypt_password_open_success);
//                mDevice.setIsEncrypt(1);
//                bAction = !bAction;
                mEZCameraInfo.setEncryptStatus(bAction ? 1 : 0);
                mEncryptButton.setBackgroundResource(bAction ? R.drawable.autologin_on : R.drawable.autologin_off);
//                if (mDevice.getSupportChangeSafePasswd() != DeviceConsts.NOT_SUPPORT)
//                    mModifyPasswordLayout.setVisibility(View.VISIBLE);
            } else {
                switch (mErrorCode) {
                    case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                        showToast(R.string.encrypt_password_open_fail_networkexception);
                        break;
                    case ErrorCode.ERROR_WEB_SESSION_ERROR:
                        ActivityUtils.handleSessionException(EZDeviceSettingActivity.this);
                        break;
                    case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                        ActivityUtils.handleHardwareError(EZDeviceSettingActivity.this, null);
                        break;
                    default:
                        showToast(R.string.encrypt_password_open_fail, mErrorCode);
                        break;
                }
            }
        }
    }

    /**
     * 删除设备任务
     */
    private class DeleteDeviceTask extends AsyncTask<Void, Void, Boolean> {

        private Dialog mWaitDialog;

        private int mErrorCode = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(EZDeviceSettingActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (!ConnectionDetector.isNetworkAvailable(EZDeviceSettingActivity.this)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return false;
            }

            try {
//            	EZCameraInfo cameraInfo = params[0];
                mEZOpenSDK.deleteDevice(mEZCameraInfo.getDeviceSerial());
                return true;
            } catch (BaseException e) {
                mErrorCode = e.getErrorCode();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();

            if (result) {
                showToast(R.string.detail_del_device_success);
                Intent intent = new Intent(EZDeviceSettingActivity.this, EZCameraListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                switch (mErrorCode) {
                    case ErrorCode.ERROR_WEB_SESSION_ERROR:
                        ActivityUtils.handleSessionException(EZDeviceSettingActivity.this);
                        break;
                    case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                        ActivityUtils.handleHardwareError(EZDeviceSettingActivity.this, null);
                        break;
                    case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                        showToast(R.string.alarm_message_del_fail_network_exception);
                        break;
                    case ErrorCode.ERROR_WEB_DEVICE_VALICATECODE_ERROR:
                        showToast(R.string.verify_code_error);
                    default:
                        showToast(R.string.alarm_message_del_fail_txt, mErrorCode);
                        break;
                }
            }
        }
    }

}