package cofty.type;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public interface Containable<T> {
    default boolean isIn(@NotNull T... values) {
        return Arrays.asList(values).contains(this);
    }
}
