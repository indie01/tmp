package com.kickmogu.yodobashi.community.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.ethz.ssh2.SCPClient;

import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.core.ssh.SshCommand;
import com.kickmogu.lib.core.ssh.SshUtils;

public class BackendStubUtils {
	
	private static String HOSTNAME;
	private static String USER;
	private static String PASSWORD;

	static {
		String hostname = null;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new CommonSystemException(e);
		}
		if (hostname.startsWith("yctco")) {
			HOSTNAME = "yctcod01";
			USER = "comm";
			PASSWORD = "comm00";
		} else if (hostname.startsWith("ycoco")) {
			HOSTNAME = "ycocod01";
			USER = "comm";
			PASSWORD = "comm00";
		} else {
			HOSTNAME = "192.168.101.104";
			USER = "community";
			PASSWORD = "community00";
		}
	}


	private static Log log = LogFactory.getLog(BackendStubUtils.class);

	public static void prepareResponse(final String property, final String  value, final Object response) {

		final ByteArrayOutputStream byteArrayOutputStream =  new ByteArrayOutputStream();
		try {
			JAXBContext context = JAXBContext.newInstance(response.getClass());
			Marshaller marshaller = context.createMarshaller();

			marshaller.marshal(response, byteArrayOutputStream);

			new SshCommand(HOSTNAME, USER, PASSWORD) {
				@Override
				public void start() throws IOException {
					SCPClient scp = getConnection().createSCPClient();
					scp.put(byteArrayOutputStream.toByteArray(), property + "-" + value +".dump", "/home/"+USER+"/backend-stub/tmpdata");
				}
			};


		} catch (Throwable th) {
			throw new CommonSystemException(th);
		} finally {
			IOUtils.closeQuietly(byteArrayOutputStream);
		}
	}

	public static void clean() {
		SshUtils.command(
				HOSTNAME,
				USER,
				PASSWORD,
				"rm -fR /home/"+USER+"/backend-stub/tmpdata/*.dump", false);
	}

	@SuppressWarnings("unchecked")
	public static <T> T loadResponse(Class<T> type, Object request) {

		File tmpDataDir = new File("/home/" + USER + "/backend-stub/tmpdata");
		if (!tmpDataDir.exists()) return null;
		for (File file : tmpDataDir.listFiles()) {
			if (!file.getName().endsWith(".dump")) continue;
			String property = file.getName().split("-")[0];
			String value = file.getName().split("-", 2)[1].replaceAll("\\.dump", "");
			try {
				Object actual = PropertyUtils.getNestedProperty(request, property);
				if (value.equals(actual)) {
					JAXBContext context = JAXBContext.newInstance(type);
					Unmarshaller unmarshaller  = context.createUnmarshaller();
					return (T)unmarshaller.unmarshal(file);
				}
			} catch (Throwable th){
				log.warn("type=" + type.getName() + ", property=" + property + ", value=" + value);
//				th.printStackTrace();
			}
		}
		return null;
	}

}
