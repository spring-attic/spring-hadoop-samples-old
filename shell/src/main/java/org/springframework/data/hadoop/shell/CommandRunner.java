/*
 * Copyright 2002-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.data.hadoop.shell;

import org.springframework.data.hadoop.shell.RuntimeCommands.AdapterAction;


/**
 * @author David Turanski
 *
 */
public class CommandRunner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RuntimeCommands commands = new RuntimeCommands();
		commands.serverRunning();
	    String value = commands.listInputAdapters(); 
	    System.out.println("input adapters:");
	    System.out.println("\t"+value);
	    value = commands.listOutputAdapters(); 
	    System.out.println("output adapters:");
	    System.out.println("\t"+value);
	    value = commands.listComponentsByType("MessageHandler");
	    System.out.println("handlers:");
	    System.out.println("\t"+value);
	    value = commands.listComponentsByType("MessageChannel");
	    System.out.println("channels:");
	    System.out.println("\t"+value);
	    
	    value = commands.controlAdapter("spring.integration:bean=endpoint,name=inputAdapter,type=ManagedEndpoint",AdapterAction.start);
	    System.out.println(value);
	}

}
