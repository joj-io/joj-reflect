package io.joj.reflect.annotation;

import static io.joj.reflect.annotation.SyntheticAnnotationCompleteness.REQUIRE_COMPLETE;
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
 * <p>
 * Usually one does not need to create annotation instances at run-time. Annotations were designed, and are mainly used,
 * to annotate source code and as such are generally determined at compile time. However, at times, in dark corners of
 * software, one can face desire, or even urgent need, to make up an annotation instance that is not bound to source
 * element or, even worse, cannot be determined at compile time. {@link AnnotationBuilder} is here to help!
 * <p>
 * Incidentally, annotations are designed as immutable, type-safe, null-safe (no {@code null}-s), value-based (in terms
 * of {@link Object#hashCode()} and {@link Object#equals(Object)}) data structures. Perfect!... if only we could
 * instantiate them at run-time! And now we can.
 *
 * @author findepi
 */
public class AnnotationBuilder {
	private AnnotationBuilder() {
	}

	/**
	 * Low level, type-unsafe, annotation synthesizer taking target annotation type and values. Whenever possible, use
	 * {@link #builderFor(Class)} instead.
	 *
	 * @param annotationClass
	 *            desired annotation type
	 * @param values
	 *            map from annotation method name to value
	 * @return synthesized annotation that is complete and correct implementation of {@code annotationClass}
	 */
	public static <A extends Annotation> A buildFromMap(Class<A> annotationClass, Map<String, ?> values) {
		return buildFromMap(annotationClass, values, REQUIRE_COMPLETE);
	}

	/**
	 * Low level, type-unsafe, annotation synthesizer taking target annotation type and values, which allows for
	 * optional incomplete implementation. Incomplete implementation is an implementation that does not have a value for
	 * at least one annotation method that has no default. Incomplete implementation does not satisfy general contract
	 * for {@link Annotation#hashCode()} nor {@link Annotation#equals(Object)}.
	 * <p>
	 * Whenever possible, use {@link #builderFor(Class)} instead.
	 *
	 * @param annotationClass
	 *            desired annotation type
	 * @param values
	 *            map from annotation method name to value
	 * @param completeness
	 *            expected annotation completeness
	 * @return synthesized annotation. When {@code completeness} is
	 *         {@link SyntheticAnnotationCompleteness#REQUIRE_COMPLETE}, the returned annotation is complete and correct
	 *         implementation of {@code annotationClass}.
	 */
	public static <A extends Annotation> A buildFromMap(Class<A> annotationClass, Map<String, ?> values,
			SyntheticAnnotationCompleteness completeness) {

		Object syntheticAnnotation = Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(),
				new Class<?>[] { annotationClass },
				new SyntheticAnnotationInvocationHandler<A>(annotationClass, values, completeness));

		return annotationClass.cast(syntheticAnnotation);
	}

	/**
	 * Returns type-safe, reusable (functional) builder for {@code A}. Unlike dynamic annotation implementation starring
	 * here and there, instances produced by this builder conform to general contract of {@link Annotation}-s
	 * (especially {@link Annotation#hashCode()}, {@link Annotation#equals(Object)}).
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * <code>
	 * import javax.validation.constraints.Pattern;
	 *
	 * Pattern generated = AnnotationBuilder.builderFor(Pattern.class)
	 *   .with(Pattern::regexp).returning("^a+.*end$")
	 *   .with(Pattern::message).returning("Value should look funny, begin with 'a' and end with 'end'.")
	 *   .build();
	 * </code>
	 * </pre>
	 */
	public static <A extends Annotation> Builder<A> builderFor(Class<A> annotationClass) {
		return new Builder<>(annotationClass);
	}

	public static final class Builder<A extends Annotation> {
		private final Class<A> clazz;
		private final PMap<String, Object> values;
		private final SyntheticAnnotationCompleteness completeness;

		private Builder(Class<A> clazz) {
			this(clazz, HashPMap.empty(IntTreePMap.empty()), REQUIRE_COMPLETE);
		}

		private Builder(Class<A> clazz, PMap<String, Object> values, SyntheticAnnotationCompleteness completeness) {
			this.clazz = requireNonNull(clazz, "clazz");
			this.values = requireNonNull(values, "values");
			this.completeness = requireNonNull(completeness, "completeness");
		}

		public <R> OngoingMethodSpec<R> with(MethodReference0<A, R> methodReference) {
			String specedMethodName = MethodReferences.getMethod(clazz, methodReference).getName();
			return new OngoingMethodSpec<>(specedMethodName);
		}

		public Builder<A> completeness(SyntheticAnnotationCompleteness completeness) {
			return new Builder<>(clazz, values, completeness);
		}

		public A build() {
			return buildFromMap(clazz, values, completeness);
		}

		public final class OngoingMethodSpec<R> {
			private final String specedMethodName;

			private OngoingMethodSpec(String specedMethodName) {
				this.specedMethodName = requireNonNull(specedMethodName, "specedMethodName");
			}

			public Builder<A> returning(R value) {
				return new Builder<>(clazz, values.plus(specedMethodName, value), completeness);
			}
		}
	}
}
