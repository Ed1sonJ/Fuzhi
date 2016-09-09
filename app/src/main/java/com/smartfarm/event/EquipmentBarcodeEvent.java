package com.smartfarm.event;

/**
 * Created by shawn on 2016/1/8.
 */
public class EquipmentBarcodeEvent {
    public String scanResult;
    public String equipmentCode;
    public EquipmentBarcodeEvent(String scanResult,String equipmentCode){
        this.scanResult = scanResult;
        this.equipmentCode = equipmentCode;
    }
}
