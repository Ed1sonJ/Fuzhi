package com.smartfarm.util;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * Http统一管理类
 * 
 * @author Adamearth
 * 
 */
public class HttpUtil {

	/**
	 * 获取网页数据（字符串），Get请求
	 * 
	 * @param httpClient
	 *            http客户端
	 * @param url
	 *            地址
	 * @param header
	 *            头部
	 * @return 返回网页数据 - 字符串形式
	 * @throws IOException
	 */
	public static String httpGet(DefaultHttpClient httpClient, String url,
			String header) throws IOException {
		// 创建Get请求
		HttpGet httpGet = new HttpGet(url);

		// 设置Get请求
		httpGet.setHeader("Referer", header);

		// 设置http客户端
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 10000); // 设置请求超时时间
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				10000); // 读取超时

		// 利用http客户端执行Get请求
		HttpResponse response = httpClient.execute(httpGet);

		// 返回结果
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return EntityUtils.toString(response.getEntity());
		} else {
			return "";
		}

	}

	/**
	 * 获取网页数据（字节），Get请求
	 * 
	 * @param httpClient
	 *            http客户端
	 * @param url
	 *            地址
	 * @param header
	 *            头部
	 * @return 返回网页数据 - 字节形式
	 * @throws IOException
	 */
	public static byte[] httpGetByte(DefaultHttpClient httpClient, String url,
			String header) throws IOException {
		// 创建Get请求
		HttpGet httpGet = new HttpGet(url);

		// 设置Get请求
		httpGet.setHeader("Referer", header);

		// 设置http客户端
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 20000); // 设置请求超时时间
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				20000); // 读取超时

		// 利用http客户端执行Get请求
		HttpResponse response = httpClient.execute(httpGet);

		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return EntityUtils.toByteArray(response.getEntity());
		} else {
			return null;
		}
	}

	/**
	 * 获取网页内容（字符串），Post请求
	 * 
	 * @param httpClient
	 *            http客户端
	 * @param url
	 *            地址
	 * @param pairs
	 *            将参数放如List 请求数据
	 * @param header
	 *            头部
	 * @return 返回网页数据
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String httpPost(DefaultHttpClient httpClient, String url,
			List<BasicNameValuePair> pairs, String header)
			throws ClientProtocolException, IOException {
		Log.d("gzfuzhi", "Start connect " + url);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Referer", header);
		httpPost.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));// 将参数填入POST
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 20000); // 设置请求超时时间
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				20000);
		httpClient.getParams().setParameter(
				CoreConnectionPNames.SOCKET_BUFFER_SIZE, 33554432);
		HttpResponse response = httpClient.execute(httpPost);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return EntityUtils.toString(response.getEntity());
		} else {
			return null;
		}
	}
}
