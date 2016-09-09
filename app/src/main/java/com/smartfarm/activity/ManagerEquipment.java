package com.smartfarm.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.smartfarm.bean.TypeBean;
import com.smartfarm.observable.DeleteEquipmentObservable;
import com.smartfarm.observable.GetIndicatorObservable;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.Config;
import com.smartfarm.util.ToastUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.schedulers.Schedulers;

public class ManagerEquipment extends Activity {

	private TextView tvEquipmentName;
	private Spinner equipmentSpinner;
	private RelativeLayout setNameLayout;
	private RelativeLayout deleteEquipmentLayout;
	private Button btnUpdate;
	private ArrayAdapter<String> equipmentSpinnerAdapter;
	private ArrayList<String> equipmentCode;
	private int currentEquipmentCodeIndex = 0;
	private Config config;	
	private BaseProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_managerequipment);
		InitView();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if (equipmentCode != null && equipmentCode.size() > currentEquipmentCodeIndex) {
				config.setLastEquipmentCode(equipmentCode.get(currentEquipmentCodeIndex));
			}
			Intent intentRtn = new Intent(ManagerEquipment.this, MainActivityNew.class);
			startActivity(intentRtn);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void InitView(){
		config = new Config(this);
		
		dialog = new BaseProgressDialog(ManagerEquipment.this);
		
		getDataFromLastActivity();
		
		tvEquipmentName = (TextView) findViewById(R.id.manager_equipment_name);
		
		setSpinnerAdapter(equipmentCode);
			
		equipmentSpinner = (Spinner) findViewById(R.id.manager_equipment_spinner);
		equipmentSpinner.setOnItemSelectedListener(new spinnerSelectedListener());			
		equipmentSpinner.setAdapter(equipmentSpinnerAdapter);
		equipmentSpinner.setSelection(currentEquipmentCodeIndex);
		
		setNameLayout = (RelativeLayout) findViewById(R.id.setname_layout);
		setNameLayout.setOnClickListener(new setNameListener());
		
		deleteEquipmentLayout = (RelativeLayout) findViewById(R.id.delete_equipment_layout);
		deleteEquipmentLayout.setOnClickListener(new deleteListener());
		
		btnUpdate = (Button) findViewById(R.id.managerEquipment_update);
		btnUpdate.setOnClickListener(new updateListener());

	}
	
	private void getDataFromLastActivity(){
		Intent intent = getIntent();
		equipmentCode = intent.getStringArrayListExtra("code");
		currentEquipmentCodeIndex = intent.getIntExtra("EquipmentIndex", 0);
	}
	
	private void setSpinnerAdapter(ArrayList<String> equipmentCode){
		equipmentSpinnerAdapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item);
		equipmentSpinnerAdapter.setNotifyOnChange(false);
		equipmentSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < equipmentCode.size(); i++) {
			equipmentSpinnerAdapter.add(equipmentCode.get(i));
		}
	}
	
	private class setNameListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			showSetNameDialog();
		}
	}
	
	private class deleteListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(!(currentEquipmentCodeIndex<equipmentCode.size())){
				ToastUtil.showShort(ManagerEquipment.this, "当前设备为空");
				return;
			}
			deleteEquipment();
		}
	}
	
	private class updateListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if (equipmentCode != null && equipmentCode.size() > currentEquipmentCodeIndex) {
				getIndicatorType(equipmentCode.get(currentEquipmentCodeIndex));
			}
		}		
	}
	
	private void getIndicatorType(String code){
		GetIndicatorObservable.createObservable(code).
			subscribeOn(Schedulers.newThread()).
			subscribe(new Subscriber<List<TypeBean>>() {
				@Override
				public void onStart() {
					super.onStart();
					runOnUiThread(new Runnable() {					
						@Override
						public void run() {
							dialog.setMessage("请求配置信息");
							dialog.show();
						}
					});
				}

				@Override
				public void onCompleted() {
					runOnUiThread(new Runnable() {					
						@Override
						public void run() {
							Log.d("getIndicatorObservable", "OnCompleted");
							dialog.dismiss();
						}
					});
				}

				@Override
				public void onError(Throwable arg0) {
					runOnUiThread(new Runnable() {					
						@Override
						public void run() {
							ToastUtil.showShort(ManagerEquipment.this, "配置请求失败");
						}
					});
				}

				@Override
				public void onNext(final List<TypeBean> list) {
					runOnUiThread(new Runnable() {						
						@Override
						public void run() {
							if(list!=null){
								goToUpdateActivity(list);
							}
							else{
								ToastUtil.showLong(ManagerEquipment.this, "该设备没有可干预选项");
							}
						}
					});
				}
			});
	}
	
	private void goToUpdateActivity(List<TypeBean> list){
		Intent intentUpdate = new Intent(ManagerEquipment.this,UpdateData.class);
		intentUpdate.putExtra("name", tvEquipmentName.getText().toString());
		intentUpdate.putExtra("code", equipmentCode.get(currentEquipmentCodeIndex));
		intentUpdate.putExtra("size", list.size());
		int i = 0;
		for (TypeBean t : list) {
			intentUpdate.putExtra("protocolName" + String.valueOf(i),
					t.name);
			intentUpdate.putExtra("protocolKey" + String.valueOf(i++),
					t.protocolKey);

		}
		startActivity(intentUpdate);
	}
	
	private class spinnerSelectedListener implements OnItemSelectedListener{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			if(equipmentCode != null && !(position<equipmentCode.size())){
				return;
			}
			currentEquipmentCodeIndex = position;
			read();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	}
	
	private void showSetNameDialog(){
		final LinearLayout setName = (LinearLayout) getLayoutInflater().
				inflate(R.layout.view_manager_equipment, null);
		new AlertDialog.Builder(this).
		setTitle("设备："+equipmentCode.get(currentEquipmentCodeIndex)).
		setView(setName).
		setPositiveButton("确定", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText etEquipmentName = (EditText) 
						setName.findViewById(R.id.et_manager_quipment_name);
				
				tvEquipmentName.setText(etEquipmentName.getText().toString());
				
				ToastUtil.showShort(ManagerEquipment.this, 
						equipmentCode.get(currentEquipmentCodeIndex)+":"+etEquipmentName.getText().toString());
				
				write(etEquipmentName.getText().toString());
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		}).create().show();
	}

	public void read(){
		if (equipmentCode != null && !(currentEquipmentCodeIndex < equipmentCode.size())) {
			ToastUtil.showLong(ManagerEquipment.this, "当前设备为空");
			return;
		}
		try {
			FileInputStream fis = openFileInput(config.getUsername()+
					equipmentCode.get(currentEquipmentCodeIndex)); 
			byte[] buff = new byte[1024];
			int hasRead = 0;
			StringBuilder sb = new StringBuilder("");
			while((hasRead = fis.read(buff))>0){
				sb.append(new String(buff,0,hasRead));
			}
			fis.close();
			String equipmentNameFromFile = sb.toString();
			equipmentNameFromFile = equipmentNameFromFile.replaceAll("\n", "");
			tvEquipmentName.setText(equipmentNameFromFile);			
		} catch (Exception e) {
			tvEquipmentName.setText("设备尚未命名");
			e.printStackTrace();
		}		
	};
	
	private void write(String content){
		try {
			FileOutputStream fos = openFileOutput(config.getUsername()+
					equipmentCode.get(currentEquipmentCodeIndex), MODE_PRIVATE);
			PrintStream ps = new PrintStream(fos);
			ps.println(content);
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void deleteEquipment(){
		new AlertDialog.Builder(this).
		setTitle("删除设备").
		setMessage("您确定要删除设备："+equipmentCode.get(currentEquipmentCodeIndex)+"？").
		setPositiveButton("确定", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				unbindEquipment(equipmentCode.get(currentEquipmentCodeIndex));
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		}).create().show();
	}
	
	private void unbindEquipment(String code){
		DeleteEquipmentObservable.create(code).subscribeOn(Schedulers.newThread()).subscribe(
				new Subscriber<Boolean>() {
					@Override
					public void onCompleted() {
						Log.d("unbindEquipment", "UnbindEquipmentOnCompleted");
					}

					@Override
					public void onError(Throwable arg0) {
						runOnUiThread(new Runnable() {						
							@Override
							public void run() {
								ToastUtil.showShort(ManagerEquipment.this, "对不起，设备删除失败，请重试");
							}
						});
					}

					@Override
					public void onNext(final Boolean arg0) {
						runOnUiThread(new Runnable() {						
							@Override
							public void run() {
								if(arg0 == true){
									ToastUtil.showShort(ManagerEquipment.this, "设备删除成功");
									refreshView();
								}else{
									ToastUtil.showShort(ManagerEquipment.this, "对不起，设备删除失败，请重试");
								}
							}
						});
					}
				});
	}
	
	private void refreshView(){
		//config.removeEquipmentName(equipmentCode.get(currentEquipmentCodeIndex));
		delete();
		equipmentSpinnerAdapter.remove(equipmentCode.get(currentEquipmentCodeIndex));
		equipmentSpinnerAdapter.notifyDataSetChanged();
		equipmentCode.remove(currentEquipmentCodeIndex);
		if (equipmentCode != null && currentEquipmentCodeIndex + 1 < equipmentCode
				.size()) {
			++currentEquipmentCodeIndex;
		} else if (equipmentCode != null && currentEquipmentCodeIndex - 1 >= 0) {
			--currentEquipmentCodeIndex;
		}else{
			Intent intent = new Intent(ManagerEquipment.this,MainActivityNew.class);
			startActivity(intent);
			ManagerEquipment.this.finish();
		}
		equipmentSpinner.setSelection(currentEquipmentCodeIndex);
	}
	
	private void delete(){
		try {
			File file = new File(getFilesDir(),config.getUsername()+equipmentCode.get(currentEquipmentCodeIndex));
			if(file.exists()){
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
