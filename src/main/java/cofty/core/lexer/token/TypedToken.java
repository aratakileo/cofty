package cofty.core.lexer.token;

import cofty.core.lexer.token.type.TokenType;
import cofty.type.Representable;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;

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
        return Cast.unsafe(this);
    }

    @Override
    public @NotNull String toString() {
        return "Token{" +
                "type=" + type +
                ", content=" + Representable.repr(content) +
                ", position=[" + start +
                "-" + end +
                "]}";
    }
}
