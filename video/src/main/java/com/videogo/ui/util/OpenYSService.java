/* 
 * @ProjectName ezviz-openapi-android-demo
 * @Copyright HangZhou Hikvision System Technology Co.,Ltd. All Right Reserved
 * 
 * @FileName OpenYSService.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2015-7-14
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.videogo.api.TransferAPI;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.open.R;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EzvizAPI;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.Utils;
import com.videogo.widget.WaitDialog;

import org.json.JSONException;

/**
 * 在此对类做相应的描述
 * @author chenxingyf1
 * @data 2015-7-14
 */
public class OpenYSService {
    public interface OpenYSServiceListener {
        void onOpenYSService(int result);
    }
    
    public static void openYSServiceDialog(final Context context, final OpenYSServiceListener l) {
        LayoutInflater mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup smsVerifyView = (ViewGroup) mLayoutInflater.inflate(R.layout.open_ysservice_dialog, null, true);
        
        final EditText phoneEt = (EditText) smsVerifyView.findViewById(R.id.phone_et);
        final EditText smsCodeEt = (EditText) smsVerifyView.findViewById(R.id.sms_code_et);
        final Button getSmsBtn = (Button) smsVerifyView.findViewById(R.id.get_sms_code_btn);
        getSmsBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String phone = phoneEt.getText().toString();
                new GetSmsCodeTask(context).execute(phone);
            }
            
        });

        new  AlertDialog.Builder(context)  
        .setTitle(R.string.please_input_phone_txt)   
        .setIcon(android.R.drawable.ic_dialog_info)   
        .setView(smsVerifyView)
        .setPositiveButton(R.string.open_ys_service, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                new OpenYSServiceTask(context, l,  
                        phoneEt.getText().toString(),
                        smsCodeEt.getText().toString()).execute();
            }
            
        })   
        .setNegativeButton(R.string.cancel, null)
        .show();  
    }
    
    private static class GetSmsCodeTask extends AsyncTask<String, Void, Boolean> {
        private Context mContext;
        private Dialog mWaitDialog;
        private int mErrorCode = 0;
        
        public GetSmsCodeTask(Context context) {
            mContext = context;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (!ConnectionDetector.isNetworkAvailable(mContext)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return false;
            }

            try {
                return EZOpenSDK.getInstance().getOpenEzvizServiceSMSCode(params[0]);
            } catch (BaseException e) {
                mErrorCode = e.getErrorCode();
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();

            if (mErrorCode != 0)
                onError(mErrorCode);
        }

        protected void onError(int errorCode) {
            switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                    EzvizAPI.getInstance().gotoLoginPage();
                    break;

                default:
                    Utils.showToast(mContext, R.string.get_sms_code_fail, mErrorCode);
                    break;
            }
        }
    }
    
    private static class OpenYSServiceTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;
        private OpenYSServiceListener mOpenYSServiceListener;
        private String mSmsCode;
        private String mPhone;
        private Dialog mWaitDialog;
        private int mErrorCode = 0;
        
        public OpenYSServiceTask(Context context, OpenYSServiceListener l, String phone, String smsCode) {
            mContext = context;
            mOpenYSServiceListener = l;
            mPhone = phone;
            mSmsCode = smsCode;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWaitDialog = new WaitDialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
            mWaitDialog.setCancelable(false);
            mWaitDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (!ConnectionDetector.isNetworkAvailable(mContext)) {
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return false;
            }

            try {
            	return EZOpenSDK.getInstance().openEzvizService(mPhone, mSmsCode);
            } catch (BaseException e) {
                mErrorCode = e.getErrorCode();
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mWaitDialog.dismiss();
            
            if (mErrorCode == 0) {
                Utils.showToast(mContext, R.string.open_ys_service_success);
                mOpenYSServiceListener.onOpenYSService(mErrorCode);
            } else {
                onError(mErrorCode);
            }
        }

        protected void onError(int errorCode) {
            switch (errorCode) {
                case ErrorCode.ERROR_WEB_SESSION_ERROR:
                case ErrorCode.ERROR_WEB_SESSION_EXPIRE:
                case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                    EzvizAPI.getInstance().gotoLoginPage();
                    break;
                default:
                    Utils.showToast(mContext, R.string.open_ys_service_fail, mErrorCode);
                    break;
            }
        }
    }
}
