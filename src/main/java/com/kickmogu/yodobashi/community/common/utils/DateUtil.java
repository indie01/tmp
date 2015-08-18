package com.kickmogu.yodobashi.community.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {


	/**
	 * 経過日数を算出して返します。
	 * @return 経過日数
	 */
	public static int getElapsedDays(Date date) {
		return getElapsedDays(date, new Date());
	}

	/**
	 * 経過日数を算出して返します。
	 * @return 経過日数
	 */
	public static int getElapsedDays(Date date, Date now) {
		if (date == null) {
			throw new IllegalArgumentException("date required.");
		}
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			Date base = formatter.parse(formatter.format(date));
			Date today = formatter.parse(formatter.format(now));
			long elapsedDays = (today.getTime() - base.getTime()) / (1000 * 60 * 60 * 24);
			return (int) elapsedDays + 1;
		} catch (ParseException e) {
			throw new IllegalStateException();
		}
	}

	public static boolean matchTerm(Date start, Date end, Date target) {
		if (start == null || end == null || target == null) {
			return false;
		}
		if (start.compareTo(target) > 0
				|| end.compareTo(target) <= 0) {
			return false;
		}
		return true;
	}

}
