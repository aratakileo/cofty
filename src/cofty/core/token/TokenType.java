package cofty.core.token;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.MatchResult;

public enum TokenType implements Representable, ITokenType {
    DOUBLE,
    INT,
    OP,
    ID,
    KW,
    SEP,
    SKIP,
    NEWLINE,
    MISMATCH;

    public static @NotNull ITokenType valueOf(@NotNull MatchResult matchResult) {
        for (final var namedGroup: matchResult.namedGroups().keySet()) {
            final var matched = matchResult.group(namedGroup);

            if (matched != null) {
                final var tokenType = TokenType.valueOf(namedGroup);

                return switch (tokenType) {
                    case OP -> Operator.of(matched);
                    case SEP -> Separator.of(matched);
                    case KW -> Keyword.of(matched);
                    default -> tokenType;
                };
            }
        }

        throw new IllegalStateException("No matched named groups");
    }

    @Override
    public boolean equals(@NotNull ITokenType itype) {
        if (itype instanceof TokenType tokenType)
            return super.equals(tokenType);

        return ITokenType.super.equals(itype);
    }

    @Override
    public @NotNull String toReprString() {
        return getClass().getName() + '.' + name();
    }

    @Override
    public @NotNull TokenType type() {
        return this;
    }

    @Override
    public @Nullable String content() {
        return null;
    }
}
