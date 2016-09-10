package com.smartfarm.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.smartfarm.util.BaseUtil;

/**
 * Created by hp on 2016/9/10.
 * @author EdisonJ
 * 封装了最底层的dialog
 * 提供设置dialog的基本样式：显示的位置、透明度、显示的View等
 */
public class BaseAlterDialogUtil {
    private AlertDialog baseDialog;
    private Context mContext;
    /**
     * dialog的属性
     */
    private WindowManager.LayoutParams params;
    /**
     * dialog应用属性的实例
     */
    private Window dialogWindow;


    public BaseAlterDialogUtil(Context context) {
        this.mContext = context;
        baseDialog = new AlertDialog.Builder(context).create();
        dialogWindow = baseDialog.getWindow();
        params = dialogWindow.getAttributes();
        baseDialog.setView(new EditText(context));
        //必须写在获取属性之前
        baseDialog.show();
    }

    /**
     * 设置宽高在屏幕中的比例
     */
    public void setWidthAndHeightRadio(float widthRaido, float heightRadio) {
        params.width = (int) (BaseUtil.getScreenWidth(mContext) * widthRaido);
        params.height = (int) (BaseUtil.getScreenHeight(mContext) * heightRadio);
        dialogWindow.setAttributes(params);
    }

    /**
     * 设置对话框显示的位置及偏移量
     *
     * @param x x为设置gravity之后x相对原始位置的偏移量，向左为负值，向右为正值
     * @param y y为设置gravity之后y相对原始位置的偏移量，向左为负值，向右为正值
     */
    public void setLocation(int gravity, int x, int y) {
        params.gravity = gravity;
        // TODO: 2016/8/30 可能要加一重判断
        params.x = x;
        params.y = y;
        dialogWindow.setAttributes(params);
    }

    /**
     * 设置对话框的透明度
     *
     * @param alphaRadio 透明度的比率：0~1f
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
     * 设置dialog消失
     */
    public void dismiss() {
        if (baseDialog != null && baseDialog.isShowing())
            baseDialog.dismiss();

    }

    /**
     * 设置显示的View,设置完基本属性之后调用
     *
     * @param view
     */
    public void setContentView(View view) {
        dialogWindow.setContentView(view);
    }
}
