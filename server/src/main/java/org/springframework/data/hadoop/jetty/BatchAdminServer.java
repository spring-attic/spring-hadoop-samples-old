package org.springframework.data.hadoop.jetty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.util.StringUtils;

public class BatchAdminServer {

	private static final Log log = LogFactory.getLog(BatchAdminServer.class);

	private Server server;
	private int port;

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
		this.server.join();
	}
	
	public void stop() throws Exception {
		server.stop();
	}
}
