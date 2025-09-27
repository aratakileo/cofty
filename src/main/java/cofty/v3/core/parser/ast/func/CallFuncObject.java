package cofty.v3.core.parser.ast.func;

import cofty.core.lexer.token.Brackets;
import cofty.core.lexer.token.Separator;
import cofty.core.lexer.token.Token;
import cofty.core.lexer.token.TokenType;
import cofty.type.exception.SyntaxError;
import cofty.util.Cast;
import cofty.v3.core.parser.ast.AstObject;
import cofty.v3.core.parser.ast.value.ValueExpressionObject;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.TokenNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CallFuncObject implements AstObject {
    private Token name = null;
    private List<@NotNull ValueExpressionObject> args = null;

    private void setName(@NotNull Token name) {
        this.name = name;
    }

    private void setArgs(@NotNull List<AstObject> args) {
        for (final var arg: args)
            if (!(arg instanceof ValueExpressionObject)) throw new IllegalStateException();

        this.args = Cast.unsafe(args);
    }

    public @Nullable Token name() {
        return name;
    }

    public @Nullable List<@NotNull ValueExpressionObject> args() {
        return args;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        var node = (TokenNode)null;

        (node = ParserNode.token(TokenType.ID, NodeModifier.builder().general().tokenConsumer(this::setName).build()))
                .thenToken(Brackets.ROUND_OPEN, NodeModifier.generalAndPreview())
                .thenRepeatableQueueBuilder(NodeModifier.builder().peek().astObjectsConsumer(this::setArgs).build())
                    .setSeparator(ParserNode.token(
                            Separator.COMMA,
                            NodeModifier.builder().syntaxFail("expected a comma separator").preview().build()
                    )).makeSeparatorOnlyOneAtTime(new SyntaxError("duplicate comma"))
                    .setStopper(ParserNode.token(Brackets.ROUND_CLOSE, NodeModifier.generalAndPreview()))
                    .add(ValueExpressionObject::new)
                    .build()
                .thenToken(Brackets.ROUND_CLOSE, NodeModifier.syntaxFail("expected an end of function arguments description"));

        return node;
    }

    @Override
    public String toString() {
        return "CallFuncObject{" +
                "name=" + name +
                ", args=" + args +
                '}';
    }
}
