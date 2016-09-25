package com.smartfarm.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	static public final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	/**
	 * 使用用户格式提取字符串日期
	 *
	 * @param strDate 日期字符串
	 * @param pattern 日期格式
	 * @return
	 */

	public static Date parse(String strDate, String pattern) {

		if (TextUtil.isEmpty(strDate)) {
			return null;
		}
		try {
			SimpleDateFormat df = new SimpleDateFormat(pattern);
			return df.parse(strDate);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 使用用户格式格式化日期
	 *
	 * @param date    日期
	 * @param pattern 日期格式
	 * @return
	 */

	public static String format(Date date, String pattern) {
		String returnValue = "";
		if (date != null) {
			SimpleDateFormat df = new SimpleDateFormat(pattern);
			returnValue = df.format(date);
		}
		return (returnValue);
	}
}
