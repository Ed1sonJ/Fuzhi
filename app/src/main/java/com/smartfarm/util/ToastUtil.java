package com.smartfarm.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Toastͳһ��������
 * 
 * @author Adamearth
 * 
 */
public class ToastUtil {

	private static Toast toast;

	/**
	 * ��ʱ����ʾToast
	 * 
	 * @param context
	 * @param message
	 */
	public static void showShort(Context context, CharSequence message) {
		if (null == toast) {
			toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
		} else {
			toast.setText(message);
		}

		toast.show();
	}

	/**
	 * ��ʱ����ʾToast
	 * 
	 * @param context
	 * @param message
	 */
	public static void showShort(Context context, int message) {
		if (null == toast) {
			toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
		} else {
			toast.setText(message);
		}

		toast.show();
	}

	/**
	 * ��ʱ����ʾToast
	 * 
	 * @param context
	 * @param message
	 */
	public static void showLong(Context context, CharSequence message) {
		if (null == toast) {
			toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG);
		} else {
			toast.setText(message);
		}

		toast.show();
	}

	/**
	 * ��ʱ����ʾToast
	 * 
	 * @param context
	 * @param message
	 */
	public static void showLong(Context context, int message) {
		if (null == toast) {
			toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG);

		} else {
			toast.setText(message);
		}

		toast.show();
	}

	/**
	 * �Զ�����ʾToastʱ��
	 * 
	 * @param context
	 * @param message
	 * @param duration
	 */
	public static void show(Context context, CharSequence message, int duration) {
		if (null == toast) {
			toast = Toast.makeText(context.getApplicationContext(), message, duration);
		} else {
			toast.setText(message);
		}

		toast.show();
	}

	/**
	 * �Զ�����ʾToastʱ��
	 * 
	 * @param context
	 * @param message
	 * @param duration
	 */
	public static void show(Context context, int message, int duration) {
		if (null == toast) {
			toast = Toast.makeText(context.getApplicationContext(), message, duration);
		} else {
			toast.setText(message);
		}
		toast.show();
	}

	/**
	 * �ֶ�����Toast
	 * 
	 */
	public static void hideToast() {
		if (null != toast) {
			toast.cancel();
		}
	}
}
