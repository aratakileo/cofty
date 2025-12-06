package cofty.core.parser.ast.value.complex;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.value.ExpressionValueObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class FuncCallObject implements ValueSegmentObject {
    public final TypedToken<Simple> name;
    public final List<ExpressionValueObject> args;
    public final boolean isPostfix;

    public FuncCallObject(
            @NotNull TypedToken<Simple> name,
            @NotNull List<ExpressionValueObject> args,
            boolean isPostfix
    ) {
        this.name = name;
        this.args = args;
        this.isPostfix = isPostfix;
    }

    @Override
    public @NotNull String represent() {
        return isPostfix ? "postfix function call" : "function call";
    }

    @Override
    public @NotNull TypedToken<?> failAnchor() {
        return name;
    }
}
