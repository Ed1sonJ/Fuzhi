package com.smartfarm.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.smartfarm.activity.R;
import com.smartfarm.event.EquipmentItemConfigureClickedEvent;
import com.smartfarm.event.EquipmentItemGpsClickedEvent;
import com.smartfarm.event.EquipmentSelectedEvent;
import com.smartfarm.event.GlobalEvent;
import com.smartfarm.model.Equipment;
import com.smartfarm.model.Group;

/**
 * Created by june on 2016/1/3.
 */
public class EquipmentListAdapter extends BaseExpandableListAdapter {
    /**children最大噶数目*/
    public static final int MAX_NUM_CHILDREN = 1000;

    protected Activity activity;
    protected Group group;

    public EquipmentListAdapter(Activity act, Group g) {
        activity = act;
        group = g;
    }

    public void setList(Group list) {
        group = list;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return group.getGroupNames().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return group.getChildBeans().get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group.getGroupNames().get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return group.getChildBeans().get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition * MAX_NUM_CHILDREN + childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.fragment_equipment_list_group, parent, false);
        }
        TextView title = (TextView) convertView.findViewById(R.id.group_title);
        title.setText(group.getGroupNames().get(groupPosition));
        //bundle相当于map,主要用于activity之间的传送
        Bundle bundle = new Bundle();
        bundle.putString("Type", "Group");
        bundle.putString("Name", group.getGroupNames().get(groupPosition));
        convertView.setTag(bundle);
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.fragment_equipment_list_child, parent, false);
        }
        final String equipmentCode = group.getChildBeans().get(groupPosition).get(childPosition);
        //SimpleDraweeView，Fresco 用来加载图片
        SimpleDraweeView imageView = (SimpleDraweeView)convertView.findViewById(R.id.equipment_item_user_picture);
        Equipment.loadEquipmentImage(activity,equipmentCode,imageView,55);
        TextView title = (TextView) convertView.findViewById(R.id.child_title);
        title.setText(Equipment.getEquipmentName(activity, equipmentCode));
        TextView equipmentCodeText = (TextView) convertView.findViewById(R.id.child_equipment_code);
        equipmentCodeText.setText(equipmentCode);
        //设置
        final ImageButton configBtn = (ImageButton) convertView.findViewById(R.id.equipment_item_config_btn);
        configBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalEvent.bus.post(new EquipmentItemConfigureClickedEvent(equipmentCode, configBtn));
            }
        });
        //gps
        convertView.findViewById(R.id.equipment_item_gps_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalEvent.bus.post(new EquipmentItemGpsClickedEvent(equipmentCode));
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalEvent.bus.post(new EquipmentSelectedEvent(equipmentCode,group.getGroupNames().get(groupPosition)));
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
