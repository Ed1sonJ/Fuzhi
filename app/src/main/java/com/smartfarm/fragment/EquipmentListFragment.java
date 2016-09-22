package com.smartfarm.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.baidu.location.LocationClient;
import com.smartfarm.activity.LocationActivity;
import com.smartfarm.activity.MainActivityNew;
import com.smartfarm.activity.R;
import com.smartfarm.adapter.EquipmentListAdapter;
import com.smartfarm.adapter.SearchListAdapter;
import com.smartfarm.bean.EquipmentBean;
import com.smartfarm.bean.TopBean;
import com.smartfarm.dialog.BaseAlterDialogUtil;
import com.smartfarm.dialog.BaseCustomAlterDialog;
import com.smartfarm.event.EquipmentGroupLongClickedEvent;
import com.smartfarm.event.EquipmentImageEvent;
import com.smartfarm.event.EquipmentItemConfigureClickedEvent;
import com.smartfarm.event.EquipmentItemGpsClickedEvent;
import com.smartfarm.event.EquipmentSelectedEvent;
import com.smartfarm.event.GlobalEvent;
import com.smartfarm.fragmentUtil.UploadAndDownloadGPS;
import com.smartfarm.model.Equipment;
import com.smartfarm.model.Group;
import com.smartfarm.observable.DeleteEquipmentObservable;
import com.smartfarm.observable.EquipmentsListObservable;
import com.smartfarm.observable.SendQRCodeObservable;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.Common;
import com.smartfarm.util.ToastUtil;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.schedulers.Schedulers;

public class EquipmentListFragment extends BaseFragment {
    public static final int GET_BARCODE = 0;

    protected Activity activity;

    protected View rootView;
    //设备列表的list
    protected ExpandableListView expandableListView;
    protected ExpandableListAdapter expandableListAdapter;
    protected BaseProgressDialog baseProgressDialog;
    protected ImageView searchButton;
    protected ImageView addButton;
    protected LinearLayout searchLayout;
    //搜索结果的list
    protected ListView searchResultList;
    protected SearchListAdapter searchListAdapter;
    protected EditText searchEditText;
    protected List<EquipmentBean> equipmentBeans;
    private PopupWindow popupWindow;
    private ImageView back;
    protected EventHandler eventHandler;

    private UploadAndDownloadGPS gps = new UploadAndDownloadGPS();
    //Fragment与Activity发生关联时调用
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }
    //系统创建Fragment对象后回调该方法，在实现代码中只初始化想要在Fragment中保持的必要组件。
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在接收消息的页面，注册
        registerEvent();
    }

    //创建该Fragment的view
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_equipment_list, container, false);
        return rootView;
    }
    //当Activity的onCreate方法返回时调用
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        setUpView();
        reloadView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterEvent();
    }

    protected void findView() {
        searchLayout = (LinearLayout) rootView.findViewById(R.id.equipment_list_search_layout);
        searchResultList = (ListView) rootView.findViewById(R.id.equipment_search_result_list);
        searchEditText = (EditText) rootView.findViewById(R.id.equipment_list_search_edittext);
        back=(ImageView)rootView.findViewById(R.id.equipment_list_search_back);
        searchButton = (ImageView) rootView.findViewById(R.id.equipment_list_search_button);
        addButton = (ImageView) rootView.findViewById(R.id.equipment_list_add_button);
        expandableListView = (ExpandableListView) rootView.findViewById(R.id.equipment_expandable_list);
        baseProgressDialog = new BaseProgressDialog(activity);
    }

    protected void setUpView() {
        //标题栏的事件
        TitleBarButtonOnClickedListener titleBarButtonOnClickedListener = new TitleBarButtonOnClickedListener();
        baseProgressDialog.setMessage("请稍后");
        searchListAdapter = new SearchListAdapter(activity);
        //搜索结果的list
        searchResultList.setAdapter(searchListAdapter);
        //搜索
        searchButton.setOnClickListener(titleBarButtonOnClickedListener);
        //添加
        addButton.setOnClickListener(titleBarButtonOnClickedListener);
        //返回
        back.setOnClickListener(titleBarButtonOnClickedListener);
        //模糊搜索
        searchEditText.addTextChangedListener(new SearchListener());
        //编辑完之后点击软键盘上的回车键才会触发。EditText继承TextView
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                closeSearchLayout();
                return true;
            }
        });

        searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    closeSearchLayout();
                }
                return false;
            }
        });

        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Object tag = view.getTag();
                if (tag instanceof Bundle && ((Bundle) tag).getString("Type") == "Group") {
                    GlobalEvent.bus.post(new EquipmentGroupLongClickedEvent(((Bundle) tag).getString("Name"), view));
                }
                return true;
            }
        });

    }

    /***
     * First class option.
     ***/
    protected void reloadView() {
        EquipmentsListObservable.createObservable(Common.token).subscribeOn(Schedulers.newThread()).
                subscribe(new Subscriber<List<EquipmentBean>>() {
                    @Override
                    public void onStart() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                baseProgressDialog.show();
                            }
                        });
                    }

                    @Override
                    public void onCompleted() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshView(equipmentBeans);
                                baseProgressDialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("gzfuzhi", throwable.toString());
                        throwable.printStackTrace();
                        ToastUtil.showLong(activity, "出现异常错误，请稍后再式。");
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                baseProgressDialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onNext(List<EquipmentBean> beans) {
                        equipmentBeans = beans;
                    }
                });
    }

    /***
     * First class option.
     ***/
    protected void deleteEquipment(String equipmentCode) {
        DeleteEquipmentObservable.create(equipmentCode).subscribeOn(Schedulers.newThread()).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onStart() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        baseProgressDialog.show();
                    }
                });
            }

            @Override
            public void onCompleted() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        baseProgressDialog.dismiss();
                        reloadView();
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("gzfuzhi", throwable.toString());
                throwable.printStackTrace();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showLong(activity, "连接服务器出现异常错误，请稍后再式。");
                        baseProgressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onNext(final Boolean success) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!success) {
                            ToastUtil.showLong(activity, "连接服务器出现异常错误，请稍后再式。");
                        } else {
                            ToastUtil.showLong(activity, "删除设备成功。");
                        }
                    }
                });
            }
        });
    }

    /***
     * First class option.
     ***/
    protected void submitBarcode(String qrCode) {
        SendQRCodeObservable.create(qrCode, Common.token).subscribeOn(Schedulers.newThread()).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onStart() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        baseProgressDialog.show();
                    }
                });
            }

            @Override
            public void onCompleted() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        baseProgressDialog.dismiss();
                        reloadView();
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("gzfuzhi", throwable.toString());
                throwable.printStackTrace();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showLong(activity, "连接服务器出现异常错误，请稍后再式。");
                        baseProgressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onNext(final Boolean success) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!success) {
                            ToastUtil.showLong(activity, "连接服务器出现异常错误，请稍后再式。");
                        } else {
                            ToastUtil.showLong(activity, "扫码成功。");
                        }
                    }
                });
            }
        });
    }

    private void getLocationAndSendToServer(String scanResult){
        //声明LocationClient
        //getApplicationContext() 返回应用的上下文，生命周期是整个应用，应用摧毁它才摧毁
        //Activity.this的context 返回当前activity的上下文，属于activity ，activity 摧毁他就摧毁
        LocationClient mLocationClient = new LocationClient(activity.getApplicationContext());
        gps.getLocationAndSend(mLocationClient, scanResult);
    }

    protected void refreshView(List<EquipmentBean> beans) {
        if (beans.size() == 0) {
            showAddEquipmentHelpDialog();
        } else {

            expandableListAdapter = new EquipmentListAdapter(activity, new Group(activity, beans));
            expandableListView.setAdapter(expandableListAdapter);
            expandableListView.expandGroup(0);

        }
    }

    protected void registerEvent() {
        eventHandler = new EventHandler();
        GlobalEvent.bus.register(eventHandler);
    }

    protected void unregisterEvent() {
        GlobalEvent.bus.unregister(eventHandler);
    }

    protected void showAddEquipmentHelpDialog() {
        if (activity.isFinishing())
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false)
                .setMessage("还没有绑定设备，请点击右上角扫码添加设备")
                .setPositiveButton("好的",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        })
                .setNegativeButton("退出",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                activity.finish();
                            }
                        });
        builder.create().show();
    }

    /**
     * 点击list中的item的设置，重命名设备,适配了不同分辨率
     * @param equipmentCode
     */
    protected void showRenameEquipmentDialog(final String equipmentCode) {
        View contentView = View.inflate(activity,R.layout.equipment_add_group_content_view,null);
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        final EditText renameEditText = (EditText) contentView.findViewById(R.id.equipment_add_group_edittext);
        final String name=Equipment.getEquipmentName(activity, equipmentCode);
        baseDialog.setIcon(R.drawable.rename_group_title);
        baseDialog.setTitle("设备:"+name);
        baseDialog.setNegativeBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        });
        baseDialog.setPositiveBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renameEditText.length()>0)
                {
                    if(!renameEditText.getText().toString().equals(name))
                    {
                        Equipment.setEquipmentName(activity, equipmentCode, renameEditText.getText().toString());
                        reloadView();
                        baseDialog.dismiss();
                    }
                    baseDialog.dismiss();
                }
                else
                {
                    ToastUtil.showShort(activity,"请输入设备新的名字");
                }
            }
        });
        baseDialog.setWidthAndHeightRadio(0.8f,0.3f);
        baseDialog.setLocation(Gravity.CENTER,0,0);
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        baseDialog.setContentView(contentView,contentLp);
    }

    /**
     * 显示添加分组的对话框
     */
    protected void showAddGroupDialog() {
//        View contentView = View.inflate(activity,R.layout.equipment_add_group_content_view2,null);
//        final BaseAlterDialogUtil baseDialog = new BaseAlterDialogUtil(activity);
//        ImageView icon = (ImageView) contentView.findViewById(R.id.id_base_dialog_icon);
//        TextView title = (TextView) contentView.findViewById(R.id.id_base_dialog_title);
//        Button positiveBtn = (Button) contentView.findViewById(R.id.id_base_dialog_rightBtn);
//        Button negativeBtn = (Button) contentView.findViewById(R.id.id_base_dialog_leftBtn);
//        final EditText renameEditText = (EditText) contentView.findViewById(R.id.equipment_add_group_edittext);
//        icon.setImageResource(R.drawable.add_group_img);
//        icon.setVisibility(View.VISIBLE);
//        title.setText("创建分组");
//        positiveBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (renameEditText.length()>0)
//                {
//                    try {
//                        new Group(activity, equipmentBeans).addGroup(renameEditText.getText().toString());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        ToastUtil.showLong(activity, "创建分组失败。");
//                    }
//                    reloadView();
//                    baseDialog.dismiss();
//                }
//                else
//                {
//                    ToastUtil.showShort(activity,"请输入分组的名字");
//                }
//            }
//        });
//        negativeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                baseDialog.dismiss();
//            }
//        });
//        baseDialog.setWidthAndHeightRadio(0.8f,0.3f);
//        baseDialog.setLocation(Gravity.CENTER,0,0);
//        baseDialog.setContentView(contentView);
        // TODO: 2016/9/22 适配不同分辨率，已经适配，在平板上跑看效果
        View contentView = View.inflate(activity,R.layout.equipment_add_group_content_view,null);
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        final EditText renameEditText = (EditText) contentView.findViewById(R.id.equipment_add_group_edittext);
        baseDialog.setIcon(R.drawable.add_group_img);
        baseDialog.setTitle("创建分组");
        baseDialog.setNegativeBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        });
        baseDialog.setPositiveBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renameEditText.length()>0)
                {
                    try {
                        new Group(activity, equipmentBeans).addGroup(renameEditText.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showLong(activity, "创建分组失败。");
                    }
                    reloadView();
                    baseDialog.dismiss();
                }
                else
                {
                    ToastUtil.showShort(activity,"请输入分组的名字");
                }
            }
        });
        baseDialog.setWidthAndHeightRadio(0.8f,0.3f);
        baseDialog.setLocation(Gravity.CENTER,0,0);
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        baseDialog.setContentView(contentView,contentLp);
    }

    /**
     * 重命名分组
     * @param groupName
     */
    protected void showRenameGroupDailog(final String groupName) {
//        View contentView = View.inflate(activity,R.layout.equipment_add_group_content_view2,null);
//        ImageView icon = (ImageView) contentView.findViewById(R.id.id_base_dialog_icon);
//        TextView title = (TextView) contentView.findViewById(R.id.id_base_dialog_title);
//        final EditText renameEditText = (EditText)contentView.findViewById(R.id.equipment_add_group_edittext);
//        Button positiveBtn = (Button) contentView.findViewById(R.id.id_base_dialog_rightBtn);
//        Button negativeBtn = (Button) contentView.findViewById(R.id.id_base_dialog_leftBtn);
//
//        icon.setImageResource(R.drawable.rename_group_title);
//        icon.setVisibility(View.VISIBLE);
//        title.setText("分组:"+groupName);
//        final BaseAlterDialogUtil baseDialog = new BaseAlterDialogUtil(activity);
//        negativeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                baseDialog.dismiss();
//            }
//        });
//        positiveBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (renameEditText.length()>0)
//                {
//                    if(!renameEditText.getText().toString().equals(groupName))
//                    {
//                        try {
//                            new Group(activity, equipmentBeans).renameGroup(groupName, renameEditText.getText().toString());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            ToastUtil.showLong(activity, "重命名分组失败。");
//                        }
//                        reloadView();
//                        baseDialog.dismiss();
//                    }
//                    baseDialog.dismiss();
//                }
//                else
//                {
//                    ToastUtil.showShort(activity,"请输入分组新的名字");
//                }
//            }
//        });
//        baseDialog.setLocation(Gravity.CENTER,0,0);
//        baseDialog.setWidthAndHeightRadio(0.8f,0.3f);
//        baseDialog.setContentView(contentView);
        // TODO: 2016/9/22 适配不同分辨率
        View contentView = View.inflate(activity,R.layout.equipment_add_group_content_view,null);
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        final EditText renameEditText = (EditText) contentView.findViewById(R.id.equipment_add_group_edittext);
        baseDialog.setIcon(R.drawable.rename_group_title);
        baseDialog.setTitle("分组:"+groupName);
        baseDialog.setNegativeBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        });
        baseDialog.setPositiveBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renameEditText.length()>0)
                {
                    if(!renameEditText.getText().toString().equals(groupName))
                    {
                        try {
                            new Group(activity, equipmentBeans).renameGroup(groupName, renameEditText.getText().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtil.showLong(activity, "重命名分组失败。");
                        }
                        reloadView();
                        baseDialog.dismiss();
                    }
                    baseDialog.dismiss();
                }
                else
                {
                    ToastUtil.showShort(activity,"请输入分组新的名字");
                }
            }
        });
        baseDialog.setWidthAndHeightRadio(0.8f,0.3f);
        baseDialog.setLocation(Gravity.CENTER,0,0);
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        baseDialog.setContentView(contentView,contentLp);

    }

    /**
     * 弹出删除设备对话框，已经更新了布局
     * @param equipmentCode
     */
    protected void showDeleteEquipmentConfirmDialog(final String equipmentCode) {

        View contentView = View.inflate(activity,R.layout.delete_layout,null);
        TextView title=(TextView)contentView.findViewById(R.id.delete_title);
        title.setText("删除设备：" + Equipment.getEquipmentName(activity, equipmentCode));
        TextView positiveBtn=(TextView)contentView.findViewById(R.id.delete_dialog_sure);
        TextView negativeBtn=(TextView)contentView.findViewById(R.id.delete_dialog_cancel);

        final BaseAlterDialogUtil baseDialog = new BaseAlterDialogUtil(activity);
        baseDialog.setLocation(Gravity.CENTER,0,0);
        baseDialog.setWidthAndHeightRadio(0.8f,0.22f);
        baseDialog.setContentView(contentView);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Group(activity,equipmentBeans).deleteLatestUse(equipmentCode);
                deleteEquipment(equipmentCode);
                baseDialog.dismiss();
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        });


    }

    /**
     * 删除组对话框
     * @param groupName
     */
    protected void showDeleteGroupConfirmDialog(final String groupName) {
//        LinearLayout view=(LinearLayout)activity.getLayoutInflater().inflate(R.layout.delete_layout_new,null);
//        RelativeLayout sure=(RelativeLayout)view.findViewById(R.id.delete_dialog_sure);
//        RelativeLayout cancel=(RelativeLayout)view.findViewById(R.id.delete_dialog_cancel);
//        TextView title=(TextView)view.findViewById(R.id.delete_title);
//        title.setText("确定删除分组：" + groupName + "?");
//        final AlertDialog dialog=new AlertDialog.Builder(activity).
//                setView(view).
//                create();
//        dialog.show();
//        dialog.setCanceledOnTouchOutside(true);
//        sure.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    new Group(activity, equipmentBeans).deleteGroup(groupName);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    ToastUtil.showLong(activity, "删除分组失败。");
//                }
//                reloadView();
//                dialog.dismiss();
//            }
//        });
//        cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });

        View contentView = View.inflate(activity,R.layout.delete_layout,null);
        final BaseAlterDialogUtil baseDialog = new BaseAlterDialogUtil(activity);
        TextView title=(TextView)contentView.findViewById(R.id.delete_title);
        title.setText("删除分组：" + groupName);
        TextView positiveBtn=(TextView)contentView.findViewById(R.id.delete_dialog_sure);
        TextView negativeBtn=(TextView)contentView.findViewById(R.id.delete_dialog_cancel);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new Group(activity, equipmentBeans).deleteGroup(groupName);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showLong(activity, "删除分组失败。");
                }
                reloadView();
                baseDialog.dismiss();
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        });
        baseDialog.setLocation(Gravity.CENTER,0,0);
        baseDialog.setWidthAndHeightRadio(0.8f,0.22f);
        baseDialog.setContentView(contentView);
    }

    /**
     * 设置群组的对话框
     * @param equipmentCode
     */
    protected void showSetGroupDialog(final String equipmentCode) {
//        LinearLayout view=(LinearLayout) activity.getLayoutInflater().inflate(R.layout.add_to_group_list,null);
//        //dialog
//        final AlertDialog dialog=new AlertDialog.Builder(activity).
//                setView(view).
//                create();
//        dialog.show();
//        dialog.setCanceledOnTouchOutside(true);
//        //listview
//        ListView listView=(ListView)view.findViewById(R.id.add_to_group_dialog_list);
//        ArrayList<HashMap<String,Object>> data=new ArrayList<HashMap<String,Object>>();
//        final Group modelGroup = new Group(activity, equipmentBeans);
//        List<String> groups = modelGroup.getGroupNames();
//        String[] arrayGroups = new String[groups.size()];
//        for (int i = 0; i < groups.size(); ++i) {
//            HashMap<String,Object> map=new HashMap<String,Object>();
//            map.put("img",R.drawable.group);
//            map.put("group",groups.get(i));
//            data.add(map);
//        }
//        SimpleAdapter adapter=new SimpleAdapter(
//                activity,
//                data,
//                R.layout.add_to_group_dialog_list_item,
//                new String[]{"img","group"},
//                new int[]{R.id.add_to_group,R.id.add_to_group_dialog_title});
//        listView.setAdapter(adapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                try {
//                    new Group(activity, equipmentBeans).addChildToGroup(equipmentCode, modelGroup.getGroupNames().get(position));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    ToastUtil.showLong(activity, "设置分组失败。");
//                }
//                reloadView();
//                dialog.dismiss();
//            }
//        });

        View contentView = View.inflate(activity,R.layout.add_to_group_list,null);
        final BaseAlterDialogUtil baseDialog = new BaseAlterDialogUtil(activity);
        ListView lstView = (ListView) contentView.findViewById(R.id.add_to_group_dialog_list);
        ArrayList<HashMap<String,Object>> data=new ArrayList<HashMap<String,Object>>();
        final Group modelGroup = new Group(activity, equipmentBeans);
        List<String> groups = modelGroup.getGroupNames();
//        String[] arrayGroups = new String[groups.size()];
        for (int i = 0; i < groups.size(); ++i) {
            HashMap<String,Object> map=new HashMap<String,Object>();
            map.put("img",R.drawable.group);
            map.put("group",groups.get(i));
            data.add(map);
        }
        SimpleAdapter adapter=new SimpleAdapter(
                activity,
                data,
                R.layout.add_to_group_dialog_list_item,
                new String[]{"img","group"},
                new int[]{R.id.add_to_group_dialog_img,R.id.add_to_group_dialog_title});
        lstView.setAdapter(adapter);
        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    new Group(activity, equipmentBeans).addChildToGroup(equipmentCode, modelGroup.getGroupNames().get(position));
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showLong(activity, "设置分组失败。");
                }
                reloadView();
                baseDialog.dismiss();
            }
        });

        //item的高度为0.08或者0.07
        //title的高度为0.09

        //判断group.size（）大小，大于4个则固定大小
        if(groups.size()>=4){
            baseDialog.setWidthAndHeightRadio(0.8f,0.35f);
        }
        else {
            baseDialog.setWidthAndHeightRadio(0.8f, (float) (0.08f+groups.size()*0.07));
        }
        baseDialog.setLocation(Gravity.CENTER,0,0);
        baseDialog.setContentView(contentView);
    }

    protected void closeSearchLayout() {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        searchLayout.setVisibility(View.INVISIBLE);
    }

    protected void showSearchLayout() {
        searchEditText.requestFocus();
        searchLayout.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, 0);
    }



    protected void startBarcodeScanActivity() {
        //调用Zxing扫码框架
        Intent intent = new Intent(activity, com.zxing.activity.CaptureActivity.class);
        startActivityForResult(intent, GET_BARCODE);
    }
    //startActivityForResult(Intent intent,int requestCode)方法打开新的SecondActivity
    //SecondActivity关闭后会，会向前面的Activity回传数据。
    //在onActivityResult(int requestCode, int resultCode,Intent data)方法中，得到传回的数据
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != activity.RESULT_OK)
            return;
        switch (requestCode) {
            case GET_BARCODE:
                //扫二维码，获取Zxing扫码框架返回的结果，并上存到服务器
                submitBarcode(data.getExtras().getString("result"));
                //通过百度地图获得位置并发送到服务器
                getLocationAndSendToServer(data.getExtras().getString("result"));
                break;
            default:
                break;
        }
    }

    protected class SearchListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        //s改变后的String
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                List<EquipmentBean> equipmentBeanList = new ArrayList<>();
                for (int i = 0; i < equipmentBeans.size(); ++i) {
                    String equipmentCode = equipmentBeans.get(i).code;
                    if (Equipment.getEquipmentName(activity, equipmentCode).contains(s) || equipmentCode.contains(s)) {
                        equipmentBeanList.add(equipmentBeans.get(i));
                    }
                }
                searchListAdapter.setEquipmentBeanList(equipmentBeanList);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    protected class TitleBarButtonOnClickedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //搜索
                case R.id.equipment_list_search_button:
                    showSearchLayout();
                    break;
                //+
                case R.id.equipment_list_add_button:
                    addPopupWin();
                    break;
                case R.id.equipment_list_search_back:
                    closeSearchLayout();
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 点击“+”弹出popupWindow
     */
    private void addPopupWin(){
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.fragment_equipment_list_title_add, null);
        popupWindow = new PopupWindow();
        popupWindow.setContentView(view);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 需要设置一下此参数，点击外边可消失
        popupWindow.setBackgroundDrawable(new ColorDrawable(
                android.graphics.Color.BLACK));
        // 设置点击窗口外边窗口消失
        popupWindow.setOutsideTouchable(true);
        // 设置此参数获得焦点，否则无法点击
        popupWindow.setFocusable(true);
        // popupWindow.showAtLocation(findViewById(R.id.manufacturer_register_typeBut),
        // Gravity.LEFT|Gravity.BOTTOM, 0, 0);
        //设置popupWindow显示的位置，依靠在addButton下面
        popupWindow.showAsDropDown(addButton,0,10);
        RelativeLayout scan=(RelativeLayout)view.findViewById(R.id.scan_device);
        RelativeLayout add=(RelativeLayout)view.findViewById(R.id.add_group);
        scan.setOnClickListener(new MyAddClickListener());
        add.setOnClickListener(new MyAddClickListener());
    }
    private class MyAddClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int op=v.getId();
            switch (op){
                case R.id.add_group:
                    popupWindow.dismiss();
                    showAddGroupDialog();
                    break;
                case R.id.scan_device:
                    popupWindow.dismiss();
                    startBarcodeScanActivity();
                    break;
            }
        }
    }
    /*Event bus接收函数*/
    //onEvent:如果使用onEvent作为订阅函数，那么该事件在哪个线程发布出来的，
    //onEvent就会在这个线程中运行，也就是说发布事件和接收事件线程在同一个线程。
    // 使用这个方法时，在onEvent方法中不能执行耗时操作，如果执行耗时操作容易导致事件分发延迟。

    //onEventMainThread:如果使用onEventMainThread作为订阅函数，那么不论事件是在哪个线程中发布出来的，
    // onEventMainThread都会在UI线程中执行，接收事件就会在UI线程中运行，这个在Android中是非常有用的，
    // 因为在Android中只能在UI线程中跟新UI，所以在onEvnetMainThread方法中是不能执行耗时操作的。

    //onEventBackground:如果使用onEventBackgrond作为订阅函数，那么如果事件是在UI线程中发布出来的，
    // 那么onEventBackground就会在子线程中运行，如果事件本来就是子线程中发布出来的，
    // 那么onEventBackground函数直接在该子线程中执行。

    //onEventAsync：使用这个函数作为订阅函数，那么无论事件在哪个线程发布，
    // 都会创建新的子线程在执行onEventAsync.
    protected class EventHandler {
        //长按分组标题实现重命名与删除功能
        public void onEvent(final EquipmentGroupLongClickedEvent event) {
            final PopupMenu groupMenu = new PopupMenu(activity, event.view);
            groupMenu.getMenuInflater().inflate(R.menu.equipment_group, groupMenu.getMenu());
            groupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.equipment_group_rename:
                            showRenameGroupDailog(event.groupName);
                            break;
                        case R.id.equipment_group_delete:
                            showDeleteGroupConfirmDialog(event.groupName);
                            break;
                    }
                    return true;
                }
            });
            groupMenu.show();
        }
        //搜索选择的其中一台设备后会转到主界面
        //expendablelistview 和搜索的listview都会触发这个事件
        public void onEvent(EquipmentSelectedEvent event) {
            closeSearchLayout();
            ((MainActivityNew) activity).pressPlantBtn();
            Group group=new Group(activity,equipmentBeans);
            group.addLatestUse(event.equipmentCode,event.equipmentGroup);
            refreshView(equipmentBeans);
        }
        //设置事件，命名设备，设置群组，删除设备,置顶
        public void onEvent(final EquipmentItemConfigureClickedEvent event) {
            final PopupMenu equipmentConfigMenu = new PopupMenu(activity, event.view);
            final Group group=new Group(activity,equipmentBeans);
            TopBean topBean=group.getTopBean();
            final String code=event.equipmentCode;
            if(topBean.top.contains(code))
            {
                equipmentConfigMenu.getMenuInflater().inflate(R.menu.equipment_configure_new,
                        equipmentConfigMenu.getMenu());
                equipmentConfigMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.equipment_menu_item_rename_new:
                                showRenameEquipmentDialog(code);
                                break;
                            case R.id.equipment_menu_item_set_group_new:
                                showSetGroupDialog(code);
                                break;
                            case R.id.equipment_menu_item_delete_new:
                                showDeleteEquipmentConfirmDialog(code);
                                break;
                            case R.id.equipment_menu_item_cancel_top:
                                group.cancelTop(code);
                                refreshView(equipmentBeans);
                        }
                        return false;
                    }
                });
            }
            else
            {
                equipmentConfigMenu.getMenuInflater().inflate(R.menu.equipment_configure,
                        equipmentConfigMenu.getMenu());
                equipmentConfigMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.equipment_menu_item_rename:
                                showRenameEquipmentDialog(code);
                                break;
                            case R.id.equipment_menu_item_set_group:
                                showSetGroupDialog(code);
                                break;
                            case R.id.equipment_menu_item_delete:
                                showDeleteEquipmentConfirmDialog(code);
                                break;
                            case R.id.equipment_menu_item_add_top:
                                group.addTop(code);
                                refreshView(equipmentBeans);
                        }
                        return false;
                    }
                });

            }
            equipmentConfigMenu.show();
        }
        //GPS事件
        public void onEvent(EquipmentItemGpsClickedEvent event) {
            final ArrayList<String> equipmentCodeList = new ArrayList<>();
            equipmentCodeList.add(event.equipmentCode);
            gps.getLocationObservable(equipmentCodeList, new UploadAndDownloadGPS.GPSLisaner() {
                @Override
                public void uploadGPSsuccess(Map<String, ArrayList<String>> result) {
                    if (result == null) {
                        ToastUtil.showShort(activity, "当前设备没有坐标值");
                        return;
                    }
                    Intent intent = new Intent(activity, LocationActivity.class);
                    intent.putStringArrayListExtra("equipmentCode", equipmentCodeList);
                    intent.putStringArrayListExtra("longitude", result.get("longitude"));
                    intent.putStringArrayListExtra("latitude", result.get("latitude"));
                    System.out.println(equipmentCodeList.get(0) + "/" + result.get("longitude").get(0) + "~" + result.get("latitude").get(0));
                    startActivity(intent);
                }

                @Override
                public void uploadGPSfailure() {
                    ToastUtil.showShort(activity, "获取GPS失败");
                }

                @Override
                public void uploadGPSError() {
                    ToastUtil.showShort(activity, "获取GPS出错");
                }
            });
        }

        public void onEvent(EquipmentImageEvent event){
            refreshView(equipmentBeans);
        }
    }
}
