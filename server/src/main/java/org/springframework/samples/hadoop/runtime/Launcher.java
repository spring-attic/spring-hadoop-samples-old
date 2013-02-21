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
package org.springframework.samples.hadoop.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

public class Launcher {

	private static final Log log = LogFactory.getLog(Launcher.class);

	public static void main(String[] args) throws Exception {
		Assert.isTrue(args.length > 0, "No parameter value for runtime to run passed in.");
		Assert.hasText(args[0], "Parameter value for runtime to run can't be empty.");
		AbstractApplicationContext context = null;
		String contextFile = "/" + args[0] + "/application-context.xml";
		try {
			context = new ClassPathXmlApplicationContext(contextFile);
			log.info("Running " + args[0] + "sample ...");
		} catch (Exception e) {
			log.info("Unable to load " + contextFile);
			e.printStackTrace();
		}
		context.registerShutdownHook();
	}
}
