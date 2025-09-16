package cofty.v3.parser.node.flag;

import cofty.type.exception.SyntaxError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface NodeModifier {
    boolean isGeneral();
    boolean isPeek();
    boolean isFail();
    boolean isSucceed();

    default @Nullable Exception failMessage() {
        return null;
    }

    default @NotNull Exception failMessageOrThrow() {
        return Objects.requireNonNull(failMessage());
    }

    default @Nullable SucceedApplier succeedApplier() {
        return null;
    }

    default @NotNull SucceedApplier succeedApplierOrThrow() {
        return Objects.requireNonNull(succeedApplier());
    }

    static @NotNull NodeModifier prioritize(@NotNull NodeModifier topLevelFlag, @NotNull NodeModifier currentLevelFlag) {
        if (!topLevelFlag.isGeneral())
            return topLevelFlag;

        return currentLevelFlag;
    }

    static @NotNull SucceedModifier generalSucceed(@NotNull SucceedApplier applier) {
        return new SucceedModifier(Modifiers.GENERAL, applier);
    }

    static @NotNull SucceedModifier peekSucceed(@NotNull SucceedApplier applier) {
        return new SucceedModifier(Modifiers.PEEK, applier);
    }

    static @NotNull SucceedModifier syntaxFailSucceed(@NotNull SucceedApplier applier, @NotNull String message) {
        return new SucceedModifier(syntaxFail(message), applier);
    }

    static @NotNull FailModifier fail(@NotNull Exception message) {
        return new FailModifier(message);
    }

    static @NotNull FailModifier syntaxFail(@NotNull String message) {
        return new FailModifier(new SyntaxError(message));
    }

    static @NotNull Modifiers general() {
        return Modifiers.GENERAL;
    }

    static @NotNull Modifiers peek() {
        return Modifiers.PEEK;
    }
}
