package cofty.v3.core.parser.node;

import cofty.core.lexer.token.ITokenType;
import cofty.core.parser.ParseContext;
import cofty.type.exception.InvalidParserNodeStateInQueue;
import cofty.v3.core.parser.node.modifier.ModifierType;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface ParserNode {
    @Nullable ParserNode next();
    default @NotNull ParserNode nextOrThrow() {
        return Objects.requireNonNull(next());
    }

    @NotNull NodeModifier modifier();

    boolean proceed(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier);

    default boolean proceedQueue(@NotNull ParseContext context, @NotNull NodeModifier queueModifier) {
        if (queueModifier.isAny(ModifierType.ACTION, ModifierType.DEPENDED)) throw new IllegalStateException();

        var node = this;
        var prevNode = (ParserNode)null;
        var failMessageCursorStart = context.nonNewLineCursorOrPrev().start;
        var ignoreDependedNodes = false;
        var isInDependedQueue = false;

        if (queueModifier.isAny(ModifierType.PREVIEW, ModifierType.PEEK)) context.createIndexSnapshot();

        while (node != null) {
            if (ignoreDependedNodes) {
                if (node.modifier().is(ModifierType.DEPENDED)) {
                    prevNode = node;
                    node = node.next();
                    continue;
                }

                ignoreDependedNodes = false;
            }

            if (isInDependedQueue && !node.modifier().is(ModifierType.DEPENDED))
                isInDependedQueue = false;

            if (node.modifier().is(ModifierType.DEPENDED) && !prevNode.modifier().isAny(
                    ModifierType.DEPENDED,
                    ModifierType.PEEK
            )) throw new InvalidParserNodeStateInQueue();

            var isIndexSnapshotForNode = !queueModifier.isAny(ModifierType.PREVIEW, ModifierType.PEEK)
                    && node.modifier().is(ModifierType.PEEK);

            if (isIndexSnapshotForNode) context.createIndexSnapshot();

            var isResultProceeded = node.proceed(context, queueModifier);
            var isPreviewFinished = node.modifier().is(ModifierType.PREVIEW)
                    && queueModifier.is(ModifierType.PREVIEW)
                    && isResultProceeded;

            if (!isResultProceeded && isIndexSnapshotForNode || isPreviewFinished) context.rollbackIndex();
            if (isResultProceeded && isIndexSnapshotForNode) context.removeIndexSnapshot();

            if (isPreviewFinished) return true;
            if (node.modifier().is(ModifierType.PEEK) && !isResultProceeded) ignoreDependedNodes = true;

            if (node.modifier().is(ModifierType.PEEK)
                    && isResultProceeded
                    && node.next() != null
                    && node.nextOrThrow().modifier().is(ModifierType.DEPENDED)) isInDependedQueue = true;

            if (isResultProceeded || isIndexSnapshotForNode) {
                prevNode = node;
                node = node.next();
                continue;
            }

            if (queueModifier.isAny(ModifierType.PEEK, ModifierType.PREVIEW)) context.rollbackIndex();
            if (queueModifier.is(ModifierType.PREVIEW)) return false;

            if (queueModifier.is(ModifierType.GENERAL) && !node.modifier().is(ModifierType.FAIL)) {
                var errorCursorStart = context.nonNewLineCursorOrPrev().start;

                while (node != null && !node.modifier().is(ModifierType.FAIL)) {
                    if (isInDependedQueue && !node.modifier().is(ModifierType.DEPENDED))
                        throw new InvalidParserNodeStateInQueue("a closing fail modifier was expected for depended queue");

                    node = node.next();
                }

//                if (node == null) throw new InvalidParserNodeStateInQueue();

                if (node == null) return false;

                context.CRITICAL_MESSAGES.putErr(node.modifier().failMessageOrThrow(), errorCursorStart);
            }

            if (queueModifier.is(ModifierType.FAIL)) context.CRITICAL_MESSAGES.putErr(
                    queueModifier.failMessageOrThrow(),
                    failMessageCursorStart
            );
            return false;
        }

//        if (!context.hasCurrent()) {
//            if (queueModifier.is(ModifierType.PREVIEW))
//                throw new InvalidParserNodeStateInQueue("Expected that at least one node in the queue contains a preview anchor modifier");
//
//            return true;
//        }

        if (queueModifier.isAny(ModifierType.PREVIEW, ModifierType.PEEK)) context.removeIndexSnapshot();
        if (queueModifier.is(ModifierType.PREVIEW)) return false;

//        if (queueModifier.is(ModifierType.FAIL))
//            context.CRITICAL_MESSAGES.putErr(queueModifier.failMessageOrThrow(), cursorStart);
//        else if (prevNode.modifier().is(ModifierType.FAIL))
//            context.CRITICAL_MESSAGES.putErrAfterToken(
//                    prevNode.modifier().failMessageOrThrow(),
//                    context.currentOrThrow()
//            );
//        else throw new InvalidParserNodeStateInQueue();

        return true;
    }

    default boolean previewQueue(@NotNull ParseContext context) {
        return proceedQueue(context, NodeModifier.previewAndGeneral());
    }

    <E extends ParserNode> @NotNull E then(@NotNull E next);

    default @NotNull TokenNode thenToken(@NotNull ITokenType type) {
        return then(ParserNode.token(type, NodeModifier.general()));
    }

    default @NotNull TokenNode thenToken(@NotNull ITokenType type, @NotNull NodeModifier modifier) {
        return then(ParserNode.token(type, modifier));
    }

    default @NotNull RepeatableQueueNode.Builder thenAnyOfBuilder(@NotNull NodeModifier modifier) {
        return new RepeatableQueueNode.Builder(modifier, this::then);
    }

    static @NotNull TokenNode token(@NotNull ITokenType tokenType) {
        return new TokenNode(tokenType, NodeModifier.general());
    }

    static @NotNull TokenNode token(@NotNull ITokenType tokenType, @NotNull NodeModifier modifier) {
        return new TokenNode(tokenType, modifier);
    }

    static @NotNull RepeatableQueueNode.Builder repeatableQueueBuilder(@NotNull NodeModifier modifier) {
        return new RepeatableQueueNode.Builder(modifier, null);
    }
}
