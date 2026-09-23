package cofty.core.lexer.token;

import cofty.core.lexer.token.type.TokenType;
import cofty.type.Representable;
import cofty.type.TextContent;
import cofty.util.Cast;
import cofty.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.regex.MatchResult;

public final class TypedToken<T extends TokenType> {
    public final T type;
    public final String content;
    public final int start, end;

    public TypedToken(@NotNull T type, @NotNull String content, int start, int end) {
        this.type = type;
        this.content = content;
        this.start = start;
        this.end = end;
    }

    public @NotNull Enum<?> typeAsEnum() {
        return Cast.quiet(type);
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

    public @NotNull String content() {
        return content;
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

    public @NotNull String prettyString() {
        return MessageFormat.format(
                "{0} [{1}-{2}] -> {3}",
                Representable.repr(type),
                start,
                end,
                Representable.repr(content)
        );
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

    public int getLineNumber(@NotNull TextContent text) {
        return Strings.getLineNumber(text.text, end);
    }
}
