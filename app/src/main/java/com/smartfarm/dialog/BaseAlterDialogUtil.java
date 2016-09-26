package com.smartfarm.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.baidu.platform.comapi.map.I;
import com.smartfarm.util.BaseUtil;
import com.smartfarm.util.ToastUtil;

/**
 * Created by hp on 2016/9/10.
 *
 * @author EdisonJ
 *         ��װ����ײ��dialog
 *         �ṩ����dialog�Ļ�����ʽ����ʾ��λ�á�͸���ȡ���ʾ��View��
 */
public class BaseAlterDialogUtil {
    private AlertDialog baseDialog;
    private Context mContext;
    /**
     * dialog������
     */
    private WindowManager.LayoutParams params;
    /**
     * dialogӦ�����Ե�ʵ��
     */
    private Window dialogWindow;


    public BaseAlterDialogUtil(Context context) {
        this.mContext = context;
        baseDialog = new AlertDialog.Builder(context).create();
        dialogWindow = baseDialog.getWindow();
        params = dialogWindow.getAttributes();
        //���õ��dialog����dismiss dialog
        baseDialog.setCanceledOnTouchOutside(true);
        baseDialog.setView(new EditText(context));

        //����д�ڻ�ȡ����֮ǰ
        baseDialog.show();

    }

    /**
     * ���ÿ������Ļ�еı���
     */
    public void setWidthAndHeightRadio(float widthRadio, float heightRadio) {

        if (widthRadio != 0) {
            params.width = (int) (BaseUtil.getScreenWidth(mContext) * widthRadio);
        }
        if (heightRadio != 0) {
            params.height = (int) (BaseUtil.getScreenHeight(mContext) * heightRadio);
        }
//        warp_content=-2,match_parent=-1
        dialogWindow.setAttributes(params);
    }

    /**
     * ���öԻ�����ʾ��λ�ü�ƫ����
     *
     * @param x xΪ����gravity֮��x���ԭʼλ�õ�ƫ����������Ϊ��ֵ������Ϊ��ֵ
     * @param y yΪ����gravity֮��y���ԭʼλ�õ�ƫ����������Ϊ��ֵ������Ϊ��ֵ
     */
    public void setLocation(int gravity, int x, int y) {
        params.gravity = gravity;
        // TODO: 2016/8/30 ����Ҫ��һ���ж�
        params.x = x;
        params.y = y;
        dialogWindow.setAttributes(params);
    }

    /**
     * ���öԻ����͸����
     *
     * @param alphaRadio ͸���ȵı��ʣ�0~1f
     */
    public void setAlpha(float alphaRadio) {
        if (alphaRadio > 1 || alphaRadio < 0) {
            return;
        }
        params.alpha = alphaRadio;
    }

    public void setAnimations() {

    }

    /**
     * ����dialog��ʧ
     */
    public void dismiss() {
        if (baseDialog != null && baseDialog.isShowing())
            baseDialog.dismiss();

    }

    /**
     * ������ʾ��View,�������������֮�����
     *
     * @param view
     */
    public void setContentView(View view) {
        dialogWindow.setContentView(view);


    }

//    public void onShow(final EditText view) {
//        baseDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
//            }
//        });
//    }
}
