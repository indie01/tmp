package com.kickmogu.yodobashi.community.tdc2;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * メッセージを送信するメソッドに付与するアノテーション
 *
 *
 * @author nomura
 *
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface CreateCount {
	public static final int UNDEFINED = -1;
	double perCommunityUser() default UNDEFINED;
	int fixed() default UNDEFINED;
}
