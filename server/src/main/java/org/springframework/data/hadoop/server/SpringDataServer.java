package org.springframework.data.hadoop.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.data.hadoop.jetty.BatchAdminServer;
import org.springframework.data.hadoop.jetty.IntegrationServer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;


public class SpringDataServer {

	public static void main(String[] args) throws Exception {
		
		
		//TODO merge into two dispatcher servlets in web.xml
		ExecutorService executorService = Executors.newFixedThreadPool(4, new CustomizableThreadFactory("server-"));
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		tasks.add(createIntegrationCallable());
		tasks.add(createAdminCallable());
		List<Future<Void>> f = executorService.invokeAll(tasks);
		
	}

	/**
	 * 
	 */
	private static Callable<Void> createIntegrationCallable() {
		Callable<Void> siCallable = new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				IntegrationServer integrationServer = new IntegrationServer();
				integrationServer.start();
				return null;
			}
			
		};
		return siCallable;
	}

	/**
	 * 
	 */
	private static Callable<Void> createAdminCallable() {
		Callable<Void> adminCallable = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				BatchAdminServer adminServer = new BatchAdminServer();
				adminServer.start();
				return null;
			}
		};
		return adminCallable;
	}

}
