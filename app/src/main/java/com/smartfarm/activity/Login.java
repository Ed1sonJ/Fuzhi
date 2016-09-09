package com.smartfarm.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.smartfarm.http.FindPasswordHttp;
import com.smartfarm.observable.LoginObservable;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.Common;
import com.smartfarm.util.Config;
import com.smartfarm.util.ToastUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Subscriber;
import rx.schedulers.Schedulers;

public class Login extends Activity {

    private Button login, btnForgetPwd, btnRegister;
    private EditText username;
    private EditText password;
    private FindPasswordHttp findPwdHttp = new FindPasswordHttp();
    private Config config;
    private BaseProgressDialog progressDialog;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById();
        initView();
    }

    private void findViewById() {
        login = (Button) findViewById(R.id.loginBtn); // ��¼��ť
        btnForgetPwd = (Button) findViewById(R.id.btn_forget_pwd); // �������밴ť
        btnRegister = (Button) findViewById(R.id.btn_register); // ע�ᰴť
        username = (EditText) findViewById(R.id.account); // �����û����ı���
        password = (EditText) findViewById(R.id.password); // ����������ı���
    }

    private void initView() {
        config = new Config(this);
        username.setText(config.getLastUsername());

        progressDialog = new BaseProgressDialog(Login.this);

        login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkUsernameAndPwd()) {
                    return;
                }
                getLoginObservable(username.getText().toString(), password.getText()
                        .toString());
            }
        });

        btnForgetPwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findPwdBack();
            }
        });

        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, IdentifyingCode.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean checkUsernameAndPwd() {
        Pattern pattern = Pattern.compile("^[0-9A-z]{3,30}$");
        String strUsername = username.getText().toString();
        Matcher usernameMatcher = pattern.matcher(strUsername);
        if (!usernameMatcher.matches()) {
            ToastUtil.showShort(this, "�û�����ʽ����ȷ");
            return false;
        }
        String strPwd = password.getText().toString();
        Matcher pwdMatcher = pattern.matcher(strPwd);
        if (!pwdMatcher.matches()) {
            ToastUtil.showShort(this, "�����ʽ����ȷ");
            return false;
        }
        return true;
    }

    public void findPwdBack() {
        phone = username.getText().toString();
        if (phone.equals("")) {
            ToastUtil.showShort(Login.this, "�������˺�");
            return;
        }
        findPwdHttp.request(phone, new FindPasswordHttp.MyListener() {

            @Override
            public void success(String find_password_status) {
                progressDialog.dismiss();
                if (find_password_status.equals("ok")) {
                    ToastUtil.showShort(Login.this, "�������һأ����������");
                } else {
                    ToastUtil.showShort(Login.this, "������ĺ�����δע��");
                }
            }

            @Override
            public void loading() {
                progressDialog.setMessage("�����һ�����");
                progressDialog.show();

            }

            @Override
            public void fail() {
                progressDialog.dismiss();
                ToastUtil.showShort(Login.this, "�޷���ϵ������");
            }
        });
    }

    //LoginObservable.create(username, password)����һ��Map<String,String>��role��name����Ϣ
    //subscribeOn��ָ�� subscribe() ���������̣߳�Schedulers.newThread(): �����������̣߳��������߳�ִ�в�����
    //subscribe�������Ĺ�ϵ
    //onStart() �����������̿�ʼǰ�ĳ�ʼ����Ȼ�� onStart() ������ subscribe() ����ʱ�ͱ������ˣ�
    // ��˲���ָ���̣߳�����ֻ��ִ���� subscribe() ������ʱ���̡߳�
    //����ʹ��runOnUiThread
    private void getLoginObservable(final String username, final String password) {
        LoginObservable.create(username, password).
                subscribeOn(Schedulers.newThread()).
                subscribe(
                        new Subscriber<Map<String, String>>() {
                            @Override
                            public void onStart() {
                                super.onStart();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.setMessage("���ڵ�¼");
                                        progressDialog.show();
                                    }
                                });
                            }

                            @Override
                            public void onCompleted() {
                                progressDialog.dismiss();
                                Log.d("AutoLogin", "onCompleted");
                            }

                            @Override
                            public void onError(Throwable e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        config.clear();
                                        progressDialog.dismiss();
                                        ToastUtil.showShort(Login.this, "��¼ʧ�ܣ������µ�½");
                                    }
                                });
                            }

                            @Override
                            public void onNext(final Map<String, String> login) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (login != null) {
                                            progressDialog.dismiss();
                                            config.setToken(login.get("token"));
                                            config.setUsername(username);
                                            config.setPassword(password);
                                            config.setLastUsername(username);
                                            Common.setToken(login.get("token"));
                                            Common.setUsername(username);
                                            Intent intent = new Intent(Login.this, MainActivityNew.class);
                                            startActivity(intent);
                                            Login.this.finish();
                                        } else {
                                            config.clear();
                                            progressDialog.dismiss();
                                            ToastUtil.showShort(Login.this, "��½ʧ�ܣ���ȷ���˺��������������");
                                        }
                                    }
                                });
                            }
                        });
    }
}
