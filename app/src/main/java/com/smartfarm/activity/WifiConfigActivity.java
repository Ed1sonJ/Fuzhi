package com.smartfarm.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartfarm.util.ToastUtil;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class WifiConfigActivity extends Activity {
    protected static final int ERROR = -1;
    protected static final int WAIT_FOR_CONNECT = 0;
    protected static final int CLIENT_CONNECT_OK = 2;
    protected static final int CLIENT_CONNECT_FALSE = 3;

    protected int currentStatus = WAIT_FOR_CONNECT;
    protected String currentWifi = "";
    protected String selectedWifiID = "";

    protected boolean isRunning = false;
    protected Thread listenThread;
    protected DatagramSocket serverSocket;
    protected InetAddress currentInetAddress;
    protected int currentPort;
    protected boolean selectingWifi = false;

    protected ImageView stateImageView;
    protected TextView stateTextView;
    protected ListView wifiIDListView;
    protected EditText wifiPwdTextView;
    protected Button uploadButton;
    protected ProgressBar progressBar;
    protected Button wifiButton;
    protected Button okButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_config);
        setUpView();
        isRunning = true;
        refreshState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (serverSocket != null)
            serverSocket.close();
    }

    protected void setUpView() {
        stateImageView = (ImageView) findViewById(R.id.state_image);
        stateTextView = (TextView) findViewById(R.id.state_text);
        wifiIDListView = (ListView) findViewById(R.id.wifi_id);
        wifiPwdTextView = (EditText) findViewById(R.id.wifi_pwd);
        uploadButton = (Button) findViewById(R.id.upload_wifi);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        wifiButton = (Button) findViewById(R.id.wifi_button);
        wifiButton.setOnClickListener(new SetWifi());
        okButton = (Button) findViewById(R.id.ok_button);
        okButton.setOnClickListener(new OkAndSetWifi());
        uploadButton.setOnClickListener(new OnUploadListener());
    }

    protected void refreshState() {
        handleStateChange(WAIT_FOR_CONNECT);
        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new DatagramSocket(63333);
                    serverSocket.setSoTimeout(300000);
                    while (isRunning) {
                        byte[] buf = new byte[1024];
                        final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        try {
                            serverSocket.receive(packet);
                            currentInetAddress = packet.getAddress();
                            currentPort = packet.getPort();
                            final String data = new String(packet.getData(), packet.getOffset(), packet.getLength());
                            WifiConfigActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtil.showShort(WifiConfigActivity.this, data);
                                }
                            });

                            if (data.equals("false")) {
                                handleStateChange(CLIENT_CONNECT_FALSE);
                            } else if (data.equals("error")) {
                                handleStateChange(ERROR);
                            } else if (data.split("\n")[0].equals("true")) {
                                currentWifi = data.split("\n")[1];
                                handleStateChange(CLIENT_CONNECT_OK);
                                String msg = "ok1";
                                byte[] bytes = msg.getBytes("utf-8");
                                DatagramPacket p = new DatagramPacket(bytes, 0, bytes.length, currentInetAddress, currentPort);
                                serverSocket.send(p);
                            } else if (data.indexOf("Progress") != -1) {
                                int index = data.indexOf(":");
                                String strNum = data.substring(index + 1);
                                int p = Integer.valueOf(strNum);
                                setProgressP(p);
                            } else {
                                handleStateChange(ERROR);
                            }
                        } catch (InterruptedIOException e) {
                            handleStateChange(WAIT_FOR_CONNECT);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handleStateChange(ERROR);
                }
            }
        });
        listenThread.start();
    }

    protected void handleStateChange(final int status) {
        currentStatus = status;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (status) {
                    case ERROR:
                        showErrorState();
                        break;
                    case WAIT_FOR_CONNECT:
                        showWaitConnectState();
                        break;
                    case CLIENT_CONNECT_FALSE:
                        if (!selectingWifi) {
                            showSelectWifiIDState();
                            selectingWifi = true;
                        }
                        break;
                    case CLIENT_CONNECT_OK:
                        showWifiConnectedState();
                        break;
                }
            }
        });
    }

    protected void disableEveryThing() {
        stateImageView.setVisibility(View.GONE);
        stateTextView.setVisibility(View.GONE);
        wifiIDListView.setVisibility(View.GONE);
        wifiPwdTextView.setVisibility(View.GONE);
        uploadButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        wifiButton.setVisibility(View.GONE);
        okButton.setVisibility(View.GONE);
    }

    protected void showWaitConnectState() {
        disableEveryThing();
        stateImageView.setVisibility(View.VISIBLE);
        stateTextView.setVisibility(View.VISIBLE);
        stateImageView.setImageResource(R.drawable.icon_wait);
        stateTextView.setText("请将手机连入设备wifi");
        wifiButton.setVisibility(View.VISIBLE);
    }

    protected void showSelectWifiIDState() {
        disableEveryThing();
        stateImageView.setVisibility(View.VISIBLE);
        stateTextView.setVisibility(View.VISIBLE);
        wifiIDListView.setVisibility(View.VISIBLE);
        stateTextView.setText("请选择Wifi：");
        stateImageView.setImageResource(R.drawable.icon_wifi_not_connected);
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        final List<ScanResult> results_list = wm.getScanResults();
        ArrayList<String> list = new ArrayList();
        for (ScanResult r : results_list) {
            if (r.SSID.length() > 0) {
                list.add(r.SSID);
            }
        }
        wifiIDListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, list));
        wifiIDListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedWifiID = results_list.get(position).SSID;
                showWifiInputPwdState();
            }
        });
    }

    protected void showWifiInputPwdState() {
        disableEveryThing();
        stateImageView.setVisibility(View.VISIBLE);
        stateTextView.setVisibility(View.VISIBLE);
        wifiPwdTextView.setVisibility(View.VISIBLE);
        uploadButton.setVisibility(View.VISIBLE);
        stateTextView.setText("请输入密码：");
        stateImageView.setImageResource(R.drawable.icon_wifi_not_connected);
    }

    protected void showWifiConnectedState() {
        disableEveryThing();
        stateImageView.setVisibility(View.VISIBLE);
        stateTextView.setVisibility(View.VISIBLE);
        stateImageView.setImageResource(R.drawable.icon_wifi_connected);
        stateTextView.setText("请等待指示灯常亮");
        okButton.setVisibility(View.VISIBLE);
    }

    protected void showConnectingState() {
        disableEveryThing();
        stateImageView.setVisibility(View.VISIBLE);
        stateTextView.setVisibility(View.VISIBLE);
        stateTextView.setText("正在连接...");
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void showErrorState() {
        disableEveryThing();
        stateImageView.setVisibility(View.VISIBLE);
        stateTextView.setVisibility(View.VISIBLE);
        stateImageView.setImageResource(R.drawable.icon_error);
        stateTextView.setText("连接错误，请复位机器再试");
    }

    protected void setProgressP(int p) {
        progressBar.setProgress(p);
    }

    protected class OnUploadListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String id = selectedWifiID;
                        String pwd = wifiPwdTextView.getText().toString();
                        String msg = id + "," + pwd;
                        byte[] bytes = msg.getBytes("utf-8");
                        DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length, currentInetAddress, currentPort);
                        serverSocket.send(packet);
                        WifiConfigActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showConnectingState();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        WifiConfigActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showErrorState();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    protected class SetWifi implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
    }

    protected class OkAndSetWifi implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
    }
}