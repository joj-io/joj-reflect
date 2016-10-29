package io.joj.reflect.annotation;

import static io.joj.reflect.annotation.SyntheticAnnotationCompleteness.REQUIRE_COMPLETE;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

/**
 * @author findepi
 * @since Oct 23, 2016
 */
public class SyntheticAnnotationInvocationHandlerTest {

	@Retention(RetentionPolicy.RUNTIME)
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
		SyntheticAnnotationInvocationHandler<?> ih = newHandler(TestAnnotationWithoutAttribtues.class);
		Class<? extends Annotation> introspectedAnnotationType = ih.annotationTypeImpl();
		// Then
		assertEquals(introspectedAnnotationType, TestAnnotationWithoutAttribtues.class);
	}

	@Test
	public void testToStringNoAttributes() {
		// When
		String value = newHandler(TestAnnotationWithoutAttribtues.class, emptyMap())
				.toStringImpl();
		// Then
		assertEquals(value,
				"@io.joj.reflect.annotation.SyntheticAnnotationInvocationHandlerTest$TestAnnotationWithoutAttribtues()");
	}

	@Test
	public void testToString() {
		// When
		SyntheticAnnotationInvocationHandler<?> ih = new SyntheticAnnotationInvocationHandler<>(
				TestAnnotationWith2DefaultsAnd1Mandatory.class,
				ImmutableMap.of(
						// "first" is unmapped, using default
						"second", "second explicit",
						"third", "third explicit"),
				REQUIRE_COMPLETE);

		String value = ih.toStringImpl();
		// Then
		assertEquals(value, format("@%s(first=%s, second=%s, third=%s)",
				TestAnnotationWith2DefaultsAnd1Mandatory.class.getName(),
				TestAnnotationWith2DefaultsAnd1Mandatory.FIRST_DEFAULT_VALUE,
				"second explicit",
				"third explicit"));
	}

	@Test
	public void testReturnDefaultValue() throws Exception {
		// When
		Object value = newHandler(TestAnnotationWithDefault.class, emptyMap())
				.valueFor(TestAnnotationWithDefault.class.getMethod("foo"));
		// Then
		assertEquals(value, TestAnnotationWithDefault.DEFAULT);
	}

	@Test
	public void testReturnProvidedValue() throws Exception {
		// When
		Object value = newHandler(TestAnnotationWithMandatory.class,
				singletonMap("mandatoryMethod", "provided test value"))
						.valueFor(TestAnnotationWithMandatory.class.getMethod("mandatoryMethod"));
		// Then
		assertEquals(value, "provided test value");
	}

	@Test
	public void testReturnProvidedValueWhenDefault() throws Exception {
		// When
		Object value = newHandler(TestAnnotationWithDefault.class, singletonMap("foo", "provided test value for foo"))
				.valueFor(TestAnnotationWithDefault.class.getMethod("foo"));
		// Then
		assertEquals(value, "provided test value for foo");
	}

	@Test
	public void testHashCodeAndEqualsNoAttributes() {
		// When
		SyntheticAnnotationInvocationHandler<?> ih = newHandler(TestAnnotationWithoutAttribtues.class);

		// Then
		@TestAnnotationWithoutAttribtues
		class SameSample {
		}

		Object sameButStatic = SameSample.class.getAnnotation(TestAnnotationWithoutAttribtues.class);

		assertEquals(ih.hashCodeImpl(), sameButStatic.hashCode());
		assertTrue(ih.equalsImpl(new Object(), sameButStatic), "should compare equal to sameButStatic");
	}

	@Test
	public void testHashCodeAndEquals() {
		// When
		SyntheticAnnotationInvocationHandler<?> ih = newHandler(TestAnnotationWith2DefaultsAnd1Mandatory.class,
				ImmutableMap.of(
						"second", "second val",
						"third", "third val"));

		// Then
		@TestAnnotationWith2DefaultsAnd1Mandatory(second = "second val", third = "third val")
		class SameSample {
		}

		@TestAnnotationWith2DefaultsAnd1Mandatory(third = "third different val")
		class DifferentSample {
		}

		Object sameButStatic = SameSample.class.getAnnotation(TestAnnotationWith2DefaultsAnd1Mandatory.class);
		Object differentStatic = DifferentSample.class.getAnnotation(TestAnnotationWith2DefaultsAnd1Mandatory.class);

		assertEquals(ih.hashCodeImpl(), sameButStatic.hashCode());
		assertTrue(ih.equalsImpl(new Object(), sameButStatic), "should compare equal to sameButStatic");
		assertFalse(ih.equalsImpl(new Object(), differentStatic), "should compare inequal to differentStatic");
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ""
			+ ".*no.* corresponding method in interface \\S*TestAnnotationWithDefault: \\[extramethod\\]")
	public void testRejectUnmappedValue() {
		// When
		new SyntheticAnnotationInvocationHandler<>(TestAnnotationWithDefault.class, singletonMap("extramethod", ""),
				REQUIRE_COMPLETE);
		// Then expect exception
	}

	private <A extends Annotation> SyntheticAnnotationInvocationHandler<A> newHandler(Class<A> annotationClass) {
		return newHandler(annotationClass, emptyMap());
	}

	private <A extends Annotation> SyntheticAnnotationInvocationHandler<A> newHandler(Class<A> annotationClass,
			Map<String, ?> values) {
		return new SyntheticAnnotationInvocationHandler<>(annotationClass, values, REQUIRE_COMPLETE);
	}
}
