package cofty.v3.core.parser.ast;

import cofty.core.lexer.token.*;
import cofty.v3.core.parser.ast.value.ValueExpressionObject;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VarDeclarationObject implements AstObject {
    private final boolean isFunctionArgument;

    private Token name = null, mutable = null, explicitlySpecifiedType = null;

    private final ValueExpressionObject value = new ValueExpressionObject();

    public VarDeclarationObject(boolean isFunctionArgument) {
        this.isFunctionArgument = isFunctionArgument;
    }

    private void setMutable(@NotNull Token mutable) {
        this.mutable = mutable;
    }

    private void setName(@NotNull Token name) {
        this.name = name;
    }

    private void setExplicitlySpecifiedType(@NotNull Token explicitlySpecifiedType) {
        this.explicitlySpecifiedType = explicitlySpecifiedType;
    }

    public @Nullable Token mutable() {
        return mutable;
    }

    public @Nullable Token name() {
        return name;
    }

    public @NotNull ValueExpressionObject value() {
        return value;
    }

    public @Nullable Token explicitlySpecifiedType() {
        return explicitlySpecifiedType;
    }

    private @NotNull ParserNode typeDeclarationNode(@NotNull NodeModifier firstModifier) {
        return ParserNode.token(Separator.COLON, firstModifier).andToken(
                TokenType.ID,
                NodeModifier.builder()
                        .depended()
                        .syntaxFail("expected a variable value type")
                        .tokenConsumer(this::setExplicitlySpecifiedType)
                        .build()
        );
    }

    private @NotNull ParserNode valueNode(@NotNull NodeModifier firstModifier) {
        return ParserNode.token(Operator.ASSIGN, firstModifier).and(
                value.parserNode(),
                NodeModifier.builder()
                        .depended()
                        .syntaxFail("expected a variable value")
                        .build()
        );
    }

    @Override
    public @NotNull ParserNode parserNode() {
        final var mutNode = ParserNode.token(
                Keyword.MUT,
                NodeModifier.builder().peek().tokenConsumer(this::setMutable).build()
        );

        final var node = isFunctionArgument ? mutNode : ParserNode.token(
                Keyword.LET,
                NodeModifier.previewAndGeneral()
        ).and(mutNode);

        final var variableNameNodeModifierBuilder = NodeModifier.builder()
                .syntaxFail("expected %s name".formatted(isFunctionArgument ? "an argument" : "a variable"))
                .tokenConsumer(this::setName);

        if (isFunctionArgument)
            variableNameNodeModifierBuilder.preview();

        (isFunctionArgument ? node : node.nextOrThrow()).thenToken(
                TokenType.ID,
                variableNameNodeModifierBuilder.build()
        ).thenAnyOf(
                NodeModifier.syntaxFail("expected explicit type declaration or value assignment"),
                typeDeclarationNode(NodeModifier.previewAndGeneral()).joinWith(valueNode(NodeModifier.peek())),
                typeDeclarationNode(NodeModifier.peek()).joinWith(valueNode(NodeModifier.previewAndGeneral()))
        );

        return node;
    }

    @Override
    public String toString() {
        return "VarDeclarationObject{" +
                "name=" + name +
                ", mutable=" + mutable +
                ", explicitlySpecifiedType=" + explicitlySpecifiedType +
                ", value=" + value +
                '}';
    }
}
