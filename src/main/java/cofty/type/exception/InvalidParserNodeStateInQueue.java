package cofty.type.exception;

import org.jetbrains.annotations.NotNull;

public class InvalidParserNodeStateInQueue extends RuntimeException {
    public InvalidParserNodeStateInQueue(@NotNull String message) {
        super(message);
    }

    public InvalidParserNodeStateInQueue() {}
}
