package com.smartfarm.event;

import android.view.View;

/**
 * Created by june_qijun on 16/1/5.
 */
public class EquipmentGroupLongClickedEvent {
    public String groupName;
    public View view;

    public EquipmentGroupLongClickedEvent(String name, View v) {
        groupName = name;
        view = v;
    }
}
