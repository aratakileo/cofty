package cofty.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class Strings {
    private Strings() {}

    public static int count(@NotNull String source, @NotNull String substring) {
        int count = -1;
        int definedIndex = 0;

        do {
            definedIndex = source.indexOf(substring, definedIndex) + substring.length();
            count++;
        } while (definedIndex >= substring.length());

        return count;
    }

    public static int getLineNumber(@NotNull String source, int until) {
        return count(source.substring(0, until), "\n") + 1;
    }

    public static @NotNull String camelToSnake(@NotNull String camelString) {
        return camelString.replaceAll("([A-Z])(?=[A-Z])", "$1_")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
    }

    public static @NotNull String snakeToCamel(@NotNull String snakeString) {
        if (snakeString.isEmpty()) return snakeString;

        final var pattern = Pattern.compile("_([a-zA-Z])");
        final var matcher = pattern.matcher(snakeString);
        final var result = new StringBuilder();

        while (matcher.find()) matcher.appendReplacement(result, matcher.group(1).toUpperCase());

        matcher.appendTail(result);

        return result.toString();
    }

    public static @NotNull String capitalize(@NotNull String source) {
        return Character.toUpperCase(source.charAt(0)) + source.substring(1);
    }

    public static void println(Object... values) {
        for (var i = 0; i < values.length; i++) {
            System.out.print(values[i]);

            if (i != values.length - 1)
                System.out.print(' ');
        }

        System.out.println();
    }
}
