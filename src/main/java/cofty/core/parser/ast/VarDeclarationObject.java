package cofty.core.parser.ast;

import cofty.core.lexer.token.*;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Operator;
import cofty.core.lexer.token.type.Separator;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.value.TypeDescriptionObject;
import cofty.core.parser.ast.value.ValueExpressionObject;
import cofty.core.parser.node.ParserNode;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VarDeclarationObject implements AstObject, WithModifiers {
    private final boolean isFunctionArgument;

    private TypedToken<Simple> name = null;
    private TypedToken<Keyword> mutable = null;

    private final TypeDescriptionObject explicitlySpecifiedType = new TypeDescriptionObject();
    private final ModifiersObject modifiers = new ModifiersObject();
    private final ValueExpressionObject value = new ValueExpressionObject();

    public VarDeclarationObject(boolean isFunctionArgument) {
        this.isFunctionArgument = isFunctionArgument;
    }

    private void setMutable(@NotNull TypedToken<?> mutable) {
        this.mutable = mutable.strictAs();
    }

    private void setName(@NotNull TypedToken<?> name) {
        this.name = name.strictAs();
    }

    public @Nullable TypedToken<Simple> name() {
        return name;
    }

    public @Nullable TypedToken<Keyword> mutable() {
        return mutable;
    }

    public @NotNull TypeDescriptionObject explicitlySpecifiedType() {
        return explicitlySpecifiedType;
    }

    public @NotNull ModifiersObject modifiers() {
        return modifiers;
    }

    public @NotNull ValueExpressionObject value() {
        return value;
    }

    private @NotNull ParserNode typeDeclarationNode(@NotNull NodeModifier firstModifier) {
        return ParserNode.token(
                Separator.COLON,
                firstModifier
        ).joinWith(explicitlySpecifiedType.parserNode(
                NodeModifier.syntaxFailAndDepended("expected a variable value type")
        ));
    }

    private @NotNull ParserNode valueNode(@NotNull NodeModifier firstModifier) {
        return ParserNode.token(Operator.ASSIGN, firstModifier).joinWith(
                value.parserNode(),
                NodeModifier.syntaxFailAndDepended("expected a variable value")
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
        else node.thenToken(Keyword.VAR, NodeModifier.generalAndPreview()).then(mutNode);

        (isFunctionArgument ? node : node.nextOrThrow().nextOrThrow()).thenToken(
                Simple.WORD,
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
