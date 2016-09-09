package com.smartfarm.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.smartfarm.activity.R;
import com.smartfarm.bean.TypeBean;
import com.smartfarm.observable.GetIndicatorObservable;
import com.smartfarm.observable.InterveneObservable;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import rx.Subscriber;
import rx.schedulers.Schedulers;
//û�õ�
public class SchemeFragment extends BaseFragment {

    //�豸���ƺͱ��
    private TextView equipmentName;
    private TextView equipmentCode;
    private String name;
    private String code;
    //�ϴ���ť
    private RelativeLayout updateSureLayout;
    //�������ƽ���
    ScrollView updateScrollView;
    //Radio��ť���
    private RadioGroup radioGroup;
    private List<String> protocolName = new ArrayList<String>();
    private List<String> protocolKeys = new ArrayList<String>();
    private int protocolKeysIndex = 1;
    //�����ʾ���ò���/��ʼʱ��/����ʱ��Ľ���
    private RelativeLayout showSetTargetLayout;
    private RelativeLayout showSetStartTimeLayout;
    private RelativeLayout showSetEndTimeLayout;
    //���ò���/��ʼʱ��/����ʱ��ĸ�������
    private LinearLayout setTargetLayout;
    private LinearLayout startTimeLayout;
    private LinearLayout endTimeLayout;
    //�жϵ�ǰ�Ƿ���ʾ���ò���/���ÿ�ʼʱ��/���ý���ʱ�����
    private boolean isShowSetTargetLayout;
    private boolean isShowStartTimeLayout;
    private boolean isShowEndTimeLayout;
    //���ò�������ʽ1/��ʽ2
    private LinearLayout updateControlPart1;
    private LinearLayout updateControlPart2;
    //���ò�����ʽ1�Ĳ���/����ֵ/����ֵ��ѡ����
    private SeekBar target;
    private SeekBar upper;
    private SeekBar lower;
    //���ò�����ʽ2��Ĭ������
    RelativeLayout targetDefault;
    ImageView imageLedBlue;
    ImageView imageLedRed;
    ImageView imageLedInfrared;
    //���ò�����ʽ2���Զ�������
    RadioGroup setTargetRadioGroup;
    RadioButton ledDefault;
    RadioButton ledDefineByYourSelf;
    RelativeLayout targetDefaultLayout;
    LinearLayout showSetTargetByYourSelfLayout;
    //��Ƶ
    RelativeLayout progressLayout;
    VideoView mVideoView;
    //���ò�����ʽ2������ѡ����
    private NumberPicker np1;
    private NumberPicker np2;
    private NumberPicker np3;
    //���ò�����ʽ2������ѡ������ֵ
    private int np1Value = 1;
    private int np2Value = 1;
    private int np3Value = 1;
    //���ò�����ʽ2�Ļ�ȡ����ѡ�������ݵĲ���/����ֵ/����ֵ��ť
    private Button btnTarget;
    private Button btnUp;
    private Button btnLow;
    //���ò�����ʽ2�ı༭��
    private EditText updateControlPart2Target;
    private EditText updateControlPart2Up;
    private EditText updateControlPart2Low;
    //ʱ��ѡ����
    private DatePicker startDate;
    private TimePicker startTime;
    private DatePicker endDate;
    private TimePicker endTime;
    //Ŀ��ֵ/����ֵ/����ֵ/��ʼʱ��/����ʱ���ı���
    private TextView targetT;
    private TextView upperT;
    private TextView lowerT;
    private TextView startTimeT;
    private TextView endTimeT;

    //����
    private BaseProgressDialog dialog;
    private Activity activity;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //����fragment������
        final android.content.Context contextThemeWrapper = new android.view.ContextThemeWrapper(getActivity(), R.style.AppTheme);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        rootView = localInflater.inflate(R.layout.fragment_scheme, container, false);
        findViewById();
        setUpVideoView();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    //�����豸������/�豸�ţ����Ҳ�ѯ��ǰ���˹���Ԥ��ѡ��
    public void setEquipmentNameAndCode(String equipmentName, String equipmentCode) {
        name = equipmentName;
        code = equipmentCode;
        this.equipmentName.setText(name);
        this.equipmentCode.setText(code);
        getIndicatorType(code);
        initView();
    }

    protected void setUpVideoView() {
        String path = "rtmp://v.gzfuzhi.com/mytv/test";

        progressLayout = (RelativeLayout) rootView.findViewById(R.id.video_progress_layout);
        mVideoView = (VideoView) rootView.findViewById(R.id.vitamio_videoView);
        mVideoView.setBufferSize(128);
        mVideoView.setVideoPath(path);
        mVideoView.setMediaController(new MediaController(getActivity()));
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setPlaybackSpeed(1.0f);
            }
        });
        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        mp.pause();
                        ToastUtil.showShort(activity, "��������");
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        progressLayout.setVisibility(View.GONE);
                        mp.start();
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
                activity.finish();
                return false;
            }
        });
        progressLayout.setVisibility(View.VISIBLE);
    }

    private void noneInvalidIndicator() {
        updateScrollView.setVisibility(View.GONE);
        updateSureLayout.setEnabled(false);
    }

    private void invalidIndicator() {
        updateScrollView.setVisibility(View.VISIBLE);
        updateSureLayout.setEnabled(true);
    }

    private void getProtocolKeyAndName(List<TypeBean> list) {
        protocolKeys.clear();
        protocolName.clear();
        for (TypeBean t : list) {
            protocolKeys.add(t.protocolKey);
            protocolName.add(t.name);
        }
        initRadioGroup();
    }

    private void getIndicatorType(String code) {
        GetIndicatorObservable.createObservable(code).
                subscribeOn(Schedulers.newThread()).
                subscribe(new Subscriber<List<TypeBean>>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setMessage("����������Ϣ");
                                dialog.show();
                            }
                        });
                    }

                    @Override
                    public void onCompleted() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("getIndicatorObservable", "OnCompleted");
                                dialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable arg0) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showShort(activity, "��������ʧ��");
                            }
                        });
                    }

                    @Override
                    public void onNext(final List<TypeBean> list) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (list != null && list.size() != 0) {
                                    invalidIndicator();
                                    getProtocolKeyAndName(list);
                                } else {
                                    noneInvalidIndicator();
                                    ToastUtil.showLong(activity, "���豸û�пɸ�Ԥѡ��");
                                }
                            }
                        });
                    }
                });
    }

    private void findViewById() {
        //������
        updateScrollView = (ScrollView) rootView.findViewById(R.id.update_scrollview);
        equipmentName = (TextView) rootView.findViewById(R.id.update_equiment_name);
        equipmentCode = (TextView) rootView.findViewById(R.id.update_equipment_code);
        updateSureLayout = (RelativeLayout) rootView.findViewById(R.id.update_sure_layout);

        upperT = (TextView) rootView.findViewById(R.id.upperT);
        lowerT = (TextView) rootView.findViewById(R.id.lowerT);
        targetT = (TextView) rootView.findViewById(R.id.targetT);
        startTimeT = (TextView) rootView.findViewById(R.id.startTimeT);
        endTimeT = (TextView) rootView.findViewById(R.id.endTimeT);
        startDate = (DatePicker) rootView.findViewById(R.id.startDate);
        startDate = (DatePicker) rootView.findViewById(R.id.startDate);
        startTime = (TimePicker) rootView.findViewById(R.id.startTime);
        startTime.setIs24HourView(true);
        endDate = (DatePicker) rootView.findViewById(R.id.endDate);
        endTime = (TimePicker) rootView.findViewById(R.id.endTime);
        endTime.setIs24HourView(true);
        target = (SeekBar) rootView.findViewById(R.id.target);
        upper = (SeekBar) rootView.findViewById(R.id.upper);
        lower = (SeekBar) rootView.findViewById(R.id.lower);


        updateControlPart1 = (LinearLayout) rootView.findViewById(R.id.update_control_part1);
        updateControlPart2 = (LinearLayout) rootView.findViewById(R.id.update_control_part2);

        updateControlPart2Target = (EditText) rootView.findViewById(R.id.update_control_part2_target);
        updateControlPart2Up = (EditText) rootView.findViewById(R.id.update_control_part2_up);
        updateControlPart2Low = (EditText) rootView.findViewById(R.id.update_control_part2_low);

        showSetTargetLayout = (RelativeLayout) rootView.findViewById(R.id.show_set_target_layout);
        radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);
        showSetStartTimeLayout = (RelativeLayout) rootView.findViewById(R.id.show_set_start_time_layout);
        showSetEndTimeLayout = (RelativeLayout) rootView.findViewById(R.id.show_set_end_time_layout);
        setTargetLayout = (LinearLayout) rootView.findViewById(R.id.update_data_set_target_layout);
        startTimeLayout = (LinearLayout) rootView.findViewById(R.id.update_data_start_time_layout);
        endTimeLayout = (LinearLayout) rootView.findViewById(R.id.update_data_end_time_layout);


        np1 = (NumberPicker) rootView.findViewById(R.id.update_control_part2_numberPicker_target_1);
        np2 = (NumberPicker) rootView.findViewById(R.id.update_control_part2_numberPicker_target_2);
        np3 = (NumberPicker) rootView.findViewById(R.id.update_control_part2_numberPicker_target_3);

        btnTarget = (Button) rootView.findViewById(R.id.update_btn_target);
        btnUp = (Button) rootView.findViewById(R.id.update_btn_up);
        btnLow = (Button) rootView.findViewById(R.id.update_btn_low);

        setTargetRadioGroup = (RadioGroup) rootView.findViewById(R.id.show_set_target_radioGroup);
        targetDefaultLayout = (RelativeLayout) rootView.findViewById(R.id.target_default);
        ledDefault = (RadioButton) rootView.findViewById(R.id.led_default);
        ledDefineByYourSelf = (RadioButton) rootView.findViewById(R.id.led_define_by_yoursele);
        targetDefault = (RelativeLayout) rootView.findViewById(R.id.target_default);
        imageLedBlue = (ImageView) rootView.findViewById(R.id.imageview_led_blue);
        imageLedRed = (ImageView) rootView.findViewById(R.id.imageview_led_red);
        imageLedInfrared = (ImageView) rootView.findViewById(R.id.iamgeview_led_infrared);
        showSetTargetByYourSelfLayout = (LinearLayout) rootView.findViewById(R.id.show_set_target_define_by_yourself_layout);

        dialog = new BaseProgressDialog(activity);
    }

    private void initView() {
        //�ϴ�������Ϣ
        updateSureLayout.setOnClickListener(new updateSureListener());
        //��ʾ���ò���ֵ
        showSetTargetLayout.setOnClickListener(new showSetTargetListener());
        //��������ѡ����
        upper.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                upperT.setText(String.valueOf(progress));

            }
        });

        lower.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                lowerT.setText(String.valueOf(progress));

            }
        });

        target.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                targetT.setText(String.valueOf(progress));

            }
        });

        //���ʱȵ�ѡ��ť��
        setTargetRadioGroup.setOnCheckedChangeListener(new onTargetRaioGroupListener());
        imageLedBlue.setOnClickListener(new ledDefault("0:1:0"));
        imageLedRed.setOnClickListener(new ledDefault("1:0:0"));
        imageLedInfrared.setOnClickListener(new ledDefault("0:0:1"));
        //����ѡ����
        np1.setMinValue(0);
        np1.setMaxValue(10);
        np1.setValue(1);
        np1.setOnValueChangedListener(new valueChangeListener(1));
        np2.setMinValue(0);
        np2.setMaxValue(10);
        np2.setValue(1);
        np2.setOnValueChangedListener(new valueChangeListener(2));
        np3.setMinValue(0);
        np3.setMaxValue(10);
        np3.setValue(1);
        np3.setOnValueChangedListener(new valueChangeListener(3));

        //��ȡ����ѡ�������ݵİ�ť
        btnTarget.setOnClickListener(new btnClickListener(1));
        btnUp.setOnClickListener(new btnClickListener(2));
        btnLow.setOnClickListener(new btnClickListener(3));
        //��ʾ����ѡ�����ı༭��
        updateControlPart2Target.setOnFocusChangeListener(new textCheck("target"));
        updateControlPart2Up.setOnFocusChangeListener(new textCheck("upper"));
        updateControlPart2Low.setOnFocusChangeListener(new textCheck("lower"));

        //��ʾ���ÿ�ʼʱ��
        showSetStartTimeLayout.setOnClickListener(new showStartTimeListener());
        //��ʾ���ý���ʱ��
        showSetEndTimeLayout.setOnClickListener(new showEndTimeListener());

        //��ʾʱ����ı���
        startTimeT.setText(String.valueOf(startDate.getYear()) + "-"
                + String.valueOf(startDate.getMonth() + 1) + "-"
                + String.valueOf(startDate.getDayOfMonth()) + " "
                + String.valueOf(startTime.getCurrentHour()) + ":"
                + String.valueOf(startTime.getCurrentMinute()));

        endTimeT.setText(String.valueOf(endDate.getYear()) + "-"
                + String.valueOf(endDate.getMonth() + 1) + "-"
                + String.valueOf(endDate.getDayOfMonth()) + " "
                + String.valueOf(endTime.getCurrentHour()) + ":"
                + String.valueOf(endTime.getCurrentMinute()));


        //����ѡ������ʱ��ѡ����
        //changeDatePickerView(startDate);
        startDate.init(startDate.getYear(), startDate.getMonth(),
                startDate.getDayOfMonth(), new OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                        startTimeT.setText(String.valueOf(year) + "-"
                                + String.valueOf(monthOfYear + 1) + "-"
                                + String.valueOf(dayOfMonth) + " "
                                + String.valueOf(startTime.getCurrentHour())
                                + ":"
                                + String.valueOf(startTime.getCurrentMinute()));
                    }
                });

        //changeDatePickerView(endDate);
        endDate.init(endDate.getYear(), endDate.getMonth(),
                endDate.getDayOfMonth(), new OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                        endTimeT.setText(String.valueOf(year) + "-"
                                + String.valueOf(monthOfYear + 1) + "-"
                                + String.valueOf(dayOfMonth) + " "
                                + String.valueOf(endTime.getCurrentHour())
                                + ":"
                                + String.valueOf(endTime.getCurrentMinute()));
                    }
                });

        startTime.setOnTimeChangedListener(new OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                startTimeT.setText(String.valueOf(startDate.getYear()) + "-"
                        + String.valueOf(startDate.getMonth() + 1) + "-"
                        + String.valueOf(startDate.getDayOfMonth()) + " "
                        + String.valueOf(hourOfDay) + ":"
                        + String.valueOf(minute));
            }
        });

        endTime.setOnTimeChangedListener(new OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                endTimeT.setText(String.valueOf(endDate.getYear()) + "-"
                        + String.valueOf(endDate.getMonth() + 1) + "-"
                        + String.valueOf(endDate.getDayOfMonth()) + " "
                        + String.valueOf(hourOfDay) + ":"
                        + String.valueOf(minute));
            }
        });
    }

    private class updateSureListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            updateChange();
        }
    }

    private void updateChange() {
        //���ʱ���Ϣ���
        if (protocolKeys.get(protocolKeysIndex - 1).equals("lqc")) {
            targetT.setText(updateControlPart2Target.getText().toString());
            upperT.setText(updateControlPart2Up.getText().toString());
            lowerT.setText(updateControlPart2Low.getText().toString());
        }
        //�������ݲ��Ϸ�
        if (targetT.getText().toString().equals("")
                || upperT.getText().toString().equals("")
                || lowerT.getText().toString().equals("")
                || startTimeT.getText().toString().equals("") || endTimeT
                .getText().toString().equals("")) {
            ToastUtil.showShort(activity, "������������������");
            return;
        }
        //�ϴ�
        interveneObservable(code, startTimeT.getText().toString(),
                endTimeT.getText().toString(),
                protocolKeys.get(protocolKeysIndex - 1),
                targetT.getText().toString(),
                upperT.getText().toString(),
                lowerT.getText().toString());
    }

    private class showSetTargetListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (!isShowSetTargetLayout) {
                setTargetLayout.setVisibility(View.VISIBLE);
            } else {
                setTargetLayout.setVisibility(View.GONE);
            }
            isShowSetTargetLayout = !isShowSetTargetLayout;
        }
    }

    private class ledDefault implements OnClickListener {
        String parameters;

        public ledDefault(String parameters) {
            this.parameters = parameters;
        }

        @Override
        public void onClick(View v) {
            updateControlPart2Target.setText(parameters);
            updateControlPart2Up.setText(parameters);
            updateControlPart2Low.setText(parameters);
            updateChange();
        }
    }

    private class showStartTimeListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (!isShowStartTimeLayout) {
                startTimeLayout.setVisibility(View.VISIBLE);
            } else {
                startTimeLayout.setVisibility(View.GONE);
            }
            isShowStartTimeLayout = !isShowStartTimeLayout;
        }
    }

    private class showEndTimeListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (!isShowEndTimeLayout) {
                endTimeLayout.setVisibility(View.VISIBLE);
            } else {
                endTimeLayout.setVisibility(View.GONE);
            }

            isShowEndTimeLayout = !isShowEndTimeLayout;
        }
    }

    private void interveneObservable(String code, String startTime, String endTime, String protocolKey,
                                     String value, String upper, String lower) {
        InterveneObservable.create(code, startTime, endTime, protocolKey, value, upper, lower).
                subscribeOn(Schedulers.newThread()).
                subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setMessage("�����ϴ�����");
                                dialog.show();
                            }
                        });
                    }

                    @Override
                    public void onCompleted() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable arg0) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                ToastUtil.showShort(activity, "�����ϴ�ʧ��");
                            }
                        });
                    }

                    @Override
                    public void onNext(final Boolean arg0) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                if (arg0 == true) {
                                    ToastUtil.showShort(activity, "�����ϴ��ɹ�");
                                } else {
                                    ToastUtil.showShort(activity, "������Ϣ����");
                                }
                            }
                        });
                    }
                });
    }

    private void initRadioGroup() {
        radioGroup.removeAllViews();
        RadioButton radioButton;
        int i = 1;
        for (String str : protocolName) {
            radioButton = new RadioButton(activity);
            if (i == 1) {
                radioButton.setChecked(true);
            }
            radioButton.setMinimumWidth(18);
            radioButton.setText(str);
            radioButton.setButtonDrawable(R.drawable.selector_radiobutton);
            radioButton.setTextColor(Color.BLACK);
            //radioButton.setTextSize(14);
            radioButton.setPadding(5, 0, 20, 0);
            radioButton.setId(i++);
            radioGroup.addView(radioButton);
        }

        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // ��ȡ������ѡ�����ID
                protocolKeysIndex = checkedId;
                if (protocolKeys.get(checkedId - 1).equals("lc")) {
                    updateControlPart1.setVisibility(View.VISIBLE);
                    updateControlPart2.setVisibility(View.GONE);
                    target.setMax(60000);
                    upper.setMax(60000);
                    lower.setMax(60000);
                } else if (protocolKeys.get(checkedId - 1).equals("tc")) {
                    updateControlPart1.setVisibility(View.VISIBLE);
                    updateControlPart2.setVisibility(View.GONE);
                    target.setMax(100);
                    upper.setMax(100);
                    lower.setMax(100);
                } else if (protocolKeys.get(checkedId - 1).equals("hc")) {
                    updateControlPart1.setVisibility(View.VISIBLE);
                    updateControlPart2.setVisibility(View.GONE);
                    target.setMax(100);
                    upper.setMax(100);
                    lower.setMax(100);
                } else if (protocolKeys.get(checkedId - 1).equals("lqc")) {
                    updateControlPart1.setVisibility(View.GONE);
                    updateControlPart2.setVisibility(View.VISIBLE);
                    target.setMax(10);
                    upper.setMax(10);
                    lower.setMax(10);
                } else if (protocolKeys.get(checkedId - 1).equals("phc")) {
                    updateControlPart1.setVisibility(View.VISIBLE);
                    updateControlPart2.setVisibility(View.GONE);
                    target.setMax(10);
                    upper.setMax(10);
                    lower.setMax(10);
                } else {
                    updateControlPart1.setVisibility(View.VISIBLE);
                    updateControlPart2.setVisibility(View.GONE);
                    target.setMax(100);
                    upper.setMax(100);
                    lower.setMax(100);
                }
            }
        });

    }

    private class onTargetRaioGroupListener implements OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.led_default:
                    targetDefaultLayout.setVisibility(View.VISIBLE);
                    showSetTargetByYourSelfLayout.setVisibility(View.GONE);
                    break;
                case R.id.led_define_by_yoursele:
                    targetDefaultLayout.setVisibility(View.GONE);
                    showSetTargetByYourSelfLayout.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private boolean checkIslqcInputValid(String lqc) {
        String reg = "[0-9]:[0-9]:[0-9]";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(lqc);
        return matcher.matches();
    }

    private class textCheck implements OnFocusChangeListener {
        String inputString;

        public textCheck(String inputString) {
            this.inputString = inputString;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            EditText view = (EditText) v;
            if (hasFocus == true) {
                view.setTextColor(Color.BLACK);
                return;
            }
            String text = view.getText().toString();
            if (!checkIslqcInputValid(text)) {
                view.setTextColor(Color.RED);
            }
        }
    }

    private class valueChangeListener implements OnValueChangeListener {
        private int np;

        public valueChangeListener(int np) {
            this.np = np;
        }

        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            switch (np) {
                case 1:
                    np1Value = newVal;
                    break;
                case 2:
                    np2Value = newVal;
                    break;
                case 3:
                    np3Value = newVal;
                    break;
            }
        }
    }

    private class btnClickListener implements OnClickListener {
        private int btn;

        public btnClickListener(int btn) {
            this.btn = btn;
        }

        @Override
        public void onClick(View v) {
            switch (btn) {
                case 1:
                    updateControlPart2Target.setText(np1Value + ":" + np2Value + ":" + np3Value);
                    break;
                case 2:
                    updateControlPart2Up.setText(np1Value + ":" + np2Value + ":" + np3Value);
                    break;
                case 3:
                    updateControlPart2Low.setText(np1Value + ":" + np2Value + ":" + np3Value);
                    break;
            }
        }

    }

}
