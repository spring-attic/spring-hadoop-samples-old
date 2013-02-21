package org.springframework.data.hadoop.jetty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.StringUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ManagedResource(objectName = "spring-data-server:name=managementBean", description = "Spring Data Server Management Bean")
public class BatchAdminServer {

	private static final Log log = LogFactory.getLog(BatchAdminServer.class);

	private Server server;
	private int port;
	private org.h2.tools.Server databaseServer;

	private String webappDirLocation = "src/main/resources/META-INF/webapp/";

	public BatchAdminServer() {
		this(8081);
	}

	public BatchAdminServer(int port) {
		this.port = port;
		String path = System.getProperty("app.home");
		if (StringUtils.hasText(path)) {
			webappDirLocation = path + "/webapp/";
			log.info("Loading BatchAdminServer webapp from " + webappDirLocation);
		}
	}

	public void start() throws Exception {
		this.server = new Server(this.port);

		WebAppContext root = new WebAppContext();

		root.setContextPath("/");
		root.setDescriptor(webappDirLocation + "/web.xml");
		root.setResourceBase(webappDirLocation);

		root.setParentLoaderPriority(true);

		this.server.setHandler(root);

		this.server.setStopAtShutdown(true);
		this.server.start();
		databaseServer = org.h2.tools.Server.createTcpServer(new String[]{"-tcpAllowOthers"}).start();
		this.server.join();
	}
	
	public void stop() throws Exception {
		databaseServer.stop();
		server.stop();
	}

	@ManagedOperation
	public void shutDown() throws Exception {

		stop();

		ExecutorService executorService = Executors.newFixedThreadPool(1, new CustomizableThreadFactory("shell-"));
		executorService.submit(new Runnable() {
			public void run() {
				try {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {}
					System.exit(0);
				} catch(Exception e) {}
			}
		});
	}
}
