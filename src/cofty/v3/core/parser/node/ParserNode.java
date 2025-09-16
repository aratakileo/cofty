package cofty.v3.core.parser.node;

import cofty.core.message.MessageBuilder;
import cofty.core.parse.ParseContext;
import cofty.core.token.ITokenType;
import cofty.core.token.Keyword;
import cofty.core.token.Separator;
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
        var node = this;
        var prevNode = this;
        var cursorStart = context.cursorStart();

        if (queueModifier.isPeek())
            context.createIndexSnapshot();

        while (node != null) {
            var indexSnapshotForNode = !queueModifier.isPeek() && node.modifier().isPeek();

            if (indexSnapshotForNode)
                context.createIndexSnapshot();

            var proceedResult = node.proceed(context, queueModifier);

            if (!proceedResult && indexSnapshotForNode)
                context.rollbackIndex();

            if (proceedResult && indexSnapshotForNode)
                context.resetIndexSnapshot();

            if (proceedResult || indexSnapshotForNode) {
                prevNode = node;
                node = node.next();
                continue;
            }

            if (queueModifier.isPeek())
                context.rollbackIndex();

            if (queueModifier.isGeneral() && !node.modifier().isFail()) {
                var errorCursorStart = context.cursorStart();

                while (node != null && !node.modifier().isFail())
                    node = node.next();

                if (node == null)
                    throw new IllegalStateException();

                context.putErrorMessage(node.modifier().failMessageOrThrow(), errorCursorStart);
            }

            if (queueModifier.isFail())
                context.putErrorMessage(queueModifier.failMessageOrThrow(), cursorStart);

            return false;
        }

        if (!context.hasCurrent()) return true;

        if (queueModifier.isPeek())
            context.resetIndexSnapshot();

        if (queueModifier.isFail())
            context.putErrorMessage(queueModifier.failMessageOrThrow(), cursorStart);
        else if (prevNode.modifier().isFail())
            context.messages.putBuildedMessage(MessageBuilder.errAfter(
                    context.text,
                    context.currentOrThrow(),
                    prevNode.modifier().failMessageOrThrow()
            ));
        else throw new IllegalStateException();

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
