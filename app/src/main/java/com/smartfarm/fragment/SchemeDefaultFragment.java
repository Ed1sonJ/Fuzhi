package com.smartfarm.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.baidu.platform.comapi.map.B;
import com.smartfarm.activity.R;
import com.smartfarm.adapter.DialogTimeListAdapter;
import com.smartfarm.dialog.BaseCustomAlterDialog;
import com.smartfarm.event.EquipmentSelectedEvent;
import com.smartfarm.event.GlobalEvent;
import com.smartfarm.fragmentUtil.UploadAndDownloadScheme;
import com.smartfarm.model.Equipment;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.ToastUtil;
import com.smartfarm.util.WaterRoom;
import com.smartfarm.view.NumberPickerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.VideoView;

public class SchemeDefaultFragment extends BaseFragment {
    Activity activity;
    View rootView;
    //util
    private ToastUtil toast;
    private UploadAndDownloadScheme uploadAndDownloadScheme;
    private WaterRoom waterRoom;
    private BaseProgressDialog dialog;
    //view
    private TextView equipmentsName;   //�豸����
    /**
     * �ϴ���ť
     */
    private RelativeLayout uploadLayout;
    /**
     * ��ͷ����tableLayout����
     */
    private TableLayout schemeDefaultIndicatorLayout;   //�������б�
    /**
     * û��ѡ���豸��ʾ��Ĭ�ϲ���
     */
    private RelativeLayout schemeDefaultParameterLayout;    //����
    private TextView noIndicatorText;
    /**
     * ���� - ���ʱȣ�����
     */
    private View lqcLayout;
    /**
     * ���ʱȵ�listview
     */
    private ListView listViewlqc;
    /**
     * ��������
     */
    private LinearLayout othersParamLayout;
    /**
     * ָ��ֵ
     */
    private SeekBar targetSeekBar;
    /**
     * ������selected����һ������׹�֮��
     */
    private TextView targetTextView;
    /**
     * ��λ
     */
    private TextView targetUnit;
    /**
     * ����ֵ
     */
    private SeekBar upperSeekBar;
    private TextView upperTextView;
    private TextView upperUnit;
    /**
     * ����ֵ
     */
    private SeekBar lowerSeekBar;
    private TextView lowerTextView;
    private TextView lowerUnit;
    //��Ƶ��û�õ�
    RelativeLayout videoLayout;
    RelativeLayout progressLayout;
    VideoView mVideoView;
    ImageView videoBtn;
    ImageView fullScreenBtn;
    //data
    String equipmentCodes;  //�豸��
    /**
     * �������ؼ���
     */
    ArrayList<String> indicatorKeys = new ArrayList<>();
    /**
     * ����������
     */
    ArrayList<String> indicatorNames = new ArrayList<>();
    /**
     * Ĭ�Ϲ��ʱ�����
     */
    private String[] lqcItemText;
    /**
     * Ĭ�Ϲ��ʱ�ͼ��
     */
    private int[] lqcItemColor;
    /**
     * ѡ�й��ʱ�ֵ
     */
    private String lqc = "";
    private String target;  //Ŀ��ֵ
    private String upper;   //����ֵ
    private String lower;   //����ֵ
    private String startTime;   //��ʼʱ��
    private String endTime;     //����ʱ��
    private int timeDelay = 5;  //ʱ�����
    boolean isStartTime = true; //�Զ���ʱ���õ��ı���
    boolean isWater;    //��ˮ��ť�Ƿ���
    boolean isWatering; //���ڽ�ˮ
    /**
     * ��ˮ��������Ϣ
     */
    private static final String WATERCONTROLLER = "/c/shc/1";
    private String clientId = "ClientOfSmartFarm";
    //index
    private int currentIndex;   //��ǰѡ�п�����
    private final static int MAGIC_NUMBER = 10;     //�������õ���һ���Զ�������
    //EventBus
    EventHandler handler = new EventHandler();

    //ʱ���ʽ
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private class EventHandler {
        public void onEvent(EquipmentSelectedEvent event) {
            equipmentCodes = event.equipmentCode;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        toast = new ToastUtil();
        //��ȡ���Ը�Ԥ�����أ����ܣ����ʣ���ǿ��
        uploadAndDownloadScheme = new UploadAndDownloadScheme(activity);
        dialog = new BaseProgressDialog(activity);
        //���ܿ���
        waterRoom = new WaterRoom(new WaterRoom.WaterInterface() {
            @Override
            public void waterFailed() {
                dismissDialog();
            }

            @Override
            public void watering(String code) {
                SchemeWatering(code);
            }

            @Override
            public void waterTimeOut(String code) {
                SchemeWaterTimeout(code);
            }
        });
        GlobalEvent.bus.register(handler);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
        uploadAndDownloadScheme = null;
        GlobalEvent.bus.unregister(handler);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scheme_default, container, false);
        initView();
        setUpVideoView();
        return rootView;
    }

    //��Ƶ����û�õ�
    protected void startVideo(String path) {
        mVideoView.setBufferSize(128);
        mVideoView.setVideoPath(path);
        mVideoView.requestFocus();
//        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                mediaPlayer.setPlaybackSpeed(1.0f);
//            }
//        });
        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        progressLayout.setVisibility(View.VISIBLE);
                        ToastUtil.showShort(activity, "��������");
                        mp.pause();
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        progressLayout.setVisibility(View.GONE);
                        mp.start();
                        break;
                    case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                        ToastUtil.showLong(activity, "����:" + extra);
                        break;
                    default:
                        ToastUtil.showLong(activity, "" + what);
                        break;
                }
                return true;
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                ToastUtil.showLong(activity, "�������ݳ������Ժ�����");
                progressLayout.setVisibility(View.GONE);
                mp.pause();
                mp.reset();
                return false;
            }
        });
        progressLayout.setVisibility(View.VISIBLE);
    }

    protected void stopVideo() {
        mVideoView.pause();
        mVideoView.stopPlayback();
    }

    protected void setUpVideoView() {
        final String path = "rtmp://v.gzfuzhi.com/mytv/test";

        videoBtn = (ImageView) rootView.findViewById(R.id.video_btn);
        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoLayout.getVisibility() == View.GONE) {
                    videoLayout.setVisibility(View.VISIBLE);
                    startVideo(path);
                } else {
                    videoLayout.setVisibility(View.GONE);
                    stopVideo();
                }
            }
        });

        fullScreenBtn = (ImageView) rootView.findViewById(R.id.video_full_screen);
        fullScreenBtn.setOnClickListener(new View.OnClickListener() {
            private boolean fullScreen = false;

            @Override
            public void onClick(View v) {
                if (!fullScreen) {
                } else {
                }
            }
        });
        videoLayout = (RelativeLayout) rootView.findViewById(R.id.video_layout);
        progressLayout = (RelativeLayout) rootView.findViewById(R.id.video_progress_layout);
        mVideoView = (VideoView) rootView.findViewById(R.id.vitamio_videoView);
    }

    private void initView() {
        equipmentsName = (TextView) rootView.findViewById(R.id.scheme_default_equiment_name);

//        uploadLayout = (LinearLayout) rootView.findViewById(R.id.scheme_default_upload_layout);
//        uploadLayout.setOnClickListener(new uploadListener());
        //��ͷ���ܹ���
        schemeDefaultIndicatorLayout = (TableLayout) rootView.findViewById(R.id.scheme_default_indicator_layout);
        //û��ѡ���豸��Ĭ�ϲ���
        schemeDefaultParameterLayout = (RelativeLayout) rootView.findViewById(R.id.scheme_default_parameter_layout);
        //û��ѡ���豸���뵽�豸�б���ѡ���豸
        noIndicatorText = (TextView) rootView.findViewById(R.id.textview_no_indicator);
        //���ʱȲ���
        lqcLayout = activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_parameter_lqc, schemeDefaultParameterLayout, false);
        //lqcLayout�е�listview
        listViewlqc = (ListView) lqcLayout.findViewById(R.id.scheme_default_parameter_lqc_listview);
        //�ƹ���������飬��:�� = 1:0����:�� = 5:1
        lqcItemText = getResources().getStringArray(R.array.lqc_text);
        //������Ӧ�Ĺ�ȵ�ͼƬ
        getLqcItemImage();
        lqcListView(listViewlqc, lqcItemText, lqcItemColor);
        listViewlqc.setOnItemClickListener(new lqcListener());

        othersParamLayout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_parameter_others, schemeDefaultParameterLayout, false);
        initParamOthersLayout();
    }

    //�ϴ���ť
    private class uploadListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (indicatorKeys != null && indicatorKeys.size() > 0)
                uploadScheme(indicatorKeys.get(currentIndex));
        }
    }

    //��ȡ���ʱȵ�ͼ��
    private void getLqcItemImage() {
        TypedArray ar = activity.getResources().obtainTypedArray(R.array.lqc_color);
        int len = ar.length();
        lqcItemColor = new int[len];
        for (int i = 0; i < len; i++)
            lqcItemColor[i] = ar.getResourceId(i, 0);
        //������û����ʹ��TypedArray�����recycle������������ʾ��This TypedArray should be recycled after use with #recycle()����
        //����TypedArray���Ա�������á��ڵ��������������Ͳ�����ʹ�����TypedArray��
        //��TypedArray�����recycle��Ҫ��Ϊ�˻��档
        // ��recycle�����ú����˵�������������ڿ��Ա������ˡ�
        // TypedArray �ڲ����в������飬���ǻ�����Resources���еľ�̬�ֶ��У�
        // �����Ͳ���ÿ��ʹ��ǰ����Ҫ�����ڴ档
        ar.recycle();
    }

    //���ʱ��б������
    private class lqcListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == lqcItemText.length) {
                //���ʿ����е��Զ��尴ť
                customLed();
            } else {
                lqc = cutLqcParam(lqcItemText[position]);
                uploadScheme("lqc");
            }
        }
    }

    //�Զ�����ʱ�ֵ,�Ѿ������˲���
    private void customLed() {
        View contentView = View.inflate(activity, R.layout.fragment_scheme_new_custom_lqc, null);
        final NumberPickerView ledRed = (NumberPickerView) contentView.findViewById(R.id.scheme_custom_led_red);
        final NumberPickerView ledBlue = (NumberPickerView) contentView.findViewById(R.id.scheme_custom_led_blue);
        final NumberPickerView ledWhite = (NumberPickerView) contentView.findViewById(R.id.scheme_custom_led_white);
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        baseDialog.setWidthAndHeightRadio(0.8f, 0.65f);
        baseDialog.setTitle("�Զ����ǿ��");
        baseDialog.setNegativeBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        });
        baseDialog.setPositiveBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lqc = ledRed.getValue() + ":" + ledBlue.getValue() + ":" + ledWhite.getValue();
                uploadScheme("lqc");
                baseDialog.dismiss();
            }
        });
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        baseDialog.setContentView(contentView, contentLp);
    }

    //��Ϊ��ȡ�Ĺ��ʱȲ����ǡ����ʱ�=1��2��3�������Σ�������ֻ��Ҫ1��2��3,������Ҫ��ȡ
    private String cutLqcParam(String string) {
        int index = string.indexOf("=") + 1;
        if (index < string.length()) {
            return string.substring(index);
        }
        return null;
    }

    //�����ʱ������������ĳ�ʼ��
    private void initParamOthersLayout() {
        targetSeekBar = (SeekBar) othersParamLayout.findViewById(R.id.target);
        targetSeekBar.setProgress(1);
        targetTextView = (TextView) othersParamLayout.findViewById(R.id.target_textview);
        targetTextView.setOnClickListener(new paramTextViewListener());
        targetSeekBar.setOnSeekBarChangeListener(new onSeekBarChangedListener(targetTextView));
        targetUnit = (TextView) othersParamLayout.findViewById(R.id.target_unit);

        upperSeekBar = (SeekBar) othersParamLayout.findViewById(R.id.upper);
        upperSeekBar.setProgress(1);
        upperTextView = (TextView) othersParamLayout.findViewById(R.id.upper_textview);
        upperTextView.setOnClickListener(new paramTextViewListener());
        upperSeekBar.setOnSeekBarChangeListener(new onSeekBarChangedListener(upperTextView));
        upperUnit = (TextView) othersParamLayout.findViewById(R.id.upper_unit);

        lowerSeekBar = (SeekBar) othersParamLayout.findViewById(R.id.lower);
        lowerSeekBar.setProgress(1);
        lowerTextView = (TextView) othersParamLayout.findViewById(R.id.lower_textview);
        lowerTextView.setOnClickListener(new paramTextViewListener());
        lowerSeekBar.setOnSeekBarChangeListener(new onSeekBarChangedListener(lowerTextView));
        lowerUnit = (TextView) othersParamLayout.findViewById(R.id.lower_unit);
        //�ϴ水ť
        uploadLayout = (RelativeLayout) othersParamLayout.findViewById(R.id.upload_relate_layout);
        uploadLayout.setOnClickListener(new uploadListener());

    }

    //ָ��ֵ�����ޡ����޵��ʱ�����ĶԻ��򣬵�����ֿ�������
    private class paramTextViewListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            LinearLayout paramAdjustment = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_parameter_adjustment, null);
            final EditText target = (EditText) paramAdjustment.findViewById(R.id.param_target);
            final EditText upper = (EditText) paramAdjustment.findViewById(R.id.param_upper);
            final EditText lower = (EditText) paramAdjustment.findViewById(R.id.param_lower);
            new AlertDialog.Builder(activity)
                    .setTitle("��������")
                    .setView(paramAdjustment)
                    .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int targetTmp = getValidAdjustment(target);
                            int upperTmp = getValidAdjustment(upper);
                            int lowerTmp = getValidAdjustment(lower);
                            if (targetTmp != -1)
                                targetSeekBar.setProgress(targetTmp);
                            if (upperTmp != -1)
                                upperSeekBar.setProgress(upperTmp);
                            if (lowerTmp != -1)
                                lowerSeekBar.setProgress(lowerTmp);
                        }
                    }).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create()
                    .show();
        }
    }

    //�ж������ָ��ֵ�Ƿ����
    private int getValidAdjustment(EditText et) {
        if (et.getText().toString().equals("") || et.getText().toString() == null) {
            return -1;
        }
        if (Integer.parseInt(et.getText().toString()) <= 0)
            return 0;
        if (indicatorKeys.get(currentIndex).equals("lc")) {
            if (Integer.parseInt(et.getText().toString()) > 6000)
                return 6000;
        } else if (indicatorKeys.get(currentIndex).equals("phc")) {
            if (Integer.parseInt(et.getText().toString()) > 10)
                return 10;
        } else {
            if (Integer.parseInt(et.getText().toString()) > 100)
                return 100;
        }
        return Integer.parseInt(et.getText().toString());
    }

    //������
    private class onSeekBarChangedListener implements SeekBar.OnSeekBarChangeListener {
        private TextView textView;

        public onSeekBarChangedListener(TextView textView) {
            this.textView = textView;
        }

        //fromUser������û������ĸı��򷵻�True
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            textView.setText(String.valueOf(progress));
        }

        //֪ͨ�û��Ѿ���ʼһ�������϶����ƣ�
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        //֪ͨ�û����������Ѿ�����
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    //����ӿڣ�����ÿ���豸���豸��
    public void queryIndicators() {
        equipmentsName.setText(Equipment.getEquipmentName(activity, equipmentCodes));
        getIndicators();
    }

    //��ȡÿ���豸�ſ�����
    private void getIndicators() {
        uploadAndDownloadScheme.getIndicatorType(equipmentCodes, new UploadAndDownloadScheme.SchemeListener() {
            @Override
            public void result(Map<String, ArrayList<String>> result) {
                noIndicatorHide();
                indicatorsResult(result);
            }

            @Override
            public void noIndicator() {
                noIndicatorShow();
            }
        });
    }

    //û�п�����ʱ��Ҫ���ص���Ϣ
    private void noIndicatorHide() {
        noIndicatorText.setVisibility(View.GONE);
    }

    //û�п�����ʱ��Ҫ��ʾ����Ϣ
    private void noIndicatorShow() {
        noIndicatorText.setVisibility(View.VISIBLE);
    }

    //��ȡ���ƵĽ��
    private void indicatorsResult(Map<String, ArrayList<String>> result) {
        //����������
        sortIndicators(result, indicatorNames, indicatorKeys);
        initIndicatorLayout(schemeDefaultIndicatorLayout, indicatorNames);
    }

    //�Ի�ȡ���Ŀ���������
    //������ʽ
    //^ƥ�������ַ����Ŀ�ʼλ��
    //.Ҫƥ�������\r\n�����ڵ��κ��ַ�
    //*ƥ��ǰ����ӱ��ʽ�����
    private void sortIndicators(Map<String, ArrayList<String>> result, ArrayList<String> indicatorNames, ArrayList<String> indicatorKeys) {
        ArrayList<String> sortKeys = result.get("protocolKeys");
        ArrayList<String> sortNames = result.get("protocolNames");
        //sortKeys.size() != sortNames.size()��ʲô
        if (sortKeys == null || sortNames == null || sortKeys.size() != sortNames.size()) {
            toast.showLong(activity, "û�п�����");
            return;
        }
        indicatorNames.clear();
        indicatorKeys.clear();
        int i = findSpecialIndicator(sortNames, "^����ʪ��.*");
        if (i != -1) {
            indicatorNames.add("���ܿ�����");
            indicatorKeys.add(sortKeys.get(i));
            sortNames.remove(i);
            sortKeys.remove(i);
        }
        i = findSpecialIndicator(sortNames, "^����.*");
        if (i != -1) {
            indicatorNames.add(sortNames.get(i));
            indicatorKeys.add(sortKeys.get(i));
            sortNames.remove(i);
            sortKeys.remove(i);
        }
        i = findSpecialIndicator(sortNames, "^��ǿ.*");
        if (i != -1) {
            indicatorNames.add(sortNames.get(i));
            indicatorKeys.add(sortKeys.get(i));
            sortNames.remove(i);
            sortKeys.remove(i);
        }
        indicatorNames.addAll(sortNames);
        indicatorKeys.addAll(sortKeys);
    }

    //�Կ�������ƥ��
    private int findSpecialIndicator(ArrayList<String> indicatorNames, String indicator) {
        for (int i = 0; i < indicatorNames.size(); i++) {
            if (indicatorNames.get(i).matches(indicator)) {
                return i;
            }
        }
        return -1;
    }

    //�����ʼ����Ҳ�ǻ�ȡ��������֮��ĳ�ʼ��
    private void initIndicatorLayout(TableLayout tableLayout, ArrayList<String> indicatorNames) {
        tableLayout.removeAllViews();
        int size = indicatorNames.size();
        if (size > 0) {
            TableRow row = new TableRow(activity);
            ViewGroup.LayoutParams params = new ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(params);
            View view;
            ImageView imageView;
            TextView textView;
            //����0������4�ֿ���
            if (size < 5) {
                for (int i = 0; i < size; i++) {
                    //�豸���Ƶ�view,����һ��ͼ��һ��textview���ϳ�һ����ť
                    view = activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_indicator_btn, row, false);
                    imageView = (ImageView) view.findViewById(R.id.indicator_image);
                    setIconWithType(imageView, indicatorNames.get(i), false);
                    textView = (TextView) view.findViewById(R.id.indicator_textview);
                    textView.setText(cutIndicatorName(indicatorNames.get(i)));
                    view.setTag(i);
                    //ʵ�ֲ�ͬ���ܵĿ���
                    view.setOnClickListener(new indicatorPressedListener());
                    row.addView(view);
                }
                tableLayout.addView(row);
            } else {
                //5�ֻ����ϵĿ���
                for (int i = 0; i < 3; i++) {
                    view = activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_indicator_btn, row, false);
                    imageView = (ImageView) view.findViewById(R.id.indicator_image);
                    setIconWithType(imageView, indicatorNames.get(i), false);
                    textView = (TextView) view.findViewById(R.id.indicator_textview);
                    textView.setText(cutIndicatorName(indicatorNames.get(i)));
                    view.setTag(i);
                    view.setOnClickListener(new indicatorPressedListener());
                    row.addView(view);
                }
                //����Ĺ��ܼ�
                {
                    view = activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_indicator_btn, row, false);
                    imageView = (ImageView) view.findViewById(R.id.indicator_image);
                    imageView.setImageResource(R.drawable.icon_indicator_others);
                    textView = (TextView) view.findViewById(R.id.indicator_textview);
                    textView.setText("����");
                    view.setTag(MAGIC_NUMBER);
                    view.setOnClickListener(new indicatorPressedListener());
                    row.addView(view);
                }
                tableLayout.addView(row);
            }
        }
        paramFirstShow();
    }

    //���տ�������������ͼƬ
    private void setIconWithType(ImageView iconIV, String type, boolean pressed) {
        if (type.matches("^����.*")) {
            iconIV.setImageResource(R.drawable.icon_jiaoshui);
        } else if (type.matches("^����.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_guangzhibi);
            } else {
                iconIV.setImageResource(R.drawable.icon_guangzhibi_pressed);
            }
        } else if (type.matches("^��ǿ.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_sun);
            } else {
                iconIV.setImageResource(R.drawable.icon_sun_pressed);
            }
        } else if (type.matches("^�¶�.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_wendu);
            } else {
                iconIV.setImageResource(R.drawable.icon_wendu_pressed);
            }
        } else if (type.matches("^ʪ��.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_shidu);
            } else {
                iconIV.setImageResource(R.drawable.icon_shidu_pressed);
            }
        } else if (type.matches("^������̼.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_co2);
            } else {
                iconIV.setImageResource(R.drawable.icon_co2_pressed);
            }
        } else if (type.matches("^PH.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_ph);
            } else {
                iconIV.setImageResource(R.drawable.icon_ph_pressed);
            }
        } else if (type.matches("^�������.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_ph);
            } else {
                iconIV.setImageResource(R.drawable.icon_ph_pressed);
            }
        } else if (type.matches("^EC.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_ec);
            } else {
                iconIV.setImageResource(R.drawable.icon_ec_pressed);
            }
        } else if (type.matches("^�����¶�.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_wendu);
            } else {
                iconIV.setImageResource(R.drawable.icon_wendu_pressed);
            }
        } else if (type.matches("^����ʪ��.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_shidu);
            } else {
                iconIV.setImageResource(R.drawable.icon_shidu_pressed);
            }
        } else if (type.matches("^�����¶�.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_wendu);
            } else {
                iconIV.setImageResource(R.drawable.icon_wendu_pressed);
            }
        } else if (type.matches("^����ʪ��.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_jiaoshui);
            } else {
                iconIV.setImageResource(R.drawable.icon_jiaoshui_pressed);
            }
        } else {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_indicator_others);
            } else {
                iconIV.setImageResource(R.drawable.icon_indicator_others_pressed);
            }
        }
    }

    //��ȡ����������
    private String cutIndicatorName(String name) {
        Pattern p = Pattern.compile("(.*)������");
        Matcher m = p.matcher(name);
        boolean isMatched = m.matches();
        if (isMatched) {
            return m.group(1);
        } else {
            return "��������";
        }
    }

    //����������
    private class indicatorPressedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int choice = (int) v.getTag();
            if (choice == MAGIC_NUMBER) {     //ѡ��popupMenu��ť
                showPopupMenu(v);
                pressIndicatorMenu(3, indicatorKeys.size());
            } else if (choice < indicatorKeys.size()) {
                if (indicatorKeys.get(choice).equals("shc")) {    //ѡ�����ܰ�ť
                    water();
                } else {   //ѡ��������ť��
                    currentIndex = (int) v.getTag();
                    //��ʾ��Ӧ��view
                    indicatorShow(currentIndex);
                    pressIndicatorMenu(currentIndex, indicatorKeys.size());
                }
            }
        }
    }

    //����ѡ�еĿ�������ť���ֱ���ʾ���ʱȵ����ý���
    private void indicatorShow(int index) {
        //��λ����
        setParamsUnit(indicatorKeys.get(index));
        //lqc����
        if (indicatorKeys.get(index).equals("lqc")) {
            initParamLqc();
        }
        //lc��ǿ
        else if (indicatorKeys.get(index).equals("lc")) {
            initParamOthers(6000);
        }
        //����ʪ�ȿ��ƣ���phc?��������̼��ʲô��
        else if (indicatorKeys.get(index).equals("phc")) {
            initParamOthers(10);
        }
        //����
        else {
            initParamOthers(100);
        }
    }

    //���ÿ��Ƶĵ�λ���������ù�ǿ�ĵ�λΪlux
    private void setParamsUnit(String indicator) {
        if (indicator.equals("lc")) {
            targetUnit.setText("/lux");
            upperUnit.setText("/lux");
            lowerUnit.setText("/lux");
        } else if (indicator.equals("tc")) {
            targetUnit.setText("/��C");
            upperUnit.setText("/��C");
            lowerUnit.setText("/��C");
        } else if (indicator.equals("hc")) {
            targetUnit.setText("/%");
            upperUnit.setText("/%");
            lowerUnit.setText("/%");
        } else {
            targetUnit.setText("");
            upperUnit.setText("");
            lowerUnit.setText("");
        }
    }

    //���¿�������ťʱ���õķ������÷���³�����д���ǿ
    //index,ѡ�еĹ��ܵ��±꣬size��һ���м��ֹ��ܿ�ѡ
    private void pressIndicatorMenu(int index, int size) {
        if (schemeDefaultIndicatorLayout.getChildAt(0) instanceof TableRow) {
            //��ȡҪ��ʾ�Ĺ��ܣ����ܣ����ʣ��ȵ�
            TableRow tableRow = (TableRow) schemeDefaultIndicatorLayout.getChildAt(0);
            if (tableRow.getChildAt(index) instanceof LinearLayout) {
                //��ȡ����Ĺ��ܣ������ť��һ��ͼ��һ��textview���
                LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(index);
                if (linearLayout.getChildAt(0) instanceof ImageView &&
                        linearLayout.getChildAt(1) instanceof TextView) {
                    ImageView imageView = (ImageView) linearLayout.getChildAt(0);
                    TextView textView = (TextView) linearLayout.getChildAt(1);
                    switch (index) {
                        case 0:
                            //���ö�Ӧ��ͼƬ
                            setIconWithType(imageView, textView.getText().toString(), true);
                            textView.setTextColor(getResources().getColor(R.color.green_1));

                            if (size > 1) {
                                //��ť1
                                linearLayout = (LinearLayout) tableRow.getChildAt(1);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }

                            if (size > 2) {
                                //��ť2
                                linearLayout = (LinearLayout) tableRow.getChildAt(2);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }

                            if (size > 3) {
                                //��ť3
                                linearLayout = (LinearLayout) tableRow.getChildAt(3);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }
                            break;
                        case 1:
                            setIconWithType(imageView, textView.getText().toString(), true);
                            textView.setTextColor(getResources().getColor(R.color.green_1));
                            //��ť0
                            linearLayout = (LinearLayout) tableRow.getChildAt(0);
                            imageView = (ImageView) linearLayout.getChildAt(0);
                            textView = (TextView) linearLayout.getChildAt(1);
                            setIconWithType(imageView, textView.getText().toString(), false);
                            textView.setTextColor(getResources().getColor(R.color.black));
                            if (size > 2) {
                                //��ť2
                                linearLayout = (LinearLayout) tableRow.getChildAt(2);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }
                            if (size > 3) {
                                //��ť3
                                linearLayout = (LinearLayout) tableRow.getChildAt(3);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }
                            break;
                        case 2:
                            setIconWithType(imageView, textView.getText().toString(), true);
                            textView.setTextColor(getResources().getColor(R.color.green_1));
                            //��ť0
                            linearLayout = (LinearLayout) tableRow.getChildAt(0);
                            imageView = (ImageView) linearLayout.getChildAt(0);
                            textView = (TextView) linearLayout.getChildAt(1);
                            setIconWithType(imageView, textView.getText().toString(), false);
                            textView.setTextColor(getResources().getColor(R.color.black));
                            if (size > 1) {
                                //��ť1
                                linearLayout = (LinearLayout) tableRow.getChildAt(1);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }
                            if (size > 3) {
                                //��ť3
                                linearLayout = (LinearLayout) tableRow.getChildAt(3);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }
                            break;
                        case 3:
                            setIconWithType(imageView, textView.getText().toString(), true);
                            textView.setTextColor(getResources().getColor(R.color.green_1));
                            //��ť0
                            linearLayout = (LinearLayout) tableRow.getChildAt(0);
                            imageView = (ImageView) linearLayout.getChildAt(0);
                            textView = (TextView) linearLayout.getChildAt(1);
                            setIconWithType(imageView, textView.getText().toString(), false);
                            textView.setTextColor(getResources().getColor(R.color.black));
                            if (size > 1) {
                                //��ť1
                                linearLayout = (LinearLayout) tableRow.getChildAt(1);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }

                            if (size > 2) {
                                //��ť2
                                linearLayout = (LinearLayout) tableRow.getChildAt(2);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }
                            break;
                    }
                }
            }
        }
    }

    //������������4��ʱ�����س�ǰ��3�����������
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(activity, view);
        Menu menu = popupMenu.getMenu();
        for (int i = 3; i < indicatorKeys.size(); i++) {
            menu.add(Menu.NONE, Menu.FIRST + i, i, indicatorNames.get(i));
        }
        activity.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new popupMenuListener());
        popupMenu.show();
    }

    private class popupMenuListener implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            indicatorShow(item.getOrder());
            currentIndex = item.getOrder();
            return true;
        }
    }

    //��һ�ν���ʱ�������ֵ���ʾ
    private void paramFirstShow() {
        if (indicatorKeys != null && !indicatorKeys.isEmpty()) {
            //��һ��������ʪ�ȿ������ҿ�������������1ʱ����ʾ�ڶ���
            //��Ϊ�����Ǹ��Ǹ�dialog
            if (indicatorKeys.get(0).equals("shc") && indicatorKeys.size() > 1) {
                indicatorShow(1);
                currentIndex = 1;
                pressIndicatorMenu(1, indicatorKeys.size());
            } else {
                indicatorShow(0);
                currentIndex = 0;
                pressIndicatorMenu(0, indicatorKeys.size());
            }
        }
    }

    //��ˮ
    private void water() {
        if (!isWater) {
            setWaterTimes();
        } else {
            waterRoom.waterOffByBtn(equipmentCodes,
                    WATERCONTROLLER, clientId);
            showDialog("���ڹر�����");
        }
        //isWater = !isWater;
    }

    //���ý�ˮʱ�䣬�Ѿ��������µĲ���
    private void setWaterTimes() {
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        final View contentView = View.inflate(activity, R.layout.view_water_button, null);
        //��������Ļ�е���ʾ�ı���
        baseDialog.setWidthAndHeightRadio(0.8f, 0.4f);
        //���þ�����ʾdialog��������ƫ����
        baseDialog.setLocation(Gravity.CENTER, 0, 0);
        baseDialog.setNegativeBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        });
        baseDialog.setPositiveBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText waterTimeHours = (EditText) contentView.findViewById(R.id.et_water_time_hours);
                EditText waterTimeMins = (EditText) contentView.findViewById(R.id.et_water_time_minutes);
                EditText waterTimeSecs = (EditText) contentView.findViewById(R.id.et_water_time_seconds);
                waterTimeSecs.requestFocus();

                String waterTimeHoursMsg = waterTimeHours.getText().toString();
                String waterTimeMinsMsg = waterTimeMins.getText().toString();
                String waterTimeSecsMsg = waterTimeSecs.getText().toString();

                //ֻ��Ҫ�п�
                if (waterTimeSecsMsg.equals("") || waterTimeSecsMsg == null ||
                        waterTimeMinsMsg.equals("") || waterTimeMinsMsg == null
                        || waterTimeHoursMsg.equals("") || waterTimeHoursMsg == null) {
                    ToastUtil.showShort(activity, "������ʱ��");
                    return;
                }

                int totalTime = Integer.parseInt(waterTimeHoursMsg) * 3600 +
                        Integer.parseInt(waterTimeMinsMsg) * 60 +
                        Integer.parseInt(waterTimeSecsMsg);

                String waterMessage = String.valueOf(totalTime);

                waterRoom.waterOn(equipmentCodes, WATERCONTROLLER, clientId, waterMessage);
                showDialog("���ڴ�����");
                baseDialog.dismiss();
            }
        });
        //����dialog��ʾ�ı���
        baseDialog.setTitle(equipmentCodes + "");
        baseDialog.setIcon(R.drawable.icon_water_on_dialog);
        //����contentView��dialog����ʾ�Ĳ��ֲ���
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        //ƥ��dialog��contentView
        baseDialog.setContentView(contentView, contentLp);
    }

    //���ڽ�ˮ������ͼƬ
    public void SchemeWatering(String code) {
        if (indicatorKeys.get(0).equals("shc")) {
            if (schemeDefaultIndicatorLayout.getChildAt(0) instanceof TableRow) {
                TableRow tableRow = (TableRow) schemeDefaultIndicatorLayout.getChildAt(0);
                if (tableRow.getChildAt(0) instanceof LinearLayout) {
                    LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(0);
                    if (linearLayout.getChildAt(0) instanceof ImageView) {
                        ImageView imageView = (ImageView) linearLayout.getChildAt(0);
                        imageView.setImageResource(R.drawable.icon_water_on);
                    }
                }
            }
        }
        isWatering = true;

        reverseWaterBtn(code);

        dismissDialog();
    }

    //��ˮ��ɣ�Ϩ��ͼƬ
    public void SchemeWaterTimeout(String code) {
        if (indicatorKeys.get(0).equals("shc")) {
            if (schemeDefaultIndicatorLayout.getChildAt(0) instanceof TableRow) {
                TableRow tableRow = (TableRow) schemeDefaultIndicatorLayout.getChildAt(0);
                if (tableRow.getChildAt(0) instanceof LinearLayout) {
                    LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(0);
                    if (linearLayout.getChildAt(0) instanceof ImageView) {
                        ImageView imageView = (ImageView) linearLayout.getChildAt(0);
                        imageView.setImageResource(R.drawable.icon_jiaoshui);
                    }
                }
            }
        }
        isWatering = false;

        reverseWaterBtn(code);

        dismissDialog();
    }

    //ת����ˮ��ť״̬
    private void reverseWaterBtn(String code) {
        isWater = !isWater;
    }

    //��ʾ�ض��ı���Ϣ�ĶԻ���
    private void showDialog(String text) {
        dialog.setMessage(text);
        dialog.show();
    }

    //�ص��Ի���
    public void dismissDialog() {
        dialog.dismiss();
    }

    //��ʼ�����ʱȲ�������
    private void initParamLqc() {
        schemeDefaultParameterLayout.removeAllViews();
        schemeDefaultParameterLayout.addView(lqcLayout);
    }

    //���ʱ���ͼ��listview
    private void lqcListView(ListView listView, String[] itemString, int[] itemColor) {
        List<Map<String, Object>> listItems = new ArrayList<>();
        //���ʱȵ�ͼ���˵��
        for (int i = 0; i < itemString.length; i++) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("lqc_item_imageview", itemColor[i]);
            listItem.put("lqc_item_textview", itemString[i]);
            listItems.add(listItem);
        }
        //�Զ����ͼ���˵��
        Map<String, Object> listItem = new HashMap<>();
        listItem.put("lqc_item_imageview", R.drawable.icon_config);
        listItem.put("lqc_item_textview", "�Զ���");
        listItems.add(listItem);

        SimpleAdapter simpleAdapter = new SimpleAdapter(activity, listItems, R.layout.fragment_scheme_new_parameter_lqc_item,
                new String[]{"lqc_item_imageview", "lqc_item_textview"}, new int[]{R.id.lqc_item_iamgeview, R.id.lqc_item_textview});
        listView.setAdapter(simpleAdapter);
    }

    //��ʼ��������������
    private void initParamOthers(int theMaxValue) {
        schemeDefaultParameterLayout.removeAllViews();
        upperSeekBar.setMax(theMaxValue);
        targetSeekBar.setMax(theMaxValue);
        lowerSeekBar.setMax(theMaxValue);
        schemeDefaultParameterLayout.addView(othersParamLayout);
    }

    //�ϴ����Ʒ���
    private void uploadScheme(String indicator) {
        if (fillingParams(indicator)) {
            fillingTime();
        }
    }

    //����ѡ��ʱ��dialog���Ѿ��������µĲ���
    private void fillingTime() {
        String[] items = new String[]{"1����", "3����", "5����", "10����", "�Զ���"};
        startTime = "";
        endTime = "";
        //��ȡ�ڷŵ�dialog�е�listView
        View contentView = View.inflate(activity, R.layout.dialog_water_choose_time, null);
        ListView listTime = (ListView) contentView.findViewById(R.id.id_dialog_water_choose_time_list);
        //����listView��adapter
        final DialogTimeListAdapter adapter = new DialogTimeListAdapter(activity, items);
        listTime.setAdapter(adapter);
        //ΪlistView��item���õ��ʱ��ͼƬ
        listTime.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //viewΪitem���õ�convertView
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //��������е�ͼƬ
                adapter.removeAllViews();
                //�����õ����item��ͼƬ�仯
                ((ImageView) view.findViewById(R.id.id_water_choose_time_list_img)).setImageResource(R.drawable.dialog_img_select);
                countTime(position);
            }
        });

        //����dialog�Ļ������ԣ�����contentView���뵽dialog��
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        baseDialog.setWidthAndHeightRadio(0.8f, 0.65f);
        baseDialog.setLocation(Gravity.CENTER, 0, 0);
        baseDialog.setNegativeBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        });
        baseDialog.setPositiveBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAfterChecked();
                baseDialog.dismiss();
            }
        });
        baseDialog.setTitle("��ѡ�����ʱ��");
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentLp.setMargins(0, 80, 0, 0);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        baseDialog.setContentView(contentView, contentLp);
    }

    //����ʱ��
    private void countTime(int time) {
        if (time == 0) {
            timeDelay = 1;
        } else if (time == 1) {
            timeDelay = 3;
        } else if (time == 2) {
            timeDelay = 5;
        } else if (time == 3) {
            timeDelay = 10;
        } else {
            custom_time();
            return;
        }
        long currentTime = System.currentTimeMillis();
        startTime = format.format(currentTime);
        endTime = format.format(currentTime + timeDelay * 60 * 1000);
    }

    //�Զ���ʱ��
    private void custom_time() {
        isStartTime = true;
        LinearLayout customTime = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_timer_picker, null);
        Switch timeSwitch = (Switch) customTime.findViewById(R.id.time_switch);
        timeSwitch.setOnCheckedChangeListener(new timeCheckedListener());
        final TextView startTimeTextview = (TextView) customTime.findViewById(R.id.start_time_textview);
        final TextView endTimeTextview = (TextView) customTime.findViewById(R.id.end_time_textview);
        final DatePicker schemeDate = (DatePicker) customTime.findViewById(R.id.scheme_date);
        final TimePicker schemeTime = (TimePicker) customTime.findViewById(R.id.scheme_time);
        schemeTime.is24HourView();
        startTimeTextview.setText(String.valueOf(schemeDate.getYear()) + "-"
                + String.valueOf(schemeDate.getMonth() + 1) + "-"
                + String.valueOf(schemeDate.getDayOfMonth()) + " "
                + String.valueOf(schemeTime.getCurrentHour()) + ":"
                + String.valueOf(schemeTime.getCurrentMinute()));
        endTimeTextview.setText(String.valueOf(schemeDate.getYear()) + "-"
                + String.valueOf(schemeDate.getMonth() + 1) + "-"
                + String.valueOf(schemeDate.getDayOfMonth()) + " "
                + String.valueOf(schemeTime.getCurrentHour()) + ":"
                + String.valueOf(schemeTime.getCurrentMinute()));
        schemeDate.init(schemeDate.getYear(), schemeDate.getMonth(),
                schemeDate.getDayOfMonth(), new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                        if (isStartTime) {
                            startTimeTextview.setText(String.valueOf(year) + "-"
                                    + String.valueOf(monthOfYear + 1) + "-"
                                    + String.valueOf(dayOfMonth) + " "
                                    + String.valueOf(schemeTime.getCurrentHour())
                                    + ":"
                                    + String.valueOf(schemeTime.getCurrentMinute()));
                        } else {
                            endTimeTextview.setText(String.valueOf(year) + "-"
                                    + String.valueOf(monthOfYear + 1) + "-"
                                    + String.valueOf(dayOfMonth) + " "
                                    + String.valueOf(schemeTime.getCurrentHour())
                                    + ":"
                                    + String.valueOf(schemeTime.getCurrentMinute()));
                        }
                    }
                });
        schemeTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (isStartTime) {
                    startTimeTextview.setText(String.valueOf(schemeDate.getYear()) + "-"
                            + String.valueOf(schemeDate.getMonth() + 1) + "-"
                            + String.valueOf(schemeDate.getDayOfMonth()) + " "
                            + String.valueOf(hourOfDay) + ":"
                            + String.valueOf(minute));
                } else {
                    endTimeTextview.setText(String.valueOf(schemeDate.getYear()) + "-"
                            + String.valueOf(schemeDate.getMonth() + 1) + "-"
                            + String.valueOf(schemeDate.getDayOfMonth()) + " "
                            + String.valueOf(hourOfDay) + ":"
                            + String.valueOf(minute));
                }
            }
        });
        new AlertDialog.Builder(activity)
                .setTitle("�Զ������ʱ��")
                .setView(customTime)
                .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            startTime = customTimeFormatter(startTimeTextview.getText().toString());
                            endTime = customTimeFormatter(endTimeTextview.getText().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            startTime = startTimeTextview.getText().toString();
                            endTime = endTimeTextview.getText().toString();
                        }
                        //toast.showLong(activity, "��ʼʱ�䣺" + startTime + "\n" + "����ʱ�䣺" + endTime);
                    }
                }).setNegativeButton("����", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }).create().show();
    }

    //����Ժ�ǿ����û�д��������Ա�Ӧ�õ�String��yyyy-MM-dd hh:MM:ss�ķ���
    //����ԣ�ģ��֮���������ϵ
    private String customTimeFormatter(String orginalTime) {
        String time = orginalTime;
        String year = time.substring(0, time.indexOf("-"));
        time = time.substring(time.indexOf("-") + 1);
        String month = time.substring(0, time.indexOf("-"));
        if (Integer.parseInt(month) < 10) {
            month = "0" + month;
        }
        time = time.substring(time.indexOf("-") + 1);
        String day = time.substring(0, time.indexOf(" "));
        if (Integer.parseInt(day) < 10) {
            day = "0" + day;
        }
        time = time.substring(time.indexOf(" ") + 1);
        String hour = time.substring(0, time.indexOf(":"));
        if (Integer.parseInt(hour) < 10) {
            hour = "0" + hour;
        }
        time = time.substring(time.indexOf(":") + 1);
        String minute = time;
        if (Integer.parseInt(minute) < 10) {
            minute = "0" + minute;
        }
        String second = "00";
        String result = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
        return result;
    }

    //ת����ʼ�ͽ���ʱ���ѡ��ť
    private class timeCheckedListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                isStartTime = false;
            } else {
                isStartTime = true;
            }
        }
    }

    //������Ϊ����������
    private Boolean fillingParams(String indicator) {
        //��Ϊ��Ԥ���ʣ����ʱȣ��죺�����ף��׹�������Ĭ��Ϊ0
        if (indicator.equals("lqc")) {
            //target = lqc.trim() + ":0";ԭ��������·��
            target = lqc.trim();
            upper = target;
            lower = target;


        }
        //�ǹ�����Ϊ��Ԥ��������úõĲ����Ƿ����
        else {
            target = targetTextView.getText().toString();
            upper = upperTextView.getText().toString();
            lower = lowerTextView.getText().toString();


            if (Integer.parseInt(target) > Integer.parseInt(upper)) {
                toast.showShort(activity, "Ŀ��ֵ���ܴ�������ֵ");
                return false;
            }
            if (Integer.parseInt(lower) > Integer.parseInt(target)) {
                toast.showShort(activity, "Ŀ��ֵ����С������ֵ");
                return false;
            }
        }
        return true;
    }

    //�����ʱ������Ƿ�Ϸ�
    private boolean checkIslqcInputValid(String lqc) {
        String reg = "[0-9]:[0-9]:[0-9]";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(lqc);
        return matcher.matches();
    }

    //��������Ƿ�Ϸ�
    private boolean checkedIsValid(String target, String upper, String lower, String startTime, String endTime) {
        if (target == null || target.equals("")) {
            toast.showLong(activity, "����ֵ����Ϊ��");
            return false;
        }
        if (upper == null || upper.equals("")) {
            toast.showLong(activity, "����ֵ����Ϊ��");
            return false;
        }
        if (lower == null || lower.equals("")) {
            toast.showLong(activity, "����ֵ����Ϊ��");
            return false;
        }
        if (startTime == null || startTime.equals("")) {
            toast.showLong(activity, "��ѡ�����ʱ��");
            return false;
        }
        if (endTime == null || endTime.equals("")) {
            toast.showLong(activity, "��ѡ�����ʱ��");
            return false;
        }
        return true;
    }

    //���ʿ�������Ϸ����ϴ�
    private void uploadAfterChecked() {
        if (checkedIsValid(target, upper, lower, startTime, endTime)) {
            System.out.println("��ʼʱ��:" + startTime + "  ����ʱ��:" + endTime);
            uploadAndDownloadScheme.interveneObservable(equipmentCodes,
                    startTime, endTime, indicatorKeys.get(currentIndex), target, upper, lower);
        }
    }
}
