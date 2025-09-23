package cofty.type;

import cofty.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public interface Representable {
    @NotNull String toReprString();

    static @NotNull String repr(@Nullable String string) {
        if (string == null)
            return "null";

        final var result = new StringBuilder("\"");

        for (var ch: string.toCharArray())
            result.append(switch (ch) {
                case '\n' -> "\\n";
                case '"' -> "\\\"";
                case '\\' -> "\\\\";
                case '\r' -> "\\r";
                case '\t' -> "\\t";
                case '\u2028' -> "\\u2028";
                case '\u2029' -> "\\u2029";
                default -> ch;
            });

        return result.append('"').toString();
    }

    static @NotNull String repr(@Nullable Representable value) {
        return value == null ? "null" : value.toReprString();
    }

    static @NotNull String repr(@Nullable Exception exception) {
        return exception == null ? "null" : String.format(
                "new %s(%s)",
                exception.getClass().getSimpleName(),
                repr(exception.getMessage())
        );
    }

    static <T> @NotNull String repr(@Nullable ArrayList<T> arrayList) {
        if (arrayList == null)
            return "null";

        return String.format(
                "%s.arrayListOf(%s)",
                Lists.class.getSimpleName(),
                String.join(", ", arrayList.stream().map(Representable::repr).toList())
        );
    }

    static <T> @NotNull String repr(@Nullable List<T> arrayList) {
        if (arrayList == null)
            return "null";

        return String.format(
                "List.of(%s)",
                String.join(", ", arrayList.stream().map(Representable::repr).toList())
        );
    }

    static <T> @NotNull String repr(@Nullable HashSet<T> set) {
        if (set == null)
            return "null";

        return String.format(
                "%s.hashSetOf(%s)",
                Lists.class.getSimpleName(),
                String.join(", ", set.stream().map(Representable::repr).toList())
        );
    }

    static <T> @NotNull String repr(@Nullable T value) {
        if (value == null)
            return "null";

        if (value instanceof Representable representable)
            return repr(representable);

        return value.toString();
    }
}
