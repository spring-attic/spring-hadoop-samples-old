/*
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hadoop.admin.cli.commands;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.commands.OsCommands;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Base class for REST call commands.
 * 
 * @author Jarred Li
 *
 */
public class BaseCommand {

	private String commandURL = "jobs.json";

	public static String errorConnection = "I/O error: Connection refused";

	public static String messageConnection = "Make sure you have set correct service URL, type \"info\" to check service URL."
			+ " Or check if the service is working";

	private static final Logger LOGGER = HandlerUtils.getLogger(OsCommands.class);
	/**
	 * call rest service with "Get" 
	 */
	public String callGetService() {
		try {
			String json = getJson();
			return json;
		} catch (Throwable t) {
			showErrorMsg(t);
			return null;
		}
	}


	/**
	 * get JSON from server.
	 * 
	 * @return JSON String
	 */
	public String getJson() {
		RestTemplate template = getRestTemplate();
		String json = template.getForObject(generateCommandUrl(), String.class);
		return json;
	}


	/**
	 * @param t
	 */
	private void showErrorMsg(Throwable t) {
		if (t.getMessage().contains(errorConnection)) {
			LOGGER.warning("call service failed." + messageConnection);
		}
		else {
			LOGGER.warning("call service failed. Reason:" + t.getMessage());
		}
	}


	/**
	 * call rest service with "Post" 
	 * @param <T>
	 */
	public <T> void callPostService(T object) {
		try {
			RestTemplate template = getRestTemplate();
			//		String message = template.postForObject(getCommandUrl(), object, String.class);
			HttpEntity<T> entity = new HttpEntity<T>(object);
			ResponseEntity<String> response = template.exchange(generateCommandUrl(), HttpMethod.POST, entity, String.class);
			String message = response.getBody();
			if (message != null) {
				LOGGER.info(message);
			}
		} catch (Throwable t) {
			showErrorMsg(t);
		}
	}


	/**
	 * call rest service with "Delete" 
	 * @param object
	 */
	public <T> void callDeleteService(T object) {
		try {
			RestTemplate template = getRestTemplate();
			//template.delete(getCommandUrl());
			HttpEntity<T> entity = new HttpEntity<T>(object);
			ResponseEntity<String> response = template.exchange(generateCommandUrl(), HttpMethod.DELETE, entity,
					String.class);
			String message = response.getBody();
			if (message != null) {
				LOGGER.info(message);
			}
		} catch (Throwable t) {
			showErrorMsg(t);
		}
	}
	
	/**
	 * Download file from server.
	 */
	public void callDownloadFile(String fileName) {
		try {
			RestTemplate template = getRestTemplate();
			byte[] bytes = template.getForObject(generateCommandUrl(), byte[].class);
			FileCopyUtils.copy(bytes, new File(fileName));
			LOGGER.info("download file successfully. file name is:" + fileName);
		} catch (Throwable t) {
			showErrorMsg(t);
		}
	}

	/**
	 * get RestTempate from xml Beans.
	 * 
	 * @return
	 */
	private RestTemplate getRestTemplate() {
		ApplicationContext context = new ClassPathXmlApplicationContext("rest-context.xml");
		RestTemplate template = context.getBean("restTemplate", RestTemplate.class);
		return template;
	}

	/**
	 * get command URL
	 * 
	 * @return
	 */
	private String generateCommandUrl() {
		try {
			String serviceUrl = PropertyUtil.getTargetUrl();
			if (serviceUrl == null || serviceUrl.length() == 0) {
				LOGGER.warning("you must set Spring Hadoop Admin service URL first by running target command");
			}
			if (!serviceUrl.endsWith("/")) {
				serviceUrl += "/";
			}
			serviceUrl += getCommandURL();
			return serviceUrl;
		} catch (Exception e) {
			LOGGER.warning("get service url failed. " + e.getMessage());
		}
		return null;
	}

	/**
	 * @return the commandURL
	 */
	public String getCommandURL() {
		return commandURL;
	}

	/**
	 * @param commandURL the commandURL to set
	 */
	public void setCommandURL(String commandURL) {
		this.commandURL = commandURL;
	}

	/**
	 * judge whether service URL is set 
	 * 
	 * @return true - if service URL is set
	 * 		   false - otherwise
	 */
	protected boolean isServiceUrlSet() {
		boolean result = true;
		try {
			String serviceUrl = PropertyUtil.getTargetUrl();
			if (serviceUrl == null || serviceUrl.length() == 0) {
				result = false;
			}
		} catch (ConfigurationException e) {
			LOGGER.warning("read properties failed");
			result = false;
		}
		return result;
	}


}
