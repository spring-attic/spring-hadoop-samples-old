/*
 * Copyright 2002-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.data.hadoop.shell;

import java.util.List;

import javax.management.MalformedObjectNameException;

import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pExecRequest;
import org.jolokia.client.request.J4pExecResponse;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;
import org.jolokia.client.request.J4pSearchRequest;
import org.jolokia.client.request.J4pSearchResponse;
import org.jolokia.client.request.J4pVersionRequest;

/**
 * @author David Turanski
 *
 */
public class MBeanOps {

	private final J4pClient j4pClient;

	public MBeanOps(J4pClient j4pClient) {
		this.j4pClient = j4pClient;
	}

	public List<String> executeSearchRequest(String request) {
		J4pSearchResponse searchResponse = null;
		try {
			J4pSearchRequest searchRequest = new J4pSearchRequest(request);
			searchResponse = j4pClient.execute(searchRequest);
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (J4pException e) {
			e.printStackTrace();			
		}
		return searchResponse == null ? null : searchResponse.getMBeanNames();
	}

	//<base url>/exec/<mbean name>/<operation name>/<arg1>/<arg2>/....
	public String execOperation(String mbeanName, String opName, Object... args) throws MalformedObjectNameException, J4pException {
		J4pExecRequest exec;
		String result = null;
		exec = new J4pExecRequest(mbeanName, opName, args);
		J4pExecResponse execResp = j4pClient.execute(exec);
		if (execResp.getValue() != null) {
			result = execResp.getValue().toString();
		} else {
			result = opName + " executed";
		}
		return result;
	}
	
	public boolean ping() {
		J4pVersionRequest exec;
		boolean alive = false;
		try {

			exec = new J4pVersionRequest();
			j4pClient.execute(exec);
			alive = true;
		} catch (J4pException e) {
		}
		return alive;
	}

	/**
	 * @param mbeanName
	 * @param string
	 * @return
	 */
	public String readAttribute(String mbeanName, String attribute) {
		J4pReadRequest readRequest;
		J4pReadResponse readResponse = null;
		try {
			readRequest = new J4pReadRequest(mbeanName, attribute);
			readResponse = j4pClient.execute(readRequest);
		} catch (MalformedObjectNameException e) {
			return e.getMessage();
		} catch (J4pException e) {
			return e.getMessage();
		}
		
		return readResponse.getValue().toString();
	}
}
