package cofty.v3.core.semantics;

import cofty.core.message.MessageHandler;
import cofty.core.message.channel.AssociatedMessagesChannel;
import cofty.type.TextContent;
import org.jetbrains.annotations.NotNull;

public class SemanticsContext {
    public final TextContent text;
    public final MessageHandler messages;
    public final AssociatedMessagesChannel CRITICAL;

    public SemanticsContext(@NotNull TextContent text, @NotNull MessageHandler messages) {
        this.text = text;
        this.messages = messages;
        this.CRITICAL = messages.CRITICAL.associate(text);
    }
}
