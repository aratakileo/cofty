package cofty.core.message.channel;

import cofty.core.lexer.token.TypedToken;
import cofty.core.message.Message;
import cofty.core.message.MessageBuilder;
import cofty.v4.core.compiler.message.MessageType;
import cofty.type.TextContent;
import cofty.type.exception.SyntaxError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
public class AssociatedMessagesChannel {
    public final TextContent text;
    public final MessagesChannel channel;

    protected AssociatedMessagesChannel(@NotNull TextContent text, @NotNull MessagesChannel channel) {
        this.text = text;
        this.channel = channel;
    }

    public void putSyntaxErrAfterToken(
            @NotNull String errMessage,
            @NotNull TypedToken<?> token
    ) {
        putAfterToken(MessageType.ERROR, new SyntaxError(errMessage), token);
    }

    public void putErrAfterToken(
            @NotNull Exception err,
            @NotNull TypedToken<?> token
    ) {
        putAfterToken(MessageType.ERROR, err, token);
    }

    public void putAfterToken(
            @NotNull MessageType messageType,
            @NotNull Exception err,
            @NotNull TypedToken<?> token
    ) {
        channel.put(
                new MessageBuilder(messageType, text)
                        .setContent(err)
                        .fillCursor(token)
                        .setCursorLengthByRight(1)
                        .moveCursor(1)
                        .build()
        );
    }

    public void putSyntaxErr(
            @NotNull String errMessage,
            @NotNull TypedToken<?> token
    ) {
        put(MessageType.ERROR, new SyntaxError(errMessage), token.start, token.end);
    }

    public void putErr(
            @NotNull Exception err,
            @NotNull TypedToken<?> token
    ) {
        put(MessageType.ERROR, err, token.start, token.end);
    }

    public void putErr(
            @NotNull Exception err,
            int cursorStart,
            int cursorEnd
    ) {
        put(MessageType.ERROR, err, cursorStart, cursorEnd);
    }

    public void put(@NotNull Message msg) {
        channel.put(msg);
    }

    public void put(
            @NotNull MessageType messageType,
            @NotNull Exception msg,
            int cursorStart,
            int cursorEnd
    ) {
        channel.put(
                new MessageBuilder(messageType, text).setContent(msg).fillCursor(cursorStart, cursorEnd).build()
        );
    }

    public @Nullable Message get(int index) {
        return channel.get(index);
    }
}
