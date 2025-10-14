package cofty.core.parser.ast;

import cofty.core.lexer.token.type.Operator;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.TypedToken;
import cofty.core.parser.ast.value.ComplexNameObject;
import cofty.type.Representable;
import cofty.core.parser.ast.value.ValueExpressionObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.TokenNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetVarValueObject implements AstObject {
    private final ComplexNameObject name = new ComplexNameObject();
    private final ValueExpressionObject value = new ValueExpressionObject();

    public @NotNull ValueExpressionObject value() {
        return value;
    }

    public @NotNull ComplexNameObject name() {
        return name;
    }

    @Override
    public @NotNull ParserNode parserNode() {
        final var node = name.parserNode(NodeModifier.general());

        node.joinWithToken(Operator.ASSIGN, NodeModifier.generalAndPreview())
                .joinWith(
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
