package cofty.core.parser.ast.value;

import cofty.core.parser.ast.AstObject;
import cofty.core.parser.node.ParserNode;
import org.jetbrains.annotations.NotNull;

public class ValueExpressionObject implements AstObject {
    private final PrimitiveValueObject value = new PrimitiveValueObject();

    public @NotNull PrimitiveValueObject value() {
        return value;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        return value.parserNode();
    }

    @Override
    public String toString() {
        return "ValueExpressionObject{" +
                "value=" + value +
                '}';
    }
}
