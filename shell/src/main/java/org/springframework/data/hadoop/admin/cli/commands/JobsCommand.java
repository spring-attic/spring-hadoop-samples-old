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

import org.springframework.data.hadoop.admin.cli.commands.BaseCommand;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Commands for Jobs, such as list job, start job
 * 
 * @author Jarred Li
 *
 */
@Component
public class JobsCommand extends BaseCommand implements CommandMarker {

	private static int count = 0;

	/**
	 * list all jobs
	 */
	@CliCommand(value = "job list", help = "list all jobs information")
	public void getJobs() {
		setCommandURL("jobs.json");
		callGetService();
	}

	/**
	 * list job by name
	 * 
	 * @param jobName
	 */
	@CliCommand(value = "job byName", help = "list jobs information by name")
	public void getJobsByName(@CliOption(key = { "jobName" }, mandatory = true, help = "Job Name") final String jobName) {
		String url = "jobs/";
		url += jobName;
		url += ".json";
		setCommandURL(url);
		callGetService();
	}

	/**
	 * launch job 
	 * 
	 * @param jobName
	 */
	@CliCommand(value = "job launch", help = "execute job")
	public void executeJob(@CliOption(key = { "jobName" }, mandatory = true, help = "Job Name") final String jobName, @CliOption(key = { "jobParameters" }, mandatory = false, help = "Job Parameters") final String jobParameters) {
		String url = "jobs/";
		url += jobName;
		url += ".json";
		setCommandURL(url);
		MultiValueMap<String, String> mvm = new LinkedMultiValueMap<String, String>();
		if (jobParameters == null) {
			mvm.add("jobParameters", "fail=false, id=" + count++);
		}
		else {
			mvm.add("jobParameters", jobParameters);
		}
		callPostService(mvm);
	}

	@CliAvailabilityIndicator({ "job list", "job byName", "job launch" })
	public boolean isCommandsAvailable() {
		return isServiceUrlSet();
	}
}
