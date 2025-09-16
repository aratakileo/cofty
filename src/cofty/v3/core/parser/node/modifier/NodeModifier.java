package cofty.v3.core.parser.node.modifier;

import cofty.type.exception.SyntaxError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface NodeModifier {
    boolean isGeneral();
    boolean isPeek();
    boolean isFail();
    boolean isAction();

    default @Nullable Exception failMessage() {
        return null;
    }

    default @NotNull Exception failMessageOrThrow() {
        return Objects.requireNonNull(failMessage());
    }

    default @Nullable ModifierAction action() {
        return null;
    }

    default @NotNull ModifierAction actionOrThrow() {
        return Objects.requireNonNull(action());
    }

    static @NotNull NodeModifier prioritize(@NotNull NodeModifier topLevelFlag, @NotNull NodeModifier currentLevelFlag) {
        if (!topLevelFlag.isGeneral())
            return topLevelFlag;

        return currentLevelFlag;
    }

    static @NotNull ActionModifier generalAndAction(@NotNull ModifierAction action) {
        return new ActionModifier(Modifiers.GENERAL, action);
    }

    static @NotNull ActionModifier peekAndAction(@NotNull ModifierAction action) {
        return new ActionModifier(Modifiers.PEEK, action);
    }

    static @NotNull ActionModifier syntaxFailAndAction(@NotNull ModifierAction action, @NotNull String message) {
        return new ActionModifier(syntaxFail(message), action);
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
