package io.joj.reflect.annotation.internal;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author findepi
 * @since Oct 29, 2016
 */
public class Primitive {

	public static final Map<Class<?>, Object> primitiveToDefault;
	static {
		Map<Class<?>, Object> defaults = new HashMap<>();
		defaults.put(int.class, 0);
		defaults.put(long.class, 0L);
		defaults.put(float.class, 0.f);
		defaults.put(double.class, 0.);
		defaults.put(char.class, '\0');
		defaults.put(short.class, (short) 0);
		defaults.put(byte.class, (byte) 0);
		defaults.put(boolean.class, false);
		primitiveToDefault = unmodifiableMap(defaults);
	}

	public static final Map<Class<?>, Class<?>> primitiveToWrapper;
	static {
		Map<Class<?>, Class<?>> toWrapper = new HashMap<>();
		toWrapper.put(int.class, Integer.class);
		toWrapper.put(long.class, Long.class);
		toWrapper.put(float.class, Float.class);
		toWrapper.put(double.class, Double.class);
		toWrapper.put(char.class, Character.class);
		toWrapper.put(short.class, Short.class);
		toWrapper.put(byte.class, Byte.class);
		toWrapper.put(boolean.class, Boolean.class);
		toWrapper.put(void.class, Void.class);
		primitiveToWrapper = unmodifiableMap(toWrapper);
	}

}
