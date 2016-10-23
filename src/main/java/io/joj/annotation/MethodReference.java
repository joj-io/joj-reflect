package io.joj.annotation;

/**
 * A {@link FunctionalInterface} that should be always implemented with a method reference.
 * 
 * @author findepi
 * @since Oct 23, 2016
 */
@FunctionalInterface
public interface MethodReference<T, R> {
	R invoke(T argument);
}
