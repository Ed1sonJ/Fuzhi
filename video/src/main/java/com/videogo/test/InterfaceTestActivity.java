package com.videogo.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.videogo.exception.BaseException;
import com.videogo.main.EZLeaveMsgController;
import com.videogo.open.R;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZLeaveMessage;
import com.videogo.openapi.bean.EZProbeDeviceInfo;
import com.videogo.openapi.bean.EZStorageStatus;
import com.videogo.openapi.bean.EZUserInfo;
import com.videogo.openapi.bean.resp.EZDeviceUpgradeStatus;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterfaceTestActivity extends Activity implements View.OnClickListener
    , EZLeaveMsgController.EZLeaveMsgGetDataCb {
    public static final String TAG = "InterfaceTestActivity";
    private static final int MSG_PLAY_NEXT = 101;
    private EditText mSerialEdit;
    private Button mTestButton;
    private final String filePath = "/sdcard/videogo_test_cfg";
    private Map<String, String> mMap;
    private String mSerial;
    private  Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface_test);
        mMap = new HashMap<>();
        parseTestConfigFile(filePath, mMap);
        mSerial = mMap.get("deviceSerial");
        findViews();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                LogUtil.i(TAG, "handleMessage: msg: " + msg.what);

                switch (msg.what) {
                    case EZLeaveMsgController.MSG_LEAVEMSG_DOWNLOAD_SUCCESS:
                        break;
                    case EZLeaveMsgController.MSG_LEAVEMSG_DOWNLOAD_FAIL:
                        break;
                    case MSG_PLAY_NEXT:
                        if(mPlayIndex < mLeaveMsgList.size() ) {
                            Thread thr = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    boolean result = EZOpenSDK.getInstance().getLeaveMessageData(mHandler, mLeaveMsgList.get(mPlayIndex), InterfaceTestActivity.this);
                                    LogUtil.i(TAG, "run: getLeaveMessageData returns:" + result);
                                }
                            });
                            thr.start();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.interface_test_button) {
            testV32Interface();

        } else if (i == R.id.interface_v33_test_button) {
            testV33Interface();

        } else if (i == R.id.id_interface_test_openCloudPage) {
            final String deviceSerial = mSerialEdit.getEditableText().toString();
            try {
                EZOpenSDK.getInstance().openCloudPage(deviceSerial);
            } catch (BaseException e) {
                e.printStackTrace();
            }

        } else if (i == R.id.id_interface_test_forgetPassword) {
            EZOpenSDK.getInstance().openChangePasswordPage();

        } else if (i == R.id.id_show_stream_limit_dialog) {
            showLimitDialog(this, 0, 900 / 60);

        } else {
        }
    }

    private void findViews() {
        mSerialEdit=(EditText) findViewById(R.id.interface_test_editText);
        mTestButton = (Button) findViewById(R.id.interface_test_button);
        if(!TextUtils.isEmpty(mSerial)) {
            mSerialEdit.setText(mSerial);
        }
    }

    // test v3.2 interfaces
    private void testV31Interface() {
        final String deviceSerial = mSerialEdit.getEditableText().toString();
        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                // test interface capturePicture
            }
        });
        thr.start();
    }
    // test v3.2 interfaces
    List<EZLeaveMessage> mLeaveMsgList;
    int mPlayIndex = 0;
    private void testV32Interface() {
        final String deviceSerial = mSerialEdit.getEditableText().toString();
        Thread thr = new Thread(new Runnable(){
            @Override
            public void run() {
                // test interface capturePicture
                try {
                    String picUrl = EZOpenSDK.getInstance().capturePicture(deviceSerial, 0);
                    LogUtil.i(TAG, "testV32Interface: capturePicture: " + picUrl);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface getUserInfo
                try {
                    EZUserInfo userInfo = EZOpenSDK.getInstance().getUserInfo();
                    LogUtil.i(TAG, "EZUserInfo:" + userInfo);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface getUnreadMessageCount
                try {
                    int msgCount = EZOpenSDK.getInstance().getUnreadMessageCount(deviceSerial, EZConstants.EZMessageType.EZMessageTypeAlarm);
                    LogUtil.i(TAG, "unReadMessageCount:" + msgCount);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface getLeaveMessageList
                try {
                    Calendar begin = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    begin.set(Calendar.HOUR_OF_DAY, 0);
                    end.set(Calendar.HOUR_OF_DAY, 23);

                    List<EZLeaveMessage> ret = EZOpenSDK.getInstance().getLeaveMessageList(deviceSerial, 0, 5, begin, end);
                    LogUtil.i(TAG, "getLeaveMessageList:" + ret);
                } catch (BaseException e) {
                    e.printStackTrace();
                }
                try {
                    Calendar begin = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    begin.set(Calendar.HOUR_OF_DAY, 0);
                    end.set(Calendar.HOUR_OF_DAY, 23);

                    List<EZLeaveMessage> ret = EZOpenSDK.getInstance().getLeaveMessageList("", 0, 5, begin, end);
                    LogUtil.i(TAG, "getLeaveMessageList:" + ret);
                } catch (BaseException e) {
                    e.printStackTrace();
                }
                try {
                    Calendar begin = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    begin.set(Calendar.HOUR_OF_DAY, 0);
                    end.set(Calendar.HOUR_OF_DAY, 23);

                    mLeaveMsgList = EZOpenSDK.getInstance().getLeaveMessageList(null, 0, 5, begin, end);
//                    LogUtil.i(TAG, "getLeaveMessageList:" + ret);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface probeDeviceInfo
                try {
                    EZProbeDeviceInfo probeInfo = EZOpenSDK.getInstance().probeDeviceInfo(deviceSerial);
                    LogUtil.i(TAG, "probeDeviceInfo:" + probeInfo);
                } catch (BaseException e) {
                    e.printStackTrace();
                }
                // test interface setLeaveMessageStatus
                // TODO

                // test interface deleteLeaveMessages
                // TODO

                // test interface getLeaveMessageData
                // TODO

                // test interface formatStorage
                try {
                    boolean formatResult = EZOpenSDK.getInstance().formatStorage(deviceSerial, 1);
                    LogUtil.i(TAG, "formatStorage:" + formatResult);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface getStorageStatus
                try {
                    List<EZStorageStatus> storageList = EZOpenSDK.getInstance().getStorageStatus(deviceSerial);
                    LogUtil.i(TAG, "getStorageStatus:" + storageList);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface getDeviceUpgradeStatus
                try {
                    EZDeviceUpgradeStatus status = EZOpenSDK.getInstance().getDeviceUpgradeStatus(deviceSerial);
                    LogUtil.i(TAG, "run: getDeviceUpgradeStatus" + status);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface upgradeDevice
                try {
                    EZOpenSDK.getInstance().upgradeDevice(deviceSerial);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface getDeviceInfoBySerial
                try {
                    EZDeviceInfo info = EZOpenSDK.getInstance().getDeviceInfoBySerial(deviceSerial);
                    Log.i(TAG, "run: device info:" + info);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface getPushNoticeMessage
                try {
                    EZOpenSDK.getInstance().getTransferMessageInfo("af");
                } catch (BaseException e) {
                    e.printStackTrace();
                }



                // test interface
                // test interface getStreamLimitInfo
//                try {
//                    EZStreamLimitInfo info = EZOpenSDK.getInstance().getStreamLimitInfo();
//                    LogUtil.i(TAG, "run: getStreamLimitInfo : " + info);
//                } catch (BaseException e) {
//                    e.printStackTrace();
//                }
                // test interface getSmsCode
                // test interface deleteDevice
//                try {
//                    EZOpenSDK.getInstance().deleteDevice(deviceSerial);
//                } catch (BaseException e) {
//                    e.printStackTrace();
//                }
            }});
        thr.start();
    }
    private void testV33Interface() {
        final String deviceSerial = mSerialEdit.getEditableText().toString();
        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                // test interface getDeviceList
                try {
                    EZOpenSDK.getInstance().getDeviceList(0,5);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface getDetectorList
                try {
                    EZOpenSDK.getInstance().getDetectorList(deviceSerial);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface getAlarmListBySerial
                try {
                    Calendar begin = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    begin.set(begin.get(Calendar.YEAR), begin.get(Calendar.MONTH), begin.get(Calendar.DAY_OF_MONTH),
                            0, 0, 0);
                    end.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH),
                            23, 59, 59);

                    EZOpenSDK.getInstance().getAlarmListBySerial(deviceSerial, 0, 5, begin, end);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface setDeviceDefence
                try {
                    EZOpenSDK.getInstance().setDeviceDefence(deviceSerial, 8);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                start.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH) - 7);

                try {
                    EZOpenSDK.getInstance().getAlarmList("",1,10,start,end);
                } catch (BaseException e) {
                    e.printStackTrace();
                }
            }
        });

        thr.start();
    }

    // content of file /sdcard/videogo_test_cfg:
    // deviceSerial:427734168
    private void parseTestConfigFile(String filePath, Map<String,String> map) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
            String lineStr;
            lineStr = br.readLine();
            while (lineStr != null) {
                String[] values = lineStr.split(":");
                if(values.length == 2) {
                    map.put(values[0], values[1]);
                }
                lineStr = br.readLine();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private Context mContext;
    private Handler mMainHandler;
    private AlertDialog mLimitStreamDialog;
    private final static int LIMIT_STREAM_MSG = 10001;

    private void showLimitDialog(Context context, int type, int limit_minutes) {
        if(limit_minutes <= 0) {
            LogUtil.i(TAG, "showLimitDialog: limite_minutes invalid params");
            return;
        }
        LinearLayout passwordErrorLayout = new LinearLayout(context);
        passwordErrorLayout.setId(R.id.new_password_error_layout);
        FrameLayout.LayoutParams layoutLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        passwordErrorLayout.setOrientation(LinearLayout.VERTICAL);
        passwordErrorLayout.setLayoutParams(layoutLp);

        TextView message1 = new TextView(context);
        message1.setId(R.id.new_message1);
        LinearLayout.LayoutParams message1Lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        message1Lp.gravity = Gravity.CENTER_HORIZONTAL;
        message1Lp.leftMargin = Utils.dip2px(context, 10);
        message1Lp.rightMargin = Utils.dip2px(context, 10);
        message1Lp.topMargin = Utils.dip2px(context, 20);
        message1.setGravity(Gravity.CENTER);
        message1.setTextColor(Color.rgb(255, 255, 255));
        message1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        passwordErrorLayout.addView(message1, message1Lp);

        Button btn = new Button(context);
        btn.setText("继续");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                closePasswordDialog(mRealPlayMgr);
//                mMainHandler.removeMessages(LIMIT_STREAM_MSG);
                mLimitStreamDialog.dismiss();
            }
        });
        LinearLayout.LayoutParams buttonLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        buttonLp.leftMargin = Utils.dip2px(context, 10);
        buttonLp.gravity = Gravity.CENTER_HORIZONTAL;
        buttonLp.topMargin = Utils.dip2px(context, 30);
        buttonLp.bottomMargin = Utils.dip2px(context, 10);
        btn.setPadding(50, 10, 50, 10);

        btn.setBackgroundDrawable(new ShapeDrawable(new RectShape()));
        btn.setTextColor(Color.rgb(255, 255, 255));
        btn.setBackgroundColor(Color.GRAY);
        passwordErrorLayout.addView(btn, buttonLp);

        // 使用布局中的视图创建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        message1.setText("您已观看视频" + limit_minutes + "分钟，是否继续?");

        builder.setCancelable(true);
        builder.setView(passwordErrorLayout);

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
//                closePasswordDialog(mRealPlayMgr);
            }
        });

        mLimitStreamDialog = builder.create();

        Window window = mLimitStreamDialog.getWindow();
//        mPasswordDialog.getWindow().setGravity(Gravity.TOP);
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.TOP);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        window.setbac

        lp.y = 50;
        lp.alpha = 0.85f;
        window.setAttributes(lp);
        mLimitStreamDialog.show();
    }

    @Override
    public void onData(byte[] bytes) {
        LogUtil.i(TAG, "onData: size " + bytes.length);
        processDataWithAudioTrack8bit(bytes);

        ++ mPlayIndex;
        Message msg = Message.obtain();
        msg.what = MSG_PLAY_NEXT;
        mHandler.sendMessageDelayed(msg, 1000);

    }

    AudioTrack mAudioTrack = null;
    private void processDataWithAudioTrack8bit(byte[] bytess) {
        LogUtil.i(TAG, "Enter processDataWithAudioTrack8bit: ");
        if (mAudioTrack == null) {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    500000,
                    AudioTrack.MODE_STREAM);
        }
        // Start playback
        try {
            mAudioTrack.play();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return;
        }

        mAudioTrack.write(bytess,0, bytess.length);
        mAudioTrack.stop() ;
    }
}
