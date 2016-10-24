package io.joj.reflect.annotation;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.pcollections.HashPMap;
import org.pcollections.IntTreePMap;
import org.pcollections.PMap;

import io.joj.reflect.MethodReference0;
import io.joj.reflect.MethodReferences;

/**
 * Main entry point for building synthetic {@link Annotation} instances.
 * 
 * @author findepi
 */
public class AnnotationBuilder {
	private AnnotationBuilder() {
	}

	/**
	 * Low level, type-unsafe, annotation synthesizer. Whenever possible, use {@link #builderFor(Class)} instead.
	 * 
	 * @param annotationClass
	 *            desired annotation type
	 * @param values
	 *            map from annotation method name to value
	 * @return synthesized annotation
	 */
	public static <A extends Annotation> A buildFromMap(Class<A> annotationClass, Map<String, ?> values) {
		Object syntheticAnnotation = Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(),
				new Class<?>[] { annotationClass },
				new SyntheticAnnotationInvocationHandler<A>(annotationClass, values));

		return annotationClass.cast(syntheticAnnotation);
	}

	/**
	 * Returns type-safe, reusable (functional) builder for {@code A}. {@code A} instances produced by this builder
	 * conform to general contract of {@link Annotation}-s.
	 */
	public static <A extends Annotation> Builder<A> builderFor(Class<A> annotationClass) {
		return new Builder<>(annotationClass);
	}

	public static final class Builder<A extends Annotation> {
		private final Class<A> clazz;
		private final PMap<String, Object> values;

		private Builder(Class<A> clazz) {
			this(clazz, HashPMap.empty(IntTreePMap.empty()));
		}

		private Builder(Class<A> clazz, PMap<String, Object> values) {
			this.clazz = requireNonNull(clazz, "clazz");
			this.values = requireNonNull(values, "values");
		}

		public <R> OngoingMethodSpec<R> with(MethodReference0<A> methodReference) {
			String specedMethodName = MethodReferences.getMethod(clazz, methodReference).getName();
			return new OngoingMethodSpec<>(specedMethodName);
		}

		public A build() {
			return buildFromMap(clazz, values);
		}

		public final class OngoingMethodSpec<R> {
			private final String specedMethodName;

			public OngoingMethodSpec(String specedMethodName) {
				this.specedMethodName = requireNonNull(specedMethodName, "specedMethodName");
			}

			public Builder<A> returning(R value) {
				return new Builder<>(clazz, values.plus(specedMethodName, value));
			}
		}
	}
}
