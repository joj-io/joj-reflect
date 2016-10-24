package io.joj.reflect.annotation;

import java.util.function.Supplier;

/**
 * @author findepi
 * @since Oct 25, 2016
 */
class Suppliers {
	private Suppliers() {
	}

	/**
	 * {@code com.google.common.base.Suppliers.memoize(Supplier<T>)} for JDK {@link Supplier}
	 */
	public static <T> Supplier<T> memoize(Supplier<T> supplier) {
		// crude ...
		return com.google.common.base.Suppliers.memoize(supplier::get)::get;
	}
}
