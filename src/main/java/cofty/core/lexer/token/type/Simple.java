package cofty.core.lexer.token.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Simple implements TokenType {
    DOUBLE,
    INT,
    STR,
    OP,
    KW,
    SEP,
    WORD,
    SKIP,
    NEWLINE,
    BRACKETS,
    MISMATCH;

    @Override
    public boolean equals(@NotNull TokenType type) {
        if (type instanceof Simple simple)
            return super.equals(simple);

        return TokenType.super.equals(type);
    }

    @Override
    public @NotNull String toReprString() {
        return getClass().getSimpleName() + '.' + name();
    }

    @Override
    public @NotNull Simple type() {
        return this;
    }

    @Override
    public @Nullable String content() {
        return null;
    }
}
