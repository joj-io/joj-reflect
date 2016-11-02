package io.joj.reflect;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

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
		MethodReference0<Iterable, Iterator<?>> ref = Iterable::iterator;
		// When
		Method method = MethodReferences.getMethod(Iterable.class, ref);
		// Then
		assertEquals(method, Iterable.class.getMethod("iterator"));
	}

	@Test
	public void testIntrospect() throws Exception {
		// Given
		MethodReference0<ArrayList<?>, Integer> ref = ArrayList::size;
		// When
		Method method = MethodReferences.introspect(ref);
		// Then
		assertEquals(method, ArrayList.class.getMethod("size"));
	}
}
