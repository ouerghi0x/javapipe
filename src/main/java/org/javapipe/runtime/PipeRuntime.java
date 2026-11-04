package org.javapipe.runtime;

import java.util.function.Function;

/**
 * Runtime support utility for Java Pipe.
 */
public final class PipeRuntime {
    private PipeRuntime() {}

    /**
     * Applies a functional transformation to a piped value.
     */
    public static <T, R> R pipe(T value, Function<T, R> function) {
        return function.apply(value);
    }
}

