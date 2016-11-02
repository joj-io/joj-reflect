package io.joj.reflect;

/**
 * A {@link FunctionalInterface} that should be always implemented with a method reference.
 *
 * @author findepi
 * @since Oct 23, 2016
 */
@FunctionalInterface
public interface MethodReference0<Self, R> {
	R invokeOn(Self receiver);
}
