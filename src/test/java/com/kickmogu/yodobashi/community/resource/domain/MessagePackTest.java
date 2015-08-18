package com.kickmogu.yodobashi.community.resource.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;

@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/serviceContext.xml")
public class MessagePackTest {

	
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;
	
	@Test
	public void test() {
//		SocialMediaSettingDO socialMediaSetting = new SocialMediaSettingDO();
//		SocialMediaSettingDO.PublicSetting publicSetting = new SocialMediaSettingDO.PublicSetting();
//		publicSetting.setType(SocialMediaPublicSettingType.ANSWER);
//		publicSetting.setValue(true);
//		socialMediaSetting.setPublicSettings(new ArrayList<SocialMediaSettingDO.PublicSetting>());
//		socialMediaSetting.getPublicSettings().add(publicSetting);
//		hBaseOperations.save(socialMediaSetting);
//		
//		socialMediaSetting = hBaseOperations.load(SocialMediaSettingDO.class, socialMediaSetting.getSocialMediaSettingId());
//		assertEquals(SocialMediaPublicSettingType.ANSWER, socialMediaSetting.getPublicSettings().get(0).getType());
//		assertEquals(true, socialMediaSetting.getPublicSettings().get(0).isValue());
	}
}
