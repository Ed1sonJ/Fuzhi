package com.smartfarm.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartfarm.activity.R;
import com.smartfarm.util.ToastUtil;

/**
 * Created by hp on 2016/9/10.
 * @author EdisonJ
 * 封装了dialog的基本样式(app中最常用的dialog样式)
 * 带有两个按钮、一个title、一个icon，具体内容自己设置
 */
public class BaseCustomAlterDialog {
    private Context mContext;
    private Button negativeBtn;
    private Button positiveBtn;
    private ImageView iconImg;
    private TextView titleTV;
    private RelativeLayout contentLayout;
    /**
     * 最底层的dialog，设置基本的属性
     */
    private BaseAlterDialogUtil baseDialog;
    /**
     * 本app常用dialog的布局：两个按钮，一个title，一个icon，一个contentView
     */
    private View baseCustomView;

    public BaseCustomAlterDialog(Context context) {
        this.mContext = context;
        baseDialog = new BaseAlterDialogUtil(context);
        baseCustomView = LayoutInflater.from(context).inflate(R.layout.dialog_base,null);
        initViews();
    }

    private void initViews() {
        negativeBtn = (Button) baseCustomView.findViewById(R.id.id_base_dialog_leftBtn);
        positiveBtn = (Button) baseCustomView.findViewById(R.id.id_base_dialog_rightBtn);
        titleTV = (TextView) baseCustomView.findViewById(R.id.id_base_dialog_title);
        contentLayout = (RelativeLayout) baseCustomView.findViewById(R.id.id_base_dialog_content);
    }

    /**
     * 设置dialog显示的宽和高
     * @param widthRaido
     * @param heightRadio
     */
    public void setWidthAndHeightRadio(float widthRaido, float heightRadio) {
        baseDialog.setWidthAndHeightRadio(widthRaido, heightRadio);
    }

    /**
     * 设置dialog的位置和偏移量
     * @param gravity
     * @param x
     * @param y
     */
    public void setLocation(int gravity, int x, int y) {
        baseDialog.setLocation(gravity,x,y);
    }

    /**
     * 设置dialog的透明度
     * @param alphaRadio
     */
    public void setAlpha(float alphaRadio) {
        baseDialog.setAlpha(alphaRadio);
    }

    /**
     * 设置左边按钮的事件
     * @param listener 在外面已经写好的listener
     */
    public void setNegativeBtnListener(View.OnClickListener listener){
        if(listener!=null){
            negativeBtn.setOnClickListener(listener);
        }
        else{
            negativeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtil.showShort(mContext,"点击了内层的左边listener");
                    baseDialog.dismiss();
                }
            });
        }
    }

    /**
     * 设置右边按钮的点击事件
     * @param listener 在外面已经写好的listener
     */
    public void setPositiveBtnListener(View.OnClickListener listener){
        if(listener!=null){
            positiveBtn.setOnClickListener(listener);
        }
        else{
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtil.showShort(mContext,"点击了内层的右边listener");
                    baseDialog.dismiss();
                }
            });
        }
    }

    /**
     * 设置contentView中要显示的布局
     * 最后调用
     * @param view contentView中要显示的布局view
     * @param lp view显示的位置
     */
    public void setContentView(View view, RelativeLayout.LayoutParams lp){
        if(lp!=null) {
            contentLayout.addView(view, lp);
        }else {
            contentLayout.addView(view);
        }

        baseDialog.setContentView(baseCustomView);
    }

    /**
     * 设置dialog消失
     */
    public void dismiss(){
        baseDialog.dismiss();
    }

    //---------------------------------------dialog显示的基本信息--------------------------------
    public void setTitle(String title){
        titleTV.setText(title);
    }
    public void setIcon(int imgResId){
        if(imgResId == -1){
            return ;
        }
        iconImg = (ImageView) baseCustomView.findViewById(R.id.id_base_dialog_icon);
        iconImg.setImageResource(imgResId);
        iconImg.setVisibility(View.VISIBLE);
    }
}
