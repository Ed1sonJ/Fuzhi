package com.smartfarm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;

import com.smartfarm.activity.R;

import java.util.ArrayList;

/**
 * Created by hp on 2016/9/10.
 */
public class DialogTimeListAdapter extends BaseAdapter {

    private String[] mDatas;
    private LayoutInflater mInflater;
    private ArrayList<ImageView> imgs = new ArrayList<>();

    public DialogTimeListAdapter(Context context , String[] datas){
        this.mDatas = datas;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDatas.length;
    }

    @Override
    public Object getItem(int position) {
        return mDatas[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.dialog_water_choose_time_listview_item,null);
            viewHolder = new ViewHolder();
            viewHolder.titleTv = (TextView) convertView.findViewById(R.id.id_water_choose_time_list_tv);
            viewHolder.iconImg = (ImageView) convertView.findViewById(R.id.id_water_choose_time_list_img);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.titleTv.setText(mDatas[position]);
        //将图片保存在全局变量中，方便点击item时更改drawable
        imgs.add(viewHolder.iconImg);

        return convertView;
    }

    /**
     * 重置所有图片到no_select的情况
     */
    public void removeAllViews(){
        for (int i = 0; i < imgs.size(); i++) {
            imgs.get(i).setImageResource(R.drawable.dialog_img_no_select);
        }
    }

    class ViewHolder {
        TextView titleTv;
        ImageView iconImg;
    }
}
