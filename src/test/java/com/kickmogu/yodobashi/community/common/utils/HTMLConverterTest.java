/**
 *
 */
package com.kickmogu.yodobashi.community.common.utils;

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import com.kickmogu.yodobashi.community.common.utils.HTMLConverter.ImageUrlInfo;

/**
 * HTMLコンバーターのテストクラスです。
 * @author kamiike
 *
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class HTMLConverterTest {

	/**
	 * HTMLのサニタイズ、及び画像URL情報の抜き出しを検証します。
	 */
	@Test
	public void testSanitizeHtml() {
		HTMLConverter converter = new HTMLConverter();
		converter.setTemporaryImageUrlPattern(
				Pattern.compile("^/community/download/image/[\\da-zA-Z\\-\\.]+$"));
		converter.setUploadedImageUrlPattern(
				Pattern.compile("^/community/download/image/r/[\\da-zA-Z\\-\\.]+$"));

		String uploadedImageUrl = "/community/download/image/r/c6949904-3ff3-40c1-80b8-ad02a60beac0.jpeg";
		String tempImageId = "c6949904-3ff3-40c1-80b8-ad02a60beac1";
		String tempImageUrl = "/community/download/image/" + tempImageId;

		StringBuilder html = new StringBuilder();
		html.append("<img src=\"" + uploadedImageUrl + "\">");
		html.append("<img src=\"" + tempImageUrl + "\">");
		html.append("<script type=\"text/javascript\">alert('テスト')</script>");
		html.append("<a hrEf=\"javascript:alert(/XSS3/)\">XSS3</a>");
		html.append("<div Onclick=\"alert(/XSS4/)\">XSS4</div>");
		html.append("<div style=\"left:expression( alert('xss') )\">XSS5</div>");
		html.append("<div style=\"color:red;left:expression( alert('xss') )\">XSS6</div>");
		html.append("<span style=\"color: rgb(255, 0, 0);\">XSS7</span>");
		html.append("<div style=\" color : red \">XSS8</div>");

		StringBuilder assertHtml = new StringBuilder();
		assertHtml.append("<img src=\"" + uploadedImageUrl + "\">");
		assertHtml.append("<img src=\"" + tempImageUrl + "\">");
		assertHtml.append("&lt;script type=\"text/javascript\"&gt;alert('テスト')&lt;/script&gt;");
		assertHtml.append("<a>XSS3</a>");
		assertHtml.append("<div>XSS4</div>");
		assertHtml.append("<div>XSS5</div>");
		assertHtml.append("<div>XSS6</div>");
		assertHtml.append("<span style=\"color: rgb(255, 0, 0);\">XSS7</span>");
		assertHtml.append("<div style=\" color : red \">XSS8</div>");

		String sanitizedHtml = converter.sanitizeHtml(html.toString());
		assertEquals(assertHtml.toString(), sanitizedHtml);

		ImageUrlInfo imageUrlInfo1 = converter.getExistsImageUrlInfoList().get(0);
		assertEquals(false, imageUrlInfo1.isTemporary());
		assertEquals(uploadedImageUrl, imageUrlInfo1.getImageUrl());
		ImageUrlInfo imageUrlInfo2 = converter.getExistsImageUrlInfoList().get(1);
		assertEquals(true, imageUrlInfo2.isTemporary());
		assertEquals(tempImageId, imageUrlInfo2.getImageId());

	}
	
	@Test
	public void testSanitizeHtml01() {
		StringBuilder html = new StringBuilder();
		html.append("<style>");
		html.append("BODY{left:expression(alert(/XSS1/));}");
		html.append("</style>");
		HTMLConverter converter = new HTMLConverter();
		converter.setTemporaryImageUrlPattern(
				Pattern.compile("^/community/download/image/[\\da-zA-Z\\-\\.]+$"));
		converter.setUploadedImageUrlPattern(
				Pattern.compile("^/community/download/image/r/[\\da-zA-Z\\-\\.]+$"));
		String sanitizedHtml = converter.sanitizeHtml(html.toString());
		
		System.out.println(sanitizedHtml);
	}

	@Test
	public void testSanitizeHtml02() {
		StringBuilder html = new StringBuilder();
		html.append("<div style=\"left:\0065xpression(alert(/XSS2/))\">XSS2</div>");
		HTMLConverter converter = new HTMLConverter();
		converter.setTemporaryImageUrlPattern(
				Pattern.compile("^/community/download/image/[\\da-zA-Z\\-\\.]+$"));
		converter.setUploadedImageUrlPattern(
				Pattern.compile("^/community/download/image/r/[\\da-zA-Z\\-\\.]+$"));
		String sanitizedHtml = converter.sanitizeHtml(html.toString());
		
		System.out.println(sanitizedHtml);
	}
	
	@Test
	public void testSanitizeHtml04() {
		StringBuilder html = new StringBuilder();
		html.append("<div style=\"color:\0065xpression(alert(/XSS2/))\">XSS2</div>");
		HTMLConverter converter = new HTMLConverter();
		converter.setTemporaryImageUrlPattern(
				Pattern.compile("^/community/download/image/[\\da-zA-Z\\-\\.]+$"));
		converter.setUploadedImageUrlPattern(
				Pattern.compile("^/community/download/image/r/[\\da-zA-Z\\-\\.]+$"));
		String sanitizedHtml = converter.sanitizeHtml(html.toString());
		
		System.out.println(sanitizedHtml);
	}
	
	@Test
	public void testSanitizeHtml05() {
		StringBuilder html = new StringBuilder();
		html.append("<div style=\"color:#006500;\">XSS2</div>");
		HTMLConverter converter = new HTMLConverter();
		converter.setTemporaryImageUrlPattern(
				Pattern.compile("^/community/download/image/[\\da-zA-Z\\-\\.]+$"));
		converter.setUploadedImageUrlPattern(
				Pattern.compile("^/community/download/image/r/[\\da-zA-Z\\-\\.]+$"));
		String sanitizedHtml = converter.sanitizeHtml(html.toString());
		
		System.out.println(sanitizedHtml);
	}
	
	@Test
	public void testSanitizeHtml06() {
		StringBuilder html = new StringBuilder();
		html.append("<p>sdfdsfsd<br>");
		html.append("<br>");
		html.append("<strike><u><em><strong><span style=\"color:#b22222;\">sdf</span><br>");
		html.append("sdf<br>");
		html.append("sd<br>");
		html.append("f<br>");
		html.append("<span style=\"color:#99cc99;\">sdf</span></strong></em></u></strike><br>");
		html.append("sdf</p>");
		
		HTMLConverter converter = new HTMLConverter();
		converter.setTemporaryImageUrlPattern(
				Pattern.compile("^/community/download/image/[\\da-zA-Z\\-\\.]+$"));
		converter.setUploadedImageUrlPattern(
				Pattern.compile("^/community/download/image/r/[\\da-zA-Z\\-\\.]+$"));
		String sanitizedHtml = converter.sanitizeHtml(html.toString());
		
		System.out.println(sanitizedHtml);
	}
}
