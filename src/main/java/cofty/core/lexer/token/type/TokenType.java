package cofty.core.lexer.token.type;

import cofty.core.lexer.token.type.operator.*;
import cofty.type.Containable;
import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.MatchResult;

public interface TokenType extends Containable<TokenType>, Representable {
    @NotNull Simple type();
    @Nullable String content();

    default boolean equals(@NotNull TokenType type) {
        return type.type().equals(type())
                && (type.content() == null) == (content() == null)
                && Objects.equals(type.content(), content());
    }

    static boolean isKeywordLike(@NotNull String word) {
        return Keyword.is(word) || Modifier.is(word) || Unary.is(word) || Binary.is(word);
    }

    static @NotNull TokenType keywordLikeOf(@NotNull String word) {
        if (Keyword.is(word)) return Keyword.of(word);
        if (Modifier.is(word)) return Modifier.of(word);
        if (Binary.is(word)) return Binary.of(word);
        if (Unary.is(word)) return Unary.of(word);

        throw new IllegalStateException(String.format("`%s` is not like a keyword", word));
    }

    static @NotNull TokenType valueOf(@NotNull MatchResult matchResult) {
        for (final var namedGroup: matchResult.namedGroups().keySet()) {
            final var matched = matchResult.group(namedGroup);

            if (matched != null) {
                final var tokenType = Simple.valueOf(namedGroup);

                return switch (tokenType) {
                    case OP -> operatorLikeOf(matched);
                    case WORD -> isKeywordLike(matched) ? keywordLikeOf(matched) : Simple.WORD;
                    case BRACKETS -> Bracket.of(matched);
                    default -> tokenType;
                };
            }
        }

        throw new IllegalStateException("No matched named groups");
    }

    static @NotNull TokenType operatorLikeOf(@NotNull String op) {
        if (Separator.is(op)) return Separator.of(op);
        if (Assign.is(op)) return Assign.of(op);
        if (ContextSensitive.is(op)) return ContextSensitive.of(op);
        if (Binary.is(op)) return Binary.of(op);
        if (Unary.is(op)) return Unary.of(op);

        throw new IllegalStateException(String.format("`%s` is not like an operator", op));
    }
}
