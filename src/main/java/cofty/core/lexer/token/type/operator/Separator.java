package cofty.core.lexer.token.type.operator;

import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Separator implements TokenType {
    DOT("."),
    COLON(":"),
    COMMA(","),
    ARROW("->"),
    EXCLAMATION_MARK("!");

    public final static Map<String, Separator> VALUES;

    public final String sep;

    Separator(@NotNull String sep) {
        this.sep = sep;
    }

    @Override
    public @NotNull Simple type() {
        return Simple.OP;
    }

    @Override
    public @NotNull String content() {
        return sep;
    }

    @Override
    public @NotNull String toReprString() {
        return getClass().getSimpleName() + '.' + name();
    }

    public static boolean is(@NotNull String op) {
        return VALUES.containsKey(op);
    }

    public static @NotNull Separator of(@NotNull String op) {
        if (!is(op)) throw new IllegalStateException(String.format("unsupportable separator `%s`", op));

        return VALUES.get(op);
    }

    static {
        final var preValue = new HashMap<String, Separator>(values().length);

        for (final var value: values())
            preValue.put(value.content(), value);

        VALUES = Collections.unmodifiableMap(preValue);
    }
}
