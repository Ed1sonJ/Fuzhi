package com.videogo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.videogo.open.R;
import com.videogo.test.InterfaceSelfTestActivity;
import com.videogo.test.InterfaceTestActivity;
import com.videogo.ui.discovery.SquareColumnActivity;
import com.videogo.ui.util.OpenYSService;

public class InterfaceDemoActivity extends Activity implements OnClickListener,OpenYSService.OpenYSServiceListener {
	private static final String TAG = "InterfaceDemoActivity";
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ez_demo_interface_activity);
        
        initData();
        initView();  
	}
    private void initData() {
//    	mEZOpenSDK = EZOpenSDK.getInstance();
    }
    
    private void initView() {
        
    }

    @Override
    public void onClick(View v) {
        Intent intent  = null;
        int i1 = v.getId();
        if (i1 == R.id.ez_square_btn) {
            intent = new Intent(InterfaceDemoActivity.this, SquareColumnActivity.class);
            startActivity(intent);

        } else if (i1 == R.id.ez_platform_login_btn) {
            OpenYSService.openYSServiceDialog(this, this);

        } else if (i1 == R.id.ez_v32_api_test) {
            Intent i = new Intent(InterfaceDemoActivity.this, InterfaceTestActivity.class);
            startActivity(i);

        } else if (i1 == R.id.ez_api_self_test) {
            intent = new Intent(InterfaceDemoActivity.this, InterfaceSelfTestActivity.class);
            startActivity(intent);

        } else {
        }
    }

	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	public void onOpenYSService(int result) {
		// TODO Auto-generated method stub
		
	}

}
