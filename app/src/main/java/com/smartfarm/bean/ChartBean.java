package com.smartfarm.bean;

import java.util.List;

//ChartBean��ʵʱ���ͼ�������������ݣ���Щ��������������ʱJSON���ݰ����������ݣ�
public class ChartBean {

	public String equipmentCode;	//���������
	public String name;		//���������� ������������ֱ����ʾ���û����� ͳһΪ.*��������number����
	public String type;		//����������  (�¶ȣ�ʪ�ȡ���)
	public List<String> data;	//�������
	public String unit;		//���ݵ�λ
	public List<String> time;	//���ʱ��
	public List<String> schema;		//��ط�������
	public List<String> upSchema;	//���Ʒ�����������
	public List<String> lowSchema;	//���Ʒ�����������
	public List<Integer> isAlarm;	//�Ƿ񱨾�����Ϊ1����δ0
	public List<String> alarmMsg;	//��������
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
