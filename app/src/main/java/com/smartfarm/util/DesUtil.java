package com.smartfarm.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * DES加密工具类
 * 
 * @author Adamearth
 * 
 */
public class DesUtil {

	/**
	 * 加密
	 * 
	 * @param data
	 *            数据（字节数组）
	 * @param key
	 *            密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, String key) throws Exception {
		// 生成一个可信任的随机数源
		SecureRandom random = new SecureRandom();

		// 从原始密钥数据创建DESKeySpec对象
		DESKeySpec desKey = new DESKeySpec(key.getBytes());

		// 创建一个密匙工厂
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

		// 将DESKeySpec对象转换成SecretKey对象
		SecretKey securekey = keyFactory.generateSecret(desKey);

		// Cipher对象实际完成加密操作
		Cipher cipher = Cipher.getInstance("DES");

		// 用密匙初始化Cipher对象
		cipher.init(Cipher.ENCRYPT_MODE, securekey, random);

		// 执行加密操作
		return cipher.doFinal(data);

	}

	/**
	 * 解密
	 * 
	 * @param data
	 *            数据（字节数组）
	 * @param key
	 *            密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] data, String key) throws Exception {
		// DES算法要求有一个可信任的随机数源
		SecureRandom random = new SecureRandom();

		// 创建一个DESKeySpec对象
		DESKeySpec desKey = new DESKeySpec(key.getBytes());

		// 创建一个密匙工厂
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

		// 将DESKeySpec对象转换成SecretKey对象
		SecretKey securekey = keyFactory.generateSecret(desKey);

		// Cipher对象实际完成解密操作
		Cipher cipher = Cipher.getInstance("DES");

		// 用密匙初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, securekey, random);

		// 执行解密操作
		return cipher.doFinal(data);
	}
}
