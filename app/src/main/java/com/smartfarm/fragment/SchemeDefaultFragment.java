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
    private TextView equipmentsName;   //设备名字
    /**
     * 上传按钮
     */
    private RelativeLayout uploadLayout;
    /**
     * 表头功能tableLayout布局
     */
    private TableLayout schemeDefaultIndicatorLayout;   //控制器列表
    /**
     * 没有选中设备显示的默认布局
     */
    private RelativeLayout schemeDefaultParameterLayout;    //参数
    private TextView noIndicatorText;
    /**
     * 参数 - 光质比，布局
     */
    private View lqcLayout;
    /**
     * 光质比的listview
     */
    private ListView listViewlqc;
    /**
     * 其它布局
     */
    private LinearLayout othersParamLayout;
    /**
     * 指标值
     */
    private SeekBar targetSeekBar;
    /**
     * 光质中selected的哪一项红蓝白光之比
     */
    private TextView targetTextView;
    /**
     * 单位
     */
    private TextView targetUnit;
    /**
     * 上限值
     */
    private SeekBar upperSeekBar;
    private TextView upperTextView;
    private TextView upperUnit;
    /**
     * 下限值
     */
    private SeekBar lowerSeekBar;
    private TextView lowerTextView;
    private TextView lowerUnit;
    //视频，没用到
    RelativeLayout videoLayout;
    RelativeLayout progressLayout;
    VideoView mVideoView;
    ImageView videoBtn;
    ImageView fullScreenBtn;
    //data
    String equipmentCodes;  //设备号
    /**
     * 控制器关键字
     */
    ArrayList<String> indicatorKeys = new ArrayList<>();
    /**
     * 控制器名字
     */
    ArrayList<String> indicatorNames = new ArrayList<>();
    /**
     * 默认光质比内容
     */
    private String[] lqcItemText;
    /**
     * 默认光质比图像
     */
    private int[] lqcItemColor;
    /**
     * 选中光质比值
     */
    private String lqc = "";
    private String target;  //目标值
    private String upper;   //上限值
    private String lower;   //下限值
    private String startTime;   //开始时间
    private String endTime;     //结束时间
    private int timeDelay = 5;  //时间参数
    boolean isStartTime = true; //自定义时间用到的变量
    boolean isWater;    //浇水按钮是否按下
    boolean isWatering; //正在浇水
    /**
     * 浇水控制器信息
     */
    private static final String WATERCONTROLLER = "/c/shc/1";
    private String clientId = "ClientOfSmartFarm";
    //index
    private int currentIndex;   //当前选中控制器
    private final static int MAGIC_NUMBER = 10;     //代码中用到的一个自定义数字
    //EventBus
    EventHandler handler = new EventHandler();

    //时间格式
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
        //获取可以干预的因素，喷淋，光质，光强等
        uploadAndDownloadScheme = new UploadAndDownloadScheme(activity);
        dialog = new BaseProgressDialog(activity);
        //喷淋控制
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

    //视频，还没用到
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
                        ToastUtil.showShort(activity, "加载数据");
                        mp.pause();
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        progressLayout.setVisibility(View.GONE);
                        mp.start();
                        break;
                    case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                        ToastUtil.showLong(activity, "速率:" + extra);
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
                ToastUtil.showLong(activity, "加载数据出错，请稍后再试");
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
        //表头功能光质
        schemeDefaultIndicatorLayout = (TableLayout) rootView.findViewById(R.id.scheme_default_indicator_layout);
        //没有选中设备的默认布局
        schemeDefaultParameterLayout = (RelativeLayout) rootView.findViewById(R.id.scheme_default_parameter_layout);
        //没有选择设备，请到设备列表中选择设备
        noIndicatorText = (TextView) rootView.findViewById(R.id.textview_no_indicator);
        //光质比布局
        lqcLayout = activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_parameter_lqc, schemeDefaultParameterLayout, false);
        //lqcLayout中的listview
        listViewlqc = (ListView) lqcLayout.findViewById(R.id.scheme_default_parameter_lqc_listview);
        //灯光比例的数组，红:蓝 = 1:0，红:蓝 = 5:1
        lqcItemText = getResources().getStringArray(R.array.lqc_text);
        //设置相应的光比的图片
        getLqcItemImage();
        lqcListView(listViewlqc, lqcItemText, lqcItemColor);
        listViewlqc.setOnItemClickListener(new lqcListener());

        othersParamLayout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_parameter_others, schemeDefaultParameterLayout, false);
        initParamOthersLayout();
    }

    //上传按钮
    private class uploadListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (indicatorKeys != null && indicatorKeys.size() > 0)
                uploadScheme(indicatorKeys.get(currentIndex));
        }
    }

    //获取光质比的图像
    private void getLqcItemImage() {
        TypedArray ar = activity.getResources().obtainTypedArray(R.array.lqc_color);
        int len = ar.length();
        lqcItemColor = new int[len];
        for (int i = 0; i < len; i++)
            lqcItemColor[i] = ar.getResourceId(i, 0);
        //当我们没有在使用TypedArray后调用recycle，编译器会提示“This TypedArray should be recycled after use with #recycle()”。
        //回收TypedArray，以便后面重用。在调用这个函数后，你就不能再使用这个TypedArray。
        //在TypedArray后调用recycle主要是为了缓存。
        // 当recycle被调用后，这就说明这个对象从现在可以被重用了。
        // TypedArray 内部持有部分数组，它们缓存在Resources类中的静态字段中，
        // 这样就不用每次使用前都需要分配内存。
        ar.recycle();
    }

    //光质比列表项监听
    private class lqcListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == lqcItemText.length) {
                //光质控制中的自定义按钮
                customLed();
            } else {
                lqc = cutLqcParam(lqcItemText[position]);
                uploadScheme("lqc");
            }
        }
    }

    //自定义光质比值,已经更新了布局
    private void customLed() {
        View contentView = View.inflate(activity, R.layout.fragment_scheme_new_custom_lqc, null);
        final NumberPickerView ledRed = (NumberPickerView) contentView.findViewById(R.id.scheme_custom_led_red);
        final NumberPickerView ledBlue = (NumberPickerView) contentView.findViewById(R.id.scheme_custom_led_blue);
        final NumberPickerView ledWhite = (NumberPickerView) contentView.findViewById(R.id.scheme_custom_led_white);
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        baseDialog.setWidthAndHeightRadio(0.8f, 0.65f);
        baseDialog.setTitle("自定义光强比");
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

    //因为获取的光质比参数是“光质比=1：2：3”的情形，而我们只需要1：2：3,所以需要截取
    private String cutLqcParam(String string) {
        int index = string.indexOf("=") + 1;
        if (index < string.length()) {
            return string.substring(index);
        }
        return null;
    }

    //除光质比外其它参数的初始化
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
        //上存按钮
        uploadLayout = (RelativeLayout) othersParamLayout.findViewById(R.id.upload_relate_layout);
        uploadLayout.setOnClickListener(new uploadListener());

    }

    //指标值、上限、下限点击时弹出的对话框，点击数字可以设置
    private class paramTextViewListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            LinearLayout paramAdjustment = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_parameter_adjustment, null);
            final EditText target = (EditText) paramAdjustment.findViewById(R.id.param_target);
            final EditText upper = (EditText) paramAdjustment.findViewById(R.id.param_upper);
            final EditText lower = (EditText) paramAdjustment.findViewById(R.id.param_lower);
            new AlertDialog.Builder(activity)
                    .setTitle("参数调整")
                    .setView(paramAdjustment)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create()
                    .show();
        }
    }

    //判断输入的指标值是否合理
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

    //滑动条
    private class onSeekBarChangedListener implements SeekBar.OnSeekBarChangeListener {
        private TextView textView;

        public onSeekBarChangedListener(TextView textView) {
            this.textView = textView;
        }

        //fromUser如果是用户触发的改变则返回True
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            textView.setText(String.valueOf(progress));
        }

        //通知用户已经开始一个触摸拖动手势，
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        //通知用户触摸手势已经结束
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    //对外接口，传入每个设备的设备号
    public void queryIndicators() {
        equipmentsName.setText(Equipment.getEquipmentName(activity, equipmentCodes));
        getIndicators();
    }

    //获取每个设备号控制器
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

    //没有控制器时需要隐藏的信息
    private void noIndicatorHide() {
        noIndicatorText.setVisibility(View.GONE);
    }

    //没有控制器时需要显示的信息
    private void noIndicatorShow() {
        noIndicatorText.setVisibility(View.VISIBLE);
    }

    //获取控制的结果
    private void indicatorsResult(Map<String, ArrayList<String>> result) {
        //控制器排序
        sortIndicators(result, indicatorNames, indicatorKeys);
        initIndicatorLayout(schemeDefaultIndicatorLayout, indicatorNames);
    }

    //对获取到的控制器排序
    //正则表达式
    //^匹配输入字符串的开始位置
    //.要匹配包括“\r\n”在内的任何字符
    //*匹配前面的子表达式任意次
    private void sortIndicators(Map<String, ArrayList<String>> result, ArrayList<String> indicatorNames, ArrayList<String> indicatorKeys) {
        ArrayList<String> sortKeys = result.get("protocolKeys");
        ArrayList<String> sortNames = result.get("protocolNames");
        //sortKeys.size() != sortNames.size()是什么
        if (sortKeys == null || sortNames == null || sortKeys.size() != sortNames.size()) {
            toast.showLong(activity, "没有控制器");
            return;
        }
        indicatorNames.clear();
        indicatorKeys.clear();
        int i = findSpecialIndicator(sortNames, "^土壤湿度.*");
        if (i != -1) {
            indicatorNames.add("喷淋控制器");
            indicatorKeys.add(sortKeys.get(i));
            sortNames.remove(i);
            sortKeys.remove(i);
        }
        i = findSpecialIndicator(sortNames, "^光质.*");
        if (i != -1) {
            indicatorNames.add(sortNames.get(i));
            indicatorKeys.add(sortKeys.get(i));
            sortNames.remove(i);
            sortKeys.remove(i);
        }
        i = findSpecialIndicator(sortNames, "^光强.*");
        if (i != -1) {
            indicatorNames.add(sortNames.get(i));
            indicatorKeys.add(sortKeys.get(i));
            sortNames.remove(i);
            sortKeys.remove(i);
        }
        indicatorNames.addAll(sortNames);
        indicatorKeys.addAll(sortKeys);
    }

    //对控制器的匹配
    private int findSpecialIndicator(ArrayList<String> indicatorNames, String indicator) {
        for (int i = 0; i < indicatorNames.size(); i++) {
            if (indicatorNames.get(i).matches(indicator)) {
                return i;
            }
        }
        return -1;
    }

    //整体初始化，也是获取到控制器之后的初始化
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
            //大于0，少于4种控制
            if (size < 5) {
                for (int i = 0; i < size; i++) {
                    //设备控制的view,包括一张图和一个textview，合成一个按钮
                    view = activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_indicator_btn, row, false);
                    imageView = (ImageView) view.findViewById(R.id.indicator_image);
                    setIconWithType(imageView, indicatorNames.get(i), false);
                    textView = (TextView) view.findViewById(R.id.indicator_textview);
                    textView.setText(cutIndicatorName(indicatorNames.get(i)));
                    view.setTag(i);
                    //实现不同功能的控制
                    view.setOnClickListener(new indicatorPressedListener());
                    row.addView(view);
                }
                tableLayout.addView(row);
            } else {
                //5种或以上的控制
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
                //更多的功能键
                {
                    view = activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_indicator_btn, row, false);
                    imageView = (ImageView) view.findViewById(R.id.indicator_image);
                    imageView.setImageResource(R.drawable.icon_indicator_others);
                    textView = (TextView) view.findViewById(R.id.indicator_textview);
                    textView.setText("其它");
                    view.setTag(MAGIC_NUMBER);
                    view.setOnClickListener(new indicatorPressedListener());
                    row.addView(view);
                }
                tableLayout.addView(row);
            }
        }
        paramFirstShow();
    }

    //依照控制器类型设置图片
    private void setIconWithType(ImageView iconIV, String type, boolean pressed) {
        if (type.matches("^喷淋.*")) {
            iconIV.setImageResource(R.drawable.icon_jiaoshui);
        } else if (type.matches("^光质.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_guangzhibi);
            } else {
                iconIV.setImageResource(R.drawable.icon_guangzhibi_pressed);
            }
        } else if (type.matches("^光强.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_sun);
            } else {
                iconIV.setImageResource(R.drawable.icon_sun_pressed);
            }
        } else if (type.matches("^温度.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_wendu);
            } else {
                iconIV.setImageResource(R.drawable.icon_wendu_pressed);
            }
        } else if (type.matches("^湿度.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_shidu);
            } else {
                iconIV.setImageResource(R.drawable.icon_shidu_pressed);
            }
        } else if (type.matches("^二氧化碳.*")) {
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
        } else if (type.matches("^土壤酸碱.*")) {
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
        } else if (type.matches("^空气温度.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_wendu);
            } else {
                iconIV.setImageResource(R.drawable.icon_wendu_pressed);
            }
        } else if (type.matches("^空气湿度.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_shidu);
            } else {
                iconIV.setImageResource(R.drawable.icon_shidu_pressed);
            }
        } else if (type.matches("^土壤温度.*")) {
            if (!pressed) {
                iconIV.setImageResource(R.drawable.icon_wendu);
            } else {
                iconIV.setImageResource(R.drawable.icon_wendu_pressed);
            }
        } else if (type.matches("^土壤湿度.*")) {
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

    //截取控制器名称
    private String cutIndicatorName(String name) {
        Pattern p = Pattern.compile("(.*)控制器");
        Matcher m = p.matcher(name);
        boolean isMatched = m.matches();
        if (isMatched) {
            return m.group(1);
        } else {
            return "名字有误";
        }
    }

    //控制器监听
    private class indicatorPressedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int choice = (int) v.getTag();
            if (choice == MAGIC_NUMBER) {     //选中popupMenu按钮
                showPopupMenu(v);
                pressIndicatorMenu(3, indicatorKeys.size());
            } else if (choice < indicatorKeys.size()) {
                if (indicatorKeys.get(choice).equals("shc")) {    //选中喷淋按钮
                    water();
                } else {   //选中其它按钮，
                    currentIndex = (int) v.getTag();
                    //显示相应的view
                    indicatorShow(currentIndex);
                    pressIndicatorMenu(currentIndex, indicatorKeys.size());
                }
            }
        }
    }

    //监听选中的控制器按钮，分别显示光质比等设置界面
    private void indicatorShow(int index) {
        //单位设置
        setParamsUnit(indicatorKeys.get(index));
        //lqc光质
        if (indicatorKeys.get(index).equals("lqc")) {
            initParamLqc();
        }
        //lc光强
        else if (indicatorKeys.get(index).equals("lc")) {
            initParamOthers(6000);
        }
        //土养湿度控制？有phc?，二氧化碳是什么？
        else if (indicatorKeys.get(index).equals("phc")) {
            initParamOthers(10);
        }
        //其他
        else {
            initParamOthers(100);
        }
    }

    //设置控制的单位，比如设置光强的单位为lux
    private void setParamsUnit(String indicator) {
        if (indicator.equals("lc")) {
            targetUnit.setText("/lux");
            upperUnit.setText("/lux");
            lowerUnit.setText("/lux");
        } else if (indicator.equals("tc")) {
            targetUnit.setText("/°C");
            upperUnit.setText("/°C");
            lowerUnit.setText("/°C");
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

    //按下控制器按钮时调用的方法，该方法鲁棒性有待增强
    //index,选中的功能的下标，size，一共有几种功能可选
    private void pressIndicatorMenu(int index, int size) {
        if (schemeDefaultIndicatorLayout.getChildAt(0) instanceof TableRow) {
            //获取要显示的功能，喷淋，光质，等等
            TableRow tableRow = (TableRow) schemeDefaultIndicatorLayout.getChildAt(0);
            if (tableRow.getChildAt(index) instanceof LinearLayout) {
                //获取点击的功能，这个按钮由一张图和一个textview组成
                LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(index);
                if (linearLayout.getChildAt(0) instanceof ImageView &&
                        linearLayout.getChildAt(1) instanceof TextView) {
                    ImageView imageView = (ImageView) linearLayout.getChildAt(0);
                    TextView textView = (TextView) linearLayout.getChildAt(1);
                    switch (index) {
                        case 0:
                            //设置对应的图片
                            setIconWithType(imageView, textView.getText().toString(), true);
                            textView.setTextColor(getResources().getColor(R.color.green_1));

                            if (size > 1) {
                                //按钮1
                                linearLayout = (LinearLayout) tableRow.getChildAt(1);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }

                            if (size > 2) {
                                //按钮2
                                linearLayout = (LinearLayout) tableRow.getChildAt(2);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }

                            if (size > 3) {
                                //按钮3
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
                            //按钮0
                            linearLayout = (LinearLayout) tableRow.getChildAt(0);
                            imageView = (ImageView) linearLayout.getChildAt(0);
                            textView = (TextView) linearLayout.getChildAt(1);
                            setIconWithType(imageView, textView.getText().toString(), false);
                            textView.setTextColor(getResources().getColor(R.color.black));
                            if (size > 2) {
                                //按钮2
                                linearLayout = (LinearLayout) tableRow.getChildAt(2);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }
                            if (size > 3) {
                                //按钮3
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
                            //按钮0
                            linearLayout = (LinearLayout) tableRow.getChildAt(0);
                            imageView = (ImageView) linearLayout.getChildAt(0);
                            textView = (TextView) linearLayout.getChildAt(1);
                            setIconWithType(imageView, textView.getText().toString(), false);
                            textView.setTextColor(getResources().getColor(R.color.black));
                            if (size > 1) {
                                //按钮1
                                linearLayout = (LinearLayout) tableRow.getChildAt(1);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }
                            if (size > 3) {
                                //按钮3
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
                            //按钮0
                            linearLayout = (LinearLayout) tableRow.getChildAt(0);
                            imageView = (ImageView) linearLayout.getChildAt(0);
                            textView = (TextView) linearLayout.getChildAt(1);
                            setIconWithType(imageView, textView.getText().toString(), false);
                            textView.setTextColor(getResources().getColor(R.color.black));
                            if (size > 1) {
                                //按钮1
                                linearLayout = (LinearLayout) tableRow.getChildAt(1);
                                imageView = (ImageView) linearLayout.getChildAt(0);
                                textView = (TextView) linearLayout.getChildAt(1);
                                setIconWithType(imageView, textView.getText().toString(), false);
                                textView.setTextColor(getResources().getColor(R.color.black));
                            }

                            if (size > 2) {
                                //按钮2
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

    //当控制器多于4项时，隐藏除前面3项外的其它项
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

    //第一次进入时参数布局的显示
    private void paramFirstShow() {
        if (indicatorKeys != null && !indicatorKeys.isEmpty()) {
            //第一项是土壤湿度控制器且控制器个数多于1时，显示第二项
            //因为喷淋那个是个dialog
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

    //浇水
    private void water() {
        if (!isWater) {
            setWaterTimes();
        } else {
            waterRoom.waterOffByBtn(equipmentCodes,
                    WATERCONTROLLER, clientId);
            showDialog("正在关闭喷淋");
        }
        //isWater = !isWater;
    }

    //设置浇水时间，已经更改了新的布局
    private void setWaterTimes() {
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        final View contentView = View.inflate(activity, R.layout.view_water_button, null);
        //设置在屏幕中的显示的比例
        baseDialog.setWidthAndHeightRadio(0.8f, 0.4f);
        //设置居中显示dialog并不设置偏移量
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

                //只需要判空
                if (waterTimeSecsMsg.equals("") || waterTimeSecsMsg == null ||
                        waterTimeMinsMsg.equals("") || waterTimeMinsMsg == null
                        || waterTimeHoursMsg.equals("") || waterTimeHoursMsg == null) {
                    ToastUtil.showShort(activity, "请输入时长");
                    return;
                }

                int totalTime = Integer.parseInt(waterTimeHoursMsg) * 3600 +
                        Integer.parseInt(waterTimeMinsMsg) * 60 +
                        Integer.parseInt(waterTimeSecsMsg);

                String waterMessage = String.valueOf(totalTime);

                waterRoom.waterOn(equipmentCodes, WATERCONTROLLER, clientId, waterMessage);
                showDialog("正在打开喷淋");
                baseDialog.dismiss();
            }
        });
        //设置dialog显示的标题
        baseDialog.setTitle(equipmentCodes + "");
        baseDialog.setIcon(R.drawable.icon_water_on_dialog);
        //设置contentView在dialog中显示的布局参数
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        //匹配dialog与contentView
        baseDialog.setContentView(contentView, contentLp);
    }

    //正在浇水，点亮图片
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

    //浇水完成，熄灭图片
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

    //转换浇水按钮状态
    private void reverseWaterBtn(String code) {
        isWater = !isWater;
    }

    //显示特定文本信息的对话框
    private void showDialog(String text) {
        dialog.setMessage(text);
        dialog.show();
    }

    //关掉对话框
    public void dismissDialog() {
        dialog.dismiss();
    }

    //初始化光质比参数布局
    private void initParamLqc() {
        schemeDefaultParameterLayout.removeAllViews();
        schemeDefaultParameterLayout.addView(lqcLayout);
    }

    //光质比视图，listview
    private void lqcListView(ListView listView, String[] itemString, int[] itemColor) {
        List<Map<String, Object>> listItems = new ArrayList<>();
        //光质比的图标和说明
        for (int i = 0; i < itemString.length; i++) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("lqc_item_imageview", itemColor[i]);
            listItem.put("lqc_item_textview", itemString[i]);
            listItems.add(listItem);
        }
        //自定义的图标和说明
        Map<String, Object> listItem = new HashMap<>();
        listItem.put("lqc_item_imageview", R.drawable.icon_config);
        listItem.put("lqc_item_textview", "自定义");
        listItems.add(listItem);

        SimpleAdapter simpleAdapter = new SimpleAdapter(activity, listItems, R.layout.fragment_scheme_new_parameter_lqc_item,
                new String[]{"lqc_item_imageview", "lqc_item_textview"}, new int[]{R.id.lqc_item_iamgeview, R.id.lqc_item_textview});
        listView.setAdapter(simpleAdapter);
    }

    //初始化其它参数布局
    private void initParamOthers(int theMaxValue) {
        schemeDefaultParameterLayout.removeAllViews();
        upperSeekBar.setMax(theMaxValue);
        targetSeekBar.setMax(theMaxValue);
        lowerSeekBar.setMax(theMaxValue);
        schemeDefaultParameterLayout.addView(othersParamLayout);
    }

    //上传控制方案
    private void uploadScheme(String indicator) {
        if (fillingParams(indicator)) {
            fillingTime();
        }
    }

    //控制选择时间dialog，已经更改了新的布局
    private void fillingTime() {
        String[] items = new String[]{"1分钟", "3分钟", "5分钟", "10分钟", "自定义"};
        startTime = "";
        endTime = "";
        //获取摆放到dialog中的listView
        View contentView = View.inflate(activity, R.layout.dialog_water_choose_time, null);
        ListView listTime = (ListView) contentView.findViewById(R.id.id_dialog_water_choose_time_list);
        //设置listView的adapter
        final DialogTimeListAdapter adapter = new DialogTimeListAdapter(activity, items);
        listTime.setAdapter(adapter);
        //为listView的item设置点击时换图片
        listTime.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            //view为item复用的convertView
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //先清空所有的图片
                adapter.removeAllViews();
                //再设置点击的item的图片变化
                ((ImageView) view.findViewById(R.id.id_water_choose_time_list_img)).setImageResource(R.drawable.dialog_img_select);
                countTime(position);
            }
        });

        //设置dialog的基本属性，并将contentView加入到dialog中
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
        baseDialog.setTitle("请选择持续时间");
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentLp.setMargins(0, 80, 0, 0);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        baseDialog.setContentView(contentView, contentLp);
    }

    //计算时间
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

    //自定义时间
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
                .setTitle("自定义持续时间")
                .setView(customTime)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
                        //toast.showLong(activity, "开始时间：" + startTime + "\n" + "结束时间：" + endTime);
                    }
                }).setNegativeButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }).create().show();
    }

    //耦合性很强，且没有错误检查的针对本应用的String到yyyy-MM-dd hh:MM:ss的方法
    //耦合性，模块之间的依赖关系
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

    //转换开始和结束时间的选择按钮
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

    //光质人为控制填充参数
    private Boolean fillingParams(String indicator) {
        //人为干预光质，光质比，红：蓝：白，白光这里是默认为0
        if (indicator.equals("lqc")) {
            //target = lqc.trim() + ":0";原来红蓝两路光
            target = lqc.trim();
            upper = target;
            lower = target;


        }
        //非光质人为干预，检查设置好的参数是否合理
        else {
            target = targetTextView.getText().toString();
            upper = upperTextView.getText().toString();
            lower = lowerTextView.getText().toString();


            if (Integer.parseInt(target) > Integer.parseInt(upper)) {
                toast.showShort(activity, "目标值不能大于上限值");
                return false;
            }
            if (Integer.parseInt(lower) > Integer.parseInt(target)) {
                toast.showShort(activity, "目标值不能小于上限值");
                return false;
            }
        }
        return true;
    }

    //检查光质比输入是否合法
    private boolean checkIslqcInputValid(String lqc) {
        String reg = "[0-9]:[0-9]:[0-9]";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(lqc);
        return matcher.matches();
    }

    //检查输入是否合法
    private boolean checkedIsValid(String target, String upper, String lower, String startTime, String endTime) {
        if (target == null || target.equals("")) {
            toast.showLong(activity, "参数值不能为空");
            return false;
        }
        if (upper == null || upper.equals("")) {
            toast.showLong(activity, "上限值不能为空");
            return false;
        }
        if (lower == null || lower.equals("")) {
            toast.showLong(activity, "下限值不能为空");
            return false;
        }
        if (startTime == null || startTime.equals("")) {
            toast.showLong(activity, "请选择持续时间");
            return false;
        }
        if (endTime == null || endTime.equals("")) {
            toast.showLong(activity, "请选择持续时间");
            return false;
        }
        return true;
    }

    //光质控制输入合法后上传
    private void uploadAfterChecked() {
        if (checkedIsValid(target, upper, lower, startTime, endTime)) {
            System.out.println("开始时间:" + startTime + "  结束时间:" + endTime);
            uploadAndDownloadScheme.interveneObservable(equipmentCodes,
                    startTime, endTime, indicatorKeys.get(currentIndex), target, upper, lower);
        }
    }
}
