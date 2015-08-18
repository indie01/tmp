package com.kickmogu.yodobashi.community.resource.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({FIELD,METHOD})
@Retention(RUNTIME)
public @interface BackendWebServiceClient {
	String endPointUrlPropertyKey();
	String usernamePropertyKey() default "";
	String passwordPropertyKey() default "";
	long connectionTimeout() default 30000L;
	long receiveTimeout() default 60000L;
}
