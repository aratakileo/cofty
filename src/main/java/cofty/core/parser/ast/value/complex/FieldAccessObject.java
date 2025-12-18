package cofty.core.parser.ast.value.complex;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.WithName;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class FieldAccessObject implements ValueSegmentObject, WithName {
    public final TypedToken<Simple> name;

    public FieldAccessObject(@NotNull TypedToken<Simple> name) {
        this.name = name;
    }

    @Override
    public @NotNull String represent() {
        return "field access";
    }

    @Override
    public @NotNull List<TypedToken<?>> failTokensRange() {
        return List.of(name);
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return name;
    }
}
