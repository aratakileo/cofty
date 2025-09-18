package cofty.v3.core.parser.node;

import cofty.core.message.MessageBuilder;
import cofty.core.parser.ParseContext;
import cofty.core.lexer.token.ITokenType;
import cofty.core.lexer.token.Keyword;
import cofty.core.lexer.token.Separator;
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
        if (queueModifier.isPrevNodeDepended()) throw new IllegalStateException();

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

                context.putErrorMessage(node.modifier().failMessageOrThrow(), errorCursorStart);
            }

            if (queueModifier.isFail())
                context.putErrorMessage(queueModifier.failMessageOrThrow(), cursorStart);

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
            context.putErrorMessage(queueModifier.failMessageOrThrow(), cursorStart);
        else if (prevNode.modifier().isFail())
            context.messages.putBuildedMessage(MessageBuilder.errAfter(
                    context.text,
                    context.currentOrThrow(),
                    prevNode.modifier().failMessageOrThrow()
            ));
        else throw new InvalidParserNodeStateInQueue();

        return false;
    }

    <E extends ParserNode> @NotNull E then(@NotNull E next);

    default @NotNull TokenTypeNode thenToken(@NotNull ITokenType type, @NotNull NodeModifier modifier) {
        return then(ParserNode.token(type, modifier));
    }

    static @NotNull TokenTypeNode token(@NotNull ITokenType tokenType) {
        return new TokenTypeNode(tokenType, NodeModifier.general());
    }

    static @NotNull TokenTypeNode token(@NotNull ITokenType tokenType, @NotNull NodeModifier modifier) {
        return new TokenTypeNode(tokenType, modifier);
    }

    static @NotNull TokenTypeNode tokenOrSyntaxFail(@NotNull Keyword keyword) {
        return new TokenTypeNode(keyword, NodeModifier.syntaxFail("expected `" + keyword.name().toLowerCase() + "` keyword"));
    }

    static @NotNull TokenTypeNode tokenOrSyntaxFail(@NotNull Separator separator) {
        return new TokenTypeNode(separator, NodeModifier.syntaxFail("expected `" + separator.sep + "` separator"));
    }
}
