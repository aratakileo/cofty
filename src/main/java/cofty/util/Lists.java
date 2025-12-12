package cofty.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class Lists {
    private Lists() {}

    @SafeVarargs
    public static <T> @NotNull ArrayList<T> arrayListOf(T... args) {
        final var arrayList = new ArrayList<T>();

        Collections.addAll(arrayList, args);

        return arrayList;
    }

    @SafeVarargs
    public static <T> @NotNull HashSet<T> hashSetOf(T... args) {
        return new HashSet<>(List.of(args));
    }

    public static <T> @Nullable T get(@NotNull List<T> list, int index) {
        return index >= list.size() ? null : list.get(index);
    }

    public static <T> boolean containsAny(@NotNull Collection<T> collection, @NotNull T value) {
        return collection.contains(value);
    }

    public static <T> boolean containsAny(@NotNull Collection<T> collection, @NotNull T value1, @NotNull T value2) {
        return collection.contains(value1) || collection.contains(value2);
    }

    @SafeVarargs
    public static <T> boolean containsAny(@NotNull Collection<T> collection, @NotNull T @NotNull... values) {
        for (final var value: values)
            if (collection.contains(value)) return true;

        return false;
    }
}
