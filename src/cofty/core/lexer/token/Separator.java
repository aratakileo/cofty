package cofty.core.lexer.token;

import org.jetbrains.annotations.NotNull;

public enum Separator implements ITokenType {
    DOT("."),
    COLON(":");

    public final String sep;

    Separator(@NotNull String sep) {
        this.sep = sep;
    }

    @Override
    public @NotNull TokenType type() {
        return TokenType.SEP;
    }

    @Override
    public @NotNull String content() {
        return sep;
    }

    public static @NotNull Separator of(@NotNull String sep) {
        for (final var _sep: values())
            if (_sep.sep.equals(sep))
                return _sep;

        throw new IllegalStateException(String.format("unsupportable separator `%s`", sep));
    }
}
