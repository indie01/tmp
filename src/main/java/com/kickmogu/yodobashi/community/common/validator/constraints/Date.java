package com.kickmogu.yodobashi.community.common.validator.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import com.kickmogu.lib.core.validator.constraints.Relation;
import com.kickmogu.yodobashi.community.common.validator.constraints.Date.DateValidator;
import com.kickmogu.yodobashi.community.resource.domain.DateFields;

@Documented
@Constraint(validatedBy = DateValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface Date {
	String message() default "{com.kickmogu.yodobashi.community.common.validator.constraints.DateFields.message}";
	Class<?>[] groups() default { };
	Class<? extends Payload>[] payload() default { };
	Relation[] ifOnly() default {};
	String value() default "";

	static class DateValidator implements ConstraintValidator<Date, DateFields> {

		public void initialize(Date constraintAnnotation) {
		}

		public boolean isValid(DateFields value, ConstraintValidatorContext context) {
			if (value == null) return true;
			return value.convertableToDate();
		}

	}
}
