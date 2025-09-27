package cofty.v3.core.parser.ast.func;

import cofty.core.lexer.token.Keyword;
import cofty.v3.core.parser.ast.AstObject;
import cofty.v3.core.parser.ast.value.ValueExpressionObject;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.TokenNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

public class ReturnExpressionObject implements AstObject {
    private final ValueExpressionObject value = new ValueExpressionObject();

    public @NotNull ValueExpressionObject value() {
        return value;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        var node = (TokenNode)null;

        (node = ParserNode.token(Keyword.RETURN, NodeModifier.generalAndPreview()))
                .then(
                        value.parserNode(),
                        NodeModifier.builder()
                                .syntaxFail("expected a returnable value")
                                .build()
                );

        return node;
    }

    @Override
    public String toString() {
        return "ReturnExpressionObject{" +
                "value=" + value +
                '}';
    }
}
