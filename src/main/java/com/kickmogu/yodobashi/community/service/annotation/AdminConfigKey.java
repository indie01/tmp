package com.kickmogu.yodobashi.community.service.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * AdminConfigDOに登録されているキーに付与するアノテーション
 *
 *
 * @author imaizumi
 *
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface AdminConfigKey {
}
