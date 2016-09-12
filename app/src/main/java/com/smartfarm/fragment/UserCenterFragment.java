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
    /**��ά��*/
    private ImageView barcode;
    /**��ʼ���豸*/
    private RelativeLayout deviceInit;
    private RelativeLayout passwordChange;
    private RelativeLayout exit;
    private RelativeLayout about;
    private RelativeLayout update;
    /**��¼�˺�*/
    private TextView userAccount;
    private TextView version;
    /**���º��*/
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
                    .setTitle("�豸��Ϣ")
                    .setMessage("����ʱ����ɨ��")
                    .setNeutralButton("����", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create()
                    .show();
            return false;
        }
    }

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
                updateMethod();
            }
        }
    }

    private void deviceInitMethod() {
        Intent intent = new Intent(activity, WifiConfigActivity.class);
        startActivity(intent);
    }

    /**
     * �������ڵĶԻ����Ѿ����²���
     */
    private void showAbout() {
        View contentView = View.inflate(activity,R.layout.dialog_about,null);
        final BaseAlterDialogUtil baseDialog = new BaseAlterDialogUtil(activity);
        TextView tvContent = (TextView) contentView.findViewById(R.id.id_dialog_about_content);
        tvContent.setText("\t\t���ݸ�����Ϣ�Ƽ����޹�˾λ�ڹ������������������ѧ���Ҵ�ѧ�Ƽ�԰����׼�ǻ�ũҵ��������ʵ����λ�ڻ���ũҵ��ѧũѧԺ������IT��Ƕ��ʽӲ��������LED�������Զ����Ƶȼ����Ŷӣ���Ҫ�������ʵʱ��⡢�Զ����Ƶ��ִ�ũҵ������ϵͳ���ѿ�����������֪ʶ��Ȩ�ĸ��Ǿ�׼�ǻ�ũҵϵͳ��\n" +
                "\t\tϵͳ��ʵʱ�ɼ�������ϵͳ�����ڵĿ�����������/ʪ�ȡ�������̼������ǿ��/���ʱȡ�����������ҺPHֵ/ECֵ�Ȼ������ӣ�����������LED���ⷽ��ӵ��ҵ�����ȵĺ��ļ�����ϵͳ����ũ��ר�ҡ��豸����Ա���ʻ���ר�Ҽ�����������ʩ�������Զ�ƥ�䣬ʵ��һ��ר�ҡ������ߵļ�������Ŀ�ꡣϵͳ�ɹ㷺Ӧ����ֲ�﹤����ֲ������������ũҵ����׼�ι�/ʩ��/��ҩ��������Ϣ�ɼ�������\n" +
                "\t\tͨ���������ʵʱ��⡢�Զ����Ƶ��ִ�ũҵ����������˾���𲽽���һ�����Ű��ݡ������ߡ���߹�Ӯ���ִ�ũҵ����ƽ̨���ƶ���׼����������һ�塢�ۺϽ��ڷ�����ִ�ũҵ��ҵ���ķ�չ��");
        Button btnBack = (Button) contentView.findViewById(R.id.id_dialog_about_btn);
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        });
        baseDialog.setWidthAndHeightRadio(0.8f,0.8f);
        baseDialog.setLocation(Gravity.CENTER,0,0);
        baseDialog.setContentView(contentView);

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
                                builder.setMessage("��ȡ���ذ汾�ų���");
                            } else {
                                builder.setMessage("��ȡԶ�˰汾�ų���");
                            }
                            builder.setCancelable(false)
                                    .setPositiveButton(
                                            "�õ�",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) {
                                                    dialog.cancel();
                                                }
                                            });
                        } else if (lastestVersion > currentVersion) {
                            builder.setTitle("��������: " + date)
                                    .setMessage(note)
                                    .setCancelable(false)
                                    .setPositiveButton(
                                            "����",
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
                                            "��һ��",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int id) {
                                                    dialog.cancel();
                                                }
                                            });
                        } else {
                            builder.setMessage("�Ѿ������°���")
                                    .setCancelable(true)
                                    .setPositiveButton(
                                            "�õ�",
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
                        progressDialog.setMessage("�������Ӹ��·�����");
                        progressDialog.show();
                    }
                });
            }
        });
    }

}
