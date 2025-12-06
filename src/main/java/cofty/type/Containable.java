package cofty.type;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public interface Containable<T> {
    default boolean isAny(@NotNull T... values) {
        return Arrays.asList(values).contains(this);
    }
}
