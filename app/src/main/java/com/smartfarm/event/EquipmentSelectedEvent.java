package com.smartfarm.event;

/**
 * Created by june on 2016/1/4.
 */
public class EquipmentSelectedEvent {
    public String equipmentCode;
    public String equipmentGroup;
    public EquipmentSelectedEvent(String code) {
        equipmentCode = code;
    }
    public EquipmentSelectedEvent(String code,String group) {
        equipmentCode = code;
        equipmentGroup=group;
    }
}
