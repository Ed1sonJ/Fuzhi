package com.smartfarm.event;

import android.view.View;

/**
 * Created by june on 2016/1/4.
 */
public class EquipmentItemConfigureClickedEvent {
    public String equipmentCode;
    public View view;

    public EquipmentItemConfigureClickedEvent(String code, View v) {
        equipmentCode = code;
        view = v;
    }
}
