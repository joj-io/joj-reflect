package io.joj.reflect.annotation;

import static io.joj.reflect.annotation.SyntheticAnnotationCompleteness.NULL_WHERE_UNDEFINED;
import static io.joj.reflect.annotation.SyntheticAnnotationCompleteness.REQUIRE_COMPLETE;
import static io.joj.reflect.annotation.SyntheticAnnotationCompleteness.THROW_WHERE_UNDEFINED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import io.joj.reflect.annotation.AnnotationBuilder.Builder;

/**
 * @author findepi
 * @since Oct 23, 2016
 */
public class AnnotationBuilderTest {

	@Test
	public void testBuildFromMap() {
		// When
		Test synthetic = AnnotationBuilder.buildFromMap(Test.class, ImmutableMap.of(
				"invocationCount", 10,
				"alwaysRun", false,
				"testName", "some name"));

		// Then
		assertEquals(synthetic.invocationCount(), 10);
		assertEquals(synthetic.alwaysRun(), false);
		assertEquals(synthetic.testName(), "some name");
		Assertions.assertThat(synthetic.toString()).as("toString")
				.matches("@.*Test\\(.*some name.*\\)");
	}

	@Test
	public void testBuildAnnotation() {
		// When
		Test synthetic = AnnotationBuilder.builderFor(Test.class)
				.with(Test::invocationCount).returning(10)
				.with(Test::alwaysRun).returning(false)
				.with(Test::testName).returning("some name")
				.build();

		// Then
		assertEquals(synthetic.invocationCount(), 10);
		assertEquals(synthetic.alwaysRun(), false);
		assertEquals(synthetic.testName(), "some name");
		Assertions.assertThat(synthetic.toString()).as("toString")
				.matches("@.*Test\\(.*some name.*\\)");
	}

	@Test
	public void testExplicitlyRejectIncompleteAnnotation() {
		// Given
		Builder<AnnotationWithOneMandatoryAttribute> builder = AnnotationBuilder
				.builderFor(AnnotationWithOneMandatoryAttribute.class);
		// When
		Assertions.assertThatThrownBy(() -> {
			builder
					.completeness(REQUIRE_COMPLETE)
					.build();
		})
				// Then
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageMatching(".*no value for .*\\brequired\\b.*");
		// Then expect exception
	}

	@Test
	public void testRejectIncompleteAnnotation() {
		// Given
		Builder<AnnotationWithOneMandatoryAttribute> builder = AnnotationBuilder
				.builderFor(AnnotationWithOneMandatoryAttribute.class);
		// When
		Assertions.assertThatThrownBy(() -> builder.build())
				// Then
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageMatching(".*no value for .*\\brequired\\b.*");
		// Then expect exception
	}

	@Test
	public void testAllowIncompleteAnnotationWithNulls() {
		// Given
		Builder<AnnotationWithOneMandatoryAttribute> builder = AnnotationBuilder
				.builderFor(AnnotationWithOneMandatoryAttribute.class);
		// When
		AnnotationWithOneMandatoryAttribute built = builder
				.completeness(NULL_WHERE_UNDEFINED)
				.build();
		// Then
		assertNull(built.required(), "required() on built annotation should return null");
	}

	@Test
	public void testAllowIncompleteAnnotationWithUOE() {
		// Given
		Builder<AnnotationWithOneMandatoryAttribute> builder = AnnotationBuilder
				.builderFor(AnnotationWithOneMandatoryAttribute.class);
		// When
		AnnotationWithOneMandatoryAttribute built = builder
				.completeness(THROW_WHERE_UNDEFINED)
				.build();
		// Then
		Assertions.assertThatThrownBy(() -> built.required())
				.isInstanceOf(UnsupportedOperationException.class);
	}

	private @interface AnnotationWithOneMandatoryAttribute {

		String required();
	}
}
