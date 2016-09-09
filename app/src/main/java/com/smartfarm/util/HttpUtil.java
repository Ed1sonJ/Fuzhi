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
 * Httpͳһ������
 * 
 * @author Adamearth
 * 
 */
public class HttpUtil {

	/**
	 * ��ȡ��ҳ���ݣ��ַ�������Get����
	 * 
	 * @param httpClient
	 *            http�ͻ���
	 * @param url
	 *            ��ַ
	 * @param header
	 *            ͷ��
	 * @return ������ҳ���� - �ַ�����ʽ
	 * @throws IOException
	 */
	public static String httpGet(DefaultHttpClient httpClient, String url,
			String header) throws IOException {
		// ����Get����
		HttpGet httpGet = new HttpGet(url);

		// ����Get����
		httpGet.setHeader("Referer", header);

		// ����http�ͻ���
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 10000); // ��������ʱʱ��
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				10000); // ��ȡ��ʱ

		// ����http�ͻ���ִ��Get����
		HttpResponse response = httpClient.execute(httpGet);

		// ���ؽ��
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return EntityUtils.toString(response.getEntity());
		} else {
			return "";
		}

	}

	/**
	 * ��ȡ��ҳ���ݣ��ֽڣ���Get����
	 * 
	 * @param httpClient
	 *            http�ͻ���
	 * @param url
	 *            ��ַ
	 * @param header
	 *            ͷ��
	 * @return ������ҳ���� - �ֽ���ʽ
	 * @throws IOException
	 */
	public static byte[] httpGetByte(DefaultHttpClient httpClient, String url,
			String header) throws IOException {
		// ����Get����
		HttpGet httpGet = new HttpGet(url);

		// ����Get����
		httpGet.setHeader("Referer", header);

		// ����http�ͻ���
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 20000); // ��������ʱʱ��
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				20000); // ��ȡ��ʱ

		// ����http�ͻ���ִ��Get����
		HttpResponse response = httpClient.execute(httpGet);

		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			return EntityUtils.toByteArray(response.getEntity());
		} else {
			return null;
		}
	}

	/**
	 * ��ȡ��ҳ���ݣ��ַ�������Post����
	 * 
	 * @param httpClient
	 *            http�ͻ���
	 * @param url
	 *            ��ַ
	 * @param pairs
	 *            ����������List ��������
	 * @param header
	 *            ͷ��
	 * @return ������ҳ����
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String httpPost(DefaultHttpClient httpClient, String url,
			List<BasicNameValuePair> pairs, String header)
			throws ClientProtocolException, IOException {
		Log.d("gzfuzhi", "Start connect " + url);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Referer", header);
		httpPost.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));// ����������POST
		httpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 20000); // ��������ʱʱ��
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
