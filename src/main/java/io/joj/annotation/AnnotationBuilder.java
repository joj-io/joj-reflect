package io.joj.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Main entry point for building synthetic {@link Annotation} instances.
 * 
 * @author findepi
 */
public class AnnotationBuilder {
	private AnnotationBuilder() {
	}

	public static <A extends Annotation> A buildFromMap(Class<A> annotationClass, Map<String, ?> values) {
		Object syntheticAnnotation = Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(),
				new Class<?>[] { annotationClass },
				new SyntheticAnnotationInvocationHandler(annotationClass, values));

		return annotationClass.cast(syntheticAnnotation);
	}
}
