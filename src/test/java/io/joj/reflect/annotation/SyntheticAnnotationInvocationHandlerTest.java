package io.joj.reflect.annotation;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import io.joj.reflect.annotation.SyntheticAnnotationInvocationHandler;

/**
 * @author findepi
 * @since Oct 23, 2016
 */
public class SyntheticAnnotationInvocationHandlerTest {

	private @interface TestAnnotationWithoutAttribtues {
	}

	private @interface TestAnnotationWithMandatory {
		String mandatoryMethod();
	}

	private @interface TestAnnotationWithDefault {
		public static final String DEFAULT = "foo default";

		String foo() default DEFAULT;
	}

	@Retention(RetentionPolicy.RUNTIME)
	private @interface TestAnnotationWith2DefaultsAnd1Mandatory {

		public static final String FIRST_DEFAULT_VALUE = "first default value";
		public static final String SECOND_DEFAULT_VALUE = "second default value";

		String first() default FIRST_DEFAULT_VALUE;

		String second() default SECOND_DEFAULT_VALUE;

		String third();
	}

	@Test
	public void testAnnotationType() {
		// When
		InvocationHandler ih = handlerFor(TestAnnotationWithoutAttribtues.class);
		Class<? extends Annotation> introspectedAnnotationType = proxy(Annotation.class, ih).annotationType();
		// Then
		assertEquals(introspectedAnnotationType, TestAnnotationWithoutAttribtues.class);
	}

	@Test
	public void testToStringNoAttributes() {
		// When
		String value = proxyWithHandlerFor(TestAnnotationWithoutAttribtues.class, emptyMap())
				.toString();
		// Then
		assertEquals(value,
				"@io.joj.annotation.SyntheticAnnotationInvocationHandlerTest$TestAnnotationWithoutAttribtues()");
	}

	@Test
	public void testToString() {
		// When
		InvocationHandler ih = new SyntheticAnnotationInvocationHandler<>(
				TestAnnotationWith2DefaultsAnd1Mandatory.class,
				ImmutableMap.of(
						// "first" is unmapped, using default
						"second", "second explicit",
						"third", "third explicit"));

		String value = proxy(Annotation.class, ih).toString();
		// Then
		assertEquals(value, format("@%s(first=%s, second=%s, third=%s)",
				TestAnnotationWith2DefaultsAnd1Mandatory.class.getName(),
				TestAnnotationWith2DefaultsAnd1Mandatory.FIRST_DEFAULT_VALUE,
				"second explicit",
				"third explicit"));
	}

	@Test
	public void testReturnDefaultValue() {
		// When
		String value = proxyWithHandlerFor(TestAnnotationWithDefault.class, emptyMap()).foo();
		// Then
		assertEquals(value, TestAnnotationWithDefault.DEFAULT);
	}

	@Test
	public void testReturnProvidedValue() {
		// When
		String value = proxyWithHandlerFor(TestAnnotationWithMandatory.class,
				singletonMap("mandatoryMethod", "provided test value"))
						.mandatoryMethod();
		// Then
		assertEquals(value, "provided test value");
	}

	@Test
	public void testReturnProvidedValueWhenDefault() {
		// When
		String value = proxyWithHandlerFor(TestAnnotationWithDefault.class,
				singletonMap("foo", "provided test value for foo"))
						.foo();
		// Then
		assertEquals(value, "provided test value for foo");
	}

	@Test
	public void testHashCode() {
		// When
		int value = proxyWithHandlerFor(TestAnnotationWith2DefaultsAnd1Mandatory.class, ImmutableMap.of(
				"second", "second val",
				"third", "third val"))
						.hashCode();
		// Then
		@TestAnnotationWith2DefaultsAnd1Mandatory(second = "second val", third = "third val")
		class Sample {
		}

		assertEquals(value, Sample.class.getAnnotation(TestAnnotationWith2DefaultsAnd1Mandatory.class).hashCode());

	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ""
			+ ".*no.* corresponding method in interface \\S*TestAnnotationWithDefault: \\[extramethod\\]")
	public void testRejectUnmappedValue() {
		// When
		new SyntheticAnnotationInvocationHandler<>(TestAnnotationWithDefault.class, singletonMap("extramethod", ""));
		// Then expect exception
	}

	private <A extends Annotation> SyntheticAnnotationInvocationHandler<A> handlerFor(Class<A> annotationClass) {
		return new SyntheticAnnotationInvocationHandler<>(annotationClass, emptyMap());
	}

	/**
	 * Helper {@link Proxy} for testing {@link InvocationHandler#invoke(Object, Method, Object[])}, because manually and
	 * correctly providing parameters to that method is not obvious.
	 */
	private <A extends Annotation> A proxy(Class<A> interfaceClass, InvocationHandler handler) {
		Object proxy = Proxy.newProxyInstance(SyntheticAnnotationInvocationHandlerTest.class.getClassLoader(),
				new Class<?>[] { interfaceClass },
				handler);
		return interfaceClass.cast(proxy);
	}

	private <A extends Annotation> A proxyWithHandlerFor(Class<A> annotationClass, Map<String, ?> values) {
		return proxy(annotationClass, new SyntheticAnnotationInvocationHandler<>(annotationClass, values));
	}
}
