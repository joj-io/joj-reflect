package io.joj.reflect.annotation.internal;

import static java.lang.String.format;

/**
 * To avoid dependency on Guava, have some polyfill for it.
 *
 * @author findepi
 * @since Oct 29, 2016
 */
public class Check {
	public static void checkArgument(boolean condition, String messageFormat, Object... messageFormatArgs) {
		if (!condition) {
			throw new IllegalArgumentException(format(messageFormat, messageFormatArgs));
		}
	}

	public static void checkState(boolean condition, String messageFormat, Object... messageFormatArgs) {
		if (!condition) {
			throw new IllegalStateException(format(messageFormat, messageFormatArgs));
		}
	}
}
