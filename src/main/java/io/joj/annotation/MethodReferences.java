package io.joj.annotation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Defaults;
import com.google.common.collect.Iterables;

/**
 * @author findepi
 * @since Oct 24, 2016
 */
class MethodReferences {

	static <T> Method getMethod(Class<T> clazz, MethodReference<T, ?> methodReference) {
		checkArgument(clazz.isInterface(), "currently only interface introspection is supported");
		requireNonNull(methodReference, "methodReference");

		List<Method> calledMethods = new ArrayList<>();

		Object proxy = Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(),
				new Class<?>[] { clazz },
				(p, method, args) -> {
					calledMethods.add(method);
					return Defaults.defaultValue(method.getReturnType());
				});

		methodReference.invoke(clazz.cast(proxy));

		checkState(calledMethods.size() == 1, "MethodReference is not actually a method reference");
		return Iterables.getOnlyElement(calledMethods);
	}

}
