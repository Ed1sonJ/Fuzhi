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
 * ��װ��dialog�Ļ�����ʽ(app����õ�dialog��ʽ)
 * ����������ť��һ��title��һ��icon�����������Լ�����
 */
public class BaseCustomAlterDialog {
    private Context mContext;
    private Button negativeBtn;
    private Button positiveBtn;
    private ImageView iconImg;
    private TextView titleTV;
    private RelativeLayout contentLayout;
    /**
     * ��ײ��dialog�����û���������
     */
    private BaseAlterDialogUtil baseDialog;
    /**
     * ��app����dialog�Ĳ��֣�������ť��һ��title��һ��icon��һ��contentView
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
     * ����dialog��ʾ�Ŀ�͸�
     * @param widthRaido
     * @param heightRadio
     */
    public BaseCustomAlterDialog setWidthAndHeightRadio(float widthRaido, float heightRadio) {
        baseDialog.setWidthAndHeightRadio(widthRaido, heightRadio);
        return this;
    }

    /**
     * ����dialog��λ�ú�ƫ����
     * @param gravity
     * @param x
     * @param y
     */
    public BaseCustomAlterDialog setLocation(int gravity, int x, int y) {
        baseDialog.setLocation(gravity,x,y);
        return this;
    }

    /**
     * ����dialog��͸����
     * @param alphaRadio
     */
    public BaseCustomAlterDialog setAlpha(float alphaRadio) {
        baseDialog.setAlpha(alphaRadio);
        return this;
    }

    /**
     * ������߰�ť���¼�
     * @param listener �������Ѿ�д�õ�listener
     */
    public BaseCustomAlterDialog setNegativeBtnListener(View.OnClickListener listener){
        if(listener!=null){
            negativeBtn.setOnClickListener(listener);
        }
        else{
            negativeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtil.showShort(mContext,"������ڲ�����listener");
                    baseDialog.dismiss();
                }
            });
        }
        return this;
    }

    /**
     * �����ұ߰�ť�ĵ���¼�
     * @param listener �������Ѿ�д�õ�listener
     */
    public BaseCustomAlterDialog setPositiveBtnListener(View.OnClickListener listener){
        if(listener!=null){
            positiveBtn.setOnClickListener(listener);
        }
        else{
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastUtil.showShort(mContext,"������ڲ���ұ�listener");
                    baseDialog.dismiss();
                }
            });
        }
        return this;
    }

    /**
     * ����contentView��Ҫ��ʾ�Ĳ���
     * ������
     * @param view contentView��Ҫ��ʾ�Ĳ���view
     * @param lp view��ʾ��λ��
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
     * ����dialog��ʧ
     */
    public void dismiss(){
        baseDialog.dismiss();
    }

    //---------------------------------------dialog��ʾ�Ļ�����Ϣ--------------------------------
    public BaseCustomAlterDialog setTitle(String title){
        titleTV.setText(title);
        return this;
    }
    public BaseCustomAlterDialog setIcon(int imgResId){
        if(imgResId == -1){
            return null;
        }
        iconImg = (ImageView) baseCustomView.findViewById(R.id.id_base_dialog_icon);
        iconImg.setImageResource(imgResId);
        iconImg.setVisibility(View.VISIBLE);
        return this;
    }
}
