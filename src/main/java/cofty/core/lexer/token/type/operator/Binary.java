package cofty.core.lexer.token.type.operator;

import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Binary implements Associative {
    // can be assigned to any token during tokenization
    OR("or", 1),
    AND("and", Associative.priorityHigherThan(OR)),
    EQUALS("==", Associative.priorityHigherThan(Unary.NOT)),
    NOT_EQUALS("!=", Associative.priorityHigherThan(Unary.NOT)),
    LESS("<", Associative.priorityHigherThan(EQUALS)),
    LESS_OR_EQUALS("<=", Associative.priorityHigherThan(EQUALS)),
    GREATER(">", Associative.priorityHigherThan(EQUALS)),
    GREATER_OR_EQUALS(">=", Associative.priorityHigherThan(EQUALS)),
    IN("in", Associative.priorityHigherThan(GREATER_OR_EQUALS)),
    IS("is", Associative.priorityHigherThan(GREATER_OR_EQUALS)),
    ISINSTANCE("isinstance", Associative.priorityHigherThan(GREATER_OR_EQUALS)),
    BITWISE_OR("|", Associative.priorityHigherThan(ISINSTANCE)),
    BITWISE_XOR("^", Associative.priorityHigherThan(BITWISE_OR)),
    BITWISE_AND("&", Associative.priorityHigherThan(BITWISE_XOR)),
    LEFT_SHIFT("<<", Associative.priorityHigherThan(BITWISE_AND)),
    RIGHT_SHIFT(">>", Associative.priorityHigherThan(BITWISE_AND)),

    // can not be assigned to any token during tokenization, used only during parsing
    PLUS("+", Associative.priorityHigherThan(RIGHT_SHIFT)),
    MINUS("-", Associative.priorityHigherThan(RIGHT_SHIFT)),
    NOT_IN("not in", Associative.priorityHigherThan(GREATER_OR_EQUALS)),
    IS_NOT("is not", Associative.priorityHigherThan(GREATER_OR_EQUALS)),

    // can be assigned to any token during tokenization
    DIV("/", Associative.priorityHigherThan(PLUS)),
    MUL("*", Associative.priorityHigherThan(PLUS)),
    MOD("%", Associative.priorityHigherThan(PLUS)),
    POW("**", Associativity.RIGHT, Associative.priorityHigherThan(Unary.PLUS));

    public final static Map<String, Binary> VALUES;

    private final String op;
    private final Associativity associativity;
    private final int priority;

    Binary(@NotNull String op, int priority) {
        this.op = op;
        this.associativity = Associativity.LEFT;
        this.priority = priority;
    }

    Binary(@NotNull String op, @NotNull Associativity associativity, int priority) {
        this.op = op;
        this.associativity = associativity;
        this.priority = priority;
    }

    @Override
    public @NotNull Associativity associativity() {
        return associativity;
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

    public static @NotNull Binary of(@NotNull String op) {
        if (!is(op)) throw new IllegalStateException(String.format("unsupportable binary operator `%s`", op));

        return VALUES.get(op);
    }

    static {
        final var preValue = new HashMap<String, Binary>(values().length);

        for (final var value: values())
            preValue.put(value.content(), value);

        VALUES = Collections.unmodifiableMap(preValue);
    }
}
