package cofty.core.token;

import cofty.core.ast.AstValue;
import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

import java.util.regex.MatchResult;

public class Token implements AstValue {
    public final ITokenType type;
    public final String content;
    public final int start, end;

    public Token(@NotNull ITokenType type, @NotNull String content, int start, int end) {
        this.type = type;
        this.content = content;
        this.start = start;
        this.end = end;
    }

    public @NotNull Token merge(@NotNull Token token) {
        if (!type.equals(token.type))
            throw new IllegalStateException("Both tokens should have the same type");

        return new Token(
                type,
                content.concat(token.content),
                start,
                token.end
        );
    }

    public static @NotNull Token build(
            @NotNull MatchResult matchResult,
            @NotNull ITokenType tokenType
    ) {
        return new Token(
                tokenType,
                matchResult.group(),
                matchResult.start(),
                matchResult.end()
        );
    }

    @Override
    public @NotNull String toString() {
        return "Token{" +
                "type=" + type +
                ", content=" + Representable.repr(content) +
                ", position=[" + start +
                ":" + end +
                "]}";
    }
}
