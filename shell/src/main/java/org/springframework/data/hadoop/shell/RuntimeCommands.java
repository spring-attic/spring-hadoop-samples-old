package org.springframework.data.hadoop.shell;

import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pExecRequest;
import org.jolokia.client.request.J4pExecResponse;
import org.jolokia.client.request.J4pVersionRequest;
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

import javax.management.MalformedObjectNameException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 */
@Component
public class RuntimeCommands implements CommandMarker {

	public static String VERSION = "1.0.0.BUILD-SNAPSHOT";

	private static final Logger LOGGER = HandlerUtils.getLogger(OsCommands.class);
	private static String OS = null;
	private OsOperations osOperations = new OsOperationsImpl();

	private String appPath = System.getProperty("app.home");

	private StringBuffer logs = new StringBuffer();

	enum Sample {
		wordcount("wordcount"), hive_password_analysis("hive-app"), hive_apache_log_analysis("hive-apache-log-app");

		private String app;

		private Sample(String app) {
			this.app = app;
		}

		public String getApp() {
			return app;
		}
	}

	enum Server {
		syslog_hdfs("syslog-hdfs");

		private String app;

		private Server(String app) {
			this.app = app;
		}

		public String getApp() {
			return app;
		}
	}

	private final J4pClient j4pClient;
	private boolean serverRunning;

	public RuntimeCommands() {
		j4pClient = new J4pClient("http://localhost:8778/jolokia/");
		serverRunning = !serverStatus().contains("not");
	}

	@CliAvailabilityIndicator({ "config edit", "readme", "hadoop", "server log" })
	public boolean isAlwaysAvailable() {
		return true;
	}

	@CliAvailabilityIndicator({ "sample", "server start" })
	public boolean isAvailableToRun() {
		if (serverRunning) {
			return false;
		}
		return true;
	}

	@CliAvailabilityIndicator({ "server stop", "mon batch", "mon int" })
	public boolean isAvailableToStop() {
		if (serverRunning) {
			return true;
		}
		return false;
	}

	@CliCommand(value = "sample", help = "Run sample tasks")
	public String sample(
			@CliOption(key = { "", "app" }, help = "The app app to run", mandatory = true, specifiedDefaultValue = "", unspecifiedDefaultValue = "") final Sample sample,
			@CliOption(key = { "run" }, help = "Run the app", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "true") final boolean run) {
		boolean runSample = run;
		String app = sample.getApp();
		String result = "";
		int exitVal = -1;
		if (runSample) {
			String command;
			if (isWindows()) {
				command = appPath + "\\runtime\\bin\\" + app + ".bat";
			} else {
				command = appPath + "/runtime/bin/" + app;
			}
			System.out.println("Running: " + command);
			exitVal = executeCommand(command, true);
			result = "Exited with error code " + exitVal;
		}
		return result;
	}

	@CliCommand(value = "server status", help = "Check if server is running")
	public String serverStatus() {
		J4pVersionRequest exec;
		String status = "server is not running";
		try {
			
			exec = new J4pVersionRequest();
			j4pClient.execute(exec);
			status = "server is running";
		} catch (J4pException e) {
			//System.out.println(e.getMessage());
		}
		return status;
	}

	@CliCommand(value = "server start", help = "Start server tasks")
	public String serverStart(
			@CliOption(key = { "", "app" }, help = "The app app to run", mandatory = true, specifiedDefaultValue = "", unspecifiedDefaultValue = "") final Server server) {
		String app = server.getApp();
		String result = "";
		String command;
		if (isWindows()) {
			command = appPath + "\\server\\bin\\server.bat";
		} else {
			command = appPath + "/server/bin/server";
		}
		System.out.println("Running: " + command + " " + app);
		result = startCommand(command, app, true);
		this.serverRunning = true;
		return result;
	}

	@CliCommand(value = "server log", help = "Show logs for running server tasks")
	public String serverLog(
			@CliOption(key = { "clear" }, help = "Clear the log after displaying", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final boolean clear) {
		System.out.println(this.logs.toString());
		if (clear) {
			int lastPos = this.logs.length() - 1;
			if (lastPos > 0) {
				this.logs.delete(0, lastPos);
			}
		}
		return "";
	}

	@CliCommand(value = "server stop", help = "Stop running server tasks")
	public String serverStop() {

		J4pExecRequest exec;
		try {
			exec = new J4pExecRequest("spring-data-server:name=shutdownBean", "shutDown");
			J4pExecResponse execResp = j4pClient.execute(exec);
		} catch (MalformedObjectNameException e) {
			System.out.println(e);
		} catch (J4pException e) {
			System.out.println(e);
		}
		this.serverRunning = false;
		return "Stop requested";
	}

	@CliCommand(value = "mon batch", help = "Monitor and control batch components")
	public Object monitorBatch() {
		return null;
	}

	@CliCommand(value = "mon int input adapters", help = "Monitor and control integration components")
	public Object monitorInputAdapters(
			@CliOption(key = { "", "list" }, help = "Monitor and control input adapters", mandatory = false, specifiedDefaultValue = "", unspecifiedDefaultValue = "") boolean list 
			) {
		return null;
	}

	private int executeCommand(String command, boolean withEnv) {
		int result = -1;
		String[] commandTokens;
		String[] environmentTokens = null;
		if (withEnv) {
			environmentTokens = getEnvironmentTokens(environmentTokens);
		} else {
			environmentTokens = new String[0];
		}
		if (isWindows()) {
			commandTokens = new String[] { "cmd", "/c", command };
		} else {
			commandTokens = new String[] { "sh", command };
		}
		try {
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(commandTokens, environmentTokens);
			BufferedReader sysout = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = null;
			while ((line = sysout.readLine()) != null) {
				System.out.println(line);
			}
			int exitVal = pr.waitFor();
			result = exitVal;
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		return result;
	}

	private String startCommand(String command, String app, boolean withEnv) {
		final String[] commandTokens;
		String[] environmentTokens = null;
		if (withEnv) {
			environmentTokens = getEnvironmentTokens(environmentTokens);
		} else {
			environmentTokens = new String[0];
		}
		if (isWindows()) {
			commandTokens = new String[] { "cmd", "/c", command, "-appConfig", app };
		} else {
			commandTokens = new String[] { "sh", command, "-appConfig", app };
		}

		this.logs = new StringBuffer();
		final String[] finalEnvironmentTokens = environmentTokens;
		ExecutorService executorService = Executors.newFixedThreadPool(1, new CustomizableThreadFactory("shell-"));
		Future f = executorService.submit(new Runnable() {
			public void run() {
				try {
					Runtime rt = Runtime.getRuntime();
					Process pr = rt.exec(commandTokens, finalEnvironmentTokens);
					BufferedReader sysout = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					String line = null;
					while ((line = sysout.readLine()) != null) {
						//System.out.println(line);
						logs.append(line + "\n");
					}
					int exitVal = pr.waitFor();
					logs.append("Completed with exit code " + exitVal + "\n");
				} catch (Exception e) {
					System.out.println(e.toString());
					e.printStackTrace();
				}
			}
		});

		return "Server started.";
	}

	private String[] getEnvironmentTokens(String[] environmentTokens) {
		File props = new File(appPath + "/config/config.properties");
		try {
			Reader propsReader = new FileReader(props);
			Properties configProps = new Properties();
			configProps.load(propsReader);
			String env = "";
			for (Map.Entry prop : configProps.entrySet()) {
				env = env + (env.length() > 0 ? " " : "") + "-D" + prop.getKey() + "=" + prop.getValue();
			}
			if (env.length() > 0) {
				environmentTokens = new String[] { "JAVA_OPTS=" + env };
			} else {
				environmentTokens = new String[0];
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return environmentTokens;
	}

	private static String getOsName() {
		if (OS == null) {
			OS = System.getProperty("os.name");
		}
		return OS;
	}

	public static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}

	@CliCommand(value = "config edit", help = "Edit config properties")
	public String demoEdit() {
		String fname = appPath + "/config/config.properties";
		String command;
		if (isWindows()) {
			command = "cmd /c " + appPath + "/bin/edit " + fname;
		} else {
			command = "sh " + appPath + "/bin/edit " + fname;
		}
		System.out.println("command is:" + command);
		if (command != null && command.length() > 0) {
			try {
				osOperations.executeCommand(command);
			} catch (final IOException e) {
				LOGGER.severe("Unable to execute command " + command + " [" + e.getMessage() + "]");
			}
		}
		return "Completed.";
	}

	@CliCommand(value = "readme", help = "Show README.txt")
	public String readme() {
		String fname = appPath + "/README.txt";
		String command;
		if (isWindows()) {
			command = "type " + fname;
		} else {
			command = "cat " + fname;
		}
		System.out.println("command is:" + command);
		if (command != null && command.length() > 0) {
			try {
				osOperations.executeCommand(command);
			} catch (final IOException e) {
				LOGGER.severe("Unable to execute command " + command + " [" + e.getMessage() + "]");
			}
		}
		return "";
	}

	@CliCommand(value = "hadoop", help = "Allows execution of hadoop commands.")
	public void command(
			@CliOption(key = { "", "command" }, mandatory = false, specifiedDefaultValue = "", unspecifiedDefaultValue = "", help = "The hadoop command to execute") final String command) {

		String hadoopCommand = "hadoop " + command;
		System.out.println("command is:" + hadoopCommand);
		if (command != null && command.length() > 0) {
			try {
				osOperations.executeCommand(hadoopCommand);
			} catch (final IOException e) {
				LOGGER.severe("Unable to execute '" + hadoopCommand + "' [" + e.getMessage() + "]");
			}
		}
	}
}
