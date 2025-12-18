package cofty.type;

import cofty.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public interface Representable {
    @NotNull String toReprString();

    static @NotNull String repr(@Nullable String string) {
        return repr(string, false);
    }

    static @NotNull String repr(@Nullable String string, boolean noQuotationMark) {
        if (string == null)
            return "null";

        final var result = new StringBuilder(noQuotationMark ? "" : "\"");

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

        return result.append(noQuotationMark ? "" : "\"").toString();
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

    static @NotNull String repr(@Nullable ArrayList<?> arrayList) {
        if (arrayList == null)
            return "null";

        return String.format(
                "%s.arrayListOf(%s)",
                Lists.class.getSimpleName(),
                String.join(", ", arrayList.stream().map(Representable::repr).toList())
        );
    }

    static @NotNull String repr(@Nullable List<?> arrayList) {
        if (arrayList == null)
            return "null";

        return String.format(
                "List.of(%s)",
                String.join(", ", arrayList.stream().map(Representable::repr).toList())
        );
    }

    static @NotNull String repr(@Nullable HashSet<?> set) {
        if (set == null)
            return "null";

        return String.format(
                "%s.hashSetOf(%s)",
                Lists.class.getSimpleName(),
                String.join(", ", set.stream().map(Representable::repr).toList())
        );
    }

    static @NotNull String reprAsTuple(@Nullable Collection<?> collection) {
        if (collection == null)
            return "null";

        return "(%s)".formatted(collection.stream().map(Representable::repr).collect(Collectors.joining(", ")));
    }

    static <T> @NotNull String repr(@Nullable T value) {
        if (value == null)
            return "null";

        if (value instanceof Representable representable)
            return repr(representable);

        return value.toString();
    }
}
