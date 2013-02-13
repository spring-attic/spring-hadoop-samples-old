package org.springframework.data.hadoop.shell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

/**
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class Customizations implements CommandMarker {

	@CliAvailabilityIndicator({"script", "version", "date", "*/", "/*", "//"})
	public boolean isNeverAvailable() {
		return false;
	}

}
