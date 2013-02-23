package org.springframework.data.hadoop.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MalformedObjectNameException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.springframework.data.hadoop.admin.cli.commands.BaseCommand;
import org.springframework.data.hadoop.admin.cli.commands.PropertyUtil;
import org.springframework.data.hadoop.util.JsonUtil;
import org.springframework.data.hadoop.util.UiUtils;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 */
@Component
public class AdminCommands extends BaseCommand implements CommandMarker {

	private static final Logger LOGGER = HandlerUtils.getLogger(AdminCommands.class);

	enum AdapterAction {
		start, stop, status
	}
	
	enum BatchJobAction {
		start,stop,restart,next,abandon
	}

	private final J4pClient j4pClient;
	private final MBeanOps mbeanOps;

	private static int jobCount;

	public AdminCommands() {
		j4pClient = new J4pClient("http://localhost:8778/jolokia/");
		mbeanOps = new MBeanOps(j4pClient);
		try {
			PropertyUtil.setTargetUrl("http://localhost:8081");
		} catch (ConfigurationException e) {}
	}


	@CliAvailabilityIndicator({"admin list-jobs", "admin list-executions", "admin list-input-adapters",
			"admin list-output-adapters", "admin list-components", "admin adapter"})
	public boolean isAvailableToStop() {
		if (RuntimeCommands.isServerRunning()) {
			return true;
		}
		return false;
	}

	/**
	 * Batch REST Commands
	 *
	 * These are going to need some work -
	 * have to change them to return the JSON or re-format it to something pretty
	 *
	 */
	@CliCommand(value = "admin job-list", help = "list all jobs information")
	public String getJobs() {
		setCommandURL("jobs.json");
		String response = callGetService();
		if (!StringUtils.hasText(response)) {
			return "(no data)";
		}
		Map<String, Object> map = JsonUtil.convertJsonToMap(response);
		Map<String, Object> jobs = (Map<String, Object>) ((Map<String, Object>)map.get("jobs")).get("registrations");
		if (jobs == null) {
			return "";
		}
		List<Map<String, Object>> data = new ArrayList(jobs.values());
		List<String> headers = Arrays.asList(new String[] {"name", "description", "executionCount", "launchable", "incrementable"});
		String display = UiUtils.renderMapDataAsTable(data, headers);
		return display;
	}

	@CliCommand(value = "admin job-executions", help = "get all job executions, in order of most recent to least")
	public String getExecutions() {
		setCommandURL("jobs/executions.json");
		String response = callGetService();
		if (!StringUtils.hasText(response)) {
			return "(no data)";
		}
		Map<String, Object> map = JsonUtil.convertJsonToMap(response);
		Map<String, Object> executions = (Map<String, Object>) map.get("jobExecutions");
		if (executions == null) {
			return "";
		}
		List<Map<String, Object>> data = new ArrayList();
		for (String id : executions.keySet()) {
			Map<String, Object> execution = (Map<String, Object>) executions.get(id);
			execution.put("id", id);
			String name = getJobName((String) execution.get("resource"));
			execution.put("name", name);
			data.add(execution);
		}
		List<String> headers = Arrays.asList(new String[] {"id", "name", "status", "startTime", "duration"});
		String display = UiUtils.renderMapDataAsTable(data, headers);
		return display;
	}
	
	@CliCommand(value = "admin job-execution-details", help = "Show the JobExecution details with the id provided")
	public String getExecution(@CliOption(key = { "jobExecutionId" }, mandatory = true, help = "Job Execution Id") final String jobExecutionId) {
		String url = "jobs/executions/";
		url += jobExecutionId;
		url += ".json";
		setCommandURL(url);
		String response = callGetService();
		if (!StringUtils.hasText(response)) {
			return "(no data)";
		}
		String display = formatJobExecution(response);
		return display;
	}

	private String formatJobExecution(String input) {
		Map<String, Object> map = JsonUtil.convertJsonToMap(input);
		Map<String, Object> jobExecution = (Map<String, Object>) map.get("jobExecution");
		if (jobExecution == null) {
			return "";
		}
		List<Map<String, Object>> data = new ArrayList();
		data.add(jobExecution);
		List<String> headers = Arrays.asList(new String[] {"id", "name", "status", "startTime", "duration", "exitCode"});
		String display = UiUtils.renderMapDataAsTable(data, headers);
		return display;
	}

	/**
	 * launch job 
	 * 
	 * @param jobName
	 */
	@CliCommand(value = "admin job-start", help = "Start a batch job")
	public String executeJob(
	 @CliOption(key = { "jobName" }, mandatory = true, help = "Job Name") final String jobName,
	 @CliOption(key = { "jobParameters" }, mandatory = false, help = "Job Parameters") final String jobParameters) {
		String url = "jobs/";
		url += jobName;
		url += ".json";
		setCommandURL(url);
		MultiValueMap<String, String> mvm = new LinkedMultiValueMap<String, String>();
		
		if (jobParameters == null) {
			mvm.add("jobParameters", "fail=false, id=" + jobCount++);
		}
		else {
			mvm.add("jobParameters", jobParameters);
		}
		
		String response = callPostService(mvm);
		String display = formatJobExecution(response);
		return display;
	}

	private String getJobName(String url) {
		String name = "(unknown)";
		if (url != null) {
			String urlToUse = url.substring(url.indexOf("jobs/"));
			setCommandURL(urlToUse);
			String response = callGetService();
			Map<String, Object> map = JsonUtil.convertJsonToMap(response);
			name = (String) ((Map<String, Object>)map.get("jobExecution")).get("name");
		}
		return name;
	}

	/**
	 * SI MBean Commands
	 */
	@CliCommand(value = "admin list-input-adapters", help = "list spring integration input adapters")
	public String listInputAdapters() {
		return findSIComponentsByNameStartsWith("inputAdapter");

	}

	@CliCommand(value = "admin list-output-adapters", help = "list spring integration output adapters")
	public String listOutputAdapters() {
		return findSIComponentsByNameStartsWith("outputAdapter");
	}

	@CliCommand(value = "admin list-components", help = "list spring integration components,e.g., MessageHandler, MessageChannel")
	public String listComponentsByType(
			@CliOption(key = { "type" }, help = "Specify the component type", mandatory = true) String type) {
		String result = null;
		String searchString = "*:*,type=" + type;
		List<String> results = mbeanOps.executeSearchRequest(searchString);
		if (results != null) {
			result = StringUtils.arrayToDelimitedString(results.toArray(), "\n");
		}
		return result;
	}

	@CliCommand(value = "admin adapter", help = "control input and output adapters")
	public String controlAdapter(
			@CliOption(key = { "adapter" }, help = "Specify the mbean object name", mandatory = true) String adapterName,
			@CliOption(key = { "action" }, help = "Specify the action", mandatory = true) AdapterAction action) {

		String result = null;
		try {
			List<String> mbeanNames = mbeanOps.executeSearchRequest("*:name="+adapterName+",*");
			if (CollectionUtils.isEmpty(mbeanNames)){
				return adapterName + " not found.";
			}
			
			switch (action) {
			
			case start:
			
				result = mbeanOps.execOperation(mbeanNames.get(0), "start");
				break;
			case stop:
				result = mbeanOps.execOperation(mbeanNames.get(0), "stop");
				break;
			case status:
				result = mbeanOps.readAttribute(mbeanNames.get(0), "Running");
				result = result.equals("true")?"running":"stopped";
				break;
			}
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (J4pException e) {
			e.printStackTrace();
		}
		return result;
	}

	
	private String findSIComponentsByNameStartsWith(String namePrefix) {
		String result = null;

		List<String> results = mbeanOps.executeSearchRequest("*:*,name="+namePrefix+"*");
		if (!CollectionUtils.isEmpty(results)) {
			String resultsArray[] = new String[results.size()];
			Pattern pattern = Pattern.compile(".*,name=(.*),.*");
			int i=0;
			for (String mbean: results) {
				
				Matcher m = pattern.matcher(mbean);
				m.matches();
				resultsArray[i++] = m.group(1);
			}
			result = StringUtils.arrayToDelimitedString(resultsArray, "\n");
		}
		return result;
	}

}
