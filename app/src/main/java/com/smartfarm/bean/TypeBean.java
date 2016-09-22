package com.smartfarm.bean;

/**
 * 设备的key与name
 * key为设备对应的lqc等
 * name为设备对应的传感器名称
 */
public class TypeBean {

	public String name;
	public String protocolKey;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProtocolKey() {
		return protocolKey;
	}

	public void setProtocolKey(String protocolKey) {
		this.protocolKey = protocolKey;
	}

	public TypeBean(String name, String protocolKey) {
		this.name = name;
		this.protocolKey = protocolKey;

	}

	public TypeBean() {
	}
}
