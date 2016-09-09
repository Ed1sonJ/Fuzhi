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
    //����ʾ��item��position���з����仯��ʱ��getView�����ͻᱻ���á�
    //��ListView�����Ĺ����� ����item��������Ļ �����ٱ�ʹ��
    // ��ʱ��Android����������Ŀ��view ���viewҲ���������convertView
    //��ϵͳ���պ󣬻ᱣ��һ�����󣬶�convertView����ָ����������
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.equipment_search_list_item, parent, false);
        }
        TextView titleView = (TextView) convertView.findViewById(R.id.search_title);
        titleView.setText(Equipment.getEquipmentName(activity, equipmentBeanList.get(position).code));
        TextView codeView = (TextView) convertView.findViewById(R.id.search_equipment_code);
        codeView.setText(equipmentBeanList.get(position).code);
        //��eventBus������Ϣ,�½�һ��class����Ϣ��EquipmentSelectedEvent
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalEvent.bus.post(new EquipmentSelectedEvent(equipmentBeanList.get(position).getCode()));
            }
        });
        return convertView;
    }


}
