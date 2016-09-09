package com.smartfarm.util;

import java.util.UUID;

public class MqttClientUtil {
	
	//发布broker的ip和端口  
		public static final String  BROKER = System.getProperty("BROKER", "tcp://broker.gzfuzhi.com:1883");  
		
		//客户端的Id  
		public static final String CLIENT_ID  = String.format("%-23.23s",  System.getProperty("CLIENT_ID", (UUID.randomUUID().toString())).trim()).replace('-', '_');  

		public final static int[] QOS_VALUES = {0, 1, 2};
		
		public final static String USERNAME = "shebei";
		
		public final static String PASSWORD = "888888";

}
