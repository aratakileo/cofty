package cofty.v4.core.parser.ast.value;

import cofty.core.lexer.token.TypedToken;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class BinaryExpressionObject implements ExpressionValueObject {
    public final List<TypedToken<?>> operators;
    public final ExpressionValueObject leftValue, rightValue;

    public BinaryExpressionObject(
            @NotNull List<TypedToken<?>> operators,
            @NotNull ExpressionValueObject leftValue,
            @NotNull ExpressionValueObject rightValue
    ) {
        this.operators = operators;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }
}
