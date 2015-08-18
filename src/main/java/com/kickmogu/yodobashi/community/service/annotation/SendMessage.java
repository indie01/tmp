package com.kickmogu.yodobashi.community.service.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.kickmogu.yodobashi.community.resource.domain.constants.AsyncMessageType;

/**
 * メッセージを送信するメソッドに付与するアノテーション
 *
 *
 * @author nomura
 *
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface SendMessage {
	Timing timing() default Timing.SYNC_NOW;
	AsyncMessageType asyncMessageType() default AsyncMessageType.INSTANCYSERVICE;
	long delayTime() default 0L;
	public enum Timing {
		SYNC_NOW, ASYNC_NOW, SYNC_AFTER_PROCESS, ASYNC_AFTER_PROCESS;
		public boolean isNow() {
			return this.equals(SYNC_NOW) || this.equals(ASYNC_NOW);
		}
		public boolean isAfterProcess() {
			return this.equals(SYNC_AFTER_PROCESS) || this.equals(ASYNC_AFTER_PROCESS);
		}
		public boolean isSync() {
			return this.equals(SYNC_NOW) || this.equals(SYNC_AFTER_PROCESS);
		}
		public boolean isAsync() {
			return this.equals(ASYNC_NOW) || this.equals(ASYNC_AFTER_PROCESS);
		}
	}
}
