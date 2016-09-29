package com.smartfarm.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.smartfarm.activity.MainActivityNew;
import com.smartfarm.activity.R;
import com.smartfarm.bean.ConfigBean;
import com.smartfarm.bean.RealtimeDataBean;
import com.smartfarm.dialog.BaseAlterDialogUtil;
import com.smartfarm.event.EquipmentImageEvent;
import com.smartfarm.event.EquipmentSelectedEvent;
import com.smartfarm.event.GlobalEvent;
import com.smartfarm.event.OverviewSensorCodeEvent;
import com.smartfarm.model.Equipment;
import com.smartfarm.observable.GetConfigObservable;
import com.smartfarm.observable.RealtimeDataObservable;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.Common;
import com.smartfarm.util.Config;
import com.smartfarm.util.DateUtil;
import com.smartfarm.util.IntentUtil;
import com.smartfarm.util.ToastUtil;
import com.videogo.openapi.EZOpenSDK;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.baidu.location.b.g.T;

public class OverviewFragment extends BaseFragment {
    protected Activity activity = null;

    // 用户数据
    protected String selectedEquipmentCode;

    // 界面数据
    protected TableLayout buttonGroup;
    protected TextView equipmentNameText;
    //下拉刷新
    protected SwipeRefreshLayout swipeRefreshLayout;
    //加载图片
    private SimpleDraweeView userPicture;
    protected ImageView userPhoto;
    protected ImageView realTimeBtn;

    protected EventHandler eventHandler;
    //图片保存的文件夹
    private static final String APP_FOLDER_NAME = "/SmartFarm/";
    protected void findView() {
        buttonGroup = (TableLayout) activity
                .findViewById(R.id.overview_btn_group);
        equipmentNameText = (TextView) activity
                .findViewById(R.id.overview_equiment_name);
        swipeRefreshLayout = (SwipeRefreshLayout) activity
                .findViewById(R.id.overview_pull_to_refresh);
        userPicture = (SimpleDraweeView) activity.findViewById(R.id.overview_image);
        userPhoto = (ImageView) activity.findViewById(R.id.overview_photo);
        realTimeBtn = (ImageView) activity.findViewById(R.id.overview_realtime);
    }

    protected void setUpView() {
        //下拉刷新
        swipeRefreshLayout.setOnRefreshListener(new PullToRefreshListener());
        //设置进度动画的颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.blue_A700,
                R.color.blue_A400, R.color.blue_A200, R.color.blue_A100);
        //换照片
        userPhoto.setOnClickListener(new OnCameraButtonClickedListener());
        //监控
        realTimeBtn.setOnClickListener(new OnRealTimeClickedListener());
    }

    protected void registerEvent() {
        eventHandler = new EventHandler();
        GlobalEvent.bus.register(eventHandler);
    }

    protected void unregisterEvent() {
        GlobalEvent.bus.unregister(eventHandler);
    }

    protected void refreshView(List<RealtimeDataBean> beans) {
        Log.d("gzfuzhi", "Start refresh view.");
        // 刷新设备名字
        loadEquipmentName();

        // 刷新设备图像
        loadEquipmentImage();

        // 刷新实时数据
        buttonGroup.removeAllViews();
        //传感器事件
        OnClickListener listener = new OnSensorClickedListener();


        for (int i = 0; i < beans.size(); i++) {
            if (beans.get(i).getType().equals("GPS")){
                beans.remove(i);
            }
        }
        //行
        TableRow row = null;
        for (int i = 0; i < beans.size(); ++i) {
            if (i % 3 == 0) {
                row = new TableRow(activity);
                LayoutParams param = new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(param);
            }
            View overviewItem = activity.getLayoutInflater().inflate(
                    R.layout.overview_item, row, false);
            ImageView iconIV = (ImageView) overviewItem
                    .findViewById(R.id.overview_btn_icon);
            TextView valueNameTV = (TextView) overviewItem
                    .findViewById(R.id.overview_value_name);
            TextView valueTV = (TextView) overviewItem
                    .findViewById(R.id.overview_value);
            TextView valueUnitTV = (TextView) overviewItem
                    .findViewById(R.id.overview_value_unit);
            TextView valueDate = (TextView) overviewItem
                    .findViewById(R.id.overview_value_date);
            RealtimeDataBean bean = beans.get(i);
            String name = bean.getName();
            String value = bean.getData().get(0);
            String unit = bean.getUnit();
            String type = bean.getType();
            String updateDate = bean.getTime().get(0);
            try {
                Date d = DateUtil.dateFormat.parse(updateDate);
                SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.CHINA);
                updateDate = format.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
                updateDate = "";
            }
            if (bean.getIsAlarm().get(0) == 1) {
                valueTV.setTextColor(getResources().getColor(R.color.red_2));
            } else {
                valueTV.setTextColor(getResources().getColor(R.color.green_2));
            }
            valueNameTV.setText(cutSensorName(name));
            valueTV.setText(value);
            valueUnitTV.setText(unit);
            setIconWithType(iconIV, type);
            valueDate.setText(updateDate);
            row.addView(overviewItem);
            overviewItem.setTag(i);
            //画图事件，温度，光强，湿度等等
            overviewItem.setOnClickListener(listener);
            if (i % 3 == 0) {
                buttonGroup.addView(row);
            }
        }
        //占位符，这里好似有点问题，多了占位符，删去好似也没什么影响
        for (int i = 0; i < beans.size() % 3; ++i) {
            View overviewItem = activity.getLayoutInflater().inflate(
                    R.layout.overview_item_null, buttonGroup, false);
            row.addView(overviewItem);
        }
        //历史数据需要获取sensorcode和time
        //获取名字，湿度，光强，温度等等
        ArrayList<String> names = new ArrayList<>();
        for (int z = 0; z < beans.size(); ++z) {
            names.add(cutSensorName(beans.get(z).getName()));
        }
        GlobalEvent.bus.post(new OverviewSensorCodeEvent(names, selectedEquipmentCode, getSensorCode(beans),getNewTime(beans)));
    }
    //传感器的编号
    private ArrayList<String> getSensorCode(List<RealtimeDataBean> beans) {
        ArrayList<String> sensorCodes = new ArrayList<>();
        for (int i = 0; i < beans.size(); ++i) {
            sensorCodes.add(beans.get(i).getEquipmentCode());
        }
        return sensorCodes;
    }
    //获取时间
    private ArrayList<String> getNewTime(List<RealtimeDataBean> beans) {
        ArrayList<String> historyDate = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < beans.size(); ++i) {
            try {
                Date date = DateUtil.dateFormat.parse(beans.get(i)
                        .getTime().get(0));
                historyDate.add(format.format(date));
            } catch (Exception e) {
                historyDate.add("");
                e.printStackTrace();
            }
        }
        return historyDate;
    }

    protected void reloadView() {
        if (selectedEquipmentCode != null) {
            RealtimeDataObservable.createObservable(Common.token, selectedEquipmentCode)
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new ProgressSubscriber(new OnNextListener() {
                        @Override
                        public void onNext(Object obj) {
                            refreshView((List<RealtimeDataBean>) obj);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }));
        }
    }

    protected boolean hasCamera() {
        PackageManager pm = activity.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                && !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Toast.makeText(activity, "没有摄像头", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    //获取图片的名字
    protected String getEquipmentImageName() {
        Config config = new Config(activity);
        return config.getUsername()
                + selectedEquipmentCode + ".jpeg";
    }
    //获取图片的文件
    protected File getEquipmentImageFile() throws Exception {
        File file=new File(Environment.getExternalStorageDirectory(), APP_FOLDER_NAME+getEquipmentImageName());
        if (!file.getParentFile().exists())
        {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    public void loadEquipmentName() {
        equipmentNameText.setText(Equipment.getEquipmentName(activity, selectedEquipmentCode));
    }
    //刷新照片
    public void loadEquipmentImage() {
        try {
            File equimentImageFile = getEquipmentImageFile();
            Uri uri = Uri.fromFile(equimentImageFile);
            if (!equimentImageFile.exists()) {
                uri = Uri.parse("res://com.smartfarm.activity/" + R.drawable.overview_image);
            }

//            //清楚缓存
//            ImagePipeline imagePipeline = Fresco.getImagePipeline();
//            imagePipeline.clearCaches();

            int width = 512, height = 182;
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setAutoRotateEnabled(true)
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
            PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                    .setOldController(userPicture.getController())
                    .setImageRequest(request)
                    .build();
            userPicture.setController(controller);
//            if (imageChanged == true){  //重新选择图片
//                GlobalEvent.bus.post(new EquipmentImageEvent());
//                imageChanged = false;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //本地加载图片时出错，创建不了文件夹
    public void loadPictureFromGallery(Intent data) {

        if (data != null)
        {
            Uri uri = data.getData();
            String picturePath;
            if (!TextUtils.isEmpty(uri.getAuthority()))
            {
                String[] filePathColumn = {MediaColumns.DATA};
                // 用cursor进行数据检索，保存返回的结果
                Cursor cursor = activity.getContentResolver().query(uri,
                        filePathColumn, null, null, null);
                if (null == cursor)
                {
                    ToastUtil.showShort(activity,"图片没找到");
                    return;
                }
                //移动光标到第一行，这个很重要，不小心很容易引起越界
                cursor.moveToFirst();
                //获得用户选择的图片的索引值
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                //获得图片的路径
                picturePath= cursor.getString(columnIndex);
                cursor.close();
            }
            else
            {
                picturePath = uri.getPath();
            }
            try
            {
                File img = new File(picturePath);
                //BufferedInputStream是带缓冲区的输入流，默认缓冲区大小是8M，能够减少访问磁盘的次数，提高文件读取性能；
                BufferedInputStream bin = new BufferedInputStream(
                        new FileInputStream(img));
                //与拍照保存的图片名字一样，覆盖原来的图片
                //用FilePathManager创建不了文件夹
                //改用以下方式可以创建
                File file=getEquipmentImageFile();
                //BufferedOutputStream是带缓冲区的输出流，能够提高文件的写入效率。
                BufferedOutputStream bout = new BufferedOutputStream(
                        new FileOutputStream(file));


                int c = bin.read();
                while (c != -1) {
                    bout.write(c);
                    c = bin.read();
                }
                bin.close();
                bout.close();
//                imageChanged = true;
                imageChange();
                loadEquipmentImage();
                System.out.println("没错");
            }
            catch (Exception e)
            {
            System.out.println("出错"+e.getMessage());
            e.printStackTrace();
            }
        }
        else
        {
            ToastUtil.showShort(activity,"图片没找到");
            return;
        }

    }

    protected void takePicture() {
        if (hasCamera() == false) {
            return;
        }
        try {
            //用FilePathManager创建不了文件夹
            //改用以下方式可以创建
            File file=getEquipmentImageFile();
            // 构造一个Intent对象，并将这个Intent的action指定为android.media.action.IMAGE_CAPTURE
            //再调用Intent的putExtra()方法指定图片的输出地址，这里填入刚刚得到的Uri对象，
            // 最后调用startActivityForResult()来启动活动。
            // 由于使用的是一个隐式Intent，系统会找出能够响应这个Intent的活动去启动，
            // 这样照相机程序就会被打开，拍下照片就会输出到out_image.jpg中。
            Intent localIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            localIntent.putExtra("output", Uri.fromFile(file));
            imageChange();
            activity.startActivityForResult(localIntent,
                    IntentUtil.CAMERA_WITH_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void imageChange(){
        //清楚缓存
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.clearCaches();
        //通知列表改变事件
        GlobalEvent.bus.post(new EquipmentImageEvent());
    }
    //根据类型设置图片，如类型：温度，图片就设为温度的图片
    protected void setIconWithType(ImageView iconIV, String type) {
        if (type.matches("^温度.*")) {
            iconIV.setImageResource(R.drawable.icon_wendu);
        } else if (type.matches("^湿度.*")) {
            iconIV.setImageResource(R.drawable.icon_shidu);
        } else if (type.matches("^光强.*")) {
            iconIV.setImageResource(R.drawable.icon_light);
        } else if (type.matches("^二氧化碳.*")) {
            iconIV.setImageResource(R.drawable.icon_co2);
        } else if (type.matches("^PH.*")) {
            iconIV.setImageResource(R.drawable.icon_ph);
        } else if (type.matches("^土壤酸碱.*")) {
            iconIV.setImageResource(R.drawable.icon_ph);
        } else if (type.matches("^EC.*")) {
            iconIV.setImageResource(R.drawable.icon_ec);
        } else if (type.matches("^空气温度.*")) {
            iconIV.setImageResource(R.drawable.icon_wendu);
        } else if (type.matches("^空气湿度.*")) {
            iconIV.setImageResource(R.drawable.icon_shidu);
        } else if (type.matches("^土壤温度.*")) {
            iconIV.setImageResource(R.drawable.icon_wendu);
        } else if (type.matches("^土壤湿度.*")) {
            iconIV.setImageResource(R.drawable.icon_shidu);
        } else {
            iconIV.setImageResource(R.drawable.icon_wendu);
        }
    }
    //获得传感器的名字，如温度
    protected String cutSensorName(String name) {
        //正则表达式，(.*), .匹配除“\r\n”之外的任何单个字符,*匹配前面的子表达式任意次
        Pattern p = Pattern.compile("(.*)传感器（(.*)）");
        Matcher m = p.matcher(name);
        boolean isMatched = m.matches();
        if (isMatched) {
            //group（0）就是指的整个串，
            // group（1） 指的是第一个括号里的东西，
            // group（2）指的第二个括号里的东西
            return m.group(1);
        } else {
            return "名字有误";
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_overview, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView();
        setUpView();
        registerEvent();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
        unregisterEvent();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("gzfuzhi", "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("gzfuzhi", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("gzfuzhi", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("gzfuzhi", "onStop");
    }

    protected void showTakePictureDialog() {
        View contentView = View.inflate(activity,R.layout.select_photo_dialog_layout,null);
        final BaseAlterDialogUtil baseDialog = new BaseAlterDialogUtil(activity);
        TextView tvGallery = (TextView) contentView.findViewById(R.id.id_dialog_gallery);
        TextView tvTakePhoto = (TextView) contentView.findViewById(R.id.id_dialog_takePhoto);
        tvGallery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //打开图片
                    Intent gallery = new Intent(
                            Intent.ACTION_PICK);
                    gallery.setType("image/*");
                    activity.startActivityForResult(
                            gallery,
                            IntentUtil.PICTURE_FROM_GALLERY);
                    baseDialog.dismiss();
                } catch (Exception e) {
                    ToastUtil.showLong(activity, "打开图库失败");
                    baseDialog.dismiss();

                }
            }
        });
        tvTakePhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //打开摄像头
                    takePicture();
                    baseDialog.dismiss();

                } catch (Exception e) {
                    ToastUtil.showLong(activity, "打开摄像头失败");
                    baseDialog.dismiss();

                }
            }
        });
        baseDialog
                .setLocation(Gravity.CENTER,0,0)
                .setWidthAndHeightRadio(0.8f,0)
                .setContentView(contentView);
    }

    protected class OnCameraButtonClickedListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            showTakePictureDialog();
        }
    }

    protected class OnSensorClickedListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Integer index = (Integer) v.getTag();
            ((MainActivityNew) activity).pressChartBtn(index);
        }
    }
    //监控
    protected class OnRealTimeClickedListener implements OnClickListener {
        @Override
        public void onClick(View v) {
//            Intent intent = new Intent(activity, VideoActivity.class);
//            startActivity(intent);
            EZOpenSDK.getInstance().openLoginPage();
        }
    }

    protected class PullToRefreshListener implements OnRefreshListener {
        @Override
        public void onRefresh() {
            if (selectedEquipmentCode != null) {
                RealtimeDataObservable.createObservable(Common.token, selectedEquipmentCode)
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(new ProgressSubscriber(new OnNextListener() {
                            @Override
                            public void onNext(Object obj) {
                                refreshView((List<RealtimeDataBean>) obj);
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }));
            }
        }
    }

    protected class ProgressSubscriber extends Subscriber<Object> implements
            OnCancelListener {
        protected BaseProgressDialog progressDialog;
        protected OnNextListener onNextListener;

        public ProgressSubscriber(OnNextListener listener) {
            progressDialog = new BaseProgressDialog(activity);
            progressDialog.setOnCancelListener(this);
            onNextListener = listener;
        }

        @Override
        public void onStart() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
        }

        @Override
        public void onCompleted() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });
        }

        @Override
        public void onError(final Throwable e) {
            Log.d("gzfuzhi", e.toString());
            e.printStackTrace();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showLong(activity, e.toString());
                    progressDialog.dismiss();
                    AlertDialog.Builder builder = new Builder(activity);
                    builder.setCancelable(false)
                            .setMessage("问题出现了，请重试一下")
                            .setPositiveButton("重试",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            // TODO:
                                        }
                                    })
                            .setNegativeButton("退出",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            activity.finish();
                                        }
                                    });
                    if (!activity.isFinishing())
                        builder.create().show();
                }
            });
        }

        @Override
        public void onNext(final Object obj) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onNextListener.onNext(obj);
                }
            });
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            Log.d("gzfuzhi", "cancel");
            unsubscribe();
        }
    }
    /*接口*/
    public interface OnNextListener {
        void onNext(Object obj);
    }
    //设备列表转到主界面首先执行的地方
    protected class EventHandler {
        public void onEvent(EquipmentSelectedEvent event) {

            selectedEquipmentCode = event.equipmentCode;
            reloadView();
        }
    }

    //获取配置信息，暂时未用到
    private void getConfigInfo(final String equipmentCode,final Map<String,ArrayList<String>> configInfo){
        final ArrayList<String> info = new ArrayList<>();
        GetConfigObservable.createObservable(equipmentCode)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new ProgressSubscriber(new OnNextListener() {
                    @Override
                    public void onNext(Object obj) {
                        if (obj != null) {
                            for (ConfigBean c : (ArrayList<ConfigBean>) obj) {
                                if (isMatch(c.getProtocolKey()))
                                    info.add(c.getIndicatorNum());
                            }
                            configInfo.put(equipmentCode, info);
                            //reloadView();
                        } else {
                            ToastUtil.showShort(activity, "获取配置信息为空");
                        }
                    }
                }));
    };

    private boolean isMatch(String key){
        return key.matches(".*s$");
    }
}

