package com.smartfarm.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	static public final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	/**
	 * ʹ���û���ʽ��ȡ�ַ�������
	 *
	 * @param strDate �����ַ���
	 * @param pattern ���ڸ�ʽ
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
	 * ʹ���û���ʽ��ʽ������
	 *
	 * @param date    ����
	 * @param pattern ���ڸ�ʽ
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
