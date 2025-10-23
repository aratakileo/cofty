package cofty.core.message.channel;

import cofty.core.parser.ParseContext;
import cofty.type.exception.SyntaxError;
import org.jetbrains.annotations.NotNull;

public class ParserMessagesChannel extends AssociatedMessagesChannel {
    public final ParseContext context;

    protected ParserMessagesChannel(@NotNull ParseContext context, @NotNull MessagesChannel channel) {
        super(context.text, channel);
        this.context = context;
    }

    public void putSyntaxErr(@NotNull String message) {
        putErr(new SyntaxError(message));
    }

    public void putErr(@NotNull Exception err) {
        final var token = context.nonNewLineCursorOrPrev();

        if (context.hasNonNewLineCurrent()) putErr(err, token);
        else putErrAfterToken(err, token);
    }

    public void putErr(@NotNull Exception err, int cursorStart) {
        putErr(err, cursorStart, context.nonNewLineCursorOrPrev().end);
    }
}
