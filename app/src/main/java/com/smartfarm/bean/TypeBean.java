package com.smartfarm.bean;
//�豸���Ƶ�ѡ���¶ȣ���ǿ�ȵ�
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
