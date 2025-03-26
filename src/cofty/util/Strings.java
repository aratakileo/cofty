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

    public static int getLineNumber(@NotNull String source, int charIndex) {
        return count(source.substring(0, charIndex), "\n") + 1;
    }

    private Strings() {}
}
