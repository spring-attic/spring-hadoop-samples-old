package org.springframework.data.hadoop.server;

import org.kohsuke.args4j.Option;

public class SpringDataServerOptions {

	@Option(name="-appConfig", usage="the application configuration directory")

	private String appConfig = null;

	/**
	 * @return the appConfig
	 */
	public String getAppConfig() {
		return appConfig;
	}
	
	
}
