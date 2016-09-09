package com.smartfarm.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 管理日历、日期、时间工具类
 * 
 * @author Adamearth
 * 
 */
public class CalendarUtil {

	/**
	 * 获得当前系统年、月、日、时、分的字符串
	 * 
	 * @return 当前系统年、月、日、时、分的字符串
	 */
	public static String getCurrentSystemYMDHM() {

		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String date = sDateFormat.format(new Date());

		return date;

	}

	/**
	 * 获得当前系统年、月、日、时、分、秒的字符串
	 * 
	 * @return 当前系统年、月、日、时、分、秒的字符串
	 */
	public static String getCurrentSystemYMDHMS() {

		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String date = sDateFormat.format(new Date());

		return date;

	}

	/**
	 * 获得当前系统年、月、日的字符串
	 * 
	 * @return 当前系统年、月、日的字符串
	 */
	public static String getCurrentSystemYMD() {

		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String date = sDateFormat.format(new Date());

		return date;

	}

	/**
	 * 获得当前系统时、分字符串
	 * 
	 * @return 当前系统时、分的字符串
	 */
	public static String getCurrentSystemHM() {

		SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
		String date = sDateFormat.format(new Date());

		return date;

	}

	/**
	 * 获得当前系统月、日、时、分字符串
	 * 
	 * @return 当前系统月、日、时、分的字符串
	 */
	public static String getCurrentSystemMDHM() {

		SimpleDateFormat sDateFormat = new SimpleDateFormat("MM-dd HH:mm");
		String date = sDateFormat.format(new Date());

		return date;

	}

}
