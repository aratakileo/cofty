package cofty.core.parser.ast.value.expr.op;

import cofty.core.lexer.token.TypedToken;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.value.expr.ValueExpression;
import cofty.core.parser.node.ParserNode;
import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BinaryExpressionObject implements Operator, AstObject, ValueExpression {
    private final List<TypedToken<?>> operator;
    private final ValueExpression leftValue, rightValue;

    public BinaryExpressionObject(
            @NotNull List<TypedToken<?>> operator,
            @NotNull ValueExpression leftValue,
            @NotNull ValueExpression rightValue
    ) {
        this.operator = operator;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
    }

    @Override
    public @NotNull List<TypedToken<?>> operator() {
        return operator;
    }

    public @NotNull ValueExpression leftValue() {
        return leftValue;
    }

    public @NotNull ValueExpression rightValue() {
        return rightValue;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        throw new RuntimeException();
    }

    @Override
    public String toString() {
        return "BinaryExpressionObject{" +
                "operator=" + Representable.repr(operator) +
                ", leftValue=" + leftValue +
                ", rightValue=" + rightValue +
                '}';
    }
}
