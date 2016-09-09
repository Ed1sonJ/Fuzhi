package com.smartfarm.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartfarm.activity.R;
import com.smartfarm.bean.EquipmentBean;
import com.smartfarm.event.EquipmentSelectedEvent;
import com.smartfarm.event.GlobalEvent;
import com.smartfarm.model.Equipment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by june on 2016/1/5.
 */
public class SearchListAdapter extends BaseAdapter {
    protected Activity activity;
    protected List<EquipmentBean> equipmentBeanList = new ArrayList<>();

    public SearchListAdapter(Activity act) {
        activity = act;
    }

    public void setEquipmentBeanList(List<EquipmentBean> list) {
        equipmentBeanList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return equipmentBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    //当显示的item的position序列发生变化的时候，getView（）就会被调用。
    //当ListView滑动的过程中 会有item被滑出屏幕 而不再被使用
    // 这时候Android会回收这个条目的view 这个view也就是这里的convertView
    //而系统回收后，会保留一个对象，而convertView就是指向这个对象的
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.equipment_search_list_item, parent, false);
        }
        TextView titleView = (TextView) convertView.findViewById(R.id.search_title);
        titleView.setText(Equipment.getEquipmentName(activity, equipmentBeanList.get(position).code));
        TextView codeView = (TextView) convertView.findViewById(R.id.search_equipment_code);
        codeView.setText(equipmentBeanList.get(position).code);
        //用eventBus发送消息,新建一个class发消息，EquipmentSelectedEvent
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalEvent.bus.post(new EquipmentSelectedEvent(equipmentBeanList.get(position).getCode()));
            }
        });
        return convertView;
    }


}
