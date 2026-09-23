package cofty.core.parser.ast.value.complex;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.List;

public final class SimpleValueObject implements ValueSegmentObject {
    public final TypedToken<?> value;

    public SimpleValueObject(TypedToken<?> value) {
        this.value = value;
    }

    @Override
    public @NotNull String represent() {
        // IMPORTANT: DO NOT REMOVE `(TokenType)` TO AVOID COMPILATION FAIL

        return String.format("%s value", switch ((TokenType)value.type) {
            case Simple.INT -> "integer";
            case Simple.DOUBLE -> "real number (double)";
            case Simple.STR -> "string";
            case Keyword.TRUE, Keyword.FALSE -> "boolean";
            default -> throw new IllegalStateException("Unexpected value: " + value.type);
        });
    }

    @Override
    public @NotNull List<TypedToken<?>> failTokensRange() {
        return List.of(value);
    }

    public @NotNull String valueTypeName() {
        return value.type.isAny(Keyword.TRUE, Keyword.FALSE) ? "bool" : value.typeAsEnum().name().toLowerCase();
    }

    @Override
    public @NotNull String prettyString(@NotNull String offset, int increase) {
        return MessageFormat.format(
                "{2}primitive {0}({1})",
                valueTypeName(),
                value.content,
                offset
        );
    }
}
