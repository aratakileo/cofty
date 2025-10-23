package cofty.core.parser.ast.func;

import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.core.parser.ast.value.ComplexNameObject;
import cofty.type.exception.SyntaxError;
import cofty.util.Cast;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.value.expr.ValueExpressionObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CallFuncObject implements AstObject {
    private final ComplexNameObject name = new ComplexNameObject();

    private List<@NotNull ValueExpressionObject> args = null;

    private void setArgs(@NotNull List<AstObject> args) {
        for (final var arg: args)
            if (!(arg instanceof ValueExpressionObject)) throw new IllegalStateException();

        this.args = Cast.unsafe(args);
    }

    public @NotNull ComplexNameObject name() {
        return name;
    }

    public @Nullable List<@NotNull ValueExpressionObject> args() {
        return args;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        final var node = name.parserNode();
        final var subNode = ParserNode.token(Bracket.ROUND_OPEN, NodeModifier.generalAndPreview());

        subNode.thenRepeatableQueueBuilder(NodeModifier.builder().peek().astObjectsConsumer(this::setArgs).build())
                    .setSeparator(ParserNode.token(
                            Separator.COMMA,
                            NodeModifier.builder().syntaxFail("expected a comma separator").preview().build()
                    )).makeSeparatorOnlyOneAtTime(new SyntaxError("duplicate comma"))
                    .setStopper(ParserNode.token(Bracket.ROUND_CLOSE, NodeModifier.generalAndPreview()))
                    .add(ValueExpressionObject::new)
                    .build()
                .thenToken(
                        Bracket.ROUND_CLOSE,
                        NodeModifier.syntaxFail("expected an end of function arguments description")
                );

        return node.joinWith(subNode);
    }

    @Override
    public String toString() {
        return "CallFuncObject{" +
                "name=" + name +
                ", args=" + args +
                '}';
    }
}
