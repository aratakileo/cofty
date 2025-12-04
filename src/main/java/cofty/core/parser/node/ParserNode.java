package cofty.core.parser.node;

import cofty.core.lexer.token.type.TokenType;
import cofty.core.parser.ParseContext;
import cofty.util.Cast;
import cofty.core.parser.node.modifier.ModifierType;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Deprecated
public interface ParserNode {
    @Nullable ParserNode next();
    default @NotNull ParserNode nextOrThrow() {
        return Objects.requireNonNull(next());
    }

    @NotNull NodeModifier modifier();

    boolean proceed(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier);

    default @NotNull NodeQueueIterator queueIterator(@NotNull ParseContext context, @NotNull NodeModifier queueModifier) {
        return new NodeQueueIterator(context, queueModifier, this);
    }

    default boolean proceedQueue(@NotNull ParseContext context, @NotNull NodeModifier queueModifier) {
        return queueIterator(context, queueModifier).proceed();
    }

    default boolean previewQueue(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier) {
        return queueIterator(
                context,
                topLevelModifier.is(ModifierType.PREVIEW) ? topLevelModifier : NodeModifier.generalAndPreview()
        ).proceed();
    }

    <E extends ParserNode> @NotNull E then(@NotNull E next);

    default @NotNull ContainerNode then(@NotNull ParserNode node, @NotNull NodeModifier modifier) {
        return then(ParserNode.contain(node, modifier));
    }

    default @NotNull TokenNode thenToken(@NotNull TokenType type, @NotNull NodeModifier modifier) {
        return then(ParserNode.token(type, modifier));
    }

    default @NotNull AnyOfNode thenAnyOf(@NotNull NodeModifier modifier, @NotNull ParserNode @NotNull... nodes) {
        return then(ParserNode.anyOf(modifier, nodes));
    }

    default @NotNull RepeatedAnyOfNode thenRepeatedAnyOf(@NotNull NodeModifier modifier, @NotNull ParserNode @NotNull... nodes) {
        return then(ParserNode.repeatedAnyOf(modifier, nodes));
    }

    default @NotNull RepeatableQueueNode.Builder thenRepeatableQueueBuilder(@NotNull NodeModifier modifier) {
        return new RepeatableQueueNode.Builder(modifier, this::then);
    }

    default @NotNull ParserNode joinWithToken(@NotNull TokenType type, @NotNull NodeModifier modifier) {
        return joinWith(ParserNode.token(type, modifier));
    }

    default @NotNull ParserNode joinWith(@NotNull ParserNode node, @NotNull NodeModifier modifier) {
        return joinWith(ParserNode.contain(node, modifier));
    }

    default @NotNull ParserNode joinWith(@NotNull ParserNode parserNode) {
        if (next() == null) {
            then(parserNode);
            return this;
        }

        if (nextOrThrow().next() == null) {
            nextOrThrow().then(parserNode);
            return this;
        }

        if (nextOrThrow().nextOrThrow().next() == null) {
            nextOrThrow().nextOrThrow().then(parserNode);
            return this;
        }

        throw new IllegalStateException();
    }

    static @NotNull ContainerNode contain(@NotNull ParserNode containable, @NotNull NodeModifier modifier) {
        return new ContainerNode(containable, modifier);
    }

    static @NotNull TokenNode token(@NotNull TokenType tokenType, @NotNull NodeModifier modifier) {
        return new TokenNode(tokenType, modifier);
    }

    static @NotNull OperatorExpressionNode operatorExpression(@NotNull NodeModifier modifier) {
        return new OperatorExpressionNode(modifier);
    }

    static @NotNull RepeatableQueueNode.Builder repeatableQueueBuilder(@NotNull NodeModifier modifier) {
        return new RepeatableQueueNode.Builder(modifier, null);
    }

    static @NotNull AnyOfNode anyOf(@NotNull NodeModifier modifier, @NotNull ParserNode @NotNull... nodes) {
        return new AnyOfNode(List.of(nodes), modifier);
    }

    static @NotNull RepeatedAnyOfNode repeatedAnyOf(@NotNull NodeModifier modifier, @NotNull ParserNode @NotNull... nodes) {
        return new RepeatedAnyOfNode(List.of(nodes), modifier);
    }

    static <T extends ParserNode> @NotNull AnyOfNode anyOf(
            @NotNull NodeModifier modifier,
            @NotNull List<@NotNull T> nodes
    ) {
        return new AnyOfNode(Cast.quiet(nodes), modifier);
    }

    static <T extends ParserNode> @NotNull RepeatedAnyOfNode repeatableAnyOf(
            @NotNull NodeModifier modifier,
            @NotNull List<@NotNull T> nodes
    ) {
        return new RepeatedAnyOfNode(Cast.quiet(nodes), modifier);
    }
}
