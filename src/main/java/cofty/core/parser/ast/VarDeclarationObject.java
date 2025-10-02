package cofty.core.parser.ast;

import cofty.core.lexer.token.*;
import cofty.core.parser.ast.value.ValueExpressionObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VarDeclarationObject implements AstObject, WithModifiers {
    private final boolean isFunctionArgument;

    private TypedToken<TokenType> name = null;
    private TypedToken<Keyword> mutable = null;
    private TypedToken<TokenType> explicitlySpecifiedType = null;

    private final ModifiersObject modifiers = new ModifiersObject();
    private final ValueExpressionObject value = new ValueExpressionObject();

    public VarDeclarationObject(boolean isFunctionArgument) {
        this.isFunctionArgument = isFunctionArgument;
    }

    private void setMutable(@NotNull TypedToken<?> mutable) {
        this.mutable = mutable.unsafeAs();
    }

    private void setName(@NotNull TypedToken<?> name) {
        this.name = name.unsafeAs();
    }

    private void setExplicitlySpecifiedType(@NotNull TypedToken<?> explicitlySpecifiedType) {
        this.explicitlySpecifiedType = explicitlySpecifiedType.unsafeAs();
    }

    public @Nullable TypedToken<TokenType> name() {
        return name;
    }

    public @Nullable TypedToken<Keyword> mutable() {
        return mutable;
    }

    public @Nullable TypedToken<TokenType> explicitlySpecifiedType() {
        return explicitlySpecifiedType;
    }

    public @NotNull ModifiersObject modifiers() {
        return modifiers;
    }

    public @NotNull ValueExpressionObject value() {
        return value;
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

        final var node = isFunctionArgument ? mutNode : modifiers.parserNode();

        final var variableNameNodeModifierBuilder = NodeModifier.builder()
                .syntaxFail("expected %s name".formatted(isFunctionArgument ? "an argument" : "a variable"))
                .tokenConsumer(this::setName);

        if (isFunctionArgument)
            variableNameNodeModifierBuilder.preview();
        else node.thenToken(Keyword.LET, NodeModifier.generalAndPreview()).then(mutNode);

        (isFunctionArgument ? node : node.nextOrThrow().nextOrThrow()).thenToken(
                TokenType.ID,
                variableNameNodeModifierBuilder.build()
        ).thenAnyOf(
                NodeModifier.syntaxFail("expected explicit type declaration or value assignment"),
                typeDeclarationNode(NodeModifier.generalAndPreview()).joinWith(valueNode(NodeModifier.peek())),
                typeDeclarationNode(NodeModifier.peek()).joinWith(valueNode(NodeModifier.generalAndPreview()))
        );

        return node;
    }

    @Override
    public String toString() {
        return "VarDeclarationObject{" +
                "name=" + name +
                ", mutable=" + mutable +
                ", explicitlySpecifiedType=" + explicitlySpecifiedType +
                ", modifiers=" + modifiers +
                ", value=" + value +
                '}';
    }
}
