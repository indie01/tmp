package com.kickmogu.yodobashi.community.common.utils;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class CommonsCliUtils {

	public static void argError(String cmdLineSyntax, Options options) {
        HelpFormatter help = new HelpFormatter();
        help.setWidth(120);
        help.printHelp(cmdLineSyntax, options, true);
        System.exit(1);
	}
}
