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
//    private String[] lqcText;
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
    boolean isWater;    //��ˮ��ť�Ƿ���

    // TODO: 2016/9/22 ����
    boolean isFertilize;//ʩ�ʰ�ť�Ƿ���

    /**
     * ��ˮ��������Ϣ
     */
    private static final String WATERCONTROLLER = "/c/shc/1";
    /**
     * ʩ�ʿ�������Ϣ
     */
    private static final String FERTILIZECONTROLLER = "/c/fc/1";
    private String clientId = "ClientOfSmartFarm";
    //index
    private int currentIndex;   //��ǰѡ�п�����
    private final static int MAGIC_NUMBER = 10;     //�������õ���һ���Զ�������
    //EventBus
    EventHandler handler = new EventHandler();
    private final String FORMAT_STR = "yyyy-MM-dd HH:mm:ss";
    //ʱ���ʽ
    private SimpleDateFormat format = new SimpleDateFormat(FORMAT_STR);

    /**
     * ����EventBusʵ�ֵ���豸����item����equipmentCodes
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
        //��ȡ���Ը�Ԥ�����أ����ܣ����ʣ���ǿ��
        uploadAndDownloadScheme = new UploadAndDownloadScheme(activity);
        dialog = new BaseProgressDialog(activity);
        //���ܿ��ƣ��Լ��ص�
        waterRoom = new WaterRoom(new WaterRoom.WaterInterface() {
            @Override
            public void waterFailed() {
                dismissDialog();
            }

            @Override
            public void watering(String equipmentCode, String controller) {
                //����ͼ��
//                SchemeWatering(equipmentCode);
                schemeAppointControllering(equipmentCode, controller);
            }

            @Override
            public void waterTimeOut(String equipmentCode, String controller) {
                //Ϩ��ͼ��
//                SchemeWaterTimeout(equipmentCode);
                schemeAppointControllerTimeOut(equipmentCode, controller);
            }
        });
        //ʩ�ʿ��ƣ��Լ��ص�
        fertilizeRoom = new FertilizeRoom(new FertilizeRoom.FertilizeInterface() {
            @Override
            public void FertilizeFailed() {
                dismissDialog();
            }

            @Override
            public void Fertilizing(String equipmentCode, String controller) {
                //��������ʩ�ʵ�ͼ��
                schemeAppointControllering(equipmentCode, controller);
            }

            @Override
            public void FertilizeTimeOut(String equipmentCode, String controller) {
                //������ʩ�ʵ�ͼ��
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
//        lqcItemText = getResources().getStringArray(R.array.lqc_text);

        lqcItemText = getResources().getStringArray(R.array.lqc_text_test);
        //��ȡ��Ӧ�Ĺ�ȵ�ͼƬ��������lqcItemColor������
//        getLqcItemImage();
//        lqcListView(listViewlqc, lqcItemText, lqcItemColor);

        setLqcListViewAdapter(listViewlqc, lqcItemText);
        listViewlqc.setOnItemClickListener(new lqcListener());

        othersParamLayout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_parameter_others, schemeDefaultParameterLayout, false);
        initParamOthersLayout();
    }

    /**
     * �����ܡ����ʲ����µ��ϴ���ť�ļ����¼�
     */
    private class uploadListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (indicatorKeys != null && indicatorKeys.size() > 0)
                uploadScheme(indicatorKeys.get(currentIndex));
        }
    }

    /**
     * ��ȡ���ʱȵ�ͼƬid�����浽lqcItemColor��������
     */
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

    /**
     * ���ʱ��б���ļ���
     */
    private class lqcListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == lqcItemText.length - 1) {
                //���ʿ����е��Զ��尴ť
                customLed();
            } else {
                //lqcItemText[position] --> ��:��:�� = 1:5:0
                lqc = cutLqcParam(lqcItemText[position]);
                uploadScheme("lqc");
            }
        }
    }

    /**
     * �Զ����ǿ��dialog
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
        .setTitle("�Զ����ǿ��")
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

    //��Ϊ��ȡ�Ĺ��ʱȲ����ǡ����ʱ�=1��2��3�������Σ�������ֻ��Ҫ1��2��3,������Ҫ��ȡ
    private String cutLqcParam(String string) {
        int index = string.indexOf("=") + 1;
        if (index < string.length()) {
            return string.substring(index);
        }
        return null;
    }

    /**
     * ��ʼ�������ܡ��������ͳһ����
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
        //�ϴ���ť
        uploadLayout = (RelativeLayout) othersParamLayout.findViewById(R.id.upload_relate_layout);
        uploadLayout.setOnClickListener(new uploadListener());

    }

    //ָ��ֵ�����ޡ����޵��ʱ�����ĶԻ��򣬵�����ֿ�������
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
            .setTitle("��������")
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

    /**
     * ��ȡ��ǰ�豸�������еĿ�����
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
     * ���ػ�û��ѡ���豸ʱ����ʾ
     */
    private void noIndicatorHide() {
        noIndicatorText.setVisibility(View.GONE);
    }

    //û�п�����ʱ��Ҫ��ʾ����Ϣ
    private void noIndicatorShow() {
        noIndicatorText.setVisibility(View.VISIBLE);
    }

    /**
     * �Կ������Ľ����������Ͳ���
     *
     * @param result
     */
    private void indicatorsResult(Map<String, ArrayList<String>> result) {
        //����������
        sortIndicators(result, indicatorNames, indicatorKeys);
        //�Բ��ֽ��г�ʼ��
        initIndicatorLayout(schemeDefaultIndicatorLayout, indicatorNames);
    }


    /**
     * 1����result����Ľ����ֱ�����ȫ�ֱ���indicatorNames��indicatorKeys��
     * 2������indicator����ʾλ�ý�������
     * 3��^ƥ�������ַ����Ŀ�ʼλ��
     * .Ҫƥ�������\r\n�����ڵ��κ��ַ�
     * *ƥ��ǰ����ӱ��ʽ�����
     *
     * @param result
     * @param indicatorNames
     * @param indicatorKeys
     */
    private void sortIndicators(Map<String, ArrayList<String>> result, ArrayList<String> indicatorNames, ArrayList<String> indicatorKeys) {
        ArrayList<String> sortKeys = result.get("protocolKeys");
        ArrayList<String> sortNames = result.get("protocolNames");

        //�ж�keys��names�����Ƿ�һ�£�keys��names�Ƿ�Ϊ��
        if (sortKeys == null || sortNames == null || sortKeys.size() != sortNames.size()) {
            toast.showLong(activity, "û�п�����");
            return;
        }
        /**
         * Ҫ���������Ϊÿ�ε����ͬ�豸���ǲ�ͬ���豸�룬��������Ŀ��������಻һ��
         */
        indicatorNames.clear();
        indicatorKeys.clear();
        /**
        * �Ƚ�����ʪ�ȣ�ʩ�ʡ����ʣ���ǿƥ�䣬������ʾ
         */
        Log.i("gzfuzhi","result:"+result);
        int i = findSpecialIndicator(sortNames, "^����ʪ��.*");
        if (i != -1) {
            //û�н�������ʪ�ȿ�������д�����飬�á����ܿ�������������Ƹ�����
            indicatorNames.add("���ܿ�����");
            indicatorKeys.add(sortKeys.get(i));
            //sortNames.remove(i)��Ϊ����߹ؼ��ֲ���������㷨Ч��
            sortNames.remove(i);
            //sortKeys.remove(i)��Ϊ����sortNames�±��Ӧ
            sortKeys.remove(i);
        }
        //---------------------����ʩ�ʿ�����,�����˵ڶ�------------------------------
        // TODO: 2016/9/22 Ҫ��֤�ܷ����ʩ�ʿ�����
        i = findSpecialIndicator(sortNames, "^ʩ��.*");
        if (i != -1) {
            indicatorNames.add(sortNames.get(i));
            indicatorKeys.add(sortKeys.get(i));
            sortNames.remove(i);
            sortKeys.remove(i);
        }
        //--------------------------------------------------------------------------

        i = findSpecialIndicator(sortNames, "^����.*");
        if (i != -1) {
            indicatorNames.add("��ǿ�ȿ�����");
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
        //��Ϊ�±걾���Ѿ�ƥ��ã�����ֱ��addAll()��ʣ�µ�keys��names���
        indicatorNames.addAll(sortNames);
        indicatorKeys.addAll(sortKeys);
    }

    /**
     * ���ݹؼ����ҵ���Ӧ�����������ּ��±�
     *
     * @param indicatorNames ����������������
     * @param indicator      �������Ĺؼ���
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
     * �Բ��ֽ��г�ʼ����һ��tableLayout�°���һ��tableRow���������LinearLayout
     *
     * @param tableLayout    Ҫ�ڷ�tableRow�Ĳ���
     * @param indicatorNames �Ѿ�����õĿ���������
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
            //����0�����4�ֿ��ƣ�Ŀǰ����4�֣�
            if (size < 5) {
                for (int i = 0; i < size; i++) {
                    //�豸���Ƶ�view,����һ��ͼ��һ��textview���ϳ�һ����ť
                    view = activity.getLayoutInflater().inflate(R.layout.fragment_scheme_new_indicator_btn, row, false);
                    imageView = (ImageView) view.findViewById(R.id.indicator_image);
                    setIconWithType(imageView, indicatorNames.get(i), false);
                    textView = (TextView) view.findViewById(R.id.indicator_textview);
                    textView.setText(cutIndicatorName(indicatorNames.get(i)));
                    //Ϊ�����ֵ���¼��е����item����ҪsetTag(),����ʾ��λ������־
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
        //��ʼ����ɺ���ʾ���ĸ�tableRow�����
        paramFirstShow();
    }

    /**
     * ���տ��������������Ƿ���ΪimgView����src
     *
     * @param iconIV  imgView
     * @param type    ����������
     * @param pressed �Ƿ���
     */
    private void setIconWithType(ImageView iconIV, String type, boolean pressed) {
        if (type.matches("^����.*")) {
            iconIV.setImageResource(R.drawable.icon_jiaoshui);
        }
        //---------------------����ʩ��icon��ʾ���ж�----------------------
        else if (type.matches("^ʩ��.*")) {
            iconIV.setImageResource(R.drawable.icon_shifei);
        }
        //��Ϊ�����ʿ������ĳ��˹�ǿ�ȿ���������������ҲҪ����
        else if (type.matches("^��ǿ��.*")) {
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

    /**
     * ��ȡ������������
     *
     * @param name ��ʽΪ XXX������
     * @return ������������
     */
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
            //ѡ��popupMenu��ť
            if (choice == MAGIC_NUMBER) {
                showPopupMenu(v);
                pressIndicatorMenu(3, indicatorKeys.size());

            } else if (choice < indicatorKeys.size()) {
                if (indicatorKeys.get(choice).equals("shc")) {    //ѡ�����ܰ�ť
                    water();
                } else if (indicatorKeys.get(choice).equals("fc")) {
                    // TODO: 2016/9/22 ���ԣ�����ʩ�ʵ�dialog
                    fertilize();
                } else {   //ѡ�г����ܡ�ʩ�ʡ�popupMenu�İ�ť
                    currentIndex = (int) v.getTag();
                    //��ʾ��Ӧ��view
                    indicatorShow(currentIndex);
                    pressIndicatorMenu(currentIndex, indicatorKeys.size());
                }
            }
        }
    }

    /**
     * �����ͬ��indicator��ʾ��ͬ�Ľ��棬���õ�λ
     *
     * @param index ��Ӧindicator���±�(�Ѿ��ź���)
     */
    private void indicatorShow(int index) {
        //����key���õ�λ
        setParamsUnit(indicatorKeys.get(index));
        //lqc����
        if (indicatorKeys.get(index).equals("lqc")) {
            initParamLqc();
        }
        //lc��ǿ
        else if (indicatorKeys.get(index).equals("lc")) {
            initParamOthers(6000);
        }
        //phc ����PHֵ
        else if (indicatorKeys.get(index).equals("phc")) {
            initParamOthers(10);
        }
        //co2c ������̼
        else if(indicatorKeys.get(index).equals("co2c")){
            initParamOthers(6000);
        }
        //����
        else {
            initParamOthers(100);
        }
    }

    /**
     * ���ݿ�������key���õ�λ
     *
     * @param indicatorKey
     */
    private void setParamsUnit(String indicatorKey) {
        if (indicatorKey.equals("lc")) {
            targetUnit.setText("/lux");
            upperUnit.setText("/lux");
            lowerUnit.setText("/lux");
        } else if (indicatorKey.equals("tc")) {
            targetUnit.setText("/��C");
            upperUnit.setText("/��C");
            lowerUnit.setText("/��C");
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

    //���¿�������ťʱ���õķ������÷���³�����д���ǿ���ȶ��ԣ�
    //index,ѡ�еĹ��ܵ��±꣬size��һ���м��ֹ��ܿ�ѡ
    private void pressIndicatorMenu(int index, int size) {
        if (schemeDefaultIndicatorLayout.getChildAt(0) instanceof TableRow) {
            TableRow tableRow = (TableRow) schemeDefaultIndicatorLayout.getChildAt(0);
            if (tableRow.getChildAt(index) instanceof LinearLayout) {
                //��ȡindex�µ�item���������ͼ���������ɫ
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

    /**
     * ���������ťʱ����ʾPopupMenu
     *
     * @param view
     */
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(activity, view);
        Menu menu = popupMenu.getMenu();
        for (int i = 3; i < indicatorKeys.size(); i++) {
            //menu.add(groupId,itemId,itemName);
            //�Ѿ�������orderΪFirst+i -->4��ʼ
            menu.add(Menu.NONE, Menu.FIRST + i, i, indicatorNames.get(i));
        }
        activity.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new popupMenuListener());
        popupMenu.show();
    }

    private class popupMenuListener implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            //item.getOrder()��ֵΪ4��4����
            indicatorShow(item.getOrder());
            currentIndex = item.getOrder();
            return true;
        }
    }

    /**
     * �����ĸ�tableRow����ʾ
     * �����չ�Բ��ã�Ӧ��Ϊÿ��item���ñ�ǩ�����������
     */
    private void paramFirstShow() {
        /**
         * ��3�����
         * 1�����ܡ�ʩ��
         * 2������
         * 3��ʩ��
         */
        if (indicatorKeys != null && !indicatorKeys.isEmpty()) {
            //��һ��������ʪ�ȿ������ҿ�������������1ʱ����ʾ�ڶ���
            //��Ϊ�����Ǹ��Ǹ�dialog
            //���1
            if (indicatorKeys.get(0).equals("shc") && indicatorKeys.get(1).equals("fc") && indicatorKeys.size() > 2) {
                indicatorShow(2);
                currentIndex = 2;
                pressIndicatorMenu(2, indicatorKeys.size());
            }
            //���2��3
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

    //���ý�ˮʱ��
    private void setWaterTimes() {
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        final View contentView = View.inflate(activity, R.layout.view_water_button, null);

        final EditText waterTimeHours = (EditText) contentView.findViewById(R.id.et_water_time_hours);
        final EditText waterTimeMins = (EditText) contentView.findViewById(R.id.et_water_time_minutes);
        final EditText waterTimeSecs = (EditText) contentView.findViewById(R.id.et_water_time_seconds);
        //����contentView��dialog����ʾ�Ĳ��ֲ���
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        //��������Ļ�е���ʾ�ı���
        baseDialog.setWidthAndHeightRadio(0.8f, 0f)
        //���þ�����ʾdialog��������ƫ����
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

                //����Ϊ������ʾ����ʱ��
                if ((waterTimeHoursMsg.equals("") || waterTimeHoursMsg == null)
                        && (waterTimeMinsMsg.equals("") || waterTimeMinsMsg == null)
                        && (waterTimeSecsMsg.equals("") || waterTimeSecsMsg == null)) {
                    ToastUtil.showShort(activity, "������ʱ��");
                    return;
                }
                //���������������û������ʱ�Զ���������
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
                showDialog("���ڴ�����");
                baseDialog.dismiss();
            }
        })
        //����dialog��ʾ�ı���
        .setTitle(equipmentCodes + "")
        .setIcon(R.drawable.icon_water_on_dialog)
        //ƥ��dialog��contentView
        .setContentView(contentView, contentLp);
    }

//    //���ڽ�ˮ������ͼƬ
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
     * �����豸��־λfc��shc�������õ�����ͼƬ���豸����û������
     * Ƕ�׹�ϵ��Indicatorlayout->row->���Բ���(n)->img+text
     *
     * @param equipmentCode �豸�ı���
     * @param controller    /c/fc/1����ʽ
     */
    public void schemeAppointControllering(String equipmentCode, String controller) {
        //�߼��Ѿ�д���˽�ˮ���ڵ�һ��item��λ�ã�ʩ�ʰ����˵�(һ)����item��λ��
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
     * �����豸��־λfc��shc��������Ϩ���ͼƬ���豸����û������
     *
     * @param equipmentCode
     * @param controller
     */
    public void schemeAppointControllerTimeOut(String equipmentCode, String controller) {
        if (controller.contains("shc")) {
            //����shc��ʽ
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
            //����fc����ʽ
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
     * ת����ˮ��ʩ�ʵ�״̬�������Ժ�����
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
//    //��ˮ��ɣ�Ϩ��ͼƬ
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
//    //ת����ˮ��ť״̬
//    private void reverseWaterBtn(String equipmentCode) {
//        isWater = !isWater;
//    }

    /**
     * ����ʩ��
     */
    private void fertilize() {
        if (!isFertilize) {
            setFertilizeTimes();
        } else {
            fertilizeRoom.FertilizeOffByBtn(equipmentCodes, FERTILIZECONTROLLER, clientId);
            showDialog("���ڹر�ʩ��");
        }
    }

    /**
     * ����ʩ��ʱ��
     */
    public void setFertilizeTimes() {
        final BaseCustomAlterDialog baseDialog = new BaseCustomAlterDialog(activity);
        final View contentView = View.inflate(activity, R.layout.view_fertilize_button, null);

        final EditText fertilizeTimeHours = (EditText) contentView.findViewById(R.id.et_fertilize_time_hours);
        final EditText fertilizeTimeMins = (EditText) contentView.findViewById(R.id.et_fertilize_time_minutes);
        final EditText fertilizeTimeSecs = (EditText) contentView.findViewById(R.id.et_fertilize_time_seconds);
        //����contentView��dialog����ʾ�Ĳ��ֲ���
        RelativeLayout.LayoutParams contentLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        //��������Ļ�е���ʾ�ı���
        baseDialog.setWidthAndHeightRadio(0.8f, 0f)
        //���þ�����ʾdialog��������ƫ����
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

                //����Ϊ������ʾ����ʱ��
                if ((fertilizeTimeHoursMsg.equals("") || fertilizeTimeHoursMsg == null)
                        && (fertilizeTimeMinsMsg.equals("") || fertilizeTimeMinsMsg == null)
                        && (fertilizeTimeSecsMsg.equals("") || fertilizeTimeSecsMsg == null)) {
                    ToastUtil.showShort(activity, "������ʱ��");
                    return;
                }
                //���������������û������ʱ�Զ���������
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
                //����ʩ����Ϣ
                fertilizeRoom.FertilizeOn(equipmentCodes, FERTILIZECONTROLLER, clientId, fertilizeMessage);
                showDialog("���ڴ�ʩ��");
                baseDialog.dismiss();
            }
        })
        //����dialog��ʾ�ı���
        .setTitle(equipmentCodes + "")
        .setIcon(R.drawable.icon_fertilize_on_dialog)
        //ƥ��dialog��contentView
        .setContentView(contentView, contentLp);
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

//    /**
//     * ����listView������adapter��ʾ
//     * @param listView ��Ҫ��ʾ��listView
//     * @param itemString ÿ��Ĭ��ͼƬ��Ӧ�����֣���X��X��X
//     * @param itemColor ÿ��Ĭ��ͼƬ ��R.drawable.X
//     */
//    private void lqcListView(ListView listView, String[] itemString, int[] itemColor) {
//        List<Map<String, Object>> listItems = new ArrayList<>();
//        //���ʱȵ�ͼ���˵��
//        for (int i = 0; i < itemString.length; i++) {
//            Map<String, Object> listItem = new HashMap<>();
//            listItem.put("lqc_item_imageview", itemColor[i]);
//            listItem.put("lqc_item_textview", itemString[i]);
//            listItems.add(listItem);
//        }
//        //�Զ����ͼ���˵��
//        Map<String, Object> listItem = new HashMap<>();
//        listItem.put("lqc_item_imageview", R.drawable.icon_config);
//        listItem.put("lqc_item_textview", "�Զ���");
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
     * ��ʼ�����˹��ʡ����ܽ����µ�������(���ֶ���ͳһ)
     *
     * @param theMaxValue �ɵ��ڵ����ֵ
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
     * �ϴ�����������indicatorKey
     *
     * @param indicatorKey
     */
    private void uploadScheme(String indicatorKey) {
        //Ϊtarget��upper��lower������
        if (fillingParams(indicatorKey)) {
            //ѡ����Ϊ��Ԥ��ʱ��
            fillingTime();
        }
    }

    /**
     * ����ѡ��ʱ���dialog��ѡ����Ϊ��Ԥ��ʱ��
     */
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
                //���ݵ����listView��item����startTime��endTime
                countTime(position);
            }
        });

        //����dialog�Ļ������ԣ�����contentView���뵽dialog��
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
                //���target��upper��lower��startTime��endTime�Ƿ�������ȷ�����ϴ�
                uploadAfterChecked();
                baseDialog.dismiss();
            }
        })

        .setTitle("��ѡ�����ʱ��")

        .setContentView(contentView, contentLp);
    }

    /**
     * ���ݵ����listView��timeDelay
     * ����startTime��endTime
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
    //�Զ���ʱ��
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
                        ToastUtil.showShort(activity,"��ѡ����ȷ��ʱ��");
                    }
                }
                else
                {
                    ToastUtil.showShort(activity,"��ѡ����ʱ��");
                }
            }
        }, DateUtil.format(minCalendar.getTime(), FORMAT_STR)
         , DateUtil.format(maxCalendar.getTime(), FORMAT_STR));
        timeSelector.show();
    }

    //�ж�����Ŀ�ʼʱ���Ƿ�<=����ʱ��
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
     * Ϊtarget��upper��lower������
     * ��indicatorKeyΪ"lqc"ʱ��targetΪ1:5:0�����Ĳ���
     * ��indicatorKeyΪ����ʱ��targetΪseekbar��ֵ
     *
     * @param indicatorKey
     * @return
     */
    private Boolean fillingParams(String indicatorKey) {
        //��Ԥ����
        if (indicatorKey.equals("lqc")) {
            //target = lqc.trim() + ":0";ԭ��������·��
            target = lqc.trim();
            upper = target;
            lower = target;
        }
        //��Ԥ��ǿ���¶ȡ�������̼��
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
            System.out.println(equipmentCodes+","+
                    startTime+","+ endTime+","+indicatorKeys.get(currentIndex)+","+target+","+ upper+","+ lower);
            uploadAndDownloadScheme.interveneObservable(equipmentCodes,
                    startTime, endTime, indicatorKeys.get(currentIndex), target, upper, lower);
        }
    }
}
