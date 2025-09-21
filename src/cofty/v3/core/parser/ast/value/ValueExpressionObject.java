package cofty.v3.core.parser.ast.value;

import cofty.v3.core.parser.ast.AstObject;
import cofty.v3.core.parser.node.ParserNode;
import org.jetbrains.annotations.NotNull;

public class ValueExpressionObject implements AstObject {
    private final PrimitiveValueObject value = new PrimitiveValueObject();

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
