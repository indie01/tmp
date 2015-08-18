package com.kickmogu.yodobashi.community.resource.cache;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.kickmogu.lib.core.constants.LabeledEnum;
import com.kickmogu.lib.core.utils.Asserts;



public class MethodArgument implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4147064642071000115L;
	
	private static final Class<?>[] ALLOWABLE_TYPES = new Class<?>[]{
		String.class,
		boolean.class,
		Boolean.class,
		int.class,
		Integer.class,
		long.class,
		Long.class,
		float.class,
		Float.class,
		double.class,
		Double.class,
		LabeledEnum.class,
		Date.class,
		Class.class,
		MethodCacheKey.class,
		Enum.class
	};

	private Object[] args;
	
	public MethodArgument(Method method, Object[] args) {
		validateAllowableByType(method);
		this.args = args;
	}
	
	public static void validateAllowableByType(Method method) {
		Type[] types = method.getGenericParameterTypes();
		for (int i = 0 ; i < types.length ; i++) {
			validateAllowableByType(types[i], true);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void validateAllowableByType(Type type, boolean allowCotainer) {
		Class clazz = null;
		if (type instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType)type;
			clazz = (Class)ptype.getRawType();
			if (clazz.isArray()) {
				Asserts.isTrue(allowCotainer);
				validateAllowableByType(clazz.getComponentType(), false);
				return;
			} else if (List.class.isAssignableFrom(clazz)) {
				Asserts.isTrue(allowCotainer);
				validateAllowableByType(ptype.getActualTypeArguments()[0], false);
				return;
			} else if (Set.class.isAssignableFrom(clazz)) {
				Asserts.isTrue(allowCotainer);
				validateAllowableByType(ptype.getActualTypeArguments()[0], false);
				return;
			} else if (Map.class.isAssignableFrom(clazz)) {
				Asserts.isTrue(allowCotainer);
				validateAllowableByType(ptype.getActualTypeArguments()[0], false);
				validateAllowableByType(ptype.getActualTypeArguments()[1], false);
				return;
			} 
		} else {
			clazz = (Class)type;
		}

		for (Class allowableClass:ALLOWABLE_TYPES) {
			if (allowableClass.isAssignableFrom(ClassUtils.primitiveToWrapper(clazz))) return;
		}
		throw new IllegalArgumentException(clazz.getName());
	}


	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 37);
		for (Object arg:args) hashCodeBuilder.append(arg);
		return hashCodeBuilder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof MethodArgument)) return false;
		MethodArgument other = (MethodArgument)obj;
		if (args.length == 0 && other.args.length == 0) return true;
		EqualsBuilder equalsBuilder = new EqualsBuilder();
		for (int i = 0 ; i < args.length ; i++) {
			equalsBuilder.append(args[i], other.args[i]);
		}
		return equalsBuilder.isEquals();
	}

	@Override
	public String toString() {
		if (args.length == 0) return "MethodArgument[args.len0]";
		ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		for (int i = 0 ; i < args.length; i++) {
			toStringBuilder.append(args[i]);
		}
		return toStringBuilder.toString();
	}

}
