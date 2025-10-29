package cofty.core.parser.ast.value.expr;

import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.value.PrimitiveValueObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
public class ValueExpressionObject implements AstObject, ValueExpression {
    private ValueExpression expression = null;

    public @Nullable ValueExpression expr() {
        return expression;
    }

    private void setExpression(@NotNull AstObject expression) {
        if (expression instanceof ValueExpressionObject valueExpressionObject) {
            this.expression = valueExpressionObject.expression;
            return;
        }

        this.expression = (ValueExpression) expression;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        return ParserNode.operatorExpression(
                NodeModifier.builder()
                        .general()
                        .preview()
                        .astObjectConsumer(this::setExpression)
                        .build()
        );
    }

    public @NotNull ParserNode simpleParserNode() {
        final var _expr = new PrimitiveValueObject();
        expression = _expr;

        return _expr.parserNode();
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public String toString() {
        return "ValueExpressionObject{" +
                "value=" + expression +
                '}';
    }
}
