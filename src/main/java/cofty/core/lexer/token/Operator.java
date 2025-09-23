package cofty.core.lexer.token;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

public enum Operator implements ITokenType, Representable {
    ASSIGN("=");

    public final String op;

    Operator(@NotNull String op) {
        this.op = op;
    }

    @Override
    public @NotNull TokenType type() {
        return TokenType.OP;
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

    public static @NotNull Operator of(@NotNull String op) {
        for (final var _op: values())
            if (_op.op.equals(op))
                return _op;

        throw new IllegalStateException(String.format("unsupportable operator `%s`", op));
    }
}
