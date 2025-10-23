package cofty.core.lexer.token.type.operator;

import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

public enum Assign implements TokenType {
    ASSIGN("=");

    public final String op;

    Assign(@NotNull String op) {
        this.op = op;
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

    @Override
    public String toString() {
        return String.format("%s(%s)", toReprString(), Representable.repr(op));
    }

    public static boolean is(@NotNull String op) {
        return op.equals("=");
    }

    public static @NotNull Assign of(@NotNull String op) {
        for (final var _op: values())
            if (_op.op.equals(op))
                return _op;

        throw new IllegalStateException(String.format("unsupportable assign operator `%s`", op));
    }
}
