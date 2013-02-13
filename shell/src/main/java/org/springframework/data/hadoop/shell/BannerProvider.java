package org.springframework.data.hadoop.shell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

/**
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BannerProvider extends DefaultBannerProvider
		implements CommandMarker {

	@CliCommand(value = {"version"}, help = "Displays current CLI version")
	public String getBanner() {
		StringBuffer buf = new StringBuffer();
		buf.append("        ____             _               ____        _          ____  _          _ _         " + OsUtils.LINE_SEPARATOR);
		buf.append("       / ___| _ __  _ __(_)_ __   __ _  |  _ \\  __ _| |_ __ _  / ___|| |__   ___| | |        " + OsUtils.LINE_SEPARATOR);
		buf.append("       \\___ \\| '_ \\| '__| | '_ \\ / _` | | | | |/ _` | __/ _` | \\___ \\| '_ \\ / _ \\ | |        " + OsUtils.LINE_SEPARATOR);
		buf.append("        ___) | |_) | |  | | | | | (_| | | |_| | (_| | || (_| |  ___) | | | |  __/ | |        " + OsUtils.LINE_SEPARATOR);
		buf.append("   __  |____/| .__/|_|_ |_|_| |_|\\__, | |____/ \\__,_|\\__\\__,_| |____/|_| |_|\\___|_|_|        " + OsUtils.LINE_SEPARATOR);
		buf.append("  / _| ___  _|_|     / \\   _ __  |___/  ___| |__   ___  | | | | __ _  __| | ___   ___  _ __  " + OsUtils.LINE_SEPARATOR);
		buf.append(" | |_ / _ \\| '__|   / _ \\ | '_ \\ / _` |/ __| '_ \\ / _ \\ | |_| |/ _` |/ _` |/ _ \\ / _ \\| '_ \\ " + OsUtils.LINE_SEPARATOR);
		buf.append(" |  _| (_) | |     / ___ \\| |_) | (_| | (__| | | |  __/ |  _  | (_| | (_| | (_) | (_) | |_) |" + OsUtils.LINE_SEPARATOR);
		buf.append(" |_|  \\___/|_|    /_/   \\_\\ .__/ \\__,_|\\___|_| |_|\\___| |_| |_|\\__,_|\\__,_|\\___/ \\___/| .__/ " + OsUtils.LINE_SEPARATOR);
		buf.append("                          |_|                                                         |_|    " + OsUtils.LINE_SEPARATOR);
		buf.append("                                                                                             " + OsUtils.LINE_SEPARATOR);
		buf.append("Version " + this.getVersion());
		return buf.toString();

	}

	public String getVersion() {
		return DemoCommands.VERSION;
	}

	public String getWelcomeMessage() {
		return "Welcome to Spring for Apache Hadoop Samples";
	}

	@Override
	public String name() {
		return "Spring Data HD Shell";
	}
}
