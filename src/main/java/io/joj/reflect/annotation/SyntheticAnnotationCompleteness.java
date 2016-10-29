package io.joj.reflect.annotation;

import java.lang.annotation.Annotation;

/**
 * @author findepi
 * @since Oct 29, 2016
 */
public enum SyntheticAnnotationCompleteness {
	/**
	 * Every annotation method, that does not have a default value, must have a value explicitly provided.
	 */
	REQUIRE_COMPLETE {
		@Override
		Object valueWhenMissing() {
			throw new IllegalStateException("Value should not be missing");
		}
	},

	/**
	 * Wherever accessed value is missing (i.e. not provided explicitly and no default exist), a
	 * {@link UnsupportedOperationException} will be raised.
	 * <p>
	 * <b>Warning:</b> obviously, this violates implemented {@link Annotation} contract, use with caution.
	 */
	THROW_WHERE_UNDEFINED {
		@Override
		Object valueWhenMissing() {
			throw new UnsupportedOperationException();
		}
	},

	/**
	 * Wherever accessed value is missing (i.e. not provided explicitly and no default exist), a {@code null} will be
	 * returned.
	 * <p>
	 * <b>Warning:</b> obviously, this violates implemented {@link Annotation} contract, use with caution.
	 */
	NULL_WHERE_UNDEFINED {
		@Override
		Object valueWhenMissing() {
			return null;
		}
	},

	;

	abstract Object valueWhenMissing();
}
