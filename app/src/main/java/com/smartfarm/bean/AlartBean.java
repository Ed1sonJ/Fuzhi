package com.smartfarm.bean;

public class AlartBean {
	public String alartTime;
	public String content;
	public String hostEquipments;
	public Integer alartLevel;
	public String title;

	public String getAlartTime() {
		return alartTime;
	}

	public void setAlartTime(String alartTime) {
		this.alartTime = alartTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getHostEquipments() {
		return hostEquipments;
	}

	public void setHostEquipments(String hostEquipments) {
		this.hostEquipments = hostEquipments;
	}

	public Integer getLevel() {
		return alartLevel;
	}

	public void setLevel(Integer level) {
		this.alartLevel = level;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public AlartBean(String alartTime, String content, String hostEquipments,
			Integer level, String title) {
		this.alartTime = alartTime;
		this.content = content;
		this.hostEquipments = hostEquipments;
		this.alartLevel = level;
		this.title = title;

	}

	public AlartBean() {
	}
}
