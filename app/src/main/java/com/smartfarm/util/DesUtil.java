package com.smartfarm.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * DES���ܹ�����
 * 
 * @author Adamearth
 * 
 */
public class DesUtil {

	/**
	 * ����
	 * 
	 * @param data
	 *            ���ݣ��ֽ����飩
	 * @param key
	 *            ��Կ
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, String key) throws Exception {
		// ����һ�������ε������Դ
		SecureRandom random = new SecureRandom();

		// ��ԭʼ��Կ���ݴ���DESKeySpec����
		DESKeySpec desKey = new DESKeySpec(key.getBytes());

		// ����һ���ܳ׹���
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

		// ��DESKeySpec����ת����SecretKey����
		SecretKey securekey = keyFactory.generateSecret(desKey);

		// Cipher����ʵ����ɼ��ܲ���
		Cipher cipher = Cipher.getInstance("DES");

		// ���ܳ׳�ʼ��Cipher����
		cipher.init(Cipher.ENCRYPT_MODE, securekey, random);

		// ִ�м��ܲ���
		return cipher.doFinal(data);

	}

	/**
	 * ����
	 * 
	 * @param data
	 *            ���ݣ��ֽ����飩
	 * @param key
	 *            ��Կ
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] data, String key) throws Exception {
		// DES�㷨Ҫ����һ�������ε������Դ
		SecureRandom random = new SecureRandom();

		// ����һ��DESKeySpec����
		DESKeySpec desKey = new DESKeySpec(key.getBytes());

		// ����һ���ܳ׹���
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

		// ��DESKeySpec����ת����SecretKey����
		SecretKey securekey = keyFactory.generateSecret(desKey);

		// Cipher����ʵ����ɽ��ܲ���
		Cipher cipher = Cipher.getInstance("DES");

		// ���ܳ׳�ʼ��Cipher����
		cipher.init(Cipher.DECRYPT_MODE, securekey, random);

		// ִ�н��ܲ���
		return cipher.doFinal(data);
	}
}
