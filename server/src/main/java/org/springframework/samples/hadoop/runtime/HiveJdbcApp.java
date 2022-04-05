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
package org.springframework.samples.hadoop.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.hadoop.hive.HiveTemplate;
import org.springframework.samples.hadoop.hive.JdbcPasswordRepository;

public class HiveJdbcApp {

	private static final Log log = LogFactory.getLog(HiveJdbcApp.class);

	public static void main(String[] args) throws Exception {
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"/hive/hive-context.xml");
		log.info("Hive Application Running");
		context.registerShutdownHook();	
		
		HiveTemplate template = context.getBean(HiveTemplate.class);
		template.query("show tables;");


		JdbcPasswordRepository repo = context.getBean(JdbcPasswordRepository.class);
		repo.processPasswordFile("hive/password-analysis.hql");
		log.info("Count of password entrires = " + repo.count());
	

	}
}
