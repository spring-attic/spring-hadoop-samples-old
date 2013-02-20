package org.springframework.data.hadoop.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

/**
 */
@ManagedResource(objectName = "spring-data-server:name=managementBean", description = "Spring Data Server Management Bean")
public class SpringDataServerManagement implements ApplicationContextAware {

	ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@ManagedOperation
	public void shutDown() {
		if (applicationContext instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext)applicationContext).stop();

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
}
