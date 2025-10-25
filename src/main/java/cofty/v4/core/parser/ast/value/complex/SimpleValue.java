package cofty.v4.core.parser.ast.value.complex;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import org.jetbrains.annotations.NotNull;

public final class SimpleValue implements ValueSegmentObject {
    public final TypedToken<?> value;

    public SimpleValue(TypedToken<?> value) {
        this.value = value;
    }

    @Override
    public @NotNull String represent() {
        return String.format("%s value", switch ((TokenType)value.type) {
            case Simple.INT -> "integer";
            case Simple.DOUBLE -> "real number (double)";
            case Simple.STR -> "string";
            case Keyword.TRUE, Keyword.FALSE -> "boolean";
            default -> throw new IllegalStateException("Unexpected value: " + value.type);
        });
    }

    @Override
    public @NotNull TypedToken<?> failAnchor() {
        return value;
    }
}
