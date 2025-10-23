package cofty.core.lexer.token.type.operator;

import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ContextSensitive implements TokenType {
    PLUS(Unary.PLUS, Binary.PLUS),
    MINUS(Unary.MINUS, Binary.MINUS);

    public final static Map<String, ContextSensitive> VALUES;

    private final Unary unary;
    private final Binary binary;

    ContextSensitive(@NotNull Unary unary, @NotNull Binary binary) {
        if (!unary.content().equals(binary.content()))
            throw new IllegalStateException();

        this.unary = unary;
        this.binary = binary;
    }

    @Override
    public @NotNull Simple type() {
        return Simple.OP;
    }

    @Override
    public @NotNull String content() {
        return unary.content();
    }

    public @NotNull Unary unary() {
        return unary;
    }

    public @NotNull Binary binary() {
        return binary;
    }

    @Override
    public @NotNull String toReprString() {
        return getClass().getSimpleName() + '.' + name();
    }

    public static boolean is(@NotNull String op) {
        return VALUES.containsKey(op);
    }

    public static @NotNull ContextSensitive of(@NotNull String op) {
        if (!is(op)) throw new IllegalStateException(String.format("unsupportable context sensitive operator `%s`", op));

        return VALUES.get(op);
    }

    static {
        final var preValue = new HashMap<String, ContextSensitive>(values().length);

        for (final var value: values())
            preValue.put(value.content(), value);

        VALUES = Collections.unmodifiableMap(preValue);
    }
}
