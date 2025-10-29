package cofty.core.message;

import cofty.core.message.channel.MessageChannel;
import cofty.core.message.channel.MessagesChannel;

@Deprecated
public class MessageHandler {
    public final MessagesChannel CRITICAL = new MessagesChannel(MessageChannel.CRITICAL),
            NON_CRITICAL = new MessagesChannel(MessageChannel.NON_CRITICAL);

    public boolean hasCriticalErrors() {
        return CRITICAL.count() != 0;
    }

    public int count() {
        return CRITICAL.count() + NON_CRITICAL.count();
    }

    public boolean isEmpty() {
        return count() == 0;
    }

    public void print() {
        CRITICAL.print();
        NON_CRITICAL.print();
    }
}
