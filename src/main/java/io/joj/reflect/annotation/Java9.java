package io.joj.reflect.annotation;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Forward compatibility layer with Java 9. Java 9 adds new methods to existing JDK classes, making certain approaches
 * "more idiomatic" than others. Let's use them even now, in Java 8.
 * 
 * @author findepi
 * @since Oct 23, 2016
 */
class Java9 {

	/**
	 * Equivalent to {@code primary.or(secondary)}.
	 */
	public static <T> Optional<T> orOptionals(Optional<T> primary, Supplier<Optional<T>> secondary) {
		requireNonNull(primary, "primary");
		requireNonNull(secondary, "secondary");
		
		if (primary.isPresent()) {
			return primary;
		}
		return requireNonNull(secondary.get(), "secondary.get()");
	}

}
