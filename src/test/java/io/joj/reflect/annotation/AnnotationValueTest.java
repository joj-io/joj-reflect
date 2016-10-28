package io.joj.reflect.annotation;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author findepi
 * @since Oct 28, 2016
 */
public class AnnotationValueTest {

	@Test(dataProvider = "testEqualsDataProvider")
	public void testEquals(Object getter, Object value, Object compared, boolean expected) {
		// Given
		AnnotationValue annotationValue = AnnotationValue.valueOf((Method) getter, value);
		// When
		boolean result = annotationValue.isValueEqual(compared);
		// Then
		assertEquals(result, expected, format("comparison result of %s and %s", value, compared));
	}

	@DataProvider
	public Object[][] testEqualsDataProvider() throws Exception {
		return new Object[][] {
				{ Get.getInt, 10, new Integer(10), true },
				{ Get.getInt, 10, 11, false },
				{ Get.getString, "abc", "ab".concat("c"), true },
				{ Get.getInts, new int[] { 0, 1, 2 }, new int[] { 0, 1, 2 }, true },
				{ Get.getInts, new int[] { 0, 1, 2 }, new int[] { 0, 1, 2, 3 }, false },
				{ Get.getInts, new int[] { 0, 1, 2 }, new int[] { 0, 1, 3 }, false },
				{ Get.getInts, new int[] { 0, 1, 2 }, null, false },
				{ Get.getStrings, new String[] { "a" }, new String[] { "a" }, true },

				/*
				 * Although nulls are invalid in annotations' domain, let's be sure we compare 'false' to null rather
				 * than throw -- in case we face annotations also generated but by some other, more lax, library.
				 */
				{ Get.getInt, 10, null, false },
				{ Get.getString, "a", null, false },
				{ Get.getInts, new int[] {}, null, false },
				{ Get.getInts, new int[] { 1 }, null, false },
				{ Get.getStrings, new String[] {}, null, false },
		};
	}

	@Test(dataProvider = "testToStringDataProvider")
	public void testToString(Object getter, Object value, String expected) {
		// Given
		AnnotationValue annotationValue = AnnotationValue.valueOf((Method) getter, value);
		// When
		String result = annotationValue.valueToString();
		// Then
		assertEquals(result, expected);
	}

	@DataProvider
	public Object[][] testToStringDataProvider() throws Exception {
		return new Object[][] {
				{ Get.getInt, 10, "10" },
				{ Get.getInts, new int[] {}, "[]" },
				{ Get.getInts, new int[] { -78 }, "[-78]" },
				{ Get.getInts, new int[] { 12, 45, 67 }, "[12, 45, 67]" },
				{ Get.getString, "abc", "abc" },
				{ Get.getStrings, new String[] { "Asia" }, "[Asia]" },
		};
	}

	@Test
	public void testCloneInputPrimitiveArray() {
		// Given
		int[] input = new int[] { 0, 1, 5 };
		AnnotationValue annotationValue = AnnotationValue.valueOf(Get.getInts, input);
		// When
		Arrays.fill(input, 0);
		// Then
		assertTrue(Arrays.equals(new int[] { 0, 1, 5 }, (int[]) annotationValue.getValue()));
	}

	@Test
	public void testCloneReturnedPrimitiveArray() {
		// Given
		AnnotationValue annotationValue = AnnotationValue.valueOf(Get.getInts, new int[] { 0, 1, 5 });
		// When
		int[] returnedArray = (int[]) annotationValue.getValue();
		Arrays.fill(returnedArray, 0);
		// Then
		assertTrue(Arrays.equals(new int[] { 0, 1, 5 }, (int[]) annotationValue.getValue()));
	}

	@Test
	public void testCloneObjectArray() {
		// Given
		String[] input = new String[] { "alice" };
		AnnotationValue annotationValue = AnnotationValue.valueOf(Get.getStrings, input);
		// When
		input[0] = "has a cat";
		// Then
		assertTrue(Arrays.equals(new String[] { "alice" }, (String[]) annotationValue.getValue()));
	}

	@Test
	public void testCloneReturnedObjectArray() {
		// Given
		AnnotationValue annotationValue = AnnotationValue.valueOf(Get.getStrings, new String[] { "alice" });
		// When
		String[] returnedArray = (String[]) annotationValue.getValue();
		Arrays.fill(returnedArray, "");
		// Then
		assertTrue(Arrays.equals(new String[] { "alice" }, (String[]) annotationValue.getValue()));
	}

	static class Get {

		private final static Method getInt;
		private final static Method getInts;

		private final static Method getString;
		private final static Method getStrings;

		static {
			try {
				getInt = Get.class.getDeclaredMethod("getInt");
				getString = Get.class.getDeclaredMethod("getString");
				getInts = Get.class.getDeclaredMethod("getInts");
				getStrings = Get.class.getDeclaredMethod("getStrings");

			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}

		int getInt() {
			throw new UnsupportedOperationException();
		}

		int[] getInts() {
			throw new UnsupportedOperationException();
		}

		String getString() {
			throw new UnsupportedOperationException();
		}

		String[] getStrings() {
			throw new UnsupportedOperationException();
		}
	}
}
