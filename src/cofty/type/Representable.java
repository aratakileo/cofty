package cofty.type;

import cofty.util.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public interface Representable {
    @NotNull String toReprString();

    static @NotNull String repr(@NotNull String string) {
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

    static @NotNull String repr(@NotNull Representable value) {
        return value.toReprString();
    }

    static <T> @NotNull String repr(@NotNull ArrayList<T> arrayList) {
        return String.format(
                "%s.arrayListOf(%s)",
                Lists.class.getSimpleName(),
                String.join(", ", arrayList.stream().map(Representable::repr).toList())
        );
    }

    static <T> @NotNull String repr(T value) {
        return value.toString();
    }
}
