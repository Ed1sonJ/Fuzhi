package com.smartfarm.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Subscriber;
import rx.schedulers.Schedulers;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.smartfarm.observable.InterveneObservable;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.ToastUtil;

public class UpdateData extends Activity {

	private TextView equipomentName;
	private TextView equipmentCode;
	private ImageView updateSure;
	private TextView upperT;
	private TextView lowerT;
	private TextView targetT;
	private TextView startTimeT;
	private TextView endTimeT;
	private SeekBar target;
	private SeekBar upper;
	private SeekBar lower;
	private DatePicker startDate;
	private TimePicker startTime;
	private DatePicker endDate;
	private TimePicker endTime;
	private NumberPicker np1;
	private NumberPicker np2;
	private NumberPicker np3;
	private Button btnTarget;
	private Button btnUp;
	private Button btnLow;

	private LinearLayout setTargetLayout;
	private LinearLayout startTimeLayout;
	private LinearLayout endTimeLayout;
	private boolean isShowSetTargetLayout;
	private boolean isShowStartTimeLayout;
	private boolean isShowEndTimeLayout;
	
	
	private int np1Value = 1;
	private int np2Value = 1;
	private int np3Value = 1;
	
	private LinearLayout updateControlPart1;
	private LinearLayout updateControlPart2;
	
	private EditText updateControlPart2Target;
	private EditText updateControlPart2Up;
	private EditText updateControlPart2Low;

	private RadioGroup radioGroup;
	private List<String> protocolName = new ArrayList<String>();
	private List<String> protocolKeys = new ArrayList<String>();
	private String name;
	private String code;
	private int protocolKeysIndex = 1;

	private BaseProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_scheme);

		getInfoFromLastActivity();
		findViewById();
		initView();
		initRadioGroup();

	}
	
	private void getInfoFromLastActivity(){
		Intent intent = getIntent();
		name = intent.getStringExtra("name");
		code = intent.getStringExtra("code");
		for (int i = 0; i < intent.getIntExtra("size", 0); i++){
			protocolKeys.add(intent.getStringExtra("protocolKey" + String.valueOf(i)));
			protocolName.add(intent.getStringExtra("protocolName"+String.valueOf(i)));
		}
	}

	private void findViewById() {
		equipomentName = (TextView) findViewById(R.id.update_equiment_name);
		equipmentCode = (TextView) findViewById(R.id.update_equipment_code);
		updateSure = (ImageView) findViewById(R.id.image_update_sure);
		upperT = (TextView) findViewById(R.id.upperT);
		lowerT = (TextView) findViewById(R.id.lowerT);
		targetT = (TextView) findViewById(R.id.targetT);
		startTimeT = (TextView) findViewById(R.id.startTimeT);
		endTimeT = (TextView) findViewById(R.id.endTimeT);
		startDate = (DatePicker) findViewById(R.id.startDate);
		startTime = (TimePicker) findViewById(R.id.startTime);
		endDate = (DatePicker) findViewById(R.id.endDate);
		endTime = (TimePicker) findViewById(R.id.endTime);
		target = (SeekBar) findViewById(R.id.target);
		upper = (SeekBar) findViewById(R.id.upper);
		lower = (SeekBar) findViewById(R.id.lower);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		
		updateControlPart1 = (LinearLayout) findViewById(R.id.update_control_part1);
		updateControlPart2 = (LinearLayout) findViewById(R.id.update_control_part2);
		
		updateControlPart2Target = (EditText) findViewById(R.id.update_control_part2_target);
		updateControlPart2Up = (EditText) findViewById(R.id.update_control_part2_up);
		updateControlPart2Low = (EditText) findViewById(R.id.update_control_part2_low);

		setTargetLayout = (LinearLayout) findViewById(R.id.update_data_set_target_layout);
		startTimeLayout = (LinearLayout) findViewById(R.id.update_data_start_time_layout);
		endTimeLayout = (LinearLayout) findViewById(R.id.update_data_end_time_layout);
		
		
		np1 = (NumberPicker) findViewById(R.id.update_control_part2_numberPicker_target_1);
		np2 = (NumberPicker) findViewById(R.id.update_control_part2_numberPicker_target_2);
		np3 = (NumberPicker) findViewById(R.id.update_control_part2_numberPicker_target_3);
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
		
		btnTarget = (Button) findViewById(R.id.update_btn_target);
		btnUp = (Button) findViewById(R.id.update_btn_up);
		btnLow = (Button) findViewById(R.id.update_btn_low);
	}

	private void initView() {		
		dialog = new BaseProgressDialog(UpdateData.this);
		
		equipomentName.setText(name);
		
		equipmentCode.setText(code);
		
		startTime.setIs24HourView(true);
		
		endTime.setIs24HourView(true);

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

		updateSure.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				if(protocolKeys.get(protocolKeysIndex-1).equals("lqc")){
					targetT.setText(updateControlPart2Target.getText().toString());
					upperT.setText(updateControlPart2Up.getText().toString());
					lowerT.setText(updateControlPart2Low.getText().toString());
				}
				if (targetT.getText().toString().equals("")
						|| upperT.getText().toString().equals("")
						|| lowerT.getText().toString().equals("")
						|| startTimeT.getText().toString().equals("") || endTimeT
						.getText().toString().equals("")) {
			ToastUtil.showShort(UpdateData.this, "请检查所有数据已输入");
			return;
		}
			interveneObservable(code, startTimeT.getText().toString(),
					endTimeT.getText().toString(), protocolKeys.get(protocolKeysIndex-1), targetT.getText()
					.toString(), 
					upperT.getText().toString(), 
					lowerT.getText().toString());
			}
		});
		
		updateControlPart2Target.setOnFocusChangeListener(new textCheck("target"));
		
		updateControlPart2Up.setOnFocusChangeListener(new textCheck("upper"));
		
		updateControlPart2Low.setOnFocusChangeListener(new textCheck("lower"));
		
		
		btnTarget.setOnClickListener(new btnClickListener(1));
		
		btnUp.setOnClickListener(new btnClickListener(2));
		
		btnLow.setOnClickListener(new btnClickListener(3));

	}
	
	private void changeDatePickerView(DatePicker datePicker){
        // Change DatePicker layout  
        LinearLayout dpContainer = (LinearLayout)datePicker.getChildAt(0)   ;   // LinearLayout  
        LinearLayout dpSpinner = (LinearLayout)dpContainer.getChildAt(0);       // 0 : LinearLayout; 1 : CalendarView  
        for(int i = 0; i < dpSpinner.getChildCount(); i ++) {  
            NumberPicker numPicker = (NumberPicker)dpSpinner.getChildAt(i);     // 0-2 : NumberPicker  
            LayoutParams params1 = new LayoutParams(120, LayoutParams.WRAP_CONTENT);  
            params1.leftMargin = 0;  
            params1.rightMargin = 30;  
            numPicker.setLayoutParams(params1);  
              
//          EditText cusET = (EditText)numPicker.getChildAt(0);     // CustomEditText  
//          cusET.setTextSize(14);  
//          cusET.setWidth(70);  
        }  
	}
	
	private class showSetTargetListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(!isShowSetTargetLayout){
				setTargetLayout.setVisibility(View.VISIBLE);
			}else{
				setTargetLayout.setVisibility(View.GONE);
			}
			isShowSetTargetLayout = !isShowSetTargetLayout;
		}			
	}
	
	private class showStartTimeListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(!isShowStartTimeLayout){
				startTimeLayout.setVisibility(View.VISIBLE);
			}else{
				startTimeLayout.setVisibility(View.GONE);
			}
			isShowStartTimeLayout = !isShowStartTimeLayout;
		}			
	} 
	
	private class showEndTimeListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(!isShowEndTimeLayout){
				endTimeLayout.setVisibility(View.VISIBLE);
			}else{
				endTimeLayout.setVisibility(View.GONE);
			}
			
			isShowEndTimeLayout = !isShowEndTimeLayout;
		}			
	} 

	private void interveneObservable(String code, String startTime, String endTime, String protocolKey,
			String value, String upper, String lower){
		InterveneObservable.create(code, startTime, endTime, protocolKey, value, upper, lower).
			subscribeOn(Schedulers.newThread()).
			subscribe(new Subscriber<Boolean>() {	
				@Override
				public void onStart() {
					super.onStart();
					runOnUiThread(new Runnable() {					
						@Override
						public void run() {
							dialog.setMessage("正在上传方案");
							dialog.show();
						}
					});
				}
				@Override
				public void onCompleted() {
					runOnUiThread(new Runnable() {					
						@Override
						public void run() {
							dialog.dismiss();
						}
					});
				}
				@Override
				public void onError(Throwable arg0) {
					runOnUiThread(new Runnable() {					
						@Override
						public void run() {
							dialog.dismiss();
							ToastUtil.showShort(UpdateData.this, "方案上传失败");
						}
					});
				}
				@Override
				public void onNext(final Boolean arg0) {
					runOnUiThread(new Runnable() {					
						@Override
						public void run() {
							if(arg0 == true){
								dialog.dismiss();
								ToastUtil.showShort(UpdateData.this, "方案上传成功");
							}else{
								dialog.dismiss();
								ToastUtil.showShort(UpdateData.this, "方案上传失败");
							}
						}
					});
				}
			});
	}

	private void initRadioGroup() {
		RadioButton radioButton;
		int i = 1;
		for (String str : protocolName) {
			radioButton = new RadioButton(this);
			if(i == 1){
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
				// 获取变更后的选中项的ID
				protocolKeysIndex = checkedId;
				//typeT.setText(protocolName.get(checkedId - 1));

				if(protocolKeys.get(checkedId - 1).equals("lc")){
					updateControlPart1.setVisibility(View.VISIBLE);
					updateControlPart2.setVisibility(View.GONE);
					target.setMax(60000);
					upper.setMax(60000);
					lower.setMax(60000);
				}else if(protocolKeys.get(checkedId - 1).equals("tc")){
					updateControlPart1.setVisibility(View.VISIBLE);
					updateControlPart2.setVisibility(View.GONE);
					target.setMax(100);
					upper.setMax(100);
					lower.setMax(100);
				}else if(protocolKeys.get(checkedId - 1).equals("hc")){
					updateControlPart1.setVisibility(View.VISIBLE);
					updateControlPart2.setVisibility(View.GONE);
					target.setMax(100);
					upper.setMax(100);
					lower.setMax(100);
				}else if(protocolKeys.get(checkedId - 1).equals("lqc")){
					updateControlPart1.setVisibility(View.GONE);
					updateControlPart2.setVisibility(View.VISIBLE);
					target.setMax(10);
					upper.setMax(10);
					lower.setMax(10);
				}else if(protocolKeys.get(checkedId - 1).equals("phc")){
					updateControlPart1.setVisibility(View.VISIBLE);
					updateControlPart2.setVisibility(View.GONE);
					target.setMax(10);
					upper.setMax(10);
					lower.setMax(10);
				}else{
					updateControlPart1.setVisibility(View.VISIBLE);
					updateControlPart2.setVisibility(View.GONE);
					target.setMax(100);
					upper.setMax(100);
					lower.setMax(100);
				}
			}
		});

	}
	
	private boolean checkIslqcInputValid(String lqc){
		String reg = "[0-9]:[0-9]:[0-9]";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(lqc);
		return matcher.matches();
	}
	
	private class textCheck implements OnFocusChangeListener{	
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
	
	private class valueChangeListener implements OnValueChangeListener{
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
	
	private class btnClickListener implements OnClickListener{
		private int btn;
		public btnClickListener(int btn) {
			this.btn = btn;
		}
		@Override
		public void onClick(View v) {
			switch (btn) {
			case 1:
				updateControlPart2Target.setText(np1Value+":"+np2Value+":"+np3Value);
				break;
			case 2:
				updateControlPart2Up.setText(np1Value+":"+np2Value+":"+np3Value);
				break;
			case 3:
				updateControlPart2Low.setText(np1Value+":"+np2Value+":"+np3Value);
				break;
			}
		}
		
	}

}
