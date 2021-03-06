package io.joj.reflect.annotation;

import static io.joj.reflect.annotation.internal.Check.checkArgument;

import java.lang.reflect.Method;

import io.joj.reflect.annotation.internal.Primitive;

/**
 * @author findepi
 * @since Oct 28, 2016
 */
class RegularAnnotationValue extends AnnotationValue {

	private final Object value;

	public RegularAnnotationValue(Method getter, Object value) {
		super(getter);
		checkArgument(!getter.getReturnType().isArray(), "expected non-array type");
		this.value = checkValue(getter, value);
	}

	private static Object checkValue(Method getter, Object value) {
		Class<?> expectedType = Primitive.primitiveToWrapper
				.getOrDefault(getter.getReturnType(), getter.getReturnType());
		return checkValue(expectedType, value);
	}

	@Override
	int hashCodeValue() {
		return value.hashCode();
	}

	@Override
	boolean isValueEqual(Object otherValue) {
		return value.equals(otherValue);
	}

	/**
	 * @implSpec Does not copy returned value. Annotations values, except arrays, are immutable.
	 */
	public Object getValue() {
		return value;
	}

	@Override
	public String valueToString() {
		return getValue().toString();
	}
}
