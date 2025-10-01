package cofty.core.lexer.token;

import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;

import java.util.regex.MatchResult;

public class AnyToken extends TypedToken<ITokenType> {
    public AnyToken(@NotNull ITokenType type, @NotNull String content, int start, int end) {
        super(type, content, start, end);
    }

    public static @NotNull AnyToken build(
            @NotNull MatchResult matchResult,
            @NotNull ITokenType tokenType
    ) {
        return new AnyToken(
                tokenType,
                matchResult.group(),
                matchResult.start(),
                matchResult.end()
        );
    }
}
