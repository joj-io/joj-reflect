package io.joj.reflect.annotation;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Holds single value for an {@link Annotation} implemented with {@link SyntheticAnnotationInvocationHandler}.
 *
 * @param <A>
 *            type of implemented {@link Annotation}
 * @author findepi
 * @since Oct 27, 2016
 */
abstract class AnnotationValue {

	public static AnnotationValue valueOf(Method getter, Object value) {
		if (!requireNonNull(getter, "getter").getReturnType().isArray()) {
			return new RegularAnnotationValue(getter, value);
		} else {
			return new ArrayAnnotationValue(getter, value);
		}
	}

	protected static Object checkValue(Class<?> expectedType, Object value) {
		requireNonNull(value, "annotation value cannot be null");
		if (!expectedType.isInstance(value)) {
			throw new ClassCastException(format(
					"expected %s, got %s", expectedType, value.getClass()));
		}

		return value;
	}

	protected final Method getter;

	// private final Supplier<Function<Object, Integer>> hashCode;
	// private final Supplier<BiFunction<Object, Object, Boolean>> equality;

	AnnotationValue(Method getter) {
		super();
		this.getter = requireNonNull(getter, "getter");
	}

	/**
	 * @implSpec Conforms to {@link Annotation#hashCode()} contract.
	 * @implNote Nothing is cached here because {@link SyntheticAnnotationInvocationHandler#hashCodeImpl()} does its own
	 *           caching.
	 */
	public int hashCodeNameAndValue() {
		return (127 * getter.getName().hashCode()) ^ hashCodeValue();
	}

	abstract int hashCodeValue();

	/**
	 * Compares {@link #value} and {@code otherAnnotation}'s corresponding value for equality.
	 *
	 * @param otherValue
	 * @return
	 */
	public boolean isValueEqualIn(Object other) {
		Object otherValue;
		try {
			otherValue = getter.invoke(other);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		return isValueEqual(otherValue);
	}

	abstract boolean isValueEqual(Object otherValue);

	public abstract Object getValue();

	public abstract String valueToString();
}
