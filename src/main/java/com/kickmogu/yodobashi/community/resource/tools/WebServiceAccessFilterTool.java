package com.kickmogu.yodobashi.community.resource.tools;

import java.util.List;

import javax.jws.WebService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Lists;
import com.kickmogu.hadoop.mapreduce.job.util.ThreadsafeOptionBuilder;
import com.kickmogu.lib.core.utils.Asserts;
import com.kickmogu.yodobashi.community.resource.config.AppConfigurationDao;
import com.kickmogu.yodobashi.community.resource.config.WebServiceClientInitializer;

public class WebServiceAccessFilterTool {

	@Autowired
	private AppConfigurationDao appConfigurationDao;
	
	@Autowired
	private WebServiceClientInitializer webServiceClientInitializer;
	
	public void list() {
		List<String> displayList = Lists.newArrayList();
		for (String endPointName:getAllEndPointName()) {
			Boolean allow = appConfigurationDao.getAsBoolean("webServiceAccessFilter." + endPointName + ".allow");
			String status = null;
			if (allow == null) {
				status = "DENY(UNDEFINED)";
			} else if (!allow) {
				status = "DENY";
			} else {
				status = "ALLOW";				
			}
			displayList.add(String.format("%-30s %s", endPointName, status));
		}
		for (String display:displayList) {
			System.out.println(display);
		}
	}
	
	
	private void update(String endPoints, boolean allow) {
		List<String> endPointNames = Lists.newArrayList();
		for (String endPoint:endPoints.split(",")) {
			if (endPoint.equals("ALL")) {
				endPointNames = getAllEndPointName();
			} else {
				boolean hit = false;
				for (String endPointName:getAllEndPointName()) {
					if (endPointName.equals(endPoint)) {
						endPointNames.add(endPointName);
						hit = true;
						break;
					}
				}
				Asserts.isTrue(hit, "endPoint invarid:" + endPoint);
			}
		}
		for (String endPointName:endPointNames) {
			appConfigurationDao.set("webServiceAccessFilter." + endPointName + ".allow", allow);
		}
		list();
	}
	
	private List<String> getAllEndPointName() {
		List<String> result = Lists.newArrayList();
		for (Class<?> webServiceInterface:webServiceClientInitializer.getWebServiceInterfaces()) {
			WebService webService = webServiceInterface.getAnnotation(WebService.class);
			result.add(webService.name());
		}
		result.add("GetCommunityUpdateData");
		return result;
	}
	
	
	public static void main(String[] args) {
		int result = 0;
		try {
			mainBody(args);
		} catch (Throwable th) {
			th.printStackTrace();
			result = 1;
		} finally {
			System.exit(result);
		}
		
	}
	
	private static void mainBody(String[] args) throws Throwable {
		
		Options options = new Options();
		options.addOption(
			new ThreadsafeOptionBuilder()
		    .hasArg(false)
		    .isRequired(false)
		    .withDescription("list all WebServiceAccessFilter config.")
		    .withLongOpt("list")
		    .create("l")
	    );
		options.addOption(
			new ThreadsafeOptionBuilder().withArgName("endPointName or ALL(separate with camma)")
		    .hasArgs()
		    .isRequired(false)
		    .withDescription("endPointName or ALL(separate with camma)")
		    .withLongOpt("allow")
		    .create("a")
	    );
		options.addOption(
			new ThreadsafeOptionBuilder().withArgName("endPointName or ALL(separate with camma)")
		    .hasArgs()
		    .isRequired(false)
		    .withDescription("endPointName or ALL(separate with camma)")
		    .withLongOpt("deny")
		    .create("d")
	    );
		
		if (args.length == 0) argError(options);

		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = null;
        try {
        	commandLine = parser.parse(options, args);
        } catch (ParseException e) {
        	argError(options);
        }
        
        WebServiceAccessFilterTool tool = new WebServiceAccessFilterTool();
        ApplicationContext context = new ClassPathXmlApplicationContext("serviceContext.xml");
        context.getAutowireCapableBeanFactory().autowireBeanProperties(tool, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
        if (commandLine.hasOption("l")) {
        	tool.list();
        } else if (commandLine.hasOption("a")) {
        	tool.update(commandLine.getOptionValue("a"),true);
        } else if (commandLine.hasOption("d")) {
        	tool.update(commandLine.getOptionValue("d"),false);
        } else {
        	argError(options);
        }
        
	}


	private static void argError(Options options) {
        HelpFormatter help = new HelpFormatter();
        help.setWidth(120);
        help.printHelp(WebServiceAccessFilterTool.class.getSimpleName(), options, true);
        System.exit(1);
	}
}
