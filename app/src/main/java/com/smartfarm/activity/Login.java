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
        login = (Button) findViewById(R.id.loginBtn); // 登录按钮
        btnForgetPwd = (Button) findViewById(R.id.btn_forget_pwd); // 忘记密码按钮
        btnRegister = (Button) findViewById(R.id.btn_register); // 注册按钮
        username = (EditText) findViewById(R.id.account); // 输入用户名文本框
        password = (EditText) findViewById(R.id.password); // 输入密码的文本框
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
            ToastUtil.showShort(this, "用户名格式不正确");
            return false;
        }
        String strPwd = password.getText().toString();
        Matcher pwdMatcher = pattern.matcher(strPwd);
        if (!pwdMatcher.matches()) {
            ToastUtil.showShort(this, "密码格式不正确");
            return false;
        }
        return true;
    }

    public void findPwdBack() {
        phone = username.getText().toString();
        if (phone.equals("")) {
            ToastUtil.showShort(Login.this, "请输入账号");
            return;
        }
        findPwdHttp.request(phone, new FindPasswordHttp.MyListener() {

            @Override
            public void success(String find_password_status) {
                progressDialog.dismiss();
                if (find_password_status.equals("ok")) {
                    ToastUtil.showShort(Login.this, "密码已找回，请留意短信");
                } else {
                    ToastUtil.showShort(Login.this, "您输入的号码尚未注册");
                }
            }

            @Override
            public void loading() {
                progressDialog.setMessage("申请找回密码");
                progressDialog.show();

            }

            @Override
            public void fail() {
                progressDialog.dismiss();
                ToastUtil.showShort(Login.this, "无法联系服务器");
            }
        });
    }

    //LoginObservable.create(username, password)返回一个Map<String,String>有role，name的信息
    //subscribeOn，指定 subscribe() 所发生的线程，Schedulers.newThread(): 总是启用新线程，并在新线程执行操作。
    //subscribe产生订阅关系
    //onStart() 可以用作流程开始前的初始化。然而 onStart() 由于在 subscribe() 发生时就被调用了，
    // 因此不能指定线程，而是只能执行在 subscribe() 被调用时的线程。
    //所以使用runOnUiThread
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
                                        progressDialog.setMessage("正在登录");
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
                                        ToastUtil.showShort(Login.this, "登录失败，请重新登陆");
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
                                            ToastUtil.showShort(Login.this, "登陆失败，请确认账号密码和网络连接");
                                        }
                                    }
                                });
                            }
                        });
    }
}
