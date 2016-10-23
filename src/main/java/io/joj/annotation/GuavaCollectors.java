package io.joj.annotation;

import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

import com.google.common.collect.ImmutableMap;

/**
 * @author findepi
 * @since Oct 23, 2016
 */
class GuavaCollectors {
	private GuavaCollectors() {
	}

	public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper) {

		return Collector.<T, ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> of(
				ImmutableMap::builder,
				(builder, element) -> builder.put(keyMapper.apply(element), valueMapper.apply(element)),
				(builder1, builder2) -> builder1.putAll(builder2.build()),
				ImmutableMap.Builder::build,
				Characteristics.UNORDERED);
	}

}
