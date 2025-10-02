package cofty.core.lexer.token.type;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

public enum Separator implements TokenType, Representable {
    DOT("."),
    COLON(":"),
    COMMA(","),
    ARROW("->");

    public final String sep;

    Separator(@NotNull String sep) {
        this.sep = sep;
    }

    @Override
    public @NotNull Simple type() {
        return Simple.SEP;
    }

    @Override
    public @NotNull String content() {
        return sep;
    }

    @Override
    public @NotNull String toReprString() {
        return getClass().getSimpleName() + '.' + name();
    }

    public static @NotNull Separator of(@NotNull String sep) {
        for (final var _sep: values())
            if (_sep.sep.equals(sep))
                return _sep;

        throw new IllegalStateException(String.format("unsupportable separator `%s`", sep));
    }
}
