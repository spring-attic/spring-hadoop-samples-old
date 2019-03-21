/*
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hadoop.admin.cli.commands;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class to configure properties used to store service URL.
 * 
 * @author Jarred Li
 *
 */
public class PropertyUtil {
	
	private static final Log logger = LogFactory.getLog(PropertyUtil.class);

	public static String adminPropertyFileName = "spring-hadoop-admin.properties";
	
	static{
		File f = new File(adminPropertyFileName);
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				logger.error("create property file failed", e);
			}
		}
	}

	/**
	 * set Spring Hadoop Admin service URL
	 * 
	 * @param targetUrl service URL
	 * @throws ConfigurationException
	 */
	public static void setTargetUrl(String targetUrl) throws ConfigurationException {
		PropertiesConfiguration config = new PropertiesConfiguration(adminPropertyFileName);
		config.setProperty("targetUrl", targetUrl);
		config.save();
	}

	/**
	 * get Spring Hadoop Admin service URL
	 * @return service URL
	 * @throws ConfigurationException
	 */
	public static String getTargetUrl() throws ConfigurationException {
		String result = null;
		PropertiesConfiguration config = new PropertiesConfiguration(adminPropertyFileName);
		result = config.getString("targetUrl");
		return result;
	}
	
	/**
	 * set HDFS URL
	 * 
	 * @param dfsName HDFS URL
	 * 
	 * @throws ConfigurationException
	 */
	public static void setDfsName(String dfsName) throws ConfigurationException {
		PropertiesConfiguration config = new PropertiesConfiguration(adminPropertyFileName);
		config.setProperty("dfs.default.name", dfsName);
		config.save();
	}

	/**
	 * get HDFS URL
	 * @return HDFS URL
	 * @throws ConfigurationException
	 */
	public static String getDfsName() throws ConfigurationException {
		String result = null;
		PropertiesConfiguration config = new PropertiesConfiguration(adminPropertyFileName);
		result = config.getString("dfs.default.name");
		return result;
	}
	
}
