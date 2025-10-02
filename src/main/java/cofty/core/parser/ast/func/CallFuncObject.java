package cofty.core.parser.ast.func;

import cofty.core.lexer.token.*;
import cofty.core.lexer.token.type.Brackets;
import cofty.core.lexer.token.type.Separator;
import cofty.core.lexer.token.type.Simple;
import cofty.type.exception.SyntaxError;
import cofty.util.Cast;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.value.ValueExpressionObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.TokenNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CallFuncObject implements AstObject {
    private TypedToken<Simple> name = null;
    private List<@NotNull ValueExpressionObject> args = null;

    private void setName(@NotNull TypedToken<?> name) {
        this.name = name.strictAs();
    }

    private void setArgs(@NotNull List<AstObject> args) {
        for (final var arg: args)
            if (!(arg instanceof ValueExpressionObject)) throw new IllegalStateException();

        this.args = Cast.unsafe(args);
    }

    public @Nullable TypedToken<Simple> name() {
        return name;
    }

    public @Nullable List<@NotNull ValueExpressionObject> args() {
        return args;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        var node = (TokenNode)null;

        (node = ParserNode.token(Simple.WORD, NodeModifier.builder().general().tokenConsumer(this::setName).build()))
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
