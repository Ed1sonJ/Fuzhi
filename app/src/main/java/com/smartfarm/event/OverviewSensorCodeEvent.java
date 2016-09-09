package com.smartfarm.event;

import java.util.ArrayList;

/**
 * Created by shawn on 2016/1/8.
 */
public class OverviewSensorCodeEvent {
    public ArrayList<String> sensorName;
    public String equipmentCode;
    public ArrayList<String> sensorCode;
    public ArrayList<String> newTime;
    public OverviewSensorCodeEvent(ArrayList<String> sensorName, String equipmentCode,ArrayList<String> sensorCode,ArrayList<String> newTime){
        this.sensorName = sensorName;
        this.equipmentCode = equipmentCode;
        this.sensorCode = sensorCode;
        this.newTime = newTime;
    }
}
