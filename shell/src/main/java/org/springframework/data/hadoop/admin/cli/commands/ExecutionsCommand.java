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

import java.util.Date;

import org.springframework.data.hadoop.admin.cli.commands.BaseCommand;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Operations for "Executions"
 *  
 * @author Jarred Li
 * @since 1.0
 */
@Component
public class ExecutionsCommand extends BaseCommand implements CommandMarker {

	/**
	 * get all executions
	 * 
	 */
	@CliCommand(value = "execution list", help = "get all job executions, in order of most recent to least")
	public void getExecutions() {
		setCommandURL("jobs/executions.json");
		callGetService();
	}

	/**
	 * get the job executions for the specified jobName
	 * 
	 * @param jobName The job name 
	 */
	@CliCommand(value = "execution byName", help = "List the JobExecutions for the job name provided")
	public void getJobExecutions(@CliOption(key = { "jobName" }, mandatory = true, help = "Job Name") final String jobName) {
		String url = "jobs/";
		url += jobName;
		url += "/executions.json";
		setCommandURL(url);
		callGetService();
	}

	/**
	 * get the job executions for the specified jobInstance
	 * 
	 * @param jobName The job name
	 * @param jobInstanceId The job instance Id
	 */
	@CliCommand(value = "execution byInstanceId", help = "List the JobExecutions for the job instance with the id provided")
	public void getJobInstanceExecutions(@CliOption(key = { "jobName" }, mandatory = true, help = "Job Name") final String jobName, @CliOption(key = { "jobInstanceId" }, mandatory = true, help = "Job Instance Id") final String jobInstanceId) {
		String url = "jobs/";
		url += jobName;
		url += "/";
		url += jobInstanceId;
		url += "/executions.json";
		setCommandURL(url);
		callGetService();
	}


	/**
	 * get one job execution with the id provided
	 * 
	 * @param jobExecutionId
	 */
	@CliCommand(value = "execution byId", help = "Show the JobExecution with the id provided")
	public void getExecution(@CliOption(key = { "jobExecutionId" }, mandatory = true, help = "Job Execution Id") final String jobExecutionId) {
		String url = "jobs/executions/";
		url += jobExecutionId;
		url += ".json";
		setCommandURL(url);
		callGetService();
	}


	/**
	 * stop all executions
	 * 
	 */
	@CliCommand(value = "execution stopAll", help = "Stop all job executions")
	public void stopExecutions() {
		setCommandURL("jobs/executions.json");
		callDeleteService(null);
	}

	/**
	 * stop one execution
	 * 
	 * @param jobExecutionId the execution to be stoped
	 * 
	 */
	@CliCommand(value = "execution stopById", help = "stop  the JobExecution with the id provided")
	public void stopExecution(@CliOption(key = { "jobExecutionId" }, mandatory = true, help = "Job Execution Id") final String jobExecutionId) {
		String url = "jobs/executions/";
		url += jobExecutionId;
		url += ".json";
		setCommandURL(url);
		callDeleteService(null);
	}


	/**
	 * restart the job executions
	 * 
	 * @param jobName
	 * @param jobInstanceId
	 */
	//@CliCommand(value = "execution restartByInstanceId", help = "restart the JobExecutions for the job instance with the id provided")
	public void restartJobInstanceExecutions(@CliOption(key = { "jobName" }, mandatory = true, help = "Job Name") final String jobName, @CliOption(key = { "jobInstanceId" }, mandatory = true, help = "Job Instance Id") final String jobInstanceId) {
		String url = "jobs/";
		url += jobName;
		url += "/";
		url += jobInstanceId;
		url += "/executions.json";
		setCommandURL(url);
		Date now = new Date();
		MultiValueMap<String, Date> mvm = new LinkedMultiValueMap<String, Date>();
		mvm.add("date", now);
		callPostService(mvm);
	}


	@CliAvailabilityIndicator({ "execution list", "execution byName", "execution byInstanceId", "execution byId",
			"execution stopAll","execution stopById" })
	public boolean isCommandsAvailable() {
		return isServiceUrlSet();
	}

}
