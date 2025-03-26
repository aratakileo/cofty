package cofty.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Lists {
    public static <T> @NotNull ArrayList<? extends T> arrayListOf(T... args) {
        final var arrayList = new ArrayList<T>();

        Collections.addAll(arrayList, args);

        return arrayList;
    }

    public static <T> @Nullable T get(@NotNull List<T> list, int index) {
        return index >= list.size() ? null : list.get(index);
    }

    private Lists() {}
}
