package io.joj.reflect.annotation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static io.joj.fluence.guava.GuavaCollectors.toImmutableMap;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;

/**
 * {@link InvocationHandler} implementing an {@link Annotation}.
 *
 * @author findepi
 */
final class SyntheticAnnotationInvocationHandler<A extends Annotation> implements InvocationHandler {

	private static final Method equalsMethod;
	private static final Method hashCodeMethod;
	private static final Method toStringMethod;
	private static final Method annotationTypeMethod;

	static {
		try {
			equalsMethod = Object.class.getMethod("equals", Object.class);
			hashCodeMethod = Object.class.getMethod("hashCode");
			toStringMethod = Object.class.getMethod("toString");
			annotationTypeMethod = Annotation.class.getMethod("annotationType");

		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private final Class<A> annotationClass;
	private final ImmutableMap<String, AnnotationValue<A>> values;

	private int hash;

	public SyntheticAnnotationInvocationHandler(Class<A> annotationClass, Map<String, ?> values) {

		this.annotationClass = requireNonNull(annotationClass, "annotationClass");

		// Implicit null-checking. Annotation can never have null values.
		Map<String, Object> providedValues = ImmutableMap.copyOf(requireNonNull(values, "values"));

		this.values =
				// Take all annotation interface methods
				Arrays.stream(annotationClass.getDeclaredMethods())
						// ... excluding static (just in case some future Java allows them)
						.filter(method -> !Modifier.isStatic(method.getModifiers()))
						// ... with their default values
						.map(method -> entry(method, Optional.ofNullable(method.getDefaultValue())))

						// Override default values with provided ones (where provided)
						.map(entry -> entry(
								entry.getKey(),
								// Use provided value or, if no value provided, use the default one
								Java9.orOptionals(
										Optional.ofNullable(providedValues.get(entry.getKey().getName())),
										entry::getValue)
						/**/ ))

						// Unpack Optionals: all methods without default should have a value provided.
						.map(entry -> entry(entry.getKey(),
								entry.getValue().orElseThrow(
										() -> new IllegalArgumentException(
												format("No value provided for %s", entry.getKey().getName())))
						/**/ ))

						// Validate types
						.map(entry -> entry(entry.getKey(),
								Primitives.wrap(entry.getKey().getReturnType()).cast(entry.getValue())))

						.collect(toImmutableMap(
								entry -> entry.getKey().getName(),
								entry -> new AnnotationValue<A>(entry.getKey(), entry.getValue())));

		// Finally, check all provided values did not contain too many (i.e. unmapped) entries
		List<String> unmapped = new ArrayList<>(Sets.difference(providedValues.keySet(), this.values.keySet()));
		checkArgument(unmapped.isEmpty(), "Some provided values do not have corresponding method in %s: %s",
				annotationClass, unmapped);
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		/*
		 * Proxy-generate class overrides the following Object methods: hashCode(), equals(Object), toString().
		 */
		if (hashCodeMethod.equals(method)) {
			return hashCodeImpl();
		}
		if (equalsMethod.equals(method)) {
			return equalsImpl(proxy, Iterables.getOnlyElement(asList(args)));
		}
		if (toStringMethod.equals(method)) {
			return toStringImpl();
		}
		if (annotationTypeMethod.equals(method)) {
			return annotationTypeImpl();
		}

		/*
		 * Now `method' must be a method of the implemented interface. And since annotations don't support inheritance,
		 * we may easily check this.
		 */
		checkState(annotationClass == method.getDeclaringClass(), "Expected method of %s, got %s",
				annotationClass, method);
		checkState(args == null, "Annotation interface methods are exepcted to be args-free");
		return valueFor(method);
	}

	/**
	 * Implements {@link Annotation#hashCode()}.
	 */
	@VisibleForTesting
	int hashCodeImpl() {
		if (hash != 0 || values.isEmpty()) {
			// hash cache; annotation without values has 0 hash code
			return hash;
		}
		hash = values.values().stream()
				.mapToInt(AnnotationValue::valueHashCode)
				.sum();
		return hash;
	}

	@VisibleForTesting
	boolean equalsImpl(Object proxy, Object o) {
		if (proxy == o) {
			return true;
		}
		if (!annotationClass.isInstance(o)) { // including null case
			return false;
		}

		@SuppressWarnings("unchecked") // already checked
		A other = (A) o;

		boolean allValuesEqual = values.entrySet().stream()
				.allMatch(entry -> {
					return entry.getValue().isValueEqualIn(other);
				});

		return allValuesEqual;
	}

	@VisibleForTesting
	String toStringImpl() {
		String valuesToString = values.entrySet().stream()
				// Sort to have deterministic toString(). Useful at least for tests, if not for humans.
				.sorted(Comparator.comparing(Entry::getKey))
				// Extract name and value
				.map(entry -> entry(entry.getKey(), entry.getValue().value))
				// Map to String
				.map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
				.collect(joining(", "));

		return format("@%s(%s)", annotationClass.getName(), valuesToString);
	}

	@VisibleForTesting
	Class<A> annotationTypeImpl() {
		return annotationClass;
	}

	@VisibleForTesting
	Object valueFor(Method method) {
		AnnotationValue<A> boundValue = values.get(method.getName());
		checkState(boundValue != null, "No value (not even default) for %s could be found", method);
		return boundValue.value;
	}

	private static <K, V> Entry<K, V> entry(K key, V value) {
		return new AbstractMap.SimpleEntry<>(key, value);
	}
}
