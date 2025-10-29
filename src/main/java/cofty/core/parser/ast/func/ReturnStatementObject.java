package cofty.core.parser.ast.func;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.WithAnchor;
import cofty.core.parser.ast.value.expr.ValueExpressionObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.TokenNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class ReturnStatementObject implements AstObject, WithAnchor<Keyword> {
    private TypedToken<Keyword> anchor = null;

    private final ValueExpressionObject value = new ValueExpressionObject();

    private void setAnchor(@NotNull TypedToken<?> anchor) {
        this.anchor = anchor.strictAs();
    }

    @Override
    public @NotNull TypedToken<Keyword> anchor() {
        return anchor;
    }

    public @NotNull ValueExpressionObject value() {
        return value;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        var node = (TokenNode)null;

        (node = ParserNode.token(
                Keyword.RETURN,
                NodeModifier.builder().general().preview().tokenConsumer(this::setAnchor).build()
        )).then(
                        value.parserNode(),
                        NodeModifier.builder()
                                .syntaxFail("expected a returnable value")
                                .build()
                );

        return node;
    }

    @Override
    public String toString() {
        return "ReturnStatementObject{" +
                "value=" + value +
                '}';
    }
}
