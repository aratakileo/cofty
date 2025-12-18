package cofty.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class Types {
    private Types() {}

    public static <T, R> @Nullable R valueAndMapOrNull(@Nullable T value, @NotNull Function<T, R> map) {
        return value == null ? null : map.apply(value);
    }

    public static <T, R> @NotNull R valueAndMapOrThrow(@Nullable T value, @NotNull Function<T, R> map) {
        if (value == null)
            throw new NullPointerException();

        return map.apply(value);
    }
}
