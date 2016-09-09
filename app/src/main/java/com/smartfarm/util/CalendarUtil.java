package com.smartfarm.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * �������������ڡ�ʱ�乤����
 * 
 * @author Adamearth
 * 
 */
public class CalendarUtil {

	/**
	 * ��õ�ǰϵͳ�ꡢ�¡��ա�ʱ���ֵ��ַ���
	 * 
	 * @return ��ǰϵͳ�ꡢ�¡��ա�ʱ���ֵ��ַ���
	 */
	public static String getCurrentSystemYMDHM() {

		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String date = sDateFormat.format(new Date());

		return date;

	}

	/**
	 * ��õ�ǰϵͳ�ꡢ�¡��ա�ʱ���֡�����ַ���
	 * 
	 * @return ��ǰϵͳ�ꡢ�¡��ա�ʱ���֡�����ַ���
	 */
	public static String getCurrentSystemYMDHMS() {

		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String date = sDateFormat.format(new Date());

		return date;

	}

	/**
	 * ��õ�ǰϵͳ�ꡢ�¡��յ��ַ���
	 * 
	 * @return ��ǰϵͳ�ꡢ�¡��յ��ַ���
	 */
	public static String getCurrentSystemYMD() {

		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String date = sDateFormat.format(new Date());

		return date;

	}

	/**
	 * ��õ�ǰϵͳʱ�����ַ���
	 * 
	 * @return ��ǰϵͳʱ���ֵ��ַ���
	 */
	public static String getCurrentSystemHM() {

		SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm");
		String date = sDateFormat.format(new Date());

		return date;

	}

	/**
	 * ��õ�ǰϵͳ�¡��ա�ʱ�����ַ���
	 * 
	 * @return ��ǰϵͳ�¡��ա�ʱ���ֵ��ַ���
	 */
	public static String getCurrentSystemMDHM() {

		SimpleDateFormat sDateFormat = new SimpleDateFormat("MM-dd HH:mm");
		String date = sDateFormat.format(new Date());

		return date;

	}

}
