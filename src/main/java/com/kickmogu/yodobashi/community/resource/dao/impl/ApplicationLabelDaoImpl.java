package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.kickmogu.lib.core.utils.ThreadSafeDateFormat;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.lib.hadoop.hbase.aop.ArroundHBase;
import com.kickmogu.yodobashi.community.resource.dao.ApplicationLabelDao;
import com.kickmogu.yodobashi.community.resource.domain.ApplicationLabelDO;

public class ApplicationLabelDaoImpl implements ApplicationLabelDao {

	private static final long START_TIME_BASE = ThreadSafeDateFormat.parse("yyyyMMddHHmmss", "20110101000000").getTime() / 1000L;
	
	private HBaseOperations hBaseOperations;
	
	public void sethBaseOperations(HBaseOperations hBaseOperations) {
		this.hBaseOperations = hBaseOperations;
	}

	@Override @ArroundHBase
	public String getLabel(String id) {
		hBaseOperations.lockRow(ApplicationLabelDO.class, id);
		try {
			ApplicationLabelDO applicationLabel = hBaseOperations.load(ApplicationLabelDO.class, id);
			if (applicationLabel == null) {
				applicationLabel = new ApplicationLabelDO();
				applicationLabel.setId(id);
				applicationLabel.setLabelAsInt((int)(new Date().getTime()/1000L - START_TIME_BASE));
			} else {
				applicationLabel.setLabelAsInt(applicationLabel.getLabelAsInt()+1);
			}
			hBaseOperations.save(applicationLabel);
			
			return intValue2String(applicationLabel.getLabelAsInt());
		} finally {
			hBaseOperations.unlockRow(ApplicationLabelDO.class, id);
		}
	}
	
	private static String intValue2String(int org) {

		int div = org;
		int amari = 0;
		StringBuilder sb = new StringBuilder();

		while (div != 0) {
			amari = div % 51;
			div = div / 51;
			sb.append(digits[amari]);
		}
		
		return StringUtils.rightPad(sb.toString(), 6, '0');
	}

	
	private final static char[] digits = {
		      '1' , '2' , '3' , '4' , '5' ,
		'6' , '7' , '8' , '9' , 'a' , 'b' ,
		'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
		'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
		'o' , 'p' , 'q' , 'r' , 's' , 't' ,
		'u' , 'v' , 'w' , 'x' , 'y' , 'z' ,
		'A' , 'B' , 'C' , 'D' , 'E' , 'F' ,
		'G' , 'H' , 'I' , 'J' , 'K' , 'L' ,
		'M' , 'N' , 'O' , 'P' , 'Q' , 'R' ,
		'S' , 'T' , 'U' , 'V' , 'W' , 'X' ,
		'Y' , 'Z'
    };
}
