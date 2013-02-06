package org.springframework.data.hadoop.jetty;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.web.servlet.DispatcherServlet;

public class IntegrationServer {

	private Server server;
	private int port;
	
	public IntegrationServer() {
		this(8081);
	}

	public IntegrationServer(int port) {
		this.port = port;
	}
	
	public void start() throws Exception  {
		this.server = new Server(this.port);
	    Context context = new Context(server, "/", Context.SESSIONS);

	    DispatcherServlet dispatcherServlet = new DispatcherServlet();
	    dispatcherServlet
	        .setContextConfigLocation("classpath:/META-INF/spring/application-context.xml");

	    ServletHolder servletHolder = new ServletHolder(dispatcherServlet);
	    context.addServlet(servletHolder, "/*");

		this.server.setStopAtShutdown(true);
	    this.server.start();
	    this.server.join();
	}
	
	public void stop() throws Exception {
		server.stop();
	}

}
