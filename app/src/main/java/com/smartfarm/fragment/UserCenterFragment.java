package com.smartfarm.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;

import com.smartfarm.activity.Login;
import com.smartfarm.activity.Password;
import com.smartfarm.activity.R;
import com.smartfarm.activity.WifiConfigActivity;
import com.smartfarm.dialog.BaseAlterDialogUtil;
import com.smartfarm.update.CheckUpdateCallback;
import com.smartfarm.update.UpdateManager;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.Common;
import com.smartfarm.util.Config;

public class UserCenterFragment extends BaseFragment {
    /**二维码*/
    private ImageView barcode;
    /**初始化设备*/
    private RelativeLayout deviceInit;
    private RelativeLayout passwordChange;
    private RelativeLayout exit;
    private RelativeLayout about;
    private RelativeLayout update;
    /**登录账号*/
    private TextView userAccount;
    private TextView version;
    /**更新红点*/
    private ImageView updateRedDot;
    private View userInfoView;

    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        userInfoView = inflater.inflate(R.layout.fragment_usercenter_new,
                container, false);
        return userInfoView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    public void showUpdateRedDot() {
        updateRedDot.setVisibility(View.VISIBLE);
    }

    private void init() {
        findViewById();
        initView();
    }

    private void findViewById() {
        userAccount = (TextView) userInfoView.findViewById(R.id.user_acount);
        barcode = (ImageView) userInfoView.findViewById(R.id.barcode);
        deviceInit = (RelativeLayout) userInfoView.findViewById(R.id.device_init);
        passwordChange = (RelativeLayout) userInfoView.findViewById(R.id.password_change);
        exit = (RelativeLayout) userInfoView.findViewById(R.id.exit);
        about = (RelativeLayout) userInfoView.findViewById(R.id.about);
        update = (RelativeLayout) userInfoView.findViewById(R.id.update);
        updateRedDot = (ImageView) userInfoView
                .findViewById(R.id.update_red_dot);
        version = (TextView) userInfoView.findViewById(R.id.usercenter_version);
        String versionName = UpdateManager.getInstance(activity)
                .getCurrentVersionName();
        version.setText(versionName);
    }

    private void initView() {
        barcode.setOnLongClickListener(new barcodeListener());

        userAccount.setText(Common.username);

        deviceInit.setOnClickListener(new choiceListener());
        deviceInit.setTag("deviceInit");

        about.setOnClickListener(new choiceListener());
        about.setTag("about");

        passwordChange.setOnClickListener(new choiceListener());
        passwordChange.setTag("passwordChange");

        exit.setOnClickListener(new choiceListener());
        exit.setTag("exit");

        update.setOnClickListener(new choiceListener());
        update.setTag("update");
    }

    private class barcodeListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            new AlertDialog.Builder(activity)
                    .setTitle("设备信息")
                    .setMessage("您暂时不能扫码")
                    .setNeutralButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create()
                    .show();
            return false;
        }
    }

    /**
     * 根据点击的按钮实现不同的点击事件
     */
    private class choiceListener implements OnClickListener {
        String tag = "";

        @Override
        public void onClick(View v) {
            tag = (String) v.getTag();
            if (tag.equals("deviceInit")) {
                deviceInitMethod();
            }
            else if (tag.equals("about")) {
                showAbout();
            }
            else if (tag.equals("passwordChange")) {
                passwordChangeMethod();
            }
            else if (tag.equals("exit")) {
                exitMethod();
            }
            else if (tag.equals("update")) {
                //更新
                updateMethod();
            }
        }
    }

    private void deviceInitMethod() {
        Intent intent = new Intent(activity, WifiConfigActivity.class);
        startActivity(intent);
    }

    /**
     * 弹出关于的对话框，已经更新布局
     */
    private void showAbout() {
        View contentView = View.inflate(activity,R.layout.dialog_about,null);
        final BaseAlterDialogUtil baseDialog = new BaseAlterDialogUtil(activity);
        TextView tvContent = (TextView) contentView.findViewById(R.id.id_dialog_about_content);
        tvContent.setText("\u3000\u3000自然一号是富智精准智慧农业物联网采控终端。\n" +
                "【实时检测】可实时采集空气或土壤的温湿度，空气CO2浓度、光照强度、土壤/水质PH、土壤EC值、溶解氧等环境因子。\n" +
                "【智能控制】可对智能LED植物照明、滴灌、喷淋、水泵、风机等设备进行智能控制。\n" +
                "【专家方案】系统设立专家帐户，专家设置的技术方案与设施管理方案可自动匹配，智能运行。");

        Button btnBack = (Button) contentView.findViewById(R.id.id_dialog_about_btn);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        });

        baseDialog
                .setLocation(Gravity.CENTER,0,0)
                .setWidthAndHeightRadio(0.8f,0.65f)
                .setContentView(contentView);

    }

    private void passwordChangeMethod() {
        Intent intent = new Intent(activity, Password.class);
        activity.startActivity(intent);
    }

    private void exitMethod() {
        new Config(activity).clear();
        Intent intent = new Intent(activity, Login.class);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * 点击检测更新回调的方法
     */
    private void updateMethod() {
        UpdateManager updateManager = UpdateManager
                .getInstance(activity);
        updateManager.checkUpdate(new CheckUpdateCallback() {
            BaseProgressDialog progressDialog = new BaseProgressDialog(
                    activity);

            @Override
            public void onCheckUpdateFinished(final int currentVersion,
                                              final int lastestVersion, final String url,
                                              final String date, final String note) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                activity);
                        if (currentVersion == -1
                                || lastestVersion == -1) {
                            if (currentVersion == -1) {
                                builder.setMessage("获取本地版本号出错");
                            } else {
                                builder.setMessage("获取远端版本号出错");
                            }
                            builder.setCancelable(false)
                                    .setPositiveButton(
                                            "好的",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) {
                                                    dialog.cancel();
                                                }
                                            });
                        } else if (lastestVersion > currentVersion) {
                            builder.setTitle("更新日期: " + date)
                                    .setMessage(note)
                                    .setCancelable(false)
                                    .setPositiveButton(
                                            "更新",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int id) {
                                                    UpdateManager
                                                            .getInstance(
                                                                    activity)
                                                            .downloadAndInstall(
                                                                    url);
                                                }
                                            })
                                    .setNegativeButton(
                                            "下一次",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int id) {
                                                    dialog.cancel();
                                                }
                                            });
                        } else {
                            builder.setMessage("已经是最新版了")
                                    .setCancelable(true)
                                    .setPositiveButton(
                                            "好的",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) {
                                                    dialog.cancel();
                                                }
                                            });
                        }
                        AlertDialog alert = builder.create();
                        if (!activity.isFinishing())
                            alert.show();
                    }
                });
            }

            @Override
            public void onCheckingUpdate() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMessage("正在连接更新服务器");
                        progressDialog.show();
                    }
                });
            }
        });
    }

}
