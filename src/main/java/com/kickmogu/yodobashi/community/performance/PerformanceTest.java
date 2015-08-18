package com.kickmogu.yodobashi.community.performance;

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
@Target({METHOD})
@Retention(RUNTIME)
public @interface PerformanceTest {
	Type type();
	Frequency frequency();
	String frequencyComment() default "";
	String[] refClassNames() default {};
	public static enum Type {
		UPDATE, SELECT;
	}
	public static enum Frequency {
		SUPER_HIGH, HIGH, MEDIUM, LOW, RARE, NONE;
	}
}
