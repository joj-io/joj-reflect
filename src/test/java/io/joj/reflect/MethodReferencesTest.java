package io.joj.reflect;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

/**
 * @author findepi
 * @since Nov 1, 2016
 */
public class MethodReferencesTest {

	@Test
	public void getMethodFromInterfaceMethodReference() throws Exception {
		// Given
		@SuppressWarnings("rawtypes")
		MethodReference0<Iterable> ref = Iterable::iterator;
		// When
		Method method = MethodReferences.getMethod(Iterable.class, ref);
		// Then
		assertEquals(method, Iterable.class.getMethod("iterator"));
	}

}
