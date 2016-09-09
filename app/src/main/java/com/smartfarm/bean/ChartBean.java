package com.smartfarm.bean;

import java.util.List;

//ChartBean是实时监控图标所含根本数据，这些数据是正常返回时JSON数据包所含的数据，
public class ChartBean {

	public String equipmentCode;	//传感器编号
	public String name;		//传感器名字 （传感器名字直接显示给用户看， 统一为.*传感器（number））
	public String type;		//传感器类型  (温度，湿度。。)
	public List<String> data;	//监控数据
	public String unit;		//数据单位
	public List<String> time;	//监控时间
	public List<String> schema;		//监控方案数据
	public List<String> upSchema;	//控制方案上限数据
	public List<String> lowSchema;	//控制方案下限数据
	public List<Integer> isAlarm;	//是否报警，有为1，否未0
	public List<String> alarmMsg;	//报警描述
//	public String title;
	

	public String getEquipmentCode(){
		return equipmentCode;
	}
	public void setEquipmentCode(String equipmentCode){
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

	public void setData(List<String> date) {
		this.data = date;
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

	public List<String> getUpSchema(){
		return upSchema;
	}
	public void setUpSchema(List<String> upSchema) {
		this.upSchema = upSchema;
	}

	public List<String> getLowSchema(){
		return lowSchema;
	}
	public void setLowSchema(List<String> lowSchema) {
		this.lowSchema = lowSchema;
	}
	
	public List<Integer> getIsAlarm(){
		return isAlarm;
	}
	public void setIsAlarm(List<Integer> isAlarm) {
		this.isAlarm = isAlarm;
	}
	public List<String> getAlarmMsg(){
		return alarmMsg;
	}
	public void setAlarmMsg(List<String> alarmMsg) {
		this.alarmMsg = alarmMsg;
	}
//	public String getTitle() {
//		return title;
//	}
//
//	public void setTitle(String title) {
//		this.title = title;
//	}



	public ChartBean(String equipmentCode,String name, String type,List<String> date, 
			String unit,List<String> time, List<String> schema, List<String> upSchema,
			 List<String> lowSchema, List<Integer> isAlarm, List<String> alarmMsg) {
		this.equipmentCode = equipmentCode;
		this.data = date;
		this.name = name;
		this.type = type;
		this.unit = unit;
		this.schema = schema;
		this.time = time;
		this.upSchema = upSchema;
		this.lowSchema = lowSchema;
		this.isAlarm = isAlarm;
		this.alarmMsg = alarmMsg;
//		this.title = title;
	}

	public ChartBean() {
	}

}
