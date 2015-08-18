/**
 *
 */
package com.kickmogu.yodobashi.community.common.utils;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * @author kamiike
 *
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class StringUtilTest {

	@Test
	public void testStorageStringArray() throws UnsupportedEncodingException {
		String[] pattern1 = StringUtil.storageStringArray("髙");
		assertEquals(1, pattern1.length);
		assertEquals(1, pattern1[0].length());
		StringBuilder input2 = new StringBuilder();
		for (int i = 0; i < 200; i++) {
			if ((i % 2) == 0) {
				input2.append("髙");
			} else {
				input2.append("~");
			}
		}
		String[] pattern2 = StringUtil.storageStringArray(input2.toString());
		assertEquals(2, pattern2.length);
		assertEquals(255, pattern2[0].getBytes("Windows-31J").length);
		assertEquals(45, pattern2[1].getBytes("Windows-31J").length);
		StringBuilder input3 = new StringBuilder();
		for (int i = 0; i < 200; i++) {
			input3.append("髙");
		}
		String[] pattern3 = StringUtil.storageStringArray(input3.toString());
		assertEquals(2, pattern3.length);
		assertEquals(254, pattern3[0].getBytes("Windows-31J").length);
		assertEquals(146, pattern3[1].getBytes("Windows-31J").length);
	}
}
