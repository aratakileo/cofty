package cofty.core.parser.ast.value.complex;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import org.jetbrains.annotations.NotNull;

public final class SimpleValueObject implements ValueSegmentObject {
    public final TypedToken<?> value;

    public SimpleValueObject(TypedToken<?> value) {
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

    public @NotNull String valueTypeName() {
        return ((Enum<?>)value.type).name().toLowerCase();
    }
}
