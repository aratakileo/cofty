package cofty.core.lexer.token.type;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

public enum Brackets implements TokenType, Representable {
    ROUND_OPEN("("),
    ROUND_CLOSE(")"),
    CURVE_OPEN("{"),
    CURVE_CLOSE("}");

    public final String bracket;

    Brackets(@NotNull String bracket) {
        this.bracket = bracket;
    }

    @Override
    public @NotNull Simple type() {
        return Simple.BRACKETS;
    }

    @Override
    public @NotNull String content() {
        return bracket;
    }

    @Override
    public @NotNull String toReprString() {
        return getClass().getSimpleName() + '.' + name();
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", toReprString(), Representable.repr(bracket));
    }

    public static @NotNull Brackets of(@NotNull String op) {
        for (final var _op: values())
            if (_op.bracket.equals(op))
                return _op;

        throw new IllegalStateException(String.format("unsupportable brackets `%s`", op));
    }
}
