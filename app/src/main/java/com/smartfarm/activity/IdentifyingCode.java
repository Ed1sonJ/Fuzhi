package com.smartfarm.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartfarm.http.GetCodeHttp;
import com.smartfarm.http.GetCodeHttp.MyListener;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.Config;
import com.smartfarm.util.HttpUtil;
import com.smartfarm.util.Protocol;
import com.smartfarm.util.ToastUtil;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifyingCode extends Activity {

	private EditText phoneNumer = null, phoneYz = null;
	private TextView getYz = null;
	private Button postYz = null;
	private CharSequence charSequence;
	private GetCodeHttp getCodeHttp = new GetCodeHttp();
	private TimeCount tc;
	private String phone;
	private BaseProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_identifying_code);
		initView();
		initSmsReceive();
	}

	private void initView() {
		phoneNumer = (EditText) findViewById(R.id.et_pho_num);
		phoneYz = (EditText) findViewById(R.id.et_yanzheng);
		getYz = (TextView) findViewById(R.id.btn_yz);
		postYz = (Button) findViewById(R.id.btn_post);

		progressDialog = new BaseProgressDialog(IdentifyingCode.this);
		progressDialog.setMessage("���Ե�...");

		phoneNumer.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				charSequence = arg0;
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (charSequence.length() > 11) {
					phoneNumer.setTextColor(Color.RED);
				} else {
					phoneNumer.setTextColor(Color.BLACK);
				}
			}
		});
		getYz.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				phone = phoneNumer.getText().toString();
				if (TextUtils.isEmpty(phone)) {
					ToastUtil.showShort(IdentifyingCode.this, "�������ֻ���");
				} else {
					if (!isMobileNO(phone)) {
						ToastUtil.showShort(IdentifyingCode.this, "��ʽ����");
						return;
					}
					startConnect();
					tc = new TimeCount(30000, 1000);
					tc.start();
				}
			}
		});
		postYz.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String phone = phoneNumer.getText().toString();
				String validCode = phoneYz.getText().toString();
				checkValidCode(phone, validCode);
			}
		});

	}

	public void startConnect() {
		getCodeHttp.request(phone, new MyListener() {

			@Override
			public void success() {
				progressDialog.dismiss();
				ToastUtil.showLong(IdentifyingCode.this, "������֤��ɹ��������ĵȴ�����");
			}

			@Override
			public void loading() {
				progressDialog.show();
			}

			@Override
			public void fail() {
				progressDialog.dismiss();
				// TODO:����������Ǻ����ѱ�ע�ᡣ������Ҫ�Ľӿڣ������������ص���Ϣ�жϡ�
				ToastUtil.showShort(IdentifyingCode.this, "�����ѱ�ע��");
				tc.cancel();
			}
		});
	}

	/*
	 * �ֻ���ʽ
	 */
	public boolean isMobileNO(String mobiles) {
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	/*
	 * �ֻ���ʽ
	 */
	public boolean isMobileNO(CharSequence mobiles) {
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	/* ����һ������ʱ���ڲ��� */
	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);// ��������Ϊ��ʱ��,�ͼ�ʱ��ʱ����
		}

		@Override
		public void onFinish() {// ��ʱ���ʱ����
			getYz.setText("������֤");
			getYz.setClickable(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {// ��ʱ������ʾ
			getYz.setClickable(false);
			getYz.setText("��ȴ�" + millisUntilFinished / 1000 + "��");
		}
	}

	/* ��ȡ���� */
	private SmsReciver smsReceive;

	private boolean isFromServer(String msg) {
		Pattern pattern = Pattern.compile("������ũҵ��");
		Matcher matcher = pattern.matcher(msg);
		if (matcher.find())
			return true;
		else
			return false;
	}

	private String getValidCode(String msg) {
		Pattern pattern = Pattern.compile(".*��֤��([0-9]*)���ֻ�ע�ᣩ");
		Matcher matcher = pattern.matcher(msg);
		if (matcher.find()) {
			String res = matcher.group(1);
			return res;
		} else {
			return "";
		}
	}

	private void initSmsReceive() {
		smsReceive = new SmsReciver();
		IntentFilter intentFilter = new IntentFilter(
				"android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(smsReceive, intentFilter);
	}

	private class SmsReciver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			SmsMessage msg = null;
			if (null != bundle) {
				Object[] smsObj = (Object[]) bundle.get("pdus");
				for (Object object : smsObj) {
					msg = SmsMessage.createFromPdu((byte[]) object);
					Log.d("gzfuzhi", "number:" + msg.getOriginatingAddress()
							+ "   body:" + msg.getDisplayMessageBody()
							+ "  time:" + msg.getTimestampMillis());
					String body = msg.getDisplayMessageBody();
					if (isFromServer(body)) {
						String validCode = getValidCode(body);
						tc.cancel();
						tc.onFinish();
						if (!validCode.equals("")) {
							phoneYz.setText(validCode);
						}
					}
				}
			}
		}
	}

	/* Valid code */
	private String uriAPI = Protocol.CHECKCODE_URL;
	private Handler handler = new Handler();

	private void showProgress() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				progressDialog.show();
			}
		});
	}

	private void dismissProgress() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				progressDialog.dismiss();
			}
		});
	}

	private void showValidOk() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				ToastUtil.showShort(IdentifyingCode.this, "��֤�ɹ�");
			}
		});
	}

	private void showValidFail() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				ToastUtil.showShort(IdentifyingCode.this, "��֤ʧ��");
			}
		});
	}

	private void switchToRegister() {
		phone = phoneNumer.getText().toString();
		Intent intent = new Intent(IdentifyingCode.this, RegisterNew.class);
		intent.putExtra("phoneNumber", phone);
		startActivity(intent);
		finish();
	}

	private void checkValidCode(String phoneNum, String validCode) {
		final List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		String jsonStr = String.format("{\"phone\":\"%s\", \"code\":\"%s\"}",
				phoneNum, validCode);
		params.add(new BasicNameValuePair("data", jsonStr));
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					showProgress();
					String response = HttpUtil.httpPost(
							new DefaultHttpClient(), uriAPI, params, "");
					if (response == null) {
						dismissProgress();
						showValidFail();
					} else {
						JSONObject responseJson = new JSONObject(response);
						String responseCode = (String) responseJson
								.get("errmsg");
						Log.d("gzfuzhi", responseCode);
						if (responseCode.equals("ok")) {
							dismissProgress();
							showValidOk();
							Config config = new Config(getApplication());
							config.setUsername(phone);
							switchToRegister(); // ��ת��ע�����
						} else {
							dismissProgress();
							showValidFail();
						}
					}
				} catch (Exception exception) {
					Log.d("gzfuzhi", exception.getMessage());
					dismissProgress();
					showValidFail();
				}
			}
		});
		thread.start();
	}
}
