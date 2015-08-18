package com.kickmogu.yodobashi.community.resource.config;

import java.util.Date;

import org.msgpack.MessagePack;
import org.msgpack.template.DateTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.constants.LabeledEnum;
import com.kickmogu.lib.core.id.DomainObjectIDGenerator;
import com.kickmogu.lib.core.id.IDPartsDomainObjectIDGenerator;
import com.kickmogu.lib.core.msgpack.ClassTemplate;
import com.kickmogu.lib.core.msgpack.LabeledEnumTemplate;
import com.kickmogu.lib.core.time.AdjustableSystemTimeImpl;
import com.kickmogu.lib.core.time.DefaultSystemTimeImpl;
import com.kickmogu.lib.core.time.SystemTime;
import com.kickmogu.lib.core.utils.Reflections;
import com.kickmogu.yodobashi.community.resource.aop.CommunityHBaseProcessContextHandler;
import com.kickmogu.yodobashi.community.resource.domain.BaseWithTimestampDO;


public class ResourceContext  {

	@Autowired
	private CommunityHBaseProcessContextHandler processContextHandler;

	@Autowired
	private ResourceConfig resourceConfig;
	
	@Bean
	public SystemTime systemTime() {
		return resourceConfig.timeAdjustable ? new AdjustableSystemTimeImpl() : new DefaultSystemTimeImpl();
	}
	
	@Bean
	public TimestampHolder timestampHolder() {
		return processContextHandler;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	public MessagePack messagePack() {
		MessagePack messagePack = new MessagePack();
		messagePack.register(Class.class, ClassTemplate.instance);
		messagePack.register(Date.class, DateTemplate.getInstance());
		for (Class<LabeledEnum> labeledEnumClass:Reflections.getClassesByPackage(BaseWithTimestampDO.class.getPackage(), LabeledEnum.class)) {
			messagePack.register(labeledEnumClass, new LabeledEnumTemplate(labeledEnumClass));
		}
		return messagePack;
	}
	
	@Bean(name="idPartsGenerator") @Qualifier("idPartsGenerator")
	public DomainObjectIDGenerator<String> idPartsGenerator() {
		return new IDPartsDomainObjectIDGenerator();
	}

}
