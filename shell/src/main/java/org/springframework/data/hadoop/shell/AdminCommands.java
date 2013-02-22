package org.springframework.data.hadoop.shell;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.hadoop.admin.cli.commands.BaseCommand;
import org.springframework.data.hadoop.admin.cli.commands.JobsCommand;
import org.springframework.data.hadoop.admin.cli.commands.PropertyUtil;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.shell.commands.OsCommands;
import org.springframework.shell.commands.OsOperations;
import org.springframework.shell.commands.OsOperationsImpl;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.management.MalformedObjectNameException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	@CliCommand(value = "admin list-jobs", help = "list all jobs information")
	public String getJobs() {
		setCommandURL("jobs.json");
		String response = callGetService();
		return response;
	}

	@CliCommand(value = "admin list-executions", help = "get all job executions, in order of most recent to least")
	public String getExecutions() {
		setCommandURL("jobs/executions.json");
		String response = callGetService();
		return response;
	}
	
	/**
	 * launch job 
	 * 
	 * @param jobName
	 */
	@CliCommand(value = "admin job start", help = "Start a batch job")
	public void executeJob(
	 @CliOption(key = { "jobName" }, mandatory = true, help = "Job Name") final String jobName,
	 @CliOption(key = { "jobParameters" }, mandatory = false, help = "Job Parameters") final String jobParameters) {
		String url = "jobs/";
		url += jobName;
		url += ".json";
		setCommandURL(url);
		MultiValueMap<String, String> mvm = new LinkedMultiValueMap<String, String>();
		
		if (jobParameters == null) {
			mvm.add("\"jobParameters\":", "fail=false, id=" + jobCount++);
		}
		else {
			mvm.add("\"jobParameters\":", jobParameters);
		}
		
		callPostService(mvm);
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
