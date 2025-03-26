package cofty.core.token;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

public enum Keyword implements ITokenType, Representable {
    LET,
    MUT;

    @Override
    public @NotNull TokenType type() {
        return TokenType.KW;
    }

    @Override
    public @NotNull String content() {
        return name().toLowerCase();
    }

    @Override
    public @NotNull String toReprString() {
        return getClass().getName() + '.' + name();
    }

    public static @NotNull Keyword of(@NotNull String kw) {
        for (final var _kw: values())
            if (_kw.name().toLowerCase().equals(kw))
                return _kw;

        throw new IllegalStateException(String.format("`%s` is not a keyword", kw));
    }
}
