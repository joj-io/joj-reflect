package io.joj.reflect.annotation;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import io.joj.fluence.util.SupplierFluence;

/**
 * Holds single value for an {@link Annotation} implemented with {@link SyntheticAnnotationInvocationHandler}.
 *
 * @param <A>
 *            type of implemented {@link Annotation}
 * @author findepi
 * @since Oct 27, 2016
 */
class AnnotationValue<A> {
	private final Method getter;
	final Object value;

	private final Supplier<Function<Object, Integer>> hashCode;
	private final Supplier<BiFunction<Object, Object, Boolean>> equality;

	AnnotationValue(Method getter, Object value) {
		super();
		this.getter = requireNonNull(getter, "getter");
		this.value = requireNonNull(value, "value"); // annotation cannot have a null value

		this.hashCode = () -> hashCodeFunction(value.getClass());
		this.equality = SupplierFluence.memoize(() -> equalityFunction(value.getClass()));
	}

	Function<Object, Integer> getHashCodeFunction() {
		return hashCode.get();
	}

	/**
	 * @implSpec Conforms to {@link Annotation#hashCode()} contract.
	 * @implNote Nothing is cached here because {@link SyntheticAnnotationInvocationHandler#hashCodeImpl()} does its own
	 *           caching.
	 */
	int valueHashCode() {
		return (127 * getter.getName().hashCode())
				^ hashCode.get().apply(value);
	}

	/**
	 * Compares {@link #value} and {@code otherAnnotation}'s corresponding value for equality.
	 *
	 * @param otherValue
	 * @return
	 */
	boolean isValueEqualIn(A other) {
		Object otherValue;
		try {
			otherValue = getter.invoke(other);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		return equality.get().apply(value, otherValue);
	}

	private static Function<Object, Integer> hashCodeFunction(Class<?> valueClass) {
		if (!valueClass.isArray()) {
			return Object::hashCode;

		} else {
			Method hashCodeMethod;
			try {
				hashCodeMethod = Arrays.class.getMethod("hashCode", valueClass);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}

			return o -> {
				try {
					return (Integer) hashCodeMethod.invoke(null, o);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			};
		}
	}

	private static BiFunction<Object, Object, Boolean> equalityFunction(Class<?> valueClass) {
		if (!valueClass.isArray()) {
			return Object::equals;

		} else {
			Method equalsMethod;
			try {
				equalsMethod = Arrays.class.getMethod("equals", valueClass, valueClass);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}

			return (o1, o2) -> {
				try {
					return (Boolean) equalsMethod.invoke(null, o1, o2);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			};
		}
	}
}
