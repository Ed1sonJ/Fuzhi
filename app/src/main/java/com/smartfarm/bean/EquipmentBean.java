package com.smartfarm.bean;

public class EquipmentBean {
	public String code;
	public String cropName;
	public String name;
	public String progress;
	public String schemaCode;
	public String schemaName;
	public String stratTime;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCropName() {
		return cropName;
	}

	public void setCropName(String cropName) {
		this.cropName = cropName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public String getSchemaCode() {
		return schemaCode;
	}

	public void setSchemaCode(String schemaCode) {
		this.schemaCode = schemaCode;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getStratTime() {
		return stratTime;
	}

	public void setStratTime(String stratTime) {
		this.stratTime = stratTime;
	}

	public EquipmentBean(String name, String cropName, String code,
			String schemaName, String schemaCode, String progress,
			String stratTime) {
		this.name = name;
		this.cropName = cropName;
		this.code = code;
		this.schemaName = schemaName;
		this.schemaCode = schemaCode;
		this.progress = progress;
		this.stratTime = stratTime;

	}

	public EquipmentBean() {
	}
}
