package cofty.util;

import org.jetbrains.annotations.NotNull;

public final class Strings {
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

    public static void println(Object... values) {
        for (var i = 0; i < values.length; i++) {
            System.out.print(values[i]);

            if (i != values.length - 1)
                System.out.print(' ');
        }

        System.out.println();
    }

    private Strings() {}
}
