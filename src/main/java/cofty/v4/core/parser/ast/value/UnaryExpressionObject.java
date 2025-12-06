package cofty.v4.core.parser.ast.value;

import cofty.core.lexer.token.TypedToken;
import org.jetbrains.annotations.NotNull;

public final class UnaryExpressionObject implements ExpressionValueObject {
    public final TypedToken<?> operator;
    public final ExpressionValueObject value;

    public UnaryExpressionObject(@NotNull TypedToken<?> operator, @NotNull ExpressionValueObject value) {
        this.operator = operator;
        this.value = value;
    }
}
