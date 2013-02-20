package org.springframework.data.hadoop.jetty;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class BatchAdminServer {

	private Server server;
	private int port;

	private String webappDirLocation = "src/main/resources/META-INF/webapp/";

	public BatchAdminServer() {
		this(8081);
	}

	public BatchAdminServer(int port) {
		this.port = port;
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
