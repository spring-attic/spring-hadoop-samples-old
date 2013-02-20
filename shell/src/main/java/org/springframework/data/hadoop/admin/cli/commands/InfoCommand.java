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

import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.springframework.shell.commands.OsCommands;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;

/**
 * Commands to show meta information
 * 
 * @author Jarred Li
 *
 */
@Component
public class InfoCommand implements CommandMarker {

	private static final Logger LOGGER = HandlerUtils.getLogger(OsCommands.class);
	/**
	 * show the current service URL
	 * 
	 */
	@CliCommand(value = "info", help = "list Spring Hadoop Admin CLI information")
	public void getCLIInfo() {
		try {
			String serviceUrl = PropertyUtil.getTargetUrl();
			LOGGER.info("    service url:    " + serviceUrl);			
			String dfsName = PropertyUtil.getDfsName();
			LOGGER.info("    fs default name:    " + dfsName);
		} catch (ConfigurationException e) {
			LOGGER.info("set target url failed. " + e.getMessage());
		}
	}

}
