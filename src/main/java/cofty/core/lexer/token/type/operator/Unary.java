package cofty.core.lexer.token.type.operator;

import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Unary implements Associative {
    // can not be assigned to any token during tokenization, used only during parsing
    PLUS("+", 13),
    MINUS("-", PLUS.priority),

    // can be assigned to any token during tokenization
    BITWISE_NOT("~", PLUS.priority),
    NOT("not", 3);

    public final static Map<String, Unary> VALUES;

    private final String op;
    private final int priority;

    Unary(@NotNull String op, int priority) {
        this.op = op;
        this.priority = priority;
    }

    @Override
    public @NotNull Associativity associativity() {
        return Associativity.RIGHT;
    }

    @Override
    public int priorityLevel() {
        return priority;
    }

    @Override
    public @NotNull Simple type() {
        return Simple.OP;
    }

    @Override
    public @NotNull String content() {
        return op;
    }

    @Override
    public @NotNull String toReprString() {
        return getClass().getSimpleName() + '.' + name();
    }

    public static boolean is(@NotNull String op) {
        return VALUES.containsKey(op);
    }

    public static @NotNull Unary of(@NotNull String op) {
        if (!is(op)) throw new IllegalStateException(String.format("unsupportable unary operator `%s`", op));

        return VALUES.get(op);
    }

    static {
        final var preValue = new HashMap<String, Unary>(values().length);

        for (final var value: values())
            preValue.put(value.content(), value);

        VALUES = Collections.unmodifiableMap(preValue);
    }
}
