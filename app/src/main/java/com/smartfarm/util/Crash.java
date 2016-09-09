package com.smartfarm.util;

public class Crash {
	// 测试崩溃报告使用。
	public static void crashTheApp() {
		int[] a = null;
		a[0] = 1;
	}
}
