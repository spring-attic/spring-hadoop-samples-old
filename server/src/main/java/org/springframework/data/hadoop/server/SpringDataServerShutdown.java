package org.springframework.data.hadoop.server;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 */
@ManagedResource(objectName = "spring:name=shutdownBean", description = "shutdown bean")
public class SpringDataServerShutdown implements ApplicationContextAware {

	ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@ManagedOperation
	public void shutDown() {
		if (applicationContext instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext)applicationContext).stop();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
			System.exit(0);
		}
	}
}
