package com.smartfarm.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.VideoView;

import com.smartfarm.activity.R;
import com.smartfarm.adapter.DialogTimeListAdapter;
import com.smartfarm.adapter.SchemeLightQualityAdapter;
import com.smartfarm.dialog.BaseCustomAlterDialog;
import com.smartfarm.event.EquipmentSelectedEvent;
import com.smartfarm.event.GlobalEvent;
import com.smartfarm.fragmentUtil.UploadAndDownloadScheme;
import com.smartfarm.model.Equipment;
import com.smartfarm.model.TimeSelector;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.DateUtil;
import com.smartfarm.util.FertilizeRoom;
import com.smartfarm.util.ToastUtil;
import com.smartfarm.util.WaterRoom;
import com.smartfarm.view.NumberPickerView;
import com.videogo.universalimageloader.utils.L;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.baidu.location.b.g.r;

public class SchemeDefaultFragment extends BaseFragment {
    Activity activity;
    View rootView;
    //util
    private ToastUtil toast;
    private UploadAndDownloadScheme uploadAndDownloadScheme;
    private WaterRoom waterRoom;
    private FertilizeRoom fertilizeRoom;
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
//    private String[] lqcText;
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
    boolean isWater;    //浇水按钮是否按下

    // TODO: 2016/9/22 测试
    boolean isFertilize;//施肥按钮是否按下

    /**
     * 浇水控制器信息
     */
    private static final String WATERCONTROLLER = "/c/shc/1";
    /**
     * 施肥控制器信息
     */
    private static final String FERTILIZECONTROLLER = "/c/fc/1";
    private String clientId = "ClientOfSmartFarm";
    //index
    private int currentIndex;   //当前选中控制器
    private final static int MAGIC_NUMBER = 10;     //代码中用到的一个自定义数字
    //EventBus
    EventHandler handler = new EventHandler();
    private final String FORMAT_STR = "yyyy-MM-dd HH:mm:ss";
    //时间格式
    private SimpleDateFormat format = new SimpleDateFormat(FORMAT_STR);

    /**
     * 根据EventBus实现点击设备控制item返回equipmentCodes
     */
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
        //喷淋控制，以及回调
        waterRoom = new WaterRoom(new WaterRoom.WaterInterface() {
            @Override
            public void waterFailed() {
                dismissDialog();
            }

            @Override
            public void watering(String equipmentCode, String controller) {
                //点亮图标
//                SchemeWatering(equipmentCode);
                schemeAppointControllering(equipmentCode, controller);
            }

            @Override
            public void waterTimeOut(String equipmentCode, String controller) {
                //熄灭图标
//                SchemeWaterTimeout(equipmentCode);
                schemeAppointControllerTimeOut(equipmentCode, controller);
            }
        });
        //施肥控制，以及回调
        fertilizeRoom = new FertilizeRoom(new FertilizeRoom.FertilizeInterface() {
            @Override
            public void FertilizeFailed() {
                dismissDialog();
            }

            @Override
            public void Fertilizing(String equipmentCode, String controller) {
                //点亮正在施肥的图标
                schemeAppointControllering(equipmentCode, controller);
            }

            @Override
            public void FertilizeTimeOut(String equipmentCode, String controller) {
                //重置是施肥的图标
                schemeAppointControllerTimeOut(equipmentCode, controller);
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
        return rootView;
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
//        lqcItemText = getResources().getStringArray(R.array.lqc_text);

        lqcItemText = getResources().getStringArray(R.array.lqc_text_test);
        //获取相应的光比的图片，保存在lqcItemColor数组中
//        getLqcItemImage();
//        lqcListView(listViewlqc, lqcItemText, lqcItemColor);

        setLqcListViewAdapter(listViewlqc, lqcItemText);
        listViewlqc.setOnItemClickListener(new lqcListener());

        othersParamLayout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_parameter_others, schemeDefaultParameterLayout, false);
        initParamOthersLayout();
    }

    /**
     * 除喷淋、光质布局下的上传按钮的监听事件
     */
    private class uploadListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (indicatorKeys != null && indicatorKeys.size() > 0)
                uploadScheme(indicatorKeys.get(currentIndex));
        }
    }

    /**
     * 获取光质比的图片id，保存到lqcItemColor的数组中
     */
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

    /**
     * 光质比列表项的监听
     */
    private class lqcListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == lqcItemText.length - 1) {
                //光质控制中的自定义按钮
                customLed();
            } else {
                //lqcItemText[position] --> 红:蓝:白 = 1:5:0
                lqc = cutLqcParam(lqcItemText[position]);
                uploadScheme("lqc");
            }
        }
    }

    /**
     * 自定义光强比dialog
     */
    private void customLed() {
        View contentView = View.inflate(activity, R.layout.fragment_scheme_new_custom_lqc, null);
        final NumberPickerView ledRed = (NumberPickerView) contentView.findViewById(R.id.scheme_custom_led_red);
        final NumberPickerView ledBlue = (NumberPickerView) contentView.findViewById(R.id.scheme_custom_led_blue);
        final NumberPickerView ledWhite = (NumberPickerView) contentView.findViewById(R.id.scheme_custom_led_white);
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
//        0.65
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        baseDialog.setWidthAndHeightRadio(0.8f, 0f)
        .setTitle("自定义光强比")
        .setNegativeBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        })
        .setPositiveBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lqc = ledRed.getValue() + ":" + ledBlue.getValue() + ":" + ledWhite.getValue();
                uploadScheme("lqc");
                baseDialog.dismiss();
            }
        })
        .setContentView(contentView, contentLp);
    }

    //因为获取的光质比参数是“光质比=1：2：3”的情形，而我们只需要1：2：3,所以需要截取
    private String cutLqcParam(String string) {
        int index = string.indexOf("=") + 1;
        if (index < string.length()) {
            return string.substring(index);
        }
        return null;
    }

    /**
     * 初始化除喷淋、光质外的统一布局
     */
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
        //上传按钮
        uploadLayout = (RelativeLayout) othersParamLayout.findViewById(R.id.upload_relate_layout);
        uploadLayout.setOnClickListener(new uploadListener());

    }

    //指标值、上限、下限点击时弹出的对话框，点击数字可以设置
    private class paramTextViewListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            View contentView = View.inflate(activity, R.layout.fragment_scheme_new_parameter_adjustment, null);
            final EditText target = (EditText) contentView.findViewById(R.id.param_target);
            final EditText upper = (EditText) contentView.findViewById(R.id.param_upper);
            final EditText lower = (EditText) contentView.findViewById(R.id.param_lower);
            final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            baseDialog.setLocation(Gravity.CENTER, 0, 0)
            .setTitle("参数调整")
//            0.4
            .setWidthAndHeightRadio(0.8f, 0f)
            .setNegativeBtnListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    baseDialog.dismiss();
                }
            })
            .setPositiveBtnListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int targetTmp = getValidAdjustment(target);
                    int upperTmp = getValidAdjustment(upper);
                    int lowerTmp = getValidAdjustment(lower);
                    if (targetTmp != -1)
                        targetSeekBar.setProgress(targetTmp);
                    if (upperTmp != -1)
                        upperSeekBar.setProgress(upperTmp);
                    if (lowerTmp != -1)
                        lowerSeekBar.setProgress(lowerTmp);
                    baseDialog.dismiss();
                }
            })
            .setContentView(contentView, lp);
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

    /**
     * 获取当前设备号下所有的控制器
     */
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

    /**
     * 隐藏还没有选择设备时的提示
     */
    private void noIndicatorHide() {
        noIndicatorText.setVisibility(View.GONE);
    }

    //没有控制器时需要显示的信息
    private void noIndicatorShow() {
        noIndicatorText.setVisibility(View.VISIBLE);
    }

    /**
     * 对控制器的结果进行排序和布局
     *
     * @param result
     */
    private void indicatorsResult(Map<String, ArrayList<String>> result) {
        //控制器排序
        sortIndicators(result, indicatorNames, indicatorKeys);
        //对布局进行初始化
        initIndicatorLayout(schemeDefaultIndicatorLayout, indicatorNames);
    }


    /**
     * 1、将result里面的结果拆分保存在全局变量indicatorNames和indicatorKeys中
     * 2、并对indicator的显示位置进行排序
     * 3、^匹配输入字符串的开始位置
     * .要匹配包括“\r\n”在内的任何字符
     * *匹配前面的子表达式任意次
     *
     * @param result
     * @param indicatorNames
     * @param indicatorKeys
     */
    private void sortIndicators(Map<String, ArrayList<String>> result, ArrayList<String> indicatorNames, ArrayList<String> indicatorKeys) {
        ArrayList<String> sortKeys = result.get("protocolKeys");
        ArrayList<String> sortNames = result.get("protocolNames");

        //判断keys与names数量是否一致，keys与names是否为空
        if (sortKeys == null || sortNames == null || sortKeys.size() != sortNames.size()) {
            toast.showLong(activity, "没有控制器");
            return;
        }
        /**
         * 要先清空是因为每次点击不同设备都是不同的设备码，请求回来的控制器种类不一样
         */
        indicatorNames.clear();
        indicatorKeys.clear();
        /**
        * 先将土壤湿度，施肥、光质，光强匹配，优先显示
         */
        Log.i("gzfuzhi","result:"+result);
        int i = findSpecialIndicator(sortNames, "^土壤湿度.*");
        if (i != -1) {
            //没有将“土壤湿度控制器”写入数组，用“喷淋控制器”这个名称覆盖了
            indicatorNames.add("喷淋控制器");
            indicatorKeys.add(sortKeys.get(i));
            //sortNames.remove(i)是为了提高关键字查控制器的算法效率
            sortNames.remove(i);
            //sortKeys.remove(i)是为了与sortNames下标对应
            sortKeys.remove(i);
        }
        //---------------------加入施肥控制器,排在了第二------------------------------
        // TODO: 2016/9/22 要验证能否加入施肥控制器
        i = findSpecialIndicator(sortNames, "^施肥.*");
        if (i != -1) {
            indicatorNames.add(sortNames.get(i));
            indicatorKeys.add(sortKeys.get(i));
            sortNames.remove(i);
            sortKeys.remove(i);
        }
        //--------------------------------------------------------------------------

        i = findSpecialIndicator(sortNames, "^光质.*");
        if (i != -1) {
            indicatorNames.add("光强比控制器");
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
        //因为下标本来已经匹配好，所以直接addAll()将剩下的keys与names添加
        indicatorNames.addAll(sortNames);
        indicatorKeys.addAll(sortKeys);
    }

    /**
     * 根据关键字找到对应控制器的名字及下标
     *
     * @param indicatorNames 控制器的名字数组
     * @param indicator      控制器的关键字
     * @return
     */
    private int findSpecialIndicator(ArrayList<String> indicatorNames, String indicator) {
        for (int i = 0; i < indicatorNames.size(); i++) {
            if (indicatorNames.get(i).matches(indicator)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 对布局进行初始化，一个tableLayout下包含一个tableRow，包含多个LinearLayout
     *
     * @param tableLayout    要摆放tableRow的布局
     * @param indicatorNames 已经排序好的控制器名称
     */
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
            //大于0，最多4种控制（目前最少4种）
            if (size < 5) {
                for (int i = 0; i < size; i++) {
                    //设备控制的view,包括一张图和一个textview，合成一个按钮
                    view = activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_indicator_btn, row, false);
                    imageView = (ImageView) view.findViewById(R.id.indicator_image);
                    setIconWithType(imageView, indicatorNames.get(i), false);
                    textView = (TextView) view.findViewById(R.id.indicator_textview);
                    textView.setText(cutIndicatorName(indicatorNames.get(i)));
                    //为了区分点击事件中点击的item所以要setTag(),用显示的位置做标志
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
        //初始化完成后显示的哪个tableRow被点击
        paramFirstShow();
    }

    /**
     * 按照控制器的类型与是否按下为imgView设置src
     *
     * @param iconIV  imgView
     * @param type    控制器类型
     * @param pressed 是否按下
     */
    private void setIconWithType(ImageView iconIV, String type, boolean pressed) {
        if (type.matches("^喷淋.*")) {
            iconIV.setImageResource(R.drawable.icon_jiaoshui);
        }
        //---------------------加入施肥icon显示的判断----------------------
        else if (type.matches("^施肥.*")) {
            iconIV.setImageResource(R.drawable.icon_shifei);
        }
        //因为将光质控制器改成了光强比控制器，所以这里也要更改
        else if (type.matches("^光强比.*")) {
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

    /**
     * 截取控制器的名称
     *
     * @param name 格式为 XXX控制器
     * @return 控制器的名称
     */
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
            //选中popupMenu按钮
            if (choice == MAGIC_NUMBER) {
                showPopupMenu(v);
                pressIndicatorMenu(3, indicatorKeys.size());

            } else if (choice < indicatorKeys.size()) {
                if (indicatorKeys.get(choice).equals("shc")) {    //选中喷淋按钮
                    water();
                } else if (indicatorKeys.get(choice).equals("fc")) {
                    // TODO: 2016/9/22 测试，弹出施肥的dialog
                    fertilize();
                } else {   //选中除喷淋、施肥、popupMenu的按钮
                    currentIndex = (int) v.getTag();
                    //显示相应的view
                    indicatorShow(currentIndex);
                    pressIndicatorMenu(currentIndex, indicatorKeys.size());
                }
            }
        }
    }

    /**
     * 点击不同的indicator显示不同的界面，设置单位
     *
     * @param index 对应indicator的下标(已经排好序)
     */
    private void indicatorShow(int index) {
        //根据key设置单位
        setParamsUnit(indicatorKeys.get(index));
        //lqc光质
        if (indicatorKeys.get(index).equals("lqc")) {
            initParamLqc();
        }
        //lc光强
        else if (indicatorKeys.get(index).equals("lc")) {
            initParamOthers(6000);
        }
        //phc 土壤PH值
        else if (indicatorKeys.get(index).equals("phc")) {
            initParamOthers(10);
        }
        //co2c 二氧化碳
        else if(indicatorKeys.get(index).equals("co2c")){
            initParamOthers(6000);
        }
        //其他
        else {
            initParamOthers(100);
        }
    }

    /**
     * 根据控制器的key设置单位
     *
     * @param indicatorKey
     */
    private void setParamsUnit(String indicatorKey) {
        if (indicatorKey.equals("lc")) {
            targetUnit.setText("/lux");
            upperUnit.setText("/lux");
            lowerUnit.setText("/lux");
        } else if (indicatorKey.equals("tc")) {
            targetUnit.setText("/°C");
            upperUnit.setText("/°C");
            lowerUnit.setText("/°C");
        } else if (indicatorKey.equals("hc")) {
            targetUnit.setText("/%");
            upperUnit.setText("/%");
            lowerUnit.setText("/%");
        } else {
            targetUnit.setText("");
            upperUnit.setText("");
            lowerUnit.setText("");
        }
    }

    //按下控制器按钮时调用的方法，该方法鲁棒性有待增强（稳定性）
    //index,选中的功能的下标，size，一共有几种功能可选
    private void pressIndicatorMenu(int index, int size) {
        if (schemeDefaultIndicatorLayout.getChildAt(0) instanceof TableRow) {
            TableRow tableRow = (TableRow) schemeDefaultIndicatorLayout.getChildAt(0);
            if (tableRow.getChildAt(index) instanceof LinearLayout) {
                //获取index下的item，方便更改图标和字体颜色
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

    /**
     * 点击其他按钮时，显示PopupMenu
     *
     * @param view
     */
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(activity, view);
        Menu menu = popupMenu.getMenu();
        for (int i = 3; i < indicatorKeys.size(); i++) {
            //menu.add(groupId,itemId,itemName);
            //已经设置了order为First+i -->4开始
            menu.add(Menu.NONE, Menu.FIRST + i, i, indicatorNames.get(i));
        }
        activity.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new popupMenuListener());
        popupMenu.show();
    }

    private class popupMenuListener implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            //item.getOrder()的值为4及4以上
            indicatorShow(item.getOrder());
            currentIndex = item.getOrder();
            return true;
        }
    }

    /**
     * 决定哪个tableRow被显示
     * 设计扩展性不好，应该为每个item设置标签？，方便分类
     */
    private void paramFirstShow() {
        /**
         * 分3种情况
         * 1：喷淋、施肥
         * 2：喷淋
         * 3：施肥
         */
        if (indicatorKeys != null && !indicatorKeys.isEmpty()) {
            //第一项是土壤湿度控制器且控制器个数多于1时，显示第二项
            //因为喷淋那个是个dialog
            //情况1
            if (indicatorKeys.get(0).equals("shc") && indicatorKeys.get(1).equals("fc") && indicatorKeys.size() > 2) {
                indicatorShow(2);
                currentIndex = 2;
                pressIndicatorMenu(2, indicatorKeys.size());
            }
            //情况2、3
            else if ((indicatorKeys.get(0).equals("shc") && indicatorKeys.size() > 1) || (indicatorKeys.get(0).equals("fc") && indicatorKeys.size() > 1)) {
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

    //设置浇水时间
    private void setWaterTimes() {
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        final View contentView = View.inflate(activity, R.layout.view_water_button, null);

        final EditText waterTimeHours = (EditText) contentView.findViewById(R.id.et_water_time_hours);
        final EditText waterTimeMins = (EditText) contentView.findViewById(R.id.et_water_time_minutes);
        final EditText waterTimeSecs = (EditText) contentView.findViewById(R.id.et_water_time_seconds);
        //设置contentView在dialog中显示的布局参数
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        //设置在屏幕中的显示的比例
        baseDialog.setWidthAndHeightRadio(0.8f, 0f)
        //设置居中显示dialog并不设置偏移量
        .setLocation(Gravity.CENTER, 0, 0)
        .setNegativeBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        })
        .setPositiveBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String waterTimeHoursMsg = waterTimeHours.getText().toString();
                String waterTimeMinsMsg = waterTimeMins.getText().toString();
                String waterTimeSecsMsg = waterTimeSecs.getText().toString();

                //三者为空则提示输入时长
                if ((waterTimeHoursMsg.equals("") || waterTimeHoursMsg == null)
                        && (waterTimeMinsMsg.equals("") || waterTimeMinsMsg == null)
                        && (waterTimeSecsMsg.equals("") || waterTimeSecsMsg == null)) {
                    ToastUtil.showShort(activity, "请输入时长");
                    return;
                }
                //以下情况是有其中没有输入时自动补零的情况
                else {
                    if (waterTimeSecsMsg.equals("") || waterTimeSecsMsg == null) {
                        waterTimeSecsMsg = "00";
                    }

                    if (waterTimeMinsMsg.equals("") || waterTimeMinsMsg == null) {
                        waterTimeMinsMsg = "00";
                    }

                    if (waterTimeHoursMsg.equals("") || waterTimeHoursMsg == null) {
                        waterTimeHoursMsg = "00";
                    }
                }

                int totalTime = Integer.parseInt(waterTimeHoursMsg) * 3600 +
                        Integer.parseInt(waterTimeMinsMsg) * 60 +
                        Integer.parseInt(waterTimeSecsMsg);

                String waterMessage = String.valueOf(totalTime);

                waterRoom.waterOn(equipmentCodes, WATERCONTROLLER, clientId, waterMessage);
                showDialog("正在打开喷淋");
                baseDialog.dismiss();
            }
        })
        //设置dialog显示的标题
        .setTitle(equipmentCodes + "")
        .setIcon(R.drawable.icon_water_on_dialog)
        //匹配dialog与contentView
        .setContentView(contentView, contentLp);
    }

//    //正在浇水，点亮图片
//    public void SchemeWatering(String equipmentCode) {
//        if (indicatorKeys.get(0).equals("shc")) {
//            if (schemeDefaultIndicatorLayout.getChildAt(0) instanceof TableRow) {
//                TableRow tableRow = (TableRow) schemeDefaultIndicatorLayout.getChildAt(0);
//                if (tableRow.getChildAt(0) instanceof LinearLayout) {
//                    LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(0);
//                    if (linearLayout.getChildAt(0) instanceof ImageView) {
//                        ImageView imageView = (ImageView) linearLayout.getChildAt(0);
//                        imageView.setImageResource(R.drawable.icon_water_on);
//                    }
//                }
//            }
//        }
//        isWatering = true;
//        reverseWaterBtn(equipmentCode);
//        dismissDialog();
//    }

    /**
     * 根据设备标志位fc、shc等来设置点亮的图片，设备编码没有用上
     * 嵌套关系是Indicatorlayout->row->线性布局(n)->img+text
     *
     * @param equipmentCode 设备的编码
     * @param controller    /c/fc/1的样式
     */
    public void schemeAppointControllering(String equipmentCode, String controller) {
        //逻辑已经写好了浇水摆在第一个item的位置，施肥摆在了第(一)二个item的位置
        if (controller.contains("shc")) {
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
            reverseControllerBtn(equipmentCode, "shc");
            dismissDialog();
        }else if (controller.contains("fc")) {
            if (indicatorKeys.get(0).equals("fc")) {
                if (schemeDefaultIndicatorLayout.getChildAt(0) instanceof TableRow) {
                    TableRow tableRow = (TableRow) schemeDefaultIndicatorLayout.getChildAt(0);
                    if (tableRow.getChildAt(0) instanceof LinearLayout) {
                        LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(0);
                        if (linearLayout.getChildAt(0) instanceof ImageView) {
                            ImageView imageView = (ImageView) linearLayout.getChildAt(0);
                            imageView.setImageResource(R.drawable.icon_fertilize_on);
                        }
                    }
                }
            } else if (indicatorKeys.get(1).equals("fc")) {
                if (schemeDefaultIndicatorLayout.getChildAt(0) instanceof TableRow) {
                    TableRow tableRow = (TableRow) schemeDefaultIndicatorLayout.getChildAt(0);
                    if (tableRow.getChildAt(1) instanceof LinearLayout) {
                        LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(1);
                        if (linearLayout.getChildAt(0) instanceof ImageView) {
                            ImageView imageView = (ImageView) linearLayout.getChildAt(0);
                            imageView.setImageResource(R.drawable.icon_fertilize_on);
                        }
                    }
                }
            }
            reverseControllerBtn(equipmentCode, "fc");
            dismissDialog();
        }
    }

    /**
     * 根据设备标志位fc、shc等来设置熄灭的图片，设备编码没有用上
     *
     * @param equipmentCode
     * @param controller
     */
    public void schemeAppointControllerTimeOut(String equipmentCode, String controller) {
        if (controller.contains("shc")) {
            //设置shc样式
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
            reverseControllerBtn(equipmentCode, "shc");
            dismissDialog();
        } else if (controller.contains("fc")) {
            //设置fc的样式
            if (indicatorKeys.get(0).equals("fc")) {
                if (schemeDefaultIndicatorLayout.getChildAt(0) instanceof TableRow) {
                    TableRow tableRow = (TableRow) schemeDefaultIndicatorLayout.getChildAt(0);
                    if (tableRow.getChildAt(0) instanceof LinearLayout) {
                        LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(0);
                        if (linearLayout.getChildAt(0) instanceof ImageView) {
                            ImageView imageView = (ImageView) linearLayout.getChildAt(0);
                            imageView.setImageResource(R.drawable.icon_shifei);
                        }
                    }
                }
            } else if (indicatorKeys.get(1).equals("fc")) {
                if (schemeDefaultIndicatorLayout.getChildAt(0) instanceof TableRow) {
                    TableRow tableRow = (TableRow) schemeDefaultIndicatorLayout.getChildAt(0);
                    if (tableRow.getChildAt(1) instanceof LinearLayout) {
                        LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(1);
                        if (linearLayout.getChildAt(0) instanceof ImageView) {
                            ImageView imageView = (ImageView) linearLayout.getChildAt(0);
                            imageView.setImageResource(R.drawable.icon_shifei);
                        }
                    }
                }
            }
            reverseControllerBtn(equipmentCode, "fc");
            dismissDialog();
        }
    }

    /**
     * 转换淋水和施肥的状态，方便以后增加
     *
     * @param equipmentCode
     * @param controller
     */
    public void reverseControllerBtn(String equipmentCode, String controller) {
        switch (controller) {
            case "shc":
                isWater = !isWater;
                break;
            case "fc":
                isFertilize = !isFertilize;
                break;
        }
    }
//    //浇水完成，熄灭图片
//    public void SchemeWaterTimeout(String equipmentCode) {
//        if (indicatorKeys.get(0).equals("shc")) {
//            if (schemeDefaultIndicatorLayout.getChildAt(0) instanceof TableRow) {
//                TableRow tableRow = (TableRow) schemeDefaultIndicatorLayout.getChildAt(0);
//                if (tableRow.getChildAt(0) instanceof LinearLayout) {
//                    LinearLayout linearLayout = (LinearLayout) tableRow.getChildAt(0);
//                    if (linearLayout.getChildAt(0) instanceof ImageView) {
//                        ImageView imageView = (ImageView) linearLayout.getChildAt(0);
//                        imageView.setImageResource(R.drawable.icon_jiaoshui);
//                    }
//                }
//            }
//        }
//        isWatering = false;
//
//        reverseWaterBtn(equipmentCode);
//
//        dismissDialog();
//    }
//
//    //转换浇水按钮状态
//    private void reverseWaterBtn(String equipmentCode) {
//        isWater = !isWater;
//    }

    /**
     * 调用施肥
     */
    private void fertilize() {
        if (!isFertilize) {
            setFertilizeTimes();
        } else {
            fertilizeRoom.FertilizeOffByBtn(equipmentCodes, FERTILIZECONTROLLER, clientId);
            showDialog("正在关闭施肥");
        }
    }

    /**
     * 设置施肥时间
     */
    public void setFertilizeTimes() {
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        final View contentView = View.inflate(activity, R.layout.view_fertilize_button, null);

        final EditText fertilizeTimeHours = (EditText) contentView.findViewById(R.id.et_fertilize_time_hours);
        final EditText fertilizeTimeMins = (EditText) contentView.findViewById(R.id.et_fertilize_time_minutes);
        final EditText fertilizeTimeSecs = (EditText) contentView.findViewById(R.id.et_fertilize_time_seconds);
        //设置contentView在dialog中显示的布局参数
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        //设置在屏幕中的显示的比例
        baseDialog.setWidthAndHeightRadio(0.8f, 0f)
        //设置居中显示dialog并不设置偏移量
        .setLocation(Gravity.CENTER, 0, 0)
        .setNegativeBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        })
        .setPositiveBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fertilizeTimeHoursMsg = fertilizeTimeHours.getText().toString();
                String fertilizeTimeMinsMsg = fertilizeTimeMins.getText().toString();
                String fertilizeTimeSecsMsg = fertilizeTimeSecs.getText().toString();

                //三者为空则提示输入时长
                if ((fertilizeTimeHoursMsg.equals("") || fertilizeTimeHoursMsg == null)
                        && (fertilizeTimeMinsMsg.equals("") || fertilizeTimeMinsMsg == null)
                        && (fertilizeTimeSecsMsg.equals("") || fertilizeTimeSecsMsg == null)) {
                    ToastUtil.showShort(activity, "请输入时长");
                    return;
                }
                //以下情况是有其中没有输入时自动补零的情况
                else {
                    if (fertilizeTimeSecsMsg.equals("") || fertilizeTimeSecsMsg == null) {
                        fertilizeTimeSecsMsg = "00";
                    }

                    if (fertilizeTimeMinsMsg.equals("") || fertilizeTimeMinsMsg == null) {
                        fertilizeTimeMinsMsg = "00";
                    }

                    if (fertilizeTimeHoursMsg.equals("") || fertilizeTimeHoursMsg == null) {
                        fertilizeTimeHoursMsg = "00";
                    }
                }

                int totalTime = Integer.parseInt(fertilizeTimeHoursMsg) * 3600 +
                        Integer.parseInt(fertilizeTimeMinsMsg) * 60 +
                        Integer.parseInt(fertilizeTimeSecsMsg);

                String fertilizeMessage = String.valueOf(totalTime);
                //发布施肥消息
                fertilizeRoom.FertilizeOn(equipmentCodes, FERTILIZECONTROLLER, clientId, fertilizeMessage);
                showDialog("正在打开施肥");
                baseDialog.dismiss();
            }
        })
        //设置dialog显示的标题
        .setTitle(equipmentCodes + "")
        .setIcon(R.drawable.icon_fertilize_on_dialog)
        //匹配dialog与contentView
        .setContentView(contentView, contentLp);
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

//    /**
//     * 光质listView，配置adapter显示
//     * @param listView 需要显示的listView
//     * @param itemString 每个默认图片对应的文字，即X：X：X
//     * @param itemColor 每个默认图片 即R.drawable.X
//     */
//    private void lqcListView(ListView listView, String[] itemString, int[] itemColor) {
//        List<Map<String, Object>> listItems = new ArrayList<>();
//        //光质比的图标和说明
//        for (int i = 0; i < itemString.length; i++) {
//            Map<String, Object> listItem = new HashMap<>();
//            listItem.put("lqc_item_imageview", itemColor[i]);
//            listItem.put("lqc_item_textview", itemString[i]);
//            listItems.add(listItem);
//        }
//        //自定义的图标和说明
//        Map<String, Object> listItem = new HashMap<>();
//        listItem.put("lqc_item_imageview", R.drawable.icon_config);
//        listItem.put("lqc_item_textview", "自定义");
//        listItems.add(listItem);
//
//        SimpleAdapter simpleAdapter = new SimpleAdapter(activity, listItems, R.layout.fragment_scheme_new_parameter_lqc_item,
//                new String[]{"lqc_item_imageview", "lqc_item_textview"}, new int[]{R.id.lqc_item_iamgeview, R.id.lqc_item_textview});
//        listView.setAdapter(simpleAdapter);
//    }

    private void setLqcListViewAdapter(ListView listView, String[] itemTexts) {
        SchemeLightQualityAdapter listAdapter = new SchemeLightQualityAdapter(activity, itemTexts);
        listView.setAdapter(listAdapter);
    }

    /**
     * 初始化除了光质、喷淋界面下的其他布(布局都是统一)
     *
     * @param theMaxValue 可调节的最大值
     */
    private void initParamOthers(int theMaxValue) {
        schemeDefaultParameterLayout.removeAllViews();
        upperSeekBar.setMax(theMaxValue);
        targetSeekBar.setMax(theMaxValue);
        lowerSeekBar.setMax(theMaxValue);
//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams()
        schemeDefaultParameterLayout.addView(othersParamLayout);
    }

    /**
     * 上传方案，根据indicatorKey
     *
     * @param indicatorKey
     */
    private void uploadScheme(String indicatorKey) {
        //为target、upper、lower填充参数
        if (fillingParams(indicatorKey)) {
            //选择人为干预的时间
            fillingTime();
        }
    }

    /**
     * 弹出选择时间的dialog，选择人为干预的时间
     */
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
                //根据点击的listView的item剪辑startTime和endTime
                countTime(position);
            }
        });

        //设置dialog的基本属性，并将contentView加入到dialog中
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//        0.55
        baseDialog.setWidthAndHeightRadio(0.8f, 0f)
        .setLocation(Gravity.CENTER, 0, 0)
        .setNegativeBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseDialog.dismiss();
            }
        })
        .setPositiveBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检查target、upper、lower、startTime、endTime是否输入正确，并上传
                uploadAfterChecked();
                baseDialog.dismiss();
            }
        })

        .setTitle("请选择持续时间")

        .setContentView(contentView, contentLp);
    }

    /**
     * 根据点击的listView的timeDelay
     * 设置startTime和endTime
     *
     * @param position
     */
    private void countTime(int position) {
        if (position == 0) {
            timeDelay = 1;
        } else if (position == 1) {
            timeDelay = 3;
        } else if (position == 2) {
            timeDelay = 5;
        } else if (position == 3) {
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
        Calendar minCalendar=Calendar.getInstance();
        Calendar maxCalendar=Calendar.getInstance();
        int year=minCalendar.get(Calendar.YEAR)+2;
        maxCalendar.set(Calendar.YEAR, year);
        TimeSelector timeSelector = new TimeSelector(activity, new TimeSelector.ResultHandler() {
            @Override
            public void handle(String start,String end) {
                if(end!="")
                {
                    if(isRightTime(start,end))
                    {
                        startTime=start;
                        endTime=end;
                    }
                    else
                    {
                        ToastUtil.showShort(activity,"请选择正确的时间");
                    }
                }
                else
                {
                    ToastUtil.showShort(activity,"请选结束时间");
                }
            }
        }, DateUtil.format(minCalendar.getTime(), FORMAT_STR)
         , DateUtil.format(maxCalendar.getTime(), FORMAT_STR));
        timeSelector.show();
    }

    //判断输入的开始时间是否<=结束时间
    private boolean isRightTime(String start,String end){
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.setTime(DateUtil.parse(start, FORMAT_STR));
        endCalendar.setTime(DateUtil.parse(end, FORMAT_STR));
        if (startCalendar.getTime().getTime() >= endCalendar.getTime().getTime()) {
            return false;
        }
        return true;
    }

    /**
     * 为target、upper、lower填充参数
     * 当indicatorKey为"lqc"时，target为1:5:0这样的参数
     * 当indicatorKey为其他时，target为seekbar的值
     *
     * @param indicatorKey
     * @return
     */
    private Boolean fillingParams(String indicatorKey) {
        //干预光质
        if (indicatorKey.equals("lqc")) {
            //target = lqc.trim() + ":0";原来红蓝两路光
            target = lqc.trim();
            upper = target;
            lower = target;
        }
        //干预光强、温度、二氧化碳等
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
            System.out.println(equipmentCodes+","+
                    startTime+","+ endTime+","+indicatorKeys.get(currentIndex)+","+target+","+ upper+","+ lower);
            uploadAndDownloadScheme.interveneObservable(equipmentCodes,
                    startTime, endTime, indicatorKeys.get(currentIndex), target, upper, lower);
        }
    }
}
