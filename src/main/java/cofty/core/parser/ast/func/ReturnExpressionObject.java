package cofty.core.parser.ast.func;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.value.ValueExpressionObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.TokenNode;
import cofty.core.parser.node.modifier.NodeModifier;
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
