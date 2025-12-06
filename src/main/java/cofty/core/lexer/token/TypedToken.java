package cofty.core.lexer.token;

import cofty.core.lexer.token.type.TokenType;
import cofty.type.Representable;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.MatchResult;

public class TypedToken<T extends TokenType> {
    public final T type;
    public final String content;
    public final int start, end;

    public TypedToken(@NotNull T type, @NotNull String content, int start, int end) {
        this.type = type;
        this.content = content;
        this.start = start;
        this.end = end;
    }

    public @NotNull TypedToken<T> merge(@NotNull TypedToken<T> token) {
        if (!type.equals(token.type))
            throw new IllegalStateException("Both tokens should have the same type");

        return new TypedToken<>(
                type,
                content.concat(token.content),
                start,
                token.end
        );
    }

    public <_T extends TokenType> @NotNull TypedToken<_T> strictAs() {
        return Cast.quiet(this);
    }

    @Override
    public @NotNull String toString() {
        return getClass().getSimpleName() + '{' +
                "type=" + Representable.repr(type) +
                ", content=" + Representable.repr(content) +
                ", position=[" + start +
                "-" + end +
                "]}";
    }

    public static <T extends TokenType, _T extends TokenType> @NotNull TypedToken<_T> strictAs(
            @Nullable TypedToken<T> token
    ) {
        return Objects.requireNonNull(token).strictAs();
    }

    public static <T extends TokenType, _T extends TokenType> @Nullable TypedToken<_T> strictAsOrNull(
            @Nullable TypedToken<T> token
    ) {
        return token == null ? null : token.strictAs();
    }

    public static <_T extends TokenType> @NotNull TypedToken<_T> build(
            @NotNull MatchResult matchResult,
            @NotNull _T tokenType
    ) {
        return new TypedToken<>(
                tokenType,
                matchResult.group(),
                matchResult.start(),
                matchResult.end()
        );
    }
}
