package com.kickmogu.yodobashi.community.resource.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE})
@Retention(RUNTIME)
public @interface BackendWebServiceClientAware {

}
