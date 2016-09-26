package com.smartfarm.adapter;

/**
 * Created by hp on 2016/9/23.
 */

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import com.smartfarm.activity.R;

public class SchemeLightQualityAdapter extends BaseAdapter {
    /**
     * 包含了自定义
     */
    private String[] mItemTexts;
    private LayoutInflater mInflater;

    public SchemeLightQualityAdapter(Activity activity, String[] itemTexts) {
        super();
        this.mItemTexts = itemTexts;
        mInflater = LayoutInflater.from(activity);
    }

    @Override
    public int getCount() {
        return mItemTexts.length;
    }

    @Override
    public Object getItem(int position) {
        return mItemTexts[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.fragment_scheme_new_parameter_lqc_item2, null);
            viewHolder.tvItemText = (TextView) convertView.findViewById(R.id.lqc_item_textview);
            viewHolder.lyItemDes = (LinearLayout) convertView.findViewById(R.id.lqc_item_des);
            viewHolder.imgSetting = (ImageView) convertView.findViewById(R.id.lqc_item_iamgeview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvItemText.setText(mItemTexts[position]);

        if(position == mItemTexts.length-1){
            viewHolder.lyItemDes.setVisibility(View.GONE);
            viewHolder.imgSetting.setVisibility(View.VISIBLE);
        }else {
            viewHolder.lyItemDes.setVisibility(View.VISIBLE);
            viewHolder.imgSetting.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder {
        //几比几
        TextView tvItemText;
        //LED
        LinearLayout lyItemDes;
        //图片
        ImageView imgSetting;
    }
}
