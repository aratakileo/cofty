package cofty.core.lexer.token.type;

import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.MatchResult;

public enum Simple implements Representable, TokenType {
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

    public static @NotNull TokenType valueOf(@NotNull MatchResult matchResult) {
        for (final var namedGroup: matchResult.namedGroups().keySet()) {
            final var matched = matchResult.group(namedGroup);

            if (matched != null) {
                final var tokenType = Simple.valueOf(namedGroup);

                return switch (tokenType) {
                    case OP -> Operator.of(matched);
                    case SEP -> Separator.of(matched);
                    case WORD -> Keyword.is(matched) ? Keyword.of(matched) : WORD;
                    case BRACKETS -> Brackets.of(matched);
                    default -> tokenType;
                };
            }
        }

        throw new IllegalStateException("No matched named groups");
    }

    @Override
    public boolean equals(@NotNull TokenType itype) {
        if (itype instanceof Simple simple)
            return super.equals(simple);

        return TokenType.super.equals(itype);
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
