package io.joj.annotation;

import static org.testng.Assert.assertEquals;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

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
}
