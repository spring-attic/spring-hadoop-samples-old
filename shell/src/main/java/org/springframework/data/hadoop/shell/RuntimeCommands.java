package org.springframework.data.hadoop.shell;

import org.springframework.shell.commands.OsCommands;
import org.springframework.shell.commands.OsOperations;
import org.springframework.shell.commands.OsOperationsImpl;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 */
@Component
public class RuntimeCommands implements CommandMarker {

	public static String VERSION = "1.0.0.BUILD-SNAPSHOT";

	private static final Logger LOGGER = HandlerUtils
         .getLogger(OsCommands.class);
	private static String OS = null;
	private OsOperations osOperations = new OsOperationsImpl();

	private String appPath = System.getProperty("app.home");

	enum Sample {
		wordcount("wordcount"),
		hive_password_analysis("hive-app"),
		hive_apache_log_analysis("hive-apache-log-app");

		private String sample;

		private Sample(String mem){
			this.sample = mem;
		}

		public String getSample(){
			return sample;
		}
	}

	@CliAvailabilityIndicator({"run", "config edit", "readme"})
	public boolean isAlwaysAvailable() {
		return true;
	}

	@CliCommand(value = "sample", help = "Run sample tasks")
	public String sample(
			@CliOption(key = {"", "app"}, help = "The sample app to run", mandatory = true,
					specifiedDefaultValue = "", unspecifiedDefaultValue = "")
			final Sample sample,
			@CliOption(key = {"run"}, help = "Run the app", mandatory = false,
					specifiedDefaultValue = "true", unspecifiedDefaultValue = "true")
			final boolean run) {
		boolean runSample = run;
		String app = sample.getSample();
		String result = "";
		int exitVal = -1;
		if (runSample) {
			String command;
			if (isWindows()) {
				command	= appPath + "\\runtime\\bin\\" + app + ".bat";
			} else {
				command	= appPath + "/runtime/bin/" + app;
			}
			System.out.println("Running: " + command);
			exitVal = executeCommand(command, true);
			result = "Exited with error code " + exitVal;
		}
		return result;
	}

	private int executeCommand(String command, boolean withEnv) {
		int result=-1;
		String[] commandTokens;
		String[] environmentTokens = null;
		if (withEnv) {
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
					environmentTokens = new String[]{"JAVA_OPTS=" + env};
				} else {
					environmentTokens = new String[0];
				}
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		} else {
			environmentTokens = new String[0];
		}
		if (isWindows()) {
			commandTokens = new String[] {"cmd",  "/c", command};
		} else {
			commandTokens = new String[] {"sh", command};
		}
		try {
		   Runtime rt = Runtime.getRuntime();
		   Process pr = rt.exec(commandTokens, environmentTokens);
		   BufferedReader sysout = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		   String line=null;
		   while((line=sysout.readLine()) != null) {
			   System.out.println(line);
		   }
		   int exitVal = pr.waitFor();
		   result = exitVal;
		} catch(Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		return result;
	}

	private static String getOsName() {
		if(OS == null) {
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
			command = "cmd /c " +appPath + "/bin/edit " + fname;
		} else {
			command = "sh " + appPath +"/bin/edit " + fname;
		}
		System.out.println("command is:" + command);
		if (command != null && command.length() > 0) {
			try {
				osOperations.executeCommand(command);
			}
			catch (final IOException e) {
				LOGGER.severe("Unable to execute command " + command + " ["
					 + e.getMessage() + "]");
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
			}
			catch (final IOException e) {
				LOGGER.severe("Unable to execute command " + command + " ["
					 + e.getMessage() + "]");
			}
		}
		return "";
	}

	@CliCommand(value = "hadoop", help = "Allows execution of hadoop commands.")
	public void command(
			@CliOption(key = {"", "command"}, mandatory = false, specifiedDefaultValue = "",
					unspecifiedDefaultValue = "", help = "The hadoop command to execute")
			final String command) {

		String hadoopCommand = "hadoop " + command;
		System.out.println("command is:" + hadoopCommand);
		if (command != null && command.length() > 0) {
			try {
				osOperations.executeCommand(hadoopCommand);
			} catch (final IOException e) {
				LOGGER.severe("Unable to execute '" + hadoopCommand + "' ["
						+ e.getMessage() + "]");
			}
		}
	}
}
