package com.allstar.nmsc.scylla.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * @author King.Gao
 *
 */
public class ScyllaConfig {

	public static Map<String, String> scyllaConfigProperties;
	private static Object _LOCK = new Object();

	private static void initialize() {
		if (null == scyllaConfigProperties || scyllaConfigProperties.isEmpty()) {
			synchronized (_LOCK) {
				if (null == scyllaConfigProperties || scyllaConfigProperties.isEmpty()) {
					Properties p = new Properties();
					InputStream in;
					try {
						in = new FileInputStream("scylla_config.properties");
						p.load(in);
					} catch (Exception e) {
						e.printStackTrace();
					}

					scyllaConfigProperties = new HashMap<String, String>();
					for (String key : p.stringPropertyNames()) {
						scyllaConfigProperties.put(key, p.getProperty(key));
					}
				}
			}
		}
	}

	public static Map<String, String> getScyllaConfig() {
		if (null == scyllaConfigProperties || scyllaConfigProperties.isEmpty()) {
			initialize();
		}

		return scyllaConfigProperties;
	}
}
