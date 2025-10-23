package cofty.core.lexer.token.type;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public enum Keyword implements TokenType {
    VAR,
    MUT,
    CLASS,
    FUN,
    IF,
    ELIF,
    ELSE,
    TRUE,
    FALSE,
    RETURN;

    public final static Map<String, Keyword> LOWERCASED_VALUES;

    @Override
    public @NotNull Simple type() {
        return Simple.KW;
    }

    @Override
    public @NotNull String content() {
        return name().toLowerCase();
    }

    @Override
    public @NotNull String toReprString() {
        return getClass().getSimpleName() + '.' + name();
    }

    public static boolean is(@NotNull String word) {
        return LOWERCASED_VALUES.containsKey(word);
    }

    public static @NotNull TokenType of(@NotNull String word) {
        if (!is(word)) throw new IllegalStateException(String.format("`%s` is not a keyword", word));
        return LOWERCASED_VALUES.get(word);
    }

    static {
        final var preValue = new HashMap<String, Keyword>(values().length);

        for (final var value: values())
            preValue.put(value.name().toLowerCase(), value);

        LOWERCASED_VALUES = Collections.unmodifiableMap(preValue);
    }
}
