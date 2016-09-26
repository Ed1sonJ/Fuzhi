package com.smartfarm.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartfarm.fragment.ChartFragment;
import com.smartfarm.fragment.EquipmentListFragment;
import com.smartfarm.fragment.OverviewFragment;
import com.smartfarm.fragment.SchemeDefaultFragment;
import com.smartfarm.fragment.UserCenterFragment;
import com.smartfarm.update.CheckUpdateCallback;
import com.smartfarm.update.UpdateManager;
import com.smartfarm.util.Common;
import com.smartfarm.util.IntentUtil;
import com.smartfarm.util.ToastUtil;


public class MainActivityNew extends AppCompatActivity {
    protected FragmentManager fm;
    /**设备列表*/
    protected EquipmentListFragment equipmentListFragment;
    /**主界面*/
    protected OverviewFragment overviewFragment;
    /**我的*/
    protected UserCenterFragment usercenterFragment;
    /**设备控制*/
    protected SchemeDefaultFragment schemeDefaultFragment;
    /**历史数据*/
    protected ChartFragment chartFragment;
    protected ImageView equipmentListBtn;
    protected TextView equipmentListTextView;
    /**主界面*/
    protected ImageView plantBtn;
    protected TextView plantTextView;
    protected ImageView chartBtn;
    protected TextView chartTextView;
    protected ImageView userBtn;
    protected TextView userTextView;
    /**设备控制*/
    protected ImageView schemeBtn;
    protected TextView schemeTextView;
    protected String currentPressedBtn;
    /**标记有没有选择了一台设备,最开始只能从点击一台设备进入主界面,没有选择设备不能进入主界面,历史数据，设备控制等界面*/
    private boolean clickable;
    public RelativeLayout bottom_bar;
    protected void initView() {
        equipmentListBtn = (ImageView) findViewById(R.id.equipment_list_btn);
        equipmentListBtn.setOnClickListener(new OnEquipmentListBtnClickedListener());
        equipmentListTextView = (TextView) findViewById(R.id.devices_textview);

        plantBtn = (ImageView) findViewById(R.id.plant_btn);
        plantBtn.setOnClickListener(new OnPlantBtnClickedListener());
        plantTextView = (TextView) findViewById(R.id.plant_textview);
        chartBtn = (ImageView) findViewById(R.id.chart_btn);
        chartBtn.setOnClickListener(new OnChartBtnClickedListener());
        chartTextView = (TextView) findViewById(R.id.chart_textview);
        schemeBtn = (ImageView) findViewById(R.id.scheme_btn);
        schemeBtn.setOnClickListener(new OnSchemeBtnClickedListener());
        schemeTextView = (TextView) findViewById(R.id.scheme_textview);
        userBtn = (ImageView) findViewById(R.id.user_btn);
        userBtn.setOnClickListener(new OnUserBtnClickedListener());
        userTextView = (TextView) findViewById(R.id.user_textview);

        //底部按钮，设备，主界面，历史数据，设备控制，我的
        bottom_bar=(RelativeLayout)findViewById(R.id.bottom_bar);

        fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        equipmentListFragment = (EquipmentListFragment) fm.findFragmentByTag("list");
        if (equipmentListFragment == null) {
            equipmentListFragment = new EquipmentListFragment();
            ft.add(R.id.main_fragment, equipmentListFragment, "list");
        }
        overviewFragment = (OverviewFragment) fm.findFragmentByTag("overview");
        if (overviewFragment == null) {
            overviewFragment = new OverviewFragment();
            ft.add(R.id.main_fragment, overviewFragment, "overview");
        }
        usercenterFragment = (UserCenterFragment) fm.findFragmentByTag("usercenter");
        if (usercenterFragment == null) {
            usercenterFragment = new UserCenterFragment();
            ft.add(R.id.main_fragment, usercenterFragment, "usercenter");
        }
        schemeDefaultFragment = (SchemeDefaultFragment) fm.findFragmentByTag("schemeDefault");
        if (schemeDefaultFragment == null) {
            schemeDefaultFragment = new SchemeDefaultFragment();
            ft.add(R.id.main_fragment, schemeDefaultFragment, "schemeDefault");
        }
        chartFragment = (ChartFragment) fm.findFragmentByTag("chart");
        if (chartFragment == null) {
            chartFragment = new ChartFragment();
            ft.add(R.id.main_fragment, chartFragment, "chart");
        }
        ft.show(equipmentListFragment);
        ft.hide(overviewFragment);
        ft.hide(usercenterFragment);
        ft.hide(schemeDefaultFragment);
        ft.hide(chartFragment);
        ft.commit();
        currentPressedBtn = "list";

        //一进来就检测更新
        UpdateManager updateManager = UpdateManager.getInstance(this);
        updateManager.checkUpdate(new OnCheckUpdateListener());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Common.token = savedInstanceState.getString("token");
            Common.username = savedInstanceState.getString("username");
        }
        setContentView(R.layout.activity_main_new);
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_new, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            //拍照获取的结果
            case IntentUtil.CAMERA_WITH_DATA:
                overviewFragment.loadEquipmentImage();
                break;
            case IntentUtil.BARCODE_SCANNER_INOVERVIEW:
                //overviewFragment.getScannerInfo(data, mLocationClient);
                break;
            //本地照片
            case IntentUtil.PICTURE_FROM_GALLERY:
                overviewFragment.loadPictureFromGallery(data);
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("token", Common.token);
        outState.putString("username", Common.username);
    }

    protected class OnEquipmentListBtnClickedListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            //是不是一直再按同一个，不要做无谓的刷新
            if (!currentPressedBtn.equals("list")) {
                pressListBtn();
            }
        }
    }

    protected class OnPlantBtnClickedListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (!currentPressedBtn.equals("plant")) {
                if(clickable)
                {
                    pressPlantBtn();
                }
                else
                {
                    ToastUtil.showShort(MainActivityNew.this,"请先选择一台设备");
                }
            }
        }
    }

    protected class OnChartBtnClickedListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (!currentPressedBtn.equals("chart")) {
                if(clickable)
                {
                    pressChartBtn(0);
                }
                else
                {
                    ToastUtil.showShort(MainActivityNew.this,"请先选择一台设备");
                }
            }
        }
    }

    protected class OnSchemeBtnClickedListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (!currentPressedBtn.equals("scheme")) {
                if(clickable)
                {
                    pressSchemeBtn();
                }
                else
                {
                    ToastUtil.showShort(MainActivityNew.this,"请先选择一台设备");
                }

            }
        }
    }

    protected class OnUserBtnClickedListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (!currentPressedBtn.equals("user")) {
                pressUserBtn();
            }
        }
    }

    protected class OnCheckUpdateListener implements CheckUpdateCallback {
        @Override
        public void onCheckingUpdate() {
        }

        @Override
        public void onCheckUpdateFinished(final int currentVersion,
                                          final int lastestVersion, String url, String date, String note) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentVersion != -1 && lastestVersion != -1
                            && lastestVersion > currentVersion) {
                        userBtn.setBackgroundResource(R.drawable.icon_red_dot);
                        usercenterFragment.showUpdateRedDot();
                    }
                }
            });
        }
    }

    public void pressListBtn() {
        currentPressedBtn = "list";
        FragmentTransaction ft = fm.beginTransaction();
        ft.show(equipmentListFragment);
        ft.hide(overviewFragment);
        ft.hide(usercenterFragment);
        ft.hide(schemeDefaultFragment);
        ft.hide(chartFragment);
        ft.commit();

        equipmentListBtn.setImageResource(R.drawable.icon_devices_pressed);
        plantBtn.setImageResource(R.drawable.icon_plant);
        userBtn.setImageResource(R.drawable.person);
        schemeBtn.setImageResource(R.drawable.controller);
        chartBtn.setImageResource(R.drawable.icon_chart);
        equipmentListTextView.setTextColor(getResources().getColor(R.color.green_1));
        plantTextView.setTextColor(getResources().getColor(R.color.gray_3));
        userTextView.setTextColor(getResources().getColor(R.color.gray_3));
        schemeTextView.setTextColor(getResources().getColor(R.color.gray_3));
        chartTextView.setTextColor(getResources().getColor(R.color.gray_3));
    }

    public void pressPlantBtn() {
        //最开始只能从点击一台设备进入主界面
        //没有选择设备不能进入主界面
        clickable=true;

        currentPressedBtn = "plant";
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(equipmentListFragment);
        ft.show(overviewFragment);
        ft.hide(usercenterFragment);
        ft.hide(schemeDefaultFragment);
        ft.hide(chartFragment);
        ft.commit();

        equipmentListBtn.setImageResource(R.drawable.icon_devices);
        plantBtn.setImageResource(R.drawable.icon_plant_pressed);
        userBtn.setImageResource(R.drawable.person);
        schemeBtn.setImageResource(R.drawable.controller);
        chartBtn.setImageResource(R.drawable.icon_chart);
        equipmentListTextView.setTextColor(getResources().getColor(R.color.gray_3));
        plantTextView.setTextColor(getResources().getColor(R.color.green_1));
        userTextView.setTextColor(getResources().getColor(R.color.gray_3));
        schemeTextView.setTextColor(getResources().getColor(R.color.gray_3));
        chartTextView.setTextColor(getResources().getColor(R.color.gray_3));
    }

    public void pressUserBtn() {
        currentPressedBtn = "user";
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(equipmentListFragment);
        ft.hide(overviewFragment);
        ft.hide(schemeDefaultFragment);
        ft.hide(chartFragment);
        ft.show(usercenterFragment);
        ft.commit();

        equipmentListBtn.setImageResource(R.drawable.icon_devices);
        plantBtn.setImageResource(R.drawable.icon_plant);
        userBtn.setImageResource(R.drawable.person_pressed);
        schemeBtn.setImageResource(R.drawable.controller);
        chartBtn.setImageResource(R.drawable.icon_chart);
        equipmentListTextView.setTextColor(getResources().getColor(R.color.gray_3));
        plantTextView.setTextColor(getResources().getColor(R.color.gray_3));
        userTextView.setTextColor(getResources().getColor(R.color.green_1));
        schemeTextView.setTextColor(getResources().getColor(R.color.gray_3));
        chartTextView.setTextColor(getResources().getColor(R.color.gray_3));
    }

    public void pressSchemeBtn() {
        currentPressedBtn = "scheme";
        schemeDefaultFragment.queryIndicators();
        FragmentTransaction ft = fm.beginTransaction();

        ft.hide(equipmentListFragment);
        ft.hide(overviewFragment);
        ft.hide(chartFragment);
        ft.show(schemeDefaultFragment);
        ft.hide(usercenterFragment);
        ft.commit();

        equipmentListBtn.setImageResource(R.drawable.icon_devices);
        userBtn.setImageResource(R.drawable.person);
        plantBtn.setImageResource(R.drawable.icon_plant);
        schemeBtn.setImageResource(R.drawable.controller_pressed);
        chartBtn.setImageResource(R.drawable.icon_chart);
        equipmentListTextView.setTextColor(getResources().getColor(R.color.gray_3));
        plantTextView.setTextColor(getResources().getColor(R.color.gray_3));
        userTextView.setTextColor(getResources().getColor(R.color.gray_3));
        schemeTextView.setTextColor(getResources().getColor(R.color.green_1));
        chartTextView.setTextColor(getResources().getColor(R.color.gray_3));
    }

    public void pressChartBtn(int sensorIndex) {
        currentPressedBtn = "chart";
        chartFragment.initHistoryFragment(sensorIndex);
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(equipmentListFragment);
        ft.hide(overviewFragment);
        ft.show(chartFragment);
        ft.hide(schemeDefaultFragment);
        ft.hide(usercenterFragment);
        ft.commit();

        equipmentListBtn.setImageResource(R.drawable.icon_devices);
        userBtn.setImageResource(R.drawable.person);
        plantBtn.setImageResource(R.drawable.icon_plant);
        schemeBtn.setImageResource(R.drawable.controller);
        chartBtn.setImageResource(R.drawable.icon_chart_pressed);
        equipmentListTextView.setTextColor(getResources().getColor(R.color.gray_3));
        plantTextView.setTextColor(getResources().getColor(R.color.gray_3));
        userTextView.setTextColor(getResources().getColor(R.color.gray_3));
        schemeTextView.setTextColor(getResources().getColor(R.color.gray_3));
        chartTextView.setTextColor(getResources().getColor(R.color.green_1));
    }

}
