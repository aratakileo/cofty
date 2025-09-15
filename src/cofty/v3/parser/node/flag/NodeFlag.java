package cofty.v3.parser.node.flag;

import cofty.type.exception.SyntaxError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface NodeFlag {
    boolean isGeneral();
    boolean isPeek();
    boolean isFailMessage();

    @Nullable Exception failMessage();

    default @NotNull Exception failMessageOrThrow() {
        return Objects.requireNonNull(failMessage());
    }

    static @NotNull NodeFlag prioritize(@NotNull NodeFlag topLevelFlag, @NotNull NodeFlag currentLevelFlag) {
        if (!topLevelFlag.isGeneral())
            return topLevelFlag;

        return currentLevelFlag;
    }

    static @NotNull FailMessageFlag fail(@NotNull Exception message) {
        return new FailMessageFlag(message);
    }

    static @NotNull FailMessageFlag syntaxFail(@NotNull String message) {
        return new FailMessageFlag(new SyntaxError(message));
    }

    static @NotNull Flags general() {
        return Flags.GENERAL;
    }

    static @NotNull Flags peek() {
        return Flags.PEEK;
    }
}
