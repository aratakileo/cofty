package cofty.v3.core.parser.node;

import cofty.core.lexer.token.ITokenType;
import cofty.core.parser.ParseContext;
import cofty.type.exception.InvalidParserNodeStateInQueue;
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
        if (queueModifier.isPrevNodeDepended() || queueModifier.isAction()) throw new IllegalStateException();

        var node = this;
        var prevNode = this;
        var cursorStart = context.cursorStart();
        var ignoreDependedNodes = false;
        var isInDependedQueue = false;

        if (queueModifier.isSnapshotMaker())
            context.createIndexSnapshot();

        while (node != null) {
            if (ignoreDependedNodes) {
                if (node.modifier().isPrevNodeDepended()) {
                    prevNode = node;
                    node = node.next();
                    continue;
                }

                ignoreDependedNodes = false;
            }

            if (isInDependedQueue && !node.modifier().isPrevNodeDepended())
                isInDependedQueue = false;

            if (node.modifier().isPrevNodeDepended()
                    && !prevNode.modifier().isPrevNodeDepended()
                    && !prevNode.modifier().isPeek()) throw new InvalidParserNodeStateInQueue();

            var isIndexSnapshotForNode = !queueModifier.isSnapshotMaker() && node.modifier().isPeek();

            if (isIndexSnapshotForNode)
                context.createIndexSnapshot();

            var isResultProceeded = node.proceed(context, queueModifier);
            var isPreviewFinished = node.modifier().isPreviewAnchor()
                    && queueModifier.isPreviewAnchor()
                    && isResultProceeded;

            if (!isResultProceeded && isIndexSnapshotForNode || isPreviewFinished) context.rollbackIndex();
            if (isResultProceeded && isIndexSnapshotForNode) context.resetIndexSnapshot();
            if (isPreviewFinished) return true;
            if (node.modifier().isPeek() && !isResultProceeded) ignoreDependedNodes = true;

            if (node.modifier().isPeek()
                    && isResultProceeded
                    && node.next() != null
                    && node.nextOrThrow().modifier().isPrevNodeDepended()) isInDependedQueue = true;

            if (isResultProceeded || isIndexSnapshotForNode) {
                prevNode = node;
                node = node.next();
                continue;
            }

            if (queueModifier.isSnapshotMaker() || queueModifier.isPreviewAnchor()) context.rollbackIndex();
            if (queueModifier.isPreviewAnchor()) return false;

            if (queueModifier.isGeneral() && !node.modifier().isFail()) {
                var errorCursorStart = context.cursorStart();

                while (node != null && !node.modifier().isFail()) {
                    if (isInDependedQueue && !node.modifier().isPrevNodeDepended())
                        throw new InvalidParserNodeStateInQueue("a closing fail modifier was expected for depended queue");

                    node = node.next();
                }

                if (node == null) throw new InvalidParserNodeStateInQueue();

                context.CRITICAL_MESSAGES.putErr(node.modifier().failMessageOrThrow(), errorCursorStart);
            }

            if (queueModifier.isFail())
                context.CRITICAL_MESSAGES.putErr(queueModifier.failMessageOrThrow(), cursorStart);

            return false;
        }

        if (!context.hasCurrent()) {
            if (queueModifier.isPreviewAnchor())
                throw new InvalidParserNodeStateInQueue("Expected that at least one node in the queue contains a preview anchor modifier");

            return true;
        }

        if (queueModifier.isSnapshotMaker()) context.resetIndexSnapshot();
        if (queueModifier.isPreviewAnchor()) return false;

        if (queueModifier.isFail())
            context.CRITICAL_MESSAGES.putErr(queueModifier.failMessageOrThrow(), cursorStart);
        else if (prevNode.modifier().isFail())
            context.CRITICAL_MESSAGES.putErrAfterToken(
                    prevNode.modifier().failMessageOrThrow(),
                    context.currentOrThrow()
            );
        else throw new InvalidParserNodeStateInQueue();

        return false;
    }

    default boolean previewQueue(@NotNull ParseContext context) {
        return proceedQueue(context, NodeModifier.previewAnchorGeneral());
    }

    <E extends ParserNode> @NotNull E then(@NotNull E next);

    default @NotNull TokenTypeNode thenToken(@NotNull ITokenType type, @NotNull NodeModifier modifier) {
        return then(ParserNode.token(type, modifier));
    }

    default @NotNull AnyOfNodeQueue.Builder thenAnyOfBuilder(@NotNull NodeModifier modifier) {
        return new AnyOfNodeQueue.Builder(modifier, this::then);
    }

    static @NotNull TokenTypeNode token(@NotNull ITokenType tokenType) {
        return new TokenTypeNode(tokenType, NodeModifier.general());
    }

    static @NotNull TokenTypeNode token(@NotNull ITokenType tokenType, @NotNull NodeModifier modifier) {
        return new TokenTypeNode(tokenType, modifier);
    }

    static @NotNull AnyOfNodeQueue.Builder anyOfBuilder(@NotNull NodeModifier modifier) {
        return new AnyOfNodeQueue.Builder(modifier, null);
    }
}
