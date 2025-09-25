package cofty.v3.core.parser.node;

import cofty.core.lexer.token.ITokenType;
import cofty.core.parser.ParseContext;
import cofty.util.Cast;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

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

    default boolean previewQueue(@NotNull ParseContext context) {
        return queueIterator(context, NodeModifier.previewAndGeneral()).proceed();
    }

    <E extends ParserNode> @NotNull E then(@NotNull E next);

    default <E extends ParserNode> @NotNull ParserNode and(@NotNull E next) {
        then(next);
        return this;
    }

    default @NotNull ContainerNode then(@NotNull ParserNode node, @NotNull NodeModifier modifier) {
        return then(ParserNode.contain(node, modifier));
    }

    default @NotNull ParserNode and(@NotNull ParserNode node, @NotNull NodeModifier modifier) {
        then(ParserNode.contain(node, modifier));
        return this;
    }

    default @NotNull TokenNode thenToken(@NotNull ITokenType type, @NotNull NodeModifier modifier) {
        return then(ParserNode.token(type, modifier));
    }

    default @NotNull ParserNode andToken(@NotNull ITokenType type, @NotNull NodeModifier modifier) {
        then(ParserNode.token(type, modifier));
        return this;
    }

    default @NotNull AnyOfNode thenAnyOf(@NotNull NodeModifier modifier, @NotNull ParserNode @NotNull... nodes) {
        return then(ParserNode.anyOf(modifier, nodes));
    }

    default @NotNull RepeatedAnyOf thenRepeatedAnyOf(@NotNull NodeModifier modifier, @NotNull ParserNode @NotNull... nodes) {
        return then(ParserNode.repeatedAnyOf(modifier, nodes));
    }

    default @NotNull RepeatableQueueNode.Builder thenRepeatableQueueBuilder(@NotNull NodeModifier modifier) {
        return new RepeatableQueueNode.Builder(modifier, this::then);
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

        throw new IllegalStateException();
    }

    static @NotNull ContainerNode contain(@NotNull ParserNode containable, @NotNull NodeModifier modifier) {
        return new ContainerNode(containable, modifier);
    }

    static @NotNull TokenNode token(@NotNull ITokenType tokenType, @NotNull NodeModifier modifier) {
        return new TokenNode(tokenType, modifier);
    }

    static @NotNull RepeatableQueueNode.Builder repeatableQueueBuilder(@NotNull NodeModifier modifier) {
        return new RepeatableQueueNode.Builder(modifier, null);
    }

    static @NotNull AnyOfNode anyOf(@NotNull NodeModifier modifier, @NotNull ParserNode @NotNull... nodes) {
        return new AnyOfNode(List.of(nodes), modifier);
    }

    static @NotNull RepeatedAnyOf repeatedAnyOf(@NotNull NodeModifier modifier, @NotNull ParserNode @NotNull... nodes) {
        return new RepeatedAnyOf(List.of(nodes), modifier);
    }

    static <T extends ParserNode> @NotNull AnyOfNode anyOf(
            @NotNull NodeModifier modifier,
            @NotNull List<@NotNull T> nodes
    ) {
        return new AnyOfNode(Cast.unsafe(nodes), modifier);
    }

    static <T extends ParserNode> @NotNull RepeatedAnyOf repeatableAnyOf(
            @NotNull NodeModifier modifier,
            @NotNull List<@NotNull T> nodes
    ) {
        return new RepeatedAnyOf(Cast.unsafe(nodes), modifier);
    }
}
