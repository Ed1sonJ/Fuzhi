package com.smartfarm.bean;

import com.github.mikephil.charting.data.LineData;

public class TabBean0_old {

	public String name;
	public LineData lineData;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LineData getLineData() {
		return lineData;
	}

	public void setLineData(LineData lineData) {
		this.lineData = lineData;
	}

	public TabBean0_old(String name, LineData lineData) {
		this.name = name;
		this.lineData = lineData;

	}

	public TabBean0_old() {
		// TODO Auto-generated constructor stub
	}
}
