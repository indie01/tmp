package com.kickmogu.yodobashi.community.resource.hbase2solr.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;




@Target({ TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface HBase2Solr {
	Class<?>[] value();
	int bulkSize() default 1000;
	
	@Target({ TYPE })
	@Retention(RUNTIME)
	@Documented
	@interface List {
		HBase2Solr[] value();
	}

}
