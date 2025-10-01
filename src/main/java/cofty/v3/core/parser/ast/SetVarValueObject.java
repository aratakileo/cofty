package cofty.v3.core.parser.ast;

import cofty.core.lexer.token.Operator;
import cofty.core.lexer.token.AnyToken;
import cofty.core.lexer.token.TokenType;
import cofty.core.lexer.token.TypedToken;
import cofty.type.Representable;
import cofty.v3.core.parser.ast.value.ValueExpressionObject;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.TokenNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetVarValueObject implements AstObject {
    private TypedToken<TokenType> name = null;

    private final ValueExpressionObject value = new ValueExpressionObject();

    private void setName(@NotNull TypedToken<?> name) {
        this.name = name.unsafeAs();
    }

    public @NotNull ValueExpressionObject value() {
        return value;
    }

    public @Nullable TypedToken<TokenType> name() {
        return name;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        var node = (TokenNode)null;

        (node = ParserNode.token(TokenType.ID, NodeModifier.builder().general().tokenConsumer(this::setName).build()))
                .thenToken(Operator.ASSIGN, NodeModifier.generalAndPreview())
                .then(
                        value.parserNode(),
                        NodeModifier.builder()
                                .syntaxFail("expected a variable value")
                                .build()
                );

        return node;
    }

    @Override
    public String toString() {
        return "SetVarValueObject{" +
                "name=" + Representable.repr(name) +
                ", value=" + value +
                '}';
    }
}
