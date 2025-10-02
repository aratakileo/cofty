package cofty.core.lexer.token;

import cofty.core.lexer.token.type.TokenType;
import org.jetbrains.annotations.NotNull;

import java.util.regex.MatchResult;

public class AnyToken extends TypedToken<TokenType> {
    public AnyToken(@NotNull TokenType type, @NotNull String content, int start, int end) {
        super(type, content, start, end);
    }

    public static @NotNull AnyToken build(
            @NotNull MatchResult matchResult,
            @NotNull TokenType tokenType
    ) {
        return new AnyToken(
                tokenType,
                matchResult.group(),
                matchResult.start(),
                matchResult.end()
        );
    }
}
