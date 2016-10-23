package io.joj.annotation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static io.joj.annotation.GuavaCollectors.toImmutableMap;
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
	private final ImmutableMap<String, ?> values;

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
								Entry::getValue));

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
	private int hashCodeImpl() {
		// hash caching just like in String
		if (hash != 0) {
			return hash;
		}
		hash = values.entrySet().stream()
				.mapToInt(entry -> (127 * entry.getKey().hashCode()) ^ valueHashCode(entry.getValue()))
				.sum();
		return hash;
	}

	private static int valueHashCode(Object value) {
		requireNonNull(value, "value");

		if (!value.getClass().isArray()) {
			return value.hashCode();

		} else {
			try {
				Method arrayHashCodeMethod = Arrays.class.getMethod("hashCode",
						value.getClass().getComponentType().isPrimitive()
								? value.getClass()
								: Object[].class);

				return (Integer) arrayHashCodeMethod.invoke(null, value);

			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private boolean equalsImpl(Object proxy, Object other) {
		if (proxy == other) {
			return true;
		}
		if (!annotationClass.isInstance(other)) {
			return false;
		}
		
		// TODO
		throw new UnsupportedOperationException("not implemented yet");
	}

	private String toStringImpl() {
		String valuesToString = values.entrySet().stream()
				.sorted(Comparator.comparing(Entry::getKey))
				.map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
				.collect(joining(", "));

		return format("@%s(%s)", annotationClass.getName(), valuesToString);
	}

	private Class<A> annotationTypeImpl() {
		return annotationClass;
	}

	private Object valueFor(Method method) {
		Object value = values.get(method.getName());
		checkState(value != null, "No value (not even default) for %s could be found", method);
		return value;
	}

	private static <K, V> Entry<K, V> entry(K key, V value) {
		return new AbstractMap.SimpleEntry<>(key, value);
	}
}
