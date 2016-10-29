package io.joj.reflect.annotation;

import static io.joj.reflect.annotation.internal.Check.checkState;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	private final SyntheticAnnotationCompleteness completeness;
	// immutable
	private final Map<String, AnnotationValue> values;

	private int hash;

	public SyntheticAnnotationInvocationHandler(Class<A> annotationClass, Map<String, ?> values,
			SyntheticAnnotationCompleteness completeness) {

		this.annotationClass = requireNonNull(annotationClass, "annotationClass");
		this.completeness = requireNonNull(completeness, "completeness");

		values.entrySet().forEach(entry -> {
			if (entry.getValue() == null) {
				throw new NullPointerException(format("Null value for %s", entry.getKey()));
			}
		});

		Map<String, AnnotationValue> effectiveValues = new HashMap<>();
		for (Method annotationGetter : annotationClass.getDeclaredMethods()) {
			if (Modifier.isStatic(annotationGetter.getModifiers())) {
				continue;
			}

			Object effectiveValue;
			if (values.containsKey(annotationGetter.getName())) {
				// explicit value
				effectiveValue = requireNonNull(values.get(annotationGetter.getName()),
						() -> format("null value for %s", annotationGetter));
			} else if (annotationGetter.getDefaultValue() != null) {
				// default value
				effectiveValue = annotationGetter.getDefaultValue();
			} else {
				// no value
				if (completeness == SyntheticAnnotationCompleteness.REQUIRE_COMPLETE) {
					throw new IllegalArgumentException(format("no value for %s", annotationGetter));
				} else {
					continue;
				}
			}

			effectiveValues.put(annotationGetter.getName(),
					AnnotationValue.valueOf(annotationGetter, effectiveValue));
		}

		// Finally, check all provided values did not contain too many (i.e. unmapped) entries
		Set<String> unmapped = new HashSet<>(values.keySet());
		unmapped.removeAll(effectiveValues.keySet());
		if (!unmapped.isEmpty()) {
			throw new IllegalArgumentException(format("Some provided values do not have corresponding method in %s: %s",
					annotationClass, unmapped));
		}

		this.values = Collections.unmodifiableMap(effectiveValues);
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		/*
		 * Proxy-generate class overrides the following Object methods: hashCode(), equals(Object), toString().
		 */
		if (hashCodeMethod.equals(method)) {
			return hashCodeImpl();
		}
		if (equalsMethod.equals(method)) {
			return equalsImpl(proxy, args[0]);
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
	int hashCodeImpl() {
		if (hash != 0 || values.isEmpty()) {
			// hash cache; annotation without values has 0 hash code
			return hash;
		}
		hash = values.values().stream()
				.mapToInt(AnnotationValue::hashCodeNameAndValue)
				.sum();
		return hash;
	}

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

	String toStringImpl() {
		String valuesToString = values.entrySet().stream()
				// Sort to have deterministic toString(). Useful at least for tests, if not for humans.
				.sorted(Comparator.comparing(Entry::getKey))
				// Extract name and value's toString
				.map(entry -> entry(entry.getKey(), entry.getValue().valueToString()))
				// Join them
				.map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
				.collect(joining(", "));

		return format("@%s(%s)", annotationClass.getName(), valuesToString);
	}

	Class<A> annotationTypeImpl() {
		return annotationClass;
	}

	Object valueFor(Method method) {
		AnnotationValue boundValue = values.get(method.getName());
		if (boundValue != null) {
			return boundValue.getValue();
		} else {
			return completeness.valueWhenMissing();
		}
	}

	private static <K, V> Entry<K, V> entry(K key, V value) {
		return new AbstractMap.SimpleEntry<>(key, value);
	}
}
