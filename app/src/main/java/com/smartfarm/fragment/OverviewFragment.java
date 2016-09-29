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

    // �û�����
    protected String selectedEquipmentCode;

    // ��������
    protected TableLayout buttonGroup;
    protected TextView equipmentNameText;
    //����ˢ��
    protected SwipeRefreshLayout swipeRefreshLayout;
    //����ͼƬ
    private SimpleDraweeView userPicture;
    protected ImageView userPhoto;
    protected ImageView realTimeBtn;

    protected EventHandler eventHandler;
    //ͼƬ������ļ���
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
        //����ˢ��
        swipeRefreshLayout.setOnRefreshListener(new PullToRefreshListener());
        //���ý��ȶ�������ɫ
        swipeRefreshLayout.setColorSchemeResources(R.color.blue_A700,
                R.color.blue_A400, R.color.blue_A200, R.color.blue_A100);
        //����Ƭ
        userPhoto.setOnClickListener(new OnCameraButtonClickedListener());
        //���
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
        // ˢ���豸����
        loadEquipmentName();

        // ˢ���豸ͼ��
        loadEquipmentImage();

        // ˢ��ʵʱ����
        buttonGroup.removeAllViews();
        //�������¼�
        OnClickListener listener = new OnSensorClickedListener();


        for (int i = 0; i < beans.size(); i++) {
            if (beans.get(i).getType().equals("GPS")){
                beans.remove(i);
            }
        }
        //��
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
            //��ͼ�¼����¶ȣ���ǿ��ʪ�ȵȵ�
            overviewItem.setOnClickListener(listener);
            if (i % 3 == 0) {
                buttonGroup.addView(row);
            }
        }
        //ռλ������������е����⣬����ռλ����ɾȥ����ҲûʲôӰ��
        for (int i = 0; i < beans.size() % 3; ++i) {
            View overviewItem = activity.getLayoutInflater().inflate(
                    R.layout.overview_item_null, buttonGroup, false);
            row.addView(overviewItem);
        }
        //��ʷ������Ҫ��ȡsensorcode��time
        //��ȡ���֣�ʪ�ȣ���ǿ���¶ȵȵ�
        ArrayList<String> names = new ArrayList<>();
        for (int z = 0; z < beans.size(); ++z) {
            names.add(cutSensorName(beans.get(z).getName()));
        }
        GlobalEvent.bus.post(new OverviewSensorCodeEvent(names, selectedEquipmentCode, getSensorCode(beans),getNewTime(beans)));
    }
    //�������ı��
    private ArrayList<String> getSensorCode(List<RealtimeDataBean> beans) {
        ArrayList<String> sensorCodes = new ArrayList<>();
        for (int i = 0; i < beans.size(); ++i) {
            sensorCodes.add(beans.get(i).getEquipmentCode());
        }
        return sensorCodes;
    }
    //��ȡʱ��
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
            Toast.makeText(activity, "û������ͷ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    //��ȡͼƬ������
    protected String getEquipmentImageName() {
        Config config = new Config(activity);
        return config.getUsername()
                + selectedEquipmentCode + ".jpeg";
    }
    //��ȡͼƬ���ļ�
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
    //ˢ����Ƭ
    public void loadEquipmentImage() {
        try {
            File equimentImageFile = getEquipmentImageFile();
            Uri uri = Uri.fromFile(equimentImageFile);
            if (!equimentImageFile.exists()) {
                uri = Uri.parse("res://com.smartfarm.activity/" + R.drawable.overview_image);
            }

//            //�������
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
//            if (imageChanged == true){  //����ѡ��ͼƬ
//                GlobalEvent.bus.post(new EquipmentImageEvent());
//                imageChanged = false;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //���ؼ���ͼƬʱ�������������ļ���
    public void loadPictureFromGallery(Intent data) {

        if (data != null)
        {
            Uri uri = data.getData();
            String picturePath;
            if (!TextUtils.isEmpty(uri.getAuthority()))
            {
                String[] filePathColumn = {MediaColumns.DATA};
                // ��cursor�������ݼ��������淵�صĽ��
                Cursor cursor = activity.getContentResolver().query(uri,
                        filePathColumn, null, null, null);
                if (null == cursor)
                {
                    ToastUtil.showShort(activity,"ͼƬû�ҵ�");
                    return;
                }
                //�ƶ���굽��һ�У��������Ҫ����С�ĺ���������Խ��
                cursor.moveToFirst();
                //����û�ѡ���ͼƬ������ֵ
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                //���ͼƬ��·��
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
                //BufferedInputStream�Ǵ�����������������Ĭ�ϻ�������С��8M���ܹ����ٷ��ʴ��̵Ĵ���������ļ���ȡ���ܣ�
                BufferedInputStream bin = new BufferedInputStream(
                        new FileInputStream(img));
                //�����ձ����ͼƬ����һ��������ԭ����ͼƬ
                //��FilePathManager���������ļ���
                //�������·�ʽ���Դ���
                File file=getEquipmentImageFile();
                //BufferedOutputStream�Ǵ�����������������ܹ�����ļ���д��Ч�ʡ�
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
                System.out.println("û��");
            }
            catch (Exception e)
            {
            System.out.println("����"+e.getMessage());
            e.printStackTrace();
            }
        }
        else
        {
            ToastUtil.showShort(activity,"ͼƬû�ҵ�");
            return;
        }

    }

    protected void takePicture() {
        if (hasCamera() == false) {
            return;
        }
        try {
            //��FilePathManager���������ļ���
            //�������·�ʽ���Դ���
            File file=getEquipmentImageFile();
            // ����һ��Intent���󣬲������Intent��actionָ��Ϊandroid.media.action.IMAGE_CAPTURE
            //�ٵ���Intent��putExtra()����ָ��ͼƬ�������ַ����������ոյõ���Uri����
            // ������startActivityForResult()���������
            // ����ʹ�õ���һ����ʽIntent��ϵͳ���ҳ��ܹ���Ӧ���Intent�Ļȥ������
            // �������������ͻᱻ�򿪣�������Ƭ�ͻ������out_image.jpg�С�
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
        //�������
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.clearCaches();
        //֪ͨ�б�ı��¼�
        GlobalEvent.bus.post(new EquipmentImageEvent());
    }
    //������������ͼƬ�������ͣ��¶ȣ�ͼƬ����Ϊ�¶ȵ�ͼƬ
    protected void setIconWithType(ImageView iconIV, String type) {
        if (type.matches("^�¶�.*")) {
            iconIV.setImageResource(R.drawable.icon_wendu);
        } else if (type.matches("^ʪ��.*")) {
            iconIV.setImageResource(R.drawable.icon_shidu);
        } else if (type.matches("^��ǿ.*")) {
            iconIV.setImageResource(R.drawable.icon_light);
        } else if (type.matches("^������̼.*")) {
            iconIV.setImageResource(R.drawable.icon_co2);
        } else if (type.matches("^PH.*")) {
            iconIV.setImageResource(R.drawable.icon_ph);
        } else if (type.matches("^�������.*")) {
            iconIV.setImageResource(R.drawable.icon_ph);
        } else if (type.matches("^EC.*")) {
            iconIV.setImageResource(R.drawable.icon_ec);
        } else if (type.matches("^�����¶�.*")) {
            iconIV.setImageResource(R.drawable.icon_wendu);
        } else if (type.matches("^����ʪ��.*")) {
            iconIV.setImageResource(R.drawable.icon_shidu);
        } else if (type.matches("^�����¶�.*")) {
            iconIV.setImageResource(R.drawable.icon_wendu);
        } else if (type.matches("^����ʪ��.*")) {
            iconIV.setImageResource(R.drawable.icon_shidu);
        } else {
            iconIV.setImageResource(R.drawable.icon_wendu);
        }
    }
    //��ô����������֣����¶�
    protected String cutSensorName(String name) {
        //������ʽ��(.*), .ƥ�����\r\n��֮����κε����ַ�,*ƥ��ǰ����ӱ��ʽ�����
        Pattern p = Pattern.compile("(.*)��������(.*)��");
        Matcher m = p.matcher(name);
        boolean isMatched = m.matches();
        if (isMatched) {
            //group��0������ָ����������
            // group��1�� ָ���ǵ�һ��������Ķ�����
            // group��2��ָ�ĵڶ���������Ķ���
            return m.group(1);
        } else {
            return "��������";
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
                    //��ͼƬ
                    Intent gallery = new Intent(
                            Intent.ACTION_PICK);
                    gallery.setType("image/*");
                    activity.startActivityForResult(
                            gallery,
                            IntentUtil.PICTURE_FROM_GALLERY);
                    baseDialog.dismiss();
                } catch (Exception e) {
                    ToastUtil.showLong(activity, "��ͼ��ʧ��");
                    baseDialog.dismiss();

                }
            }
        });
        tvTakePhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //������ͷ
                    takePicture();
                    baseDialog.dismiss();

                } catch (Exception e) {
                    ToastUtil.showLong(activity, "������ͷʧ��");
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
    //���
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
                            .setMessage("��������ˣ�������һ��")
                            .setPositiveButton("����",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            // TODO:
                                        }
                                    })
                            .setNegativeButton("�˳�",
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
    /*�ӿ�*/
    public interface OnNextListener {
        void onNext(Object obj);
    }
    //�豸�б�ת������������ִ�еĵط�
    protected class EventHandler {
        public void onEvent(EquipmentSelectedEvent event) {

            selectedEquipmentCode = event.equipmentCode;
            reloadView();
        }
    }

    //��ȡ������Ϣ����ʱδ�õ�
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
                            ToastUtil.showShort(activity, "��ȡ������ϢΪ��");
                        }
                    }
                }));
    };

    private boolean isMatch(String key){
        return key.matches(".*s$");
    }
}

