package io.joj.reflect;

import static io.joj.reflect.annotation.internal.Check.checkArgument;
import static io.joj.reflect.annotation.internal.Check.checkState;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import io.joj.reflect.annotation.internal.Primitive;

/**
 * @author findepi
 * @since Oct 24, 2016
 */
public class MethodReferences {

	public static <T> Method getMethod(Class<T> clazz, MethodReference0<T> methodReference) {
		checkArgument(clazz.isInterface(), "currently only interface introspection is supported");
		requireNonNull(methodReference, "methodReference");

		List<Method> calledMethods = new ArrayList<>();

		Object proxy = Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(),
				new Class<?>[] { clazz },
				(p, method, args) -> {
					calledMethods.add(method);
					return Primitive.primitiveToDefault.get(method.getReturnType());
				});

		methodReference.invokeOn(clazz.cast(proxy));

		checkState(calledMethods.size() == 1, "MethodReference is not actually a method reference");
		return calledMethods.get(0);
	}

}
