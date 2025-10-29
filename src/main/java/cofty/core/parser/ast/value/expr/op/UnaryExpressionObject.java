package cofty.core.parser.ast.value.expr.op;

import cofty.core.lexer.token.TypedToken;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.value.expr.ValueExpression;
import cofty.core.parser.ast.value.expr.ValueExpressionObject;
import cofty.core.parser.node.ParserNode;
import cofty.type.Representable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Deprecated
public class UnaryExpressionObject implements Operator, AstObject, ValueExpression {
    private final List<TypedToken<?>> operator;
    private final ValueExpression value;

    public UnaryExpressionObject(@NotNull List<TypedToken<?>> operator, @NotNull ValueExpression value) {
        this.operator = operator;
        this.value = value;
    }

    @Override
    public @NotNull List<TypedToken<?>> operator() {
        return operator;
    }

    public @NotNull ValueExpression value() {
        return value;
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
        return "UnaryExpressionObject{" +
                "operator=" + Representable.repr(operator) +
                ", value=" + value +
                '}';
    }
}
