package cofty.v3.parser.node;

import cofty.core.message.MessageBuilder;
import cofty.core.parse.ParseContext;
import cofty.core.token.ITokenType;
import cofty.core.token.Keyword;
import cofty.core.token.Separator;
import cofty.v3.parser.node.flag.NodeFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface ParserNode {
    @Nullable ParserNode next();
    default @NotNull ParserNode nextOrThrow() {
        return Objects.requireNonNull(next());
    }

    @NotNull NodeFlag flag();

    boolean proceed(@NotNull ParseContext context, @NotNull NodeFlag topLevelFlag);
    default boolean proceedQueue(@NotNull ParseContext context, @NotNull NodeFlag queueFlag) {
        var node = this;
        var prevNode = this;
        var cursorStart = context.cursorStart();

        if (queueFlag.isPeek())
            context.createIndexSnapshot();

        while (node != null) {
            var indexSnapshotForNode = !queueFlag.isPeek() && node.flag().isPeek();

            if (indexSnapshotForNode)
                context.createIndexSnapshot();

            if (node.proceed(context, queueFlag) || indexSnapshotForNode) {
                prevNode = node;
                node = node.next();

                if (indexSnapshotForNode) context.rollbackIndex();

                continue;
            }

            if (queueFlag.isPeek())
                context.rollbackIndex();

            if (queueFlag.isGeneral() && !node.flag().isFailMessage()) {
                var errorCursorStart = context.cursorStart();

                while (node != null && !node.flag().isFailMessage())
                    node = node.next();

                if (node == null)
                    throw new IllegalStateException();

                context.putErrorMessage(node.flag().failMessageOrThrow(), errorCursorStart);
            }

            if (queueFlag.isFailMessage())
                context.putErrorMessage(queueFlag.failMessageOrThrow(), cursorStart);

            return false;
        }

        if (!context.hasCurrent()) return true;

        if (queueFlag.isPeek())
            context.resetIndexSnapshot();

        if (queueFlag.isFailMessage())
            context.putErrorMessage(queueFlag.failMessageOrThrow(), cursorStart);
        else if (prevNode.flag().isFailMessage())
            context.messages.putBuildedMessage(MessageBuilder.errAfter(
                    context.text,
                    context.currentOrThrow(),
                    prevNode.flag().failMessageOrThrow()
            ));
        else throw new IllegalStateException();

        return false;

    }

    <E extends ParserNode> @NotNull E then(@NotNull E next);

    static @NotNull TokenTypeNode token(@NotNull ITokenType tokenType) {
        return new TokenTypeNode(tokenType, NodeFlag.general());
    }

    static @NotNull TokenTypeNode token(@NotNull ITokenType tokenType, @NotNull NodeFlag flag) {
        return new TokenTypeNode(tokenType, flag);
    }

    static @NotNull TokenTypeNode tokenOrSyntaxFail(@NotNull Keyword keyword) {
        return new TokenTypeNode(keyword, NodeFlag.syntaxFail("expected `" + keyword.name().toLowerCase() + "` keyword"));
    }

    static @NotNull TokenTypeNode tokenOrSyntaxFail(@NotNull Separator separator) {
        return new TokenTypeNode(separator, NodeFlag.syntaxFail("expected `" + separator.sep + "` separator"));
    }
}
