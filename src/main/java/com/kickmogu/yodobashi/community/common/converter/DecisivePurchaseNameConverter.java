package com.kickmogu.yodobashi.community.common.converter;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.kickmogu.lib.core.converter.Constraint;
import com.kickmogu.lib.core.converter.impl.TraversePropertiesConverterTemplete;
import com.kickmogu.lib.core.utils.StringUtil;
import com.kickmogu.yodobashi.community.common.converter.DecisivePurchaseNameConverter.DecisivePurchaseNameConverterImpl;


@Documented
@Constraint(convertWith = DecisivePurchaseNameConverterImpl.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface DecisivePurchaseNameConverter {
	String reversible() default "false";
	Class<?>[] groupsAtoB() default { };
	Class<?>[] groupsBtoA() default { };

	static public class DecisivePurchaseNameConverterImpl extends TraversePropertiesConverterTemplete<DecisivePurchaseNameConverter, String> {
		
		public DecisivePurchaseNameConverterImpl() {
			super(String.class);
		}
		@Override
		protected String doConvertAtoB(String bean, Object parent, String propName) {
			/**
			 * 購入の決め手名称を標準化します。
			 * 両端スペースは取り除き、カタカナは全角に、スペース英数字記号は半角に変換します。
			 */
			String target = (String)bean;
			target = target.replace("　", " ").trim();
			target = StringUtil.hankakuKatakanaToZenkakuKatakana(target);
			target = StringUtil.zenkakuAlphabetToHankakuAlphabet(target);
			target = StringUtil.zenkakuDigitToHankakuDigit(target);
			target = StringUtil.zenkakuSignToHankakuSign(target);
			return 	target;
		}

		@Override
		protected String doConvertBtoA(String bean, Object parent, String propName) {
			return 	(String)bean;
		}

	}
}
