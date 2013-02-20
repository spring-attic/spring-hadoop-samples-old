package org.springframework.data.hadoop.server;

import org.kohsuke.args4j.Option;

public class SpringDataServerOptions {

	@Option(name="-appConfig", usage="the application configuration directory", metaVar="<configDir>")
	private String appConfig = null;

	@Option(name="-batchAdmin", usage="start the batch admin service (true or false)")
	private boolean batchAdmin = false;
	/**
	 * @return the batchAdmin
	 */
	public boolean isBatchAdmin() {
		return batchAdmin;
	}
	/**
	 * @return the appConfig
	 */
	public String getAppConfig() {
		return appConfig;
	}
	
	
}
