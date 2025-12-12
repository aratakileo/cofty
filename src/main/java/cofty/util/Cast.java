package cofty.util;

import org.jetbrains.annotations.NotNull;

public final class Cast {
    private Cast() {}

    @SuppressWarnings("unchecked")
    public static <T1, T2> @NotNull T2 quiet(@NotNull T1 value) {
        return (T2) value;
    }
}
