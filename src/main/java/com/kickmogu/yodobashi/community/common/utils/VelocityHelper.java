/**
 *
 */
package com.kickmogu.yodobashi.community.common.utils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.kickmogu.yodobashi.community.service.config.ServiceConfig;

/**
 * Velocity 用のヘルパーです。
 * @author kamiike
 *
 */
public class VelocityHelper {

	/**
	 * 日付を指定の形でフォーマットします。<br />
	 * 次のような形で使用してください。「$helper.dateFormat(${xxxxx}, 'yyyy-MM-dd')」
	 * @param date 日付
	 * @param format フォーマット
	 * @return 日付文字列
	 */
	public String dateFormat(Date date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}

	public String shortTitle(String src){
		return convertShortContent(src, 50, true);
	}

	public String shortBody(String src){
		return convertShortContent(src, 200, true);
	}

	public String shortTextTitle(String src){
		return convertShortContent(src, 50, false);
	}

	public String shortTextBody(String src){
		return convertShortContent(src, 200, false);
	}

	private String convertShortContent(String src, int length, boolean reverseEscape){
		if(StringUtils.isEmpty(src)) return "";
		String dest = StringUtil.stripTags(src);
		dest = dest.replaceAll("&nbsp;", " ");
		dest = StringEscapeUtils.unescapeHtml(dest);
		if(dest.length() > length){
			dest = dest.substring(0, length) + "...";
		}
		if (reverseEscape) {
			dest = StringEscapeUtils.escapeHtml(dest);
		}
		return dest;
	}
	
	public String nullToEmpty(String src){
		return StringUtils.isEmpty(src)?"":src;
	}

	public String addition(long src, long target){
		return String.valueOf(src + target);
	}

	
	public String getProfileImageUrl(String communityName){
		String encName = communityName;
		try {
			encName = freemarker.template.utility.StringUtil.URLEnc(communityName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return "http://" + ServiceConfig.INSTANCE.communityDomain + "/" + ServiceConfig.INSTANCE.communityContextPath + "/mail/image/" + encName + "/profile";
	}

	public String getSubmitImageUrl(String imageId){
		return "http://" + ServiceConfig.INSTANCE.communityDomain + "/" + ServiceConfig.INSTANCE.communityContextPath + "/mail/image/" + imageId + "/submit";
	}

	public String urlEncode(String src){
		
		String dest = src;
		try{
			dest = freemarker.template.utility.StringUtil.URLEnc(dest, "UTF-8");
		}catch (Exception e) {
			dest = src;
		}
		return dest;
	}

	public String getCommunityProductUrl(String sku) {
		return "http://" + ServiceConfig.INSTANCE.communityDomain + "/" + ServiceConfig.INSTANCE.communityContextPath + String.format(ServiceConfig.INSTANCE.communityProductPage, sku);
	}

	public static String diffRoundProgressDateMessage( Date basedate, Date targetdate ){
//		Date basedate;
//		Date targetdate;
//		try{
//			basedate	= (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse( strbasedate	);
//			targetdate	= (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse( strtargetdate);
//		}catch(Exception e){
//			return "";
//		}
		long progressSec = targetdate.getTime() - basedate.getTime();
		progressSec /= 1000;
		String label = "";
		if( progressSec < 60*60*24 ){
			label = "24時間以内";
		}
		if( !"".equals(label) ){
			return label;
		}
		if( progressSec < 60*60*24*30 ){
			label = (progressSec/60/60/24)+"日時点";
		}else if( progressSec < 60*60*24*30*12 ){
			label = "約"+(progressSec/60/60/24/30)+"カ月時点";
		}else if( progressSec >= 60*60*24*30*12 ){
			label = "約"+(progressSec/60/60/24/30/12)+"年時点";
		}
		return label;
	}

}
