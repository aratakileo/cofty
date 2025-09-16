package cofty.type.exception;

import org.jetbrains.annotations.NotNull;

public class InvalidRootNodeModifier extends RuntimeException {
    public InvalidRootNodeModifier(@NotNull String message) {
        super(message);
    }
}
