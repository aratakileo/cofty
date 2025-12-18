package cofty.core.parser.ast.value;

import cofty.core.lexer.token.TypedToken;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class UnaryExpressionObject implements ExpressionValueObject {
    public final TypedToken<?> operator;
    public final ExpressionValueObject value;

    public UnaryExpressionObject(@NotNull TypedToken<?> operator, @NotNull ExpressionValueObject value) {
        this.operator = operator;
        this.value = value;
    }

    @Override
    public @NotNull List<TypedToken<?>> failTokensRange() {
        return List.of(operator, value.lastFailToken());
    }
}
