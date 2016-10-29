package io.joj.reflect.annotation;

import static io.joj.reflect.annotation.internal.Check.checkArgument;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

import io.joj.fluence.util.SupplierFluence;

/**
 * @author findepi
 * @since Oct 28, 2016
 */
class ArrayAnnotationValue extends AnnotationValue {

	/*
	 * Either object array or primitive array.
	 */
	private final Object array;

	private final Supplier<Method> arraysEqualsMethod;

	public ArrayAnnotationValue(Method getter, Object array) {
		super(getter);
		checkArgument(getter.getReturnType().isArray(), "expected array type");
		this.array = cloneArray(checkValue(getter.getReturnType(), array));

		this.arraysEqualsMethod = SupplierFluence.memoize(() -> arraysEqualsMethod(array.getClass()));
	}

	/**
	 * Clones the array.
	 *
	 * @implSpec Does shallow clone of the array. In annotations, array values can never be multidimensional, and they
	 *           can contain only immutable elements, so shallow clone is sufficient.
	 */
	private static Object cloneArray(Object array) {
		int length = Array.getLength(array);
		Object copy = Array.newInstance(array.getClass().getComponentType(), length);
		System.arraycopy(array, 0, copy, 0, length);
		return copy;
	}

	/**
	 * @implNote Nothing is cached here because {@link SyntheticAnnotationInvocationHandler#hashCodeImpl()} does its own
	 *           caching.
	 */
	@Override
	int hashCodeValue() {
		try {
			return (Integer) Arrays.class.getMethod("hashCode", arrayBaseClass(array.getClass()))
					.invoke(null, array);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	boolean isValueEqual(Object otherValue) {
		try {
			return (Boolean) arraysEqualsMethod.get()
					// otherValue is extracted using getter, so it's a known array, and of the right type
					.invoke(null, array, otherValue);

		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object getValue() {
		return cloneArray(array);
	}

	@Override
	public String valueToString() {
		try {
			return (String) Arrays.class.getMethod("toString", arrayBaseClass(array.getClass()))
					.invoke(null, array);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private static Class<?> arrayBaseClass(Class<?> arrayClass) {
		if (Object[].class.isAssignableFrom(arrayClass)) {
			return Object[].class;
		} else {
			// primitives
			return arrayClass;
		}
	}

	private static Method arraysEqualsMethod(Class<?> arrayClass) {
		Class<?> arrayBaseClass = arrayBaseClass(arrayClass);
		try {
			return Arrays.class.getMethod("equals", arrayBaseClass, arrayBaseClass);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

}
