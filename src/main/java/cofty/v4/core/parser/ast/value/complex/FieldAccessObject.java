package cofty.v4.core.parser.ast.value.complex;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;

public final class FieldAccessObject implements ValueSegmentObject {
    public final TypedToken<Simple> name;

    public FieldAccessObject(@NotNull TypedToken<Simple> name) {
        this.name = name;
    }

    @Override
    public @NotNull String represent() {
        return "field access";
    }

    @Override
    public @NotNull TypedToken<?> failAnchor() {
        return name;
    }
}
