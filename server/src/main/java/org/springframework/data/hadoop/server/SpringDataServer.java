package org.springframework.data.hadoop.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.tools.Console;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.hadoop.jetty.BatchAdminServer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class SpringDataServer {

	private static final Log log = LogFactory.getLog(SpringDataServer.class);

	public static void main(String[] args) throws Exception {
		
		SpringDataServerOptions options = new  SpringDataServerOptions();
		CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			log.error(e.getMessage());
			parser.printUsage(System.err);
			System.exit(-1);
		}

		log.info("RUNNING SpringDataServer");
				
		ExecutorService executorService = Executors.newFixedThreadPool(4, new CustomizableThreadFactory("server-"));
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		if (options.getAppConfig() != null) {
			tasks.add(createIntegrationCallable(options));
		} else if (options.isBatchAdmin() == true ) {
			tasks.add(createAdminCallable());
		} else {
			parser.printUsage(System.err);
		}
		executorService.invokeAll(tasks);
		
		
	}

	private static void launchDatabase() throws SQLException {
		new ClassPathXmlApplicationContext(
				"/META-INF/spring/batch/override/datasource-context.xml",
				"/META-INF/spring/batch/initialize/initialize-batch-schema-context.xml",
				"/META-INF/spring/batch/initialize/initialize-product-table-context.xml"				
			);
			Console.main();
	}

	/**
	 * 
	 */
	private static Callable<Void> createIntegrationCallable(final SpringDataServerOptions options) {
		log.info("Running Spring Integration Application in config dir [" + options.getAppConfig() + "]");
		Callable<Void> siCallable = new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				AbstractApplicationContext context = new ClassPathXmlApplicationContext(
						"/" + options.getAppConfig() + "/application-context.xml");
				context.registerShutdownHook();	
				return null;
			}
			
		};
		return siCallable;
	}

	/**
	 * 
	 */
	private static Callable<Void> createAdminCallable() {
		log.info("Running Spring Batch Admin");
		Callable<Void> adminCallable = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				
				try {
					launchDatabase();
				} catch (SQLException e) {
					log.error("Could not launch H2 database");
					log.error(e);
					System.exit(-1);
				}
				BatchAdminServer adminServer = new BatchAdminServer();
				adminServer.start();
				return null;
			}
		};
		return adminCallable;
	}

}
