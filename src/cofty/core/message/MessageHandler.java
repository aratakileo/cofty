package cofty.core.message;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MessageHandler {
    private final ArrayList<String> messages = new ArrayList<>();

    public void putBuildedMessage(@NotNull String message) {
        messages.add(message);
    }

    public void print() {
        for (final var message: messages)
            System.out.println(message);
    }
}
