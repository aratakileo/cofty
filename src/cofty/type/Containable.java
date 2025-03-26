package cofty.type;

import org.jetbrains.annotations.NotNull;

public interface Containable<T> {
    boolean isIn(@NotNull T... values);
}
