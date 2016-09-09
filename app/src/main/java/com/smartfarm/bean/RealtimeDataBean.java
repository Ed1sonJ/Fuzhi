package com.smartfarm.bean;

import java.util.List;

public class RealtimeDataBean {
	private String equipmentCode;
	private String name;
	private String type;
	private List<String> data;
	private String unit;
	private List<String> time;
	private List<String> schema;
	private List<String> upSchema;
	private List<String> lowSchema;
	private List<Integer> isAlarm;
	private List<String> alarmMsg;

	public String getEquipmentCode() {
		return equipmentCode;
	}

	public void setEquipmentCode(String equipmentCode) {
		this.equipmentCode = equipmentCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = data;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public List<String> getTime() {
		return time;
	}

	public void setTime(List<String> time) {
		this.time = time;
	}

	public List<String> getSchema() {
		return schema;
	}

	public void setSchema(List<String> schema) {
		this.schema = schema;
	}

	public List<String> getUpSchema() {
		return upSchema;
	}

	public void setUpSchema(List<String> upSchema) {
		this.upSchema = upSchema;
	}

	public List<String> getLowSchema() {
		return lowSchema;
	}

	public void setLowSchema(List<String> lowSchema) {
		this.lowSchema = lowSchema;
	}

	public List<Integer> getIsAlarm() {
		return isAlarm;
	}

	public void setIsAlarm(List<Integer> isAlarm) {
		this.isAlarm = isAlarm;
	}

	public List<String> getAlarmMsg() {
		return alarmMsg;
	}

	public void setAlarmMsg(List<String> alarmMsg) {
		this.alarmMsg = alarmMsg;
	}
}
