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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.schedulers.Schedulers;

public class EquipmentListFragment extends BaseFragment {
    public static final int GET_BARCODE = 0;

    protected Activity activity;

    protected View rootView;
    //�豸�б���list
    protected ExpandableListView expandableListView;
    protected ExpandableListAdapter expandableListAdapter;
    protected BaseProgressDialog baseProgressDialog;
    protected ImageView searchButton;
    protected ImageView addButton;
    protected LinearLayout searchLayout;
    //���������list
    protected ListView searchResultList;
    protected SearchListAdapter searchListAdapter;
    protected EditText searchEditText;
    protected List<EquipmentBean> equipmentBeans;
    private PopupWindow popupWindow;
    private ImageView back;
    protected EventHandler eventHandler;

    private UploadAndDownloadGPS gps = new UploadAndDownloadGPS();
    //Fragment��Activity��������ʱ����
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }
    //ϵͳ����Fragment�����ص��÷�������ʵ�ִ�����ֻ��ʼ����Ҫ��Fragment�б��ֵı�Ҫ�����
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //�ڽ�����Ϣ��ҳ�棬ע��
        registerEvent();
    }

    //������Fragment��view
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_equipment_list, container, false);
        return rootView;
    }
    //��Activity��onCreate��������ʱ����
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
        //���������¼�
        TitleBarButtonOnClickedListener titleBarButtonOnClickedListener = new TitleBarButtonOnClickedListener();
        baseProgressDialog.setMessage("���Ժ�");
        searchListAdapter = new SearchListAdapter(activity);
        //���������list
        searchResultList.setAdapter(searchListAdapter);
        //����
        searchButton.setOnClickListener(titleBarButtonOnClickedListener);
        //����
        addButton.setOnClickListener(titleBarButtonOnClickedListener);
        //����
        back.setOnClickListener(titleBarButtonOnClickedListener);
        //ģ������
        searchEditText.addTextChangedListener(new SearchListener());
        //�༭��֮�����������ϵĻس����Żᴥ����EditText�̳�TextView
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
                        ToastUtil.showLong(activity, "�����쳣�������Ժ���ʽ��");
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
                        ToastUtil.showLong(activity, "���ӷ����������쳣�������Ժ���ʽ��");
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
                            ToastUtil.showLong(activity, "���ӷ����������쳣�������Ժ���ʽ��");
                        } else {
                            ToastUtil.showLong(activity, "ɾ���豸�ɹ���");
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
                        ToastUtil.showLong(activity, "���ӷ����������쳣�������Ժ���ʽ��");
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
                            ToastUtil.showLong(activity, "���ӷ����������쳣�������Ժ���ʽ��");
                        } else {
                            ToastUtil.showLong(activity, "ɨ��ɹ���");
                        }
                    }
                });
            }
        });
    }

    private void getLocationAndSendToServer(String scanResult){
        //����LocationClient
        //getApplicationContext() ����Ӧ�õ������ģ���������������Ӧ�ã�Ӧ�ôݻ����Ŵݻ�
        //Activity.this��context ���ص�ǰactivity�������ģ�����activity ��activity �ݻ����ʹݻ�
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
                .setMessage("��û�а��豸���������Ͻ�ɨ�������豸")
                .setPositiveButton("�õ�",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        })
                .setNegativeButton("�˳�",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                activity.finish();
                            }
                        });
        builder.create().show();
    }

    protected void showRenameEquipmentDialog(final String equipmentCode) {
        LinearLayout view = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.equipment_add_group_content_view, null);
        final EditText renameEditText = (EditText) view.findViewById(R.id.equipment_add_group_edittext);
        RelativeLayout sure=(RelativeLayout)view.findViewById(R.id.add_group_dialog_sure);
        RelativeLayout cancel=(RelativeLayout)view.findViewById(R.id.add_group_dialog_cancel);
        TextView title=(TextView)view.findViewById(R.id.add_group_dialog_title);
        ImageView img=(ImageView)view.findViewById(R.id.add_group_dialog_title_img);
        img.setImageResource(R.drawable.rename_group_title);
        final String name=Equipment.getEquipmentName(activity, equipmentCode);
        title.setText("�豸:" + name);
        renameEditText.setText(name);
        //���ù��λ��
        renameEditText.setSelection(renameEditText.getText().length());
        final AlertDialog dialog= new AlertDialog.Builder(activity).
                setView(view).
                create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renameEditText.length()>0)
                {
                    if(!renameEditText.getText().toString().equals(name))
                    {
                        Equipment.setEquipmentName(activity, equipmentCode, renameEditText.getText().toString());
                        reloadView();
                        dialog.dismiss();
                    }
                    dialog.dismiss();
                }
                else
                {
                    ToastUtil.showShort(activity,"�������豸�µ�����");
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    protected void showAddGroupDialog() {
        LinearLayout view = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.equipment_add_group_content_view, null);
        final EditText renameEditText = (EditText) view.findViewById(R.id.equipment_add_group_edittext);
        RelativeLayout sure=(RelativeLayout)view.findViewById(R.id.add_group_dialog_sure);
        RelativeLayout cancel=(RelativeLayout)view.findViewById(R.id.add_group_dialog_cancel);
        final AlertDialog dialog= new AlertDialog.Builder(activity).
                setView(view).
                create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renameEditText.length()>0)
                {
                    try {
                        new Group(activity, equipmentBeans).addGroup(renameEditText.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showLong(activity, "��������ʧ�ܡ�");
                    }
                    reloadView();
                    dialog.dismiss();
                }
                else
                {
                    ToastUtil.showShort(activity,"��������������");
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    protected void showRenameGroupDailog(final String groupName) {
        LinearLayout view = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.equipment_add_group_content_view, null);
        final EditText renameEditText = (EditText) view.findViewById(R.id.equipment_add_group_edittext);
        RelativeLayout sure=(RelativeLayout)view.findViewById(R.id.add_group_dialog_sure);
        RelativeLayout cancel=(RelativeLayout)view.findViewById(R.id.add_group_dialog_cancel);
        TextView title=(TextView)view.findViewById(R.id.add_group_dialog_title);
        ImageView img=(ImageView)view.findViewById(R.id.add_group_dialog_title_img);
        img.setImageResource(R.drawable.rename_group_title);
        title.setText("����:" + groupName);
        renameEditText.setText(groupName);
        //���ù��λ��
        renameEditText.setSelection(renameEditText.getText().length());
        final AlertDialog dialog= new AlertDialog.Builder(activity).
                setView(view).
                create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
        sure.setOnClickListener(new View.OnClickListener() {
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
                            ToastUtil.showLong(activity, "����������ʧ�ܡ�");
                        }
                        reloadView();
                        dialog.dismiss();
                    }
                    dialog.dismiss();
                }
                else
                {
                    ToastUtil.showShort(activity,"����������µ�����");
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    protected void showDeleteEquipmentConfirmDialog(final String equipmentCode) {
        LinearLayout view=(LinearLayout)activity.getLayoutInflater().inflate(R.layout.delete_layout,null);
        RelativeLayout sure=(RelativeLayout)view.findViewById(R.id.delete_dialog_sure);
        RelativeLayout cancel=(RelativeLayout)view.findViewById(R.id.delete_dialog_cancel);
        TextView title=(TextView)view.findViewById(R.id.delete_title);
        title.setText("ȷ��ɾ���豸��" + Equipment.getEquipmentName(activity, equipmentCode) + "?");
        final AlertDialog dialog=new AlertDialog.Builder(activity).
                setView(view).
                create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Group(activity,equipmentBeans).deleteLatestUse(equipmentCode);
                deleteEquipment(equipmentCode);
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    protected void showDeleteGroupConfirmDialog(final String groupName) {
        LinearLayout view=(LinearLayout)activity.getLayoutInflater().inflate(R.layout.delete_layout,null);
        RelativeLayout sure=(RelativeLayout)view.findViewById(R.id.delete_dialog_sure);
        RelativeLayout cancel=(RelativeLayout)view.findViewById(R.id.delete_dialog_cancel);
        TextView title=(TextView)view.findViewById(R.id.delete_title);
        title.setText("ȷ��ɾ�����飺" + groupName + "?");
        final AlertDialog dialog=new AlertDialog.Builder(activity).
                setView(view).
                create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new Group(activity, equipmentBeans).deleteGroup(groupName);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showLong(activity, "ɾ������ʧ�ܡ�");
                }
                reloadView();
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    protected void showSetGroupDialog(final String equipmentCode) {
        LinearLayout view=(LinearLayout) activity.getLayoutInflater().inflate(R.layout.add_to_group_list,null);
        //dialog
        final AlertDialog dialog=new AlertDialog.Builder(activity).
                setView(view).
                create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
        //listview
        ListView listView=(ListView)view.findViewById(R.id.add_to_group_dialog_list);
        ArrayList<HashMap<String,Object>> data=new ArrayList<HashMap<String,Object>>();
        final Group modelGroup = new Group(activity, equipmentBeans);
        List<String> groups = modelGroup.getGroupNames();
        String[] arrayGroups = new String[groups.size()];
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
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    new Group(activity, equipmentBeans).addChildToGroup(equipmentCode, modelGroup.getGroupNames().get(position));
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showLong(activity, "���÷���ʧ�ܡ�");
                }
                reloadView();
                dialog.dismiss();
            }
        });
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
        //����Zxingɨ����
        Intent intent = new Intent(activity, com.zxing.activity.CaptureActivity.class);
        startActivityForResult(intent, GET_BARCODE);
    }
    //startActivityForResult(Intent intent,int requestCode)�������µ�SecondActivity
    //SecondActivity�رպ�ᣬ����ǰ���Activity�ش����ݡ�
    //��onActivityResult(int requestCode, int resultCode,Intent data)�����У��õ����ص�����
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != activity.RESULT_OK)
            return;
        switch (requestCode) {
            case GET_BARCODE:
                //ɨ��ά�룬��ȡZxingɨ���ܷ��صĽ�������ϴ浽������
                submitBarcode(data.getExtras().getString("result"));
                //ͨ���ٶȵ�ͼ���λ�ò����͵�������
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
        //s�ı���String
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
                //����
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
    private void addPopupWin(){
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.fragment_equipment_list_title_add, null);
        popupWindow = new PopupWindow();
        popupWindow.setContentView(view);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // ��Ҫ����һ�´˲����������߿���ʧ
        popupWindow.setBackgroundDrawable(new ColorDrawable(
                android.graphics.Color.BLACK));
        // ���õ��������ߴ�����ʧ
        popupWindow.setOutsideTouchable(true);
        // ���ô˲�����ý��㣬�����޷����
        popupWindow.setFocusable(true);
        // popupWindow.showAtLocation(findViewById(R.id.manufacturer_register_typeBut),
        // Gravity.LEFT|Gravity.BOTTOM, 0, 0);
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
    /*Event bus���պ���*/
    //onEvent:���ʹ��onEvent��Ϊ���ĺ�������ô���¼����ĸ��̷߳��������ģ�
    //onEvent�ͻ�������߳������У�Ҳ����˵�����¼��ͽ����¼��߳���ͬһ���̡߳�
    // ʹ���������ʱ����onEvent�����в���ִ�к�ʱ���������ִ�к�ʱ�������׵����¼��ַ��ӳ١�

    //onEventMainThread:���ʹ��onEventMainThread��Ϊ���ĺ�������ô�����¼������ĸ��߳��з��������ģ�
    // onEventMainThread������UI�߳���ִ�У������¼��ͻ���UI�߳������У������Android���Ƿǳ����õģ�
    // ��Ϊ��Android��ֻ����UI�߳��и���UI��������onEvnetMainThread�������ǲ���ִ�к�ʱ�����ġ�

    //onEventBackground:���ʹ��onEventBackgrond��Ϊ���ĺ�������ô����¼�����UI�߳��з��������ģ�
    // ��ôonEventBackground�ͻ������߳������У�����¼������������߳��з��������ģ�
    // ��ôonEventBackground����ֱ���ڸ����߳���ִ�С�

    //onEventAsync��ʹ�����������Ϊ���ĺ�������ô�����¼����ĸ��̷߳�����
    // ���ᴴ���µ����߳���ִ��onEventAsync.
    protected class EventHandler {
        //�����������ʵ����������ɾ������
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
        //����ѡ�������һ̨�豸���ת��������
        //expendablelistview ��������listview���ᴥ������¼�
        public void onEvent(EquipmentSelectedEvent event) {
            closeSearchLayout();
            ((MainActivityNew) activity).pressPlantBtn();
            Group group=new Group(activity,equipmentBeans);
            group.addLatestUse(event.equipmentCode,event.equipmentGroup);
            refreshView(equipmentBeans);
        }
        //�����¼��������豸������Ⱥ�飬ɾ���豸,�ö�
        public void onEvent(final EquipmentItemConfigureClickedEvent event) {
            final PopupMenu equipmentConfigMenu = new PopupMenu(activity, event.view);
            equipmentConfigMenu.getMenuInflater().inflate(R.menu.equipment_configure, equipmentConfigMenu.getMenu());
            equipmentConfigMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.equipment_menu_item_rename:
                            showRenameEquipmentDialog(event.equipmentCode);
                            break;
                        case R.id.equipment_menu_item_set_group:
                            showSetGroupDialog(event.equipmentCode);
                            break;
                        case R.id.equipment_menu_item_delete:
                            showDeleteEquipmentConfirmDialog(event.equipmentCode);
                            break;
                    }
                    return true;
                }
            });
            equipmentConfigMenu.show();
        }
        //GPS�¼�
        public void onEvent(EquipmentItemGpsClickedEvent event) {
            final ArrayList<String> equipmentCodeList = new ArrayList<>();
            equipmentCodeList.add(event.equipmentCode);
            gps.getLocationObservable(equipmentCodeList, new UploadAndDownloadGPS.GPSLisaner() {
                @Override
                public void uploadGPSsuccess(Map<String, ArrayList<String>> result) {
                    if (result == null) {
                        ToastUtil.showShort(activity, "��ǰ�豸û������ֵ");
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
                    ToastUtil.showShort(activity, "��ȡGPSʧ��");
                }

                @Override
                public void uploadGPSError() {
                    ToastUtil.showShort(activity, "��ȡGPS����");
                }
            });
        }

        public void onEvent(EquipmentImageEvent event){
            refreshView(equipmentBeans);
        }
    }
}