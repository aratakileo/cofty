package cofty.core.message.channel;

import cofty.core.message.Message;
import cofty.core.message.MessageType;
import cofty.core.parser.ParseContext;
import cofty.type.TextContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class MessagesChannel {
    private final MessageChannel channel;
    private final ArrayList<Message> messages = new ArrayList<>();

    public MessagesChannel(@NotNull MessageChannel channel) {
        this.channel = channel;
    }

    public void put(@NotNull Message message) {
        if (message.type == MessageType.WARNING && channel == MessageChannel.CRITICAL)
            throw new IllegalStateException("Warning is not a critical message");

        messages.add(message);
    }

    public int count() {
        return messages.size();
    }

    public void print() {
        for (final var message: messages)
            System.out.println(message);
    }

    public @NotNull AssociatedMessagesChannel associate(@NotNull TextContent text) {
        return new AssociatedMessagesChannel(text, this);
    }

    public @NotNull ParserMessagesChannel associate(@NotNull ParseContext context) {
        return new ParserMessagesChannel(context, this);
    }

    public @Nullable Message get(int index) {
        return messages.get(index);
    }
}
