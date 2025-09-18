package cofty.v3.core.parser.node.modifier;

import cofty.type.exception.SyntaxError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface NodeModifier {
    default boolean isGeneral() {
        return false;
    }

    default boolean isPeek() {
        return false;
    }

    default boolean isFail() {
        return false;
    }

    default boolean isAction() {
        return false;
    }

    default boolean isPreviewAnchor() {
        return false;
    }

    default boolean isPrevNodeDepended() {
        return false;
    }

    default boolean isSnapshotMaker() {
        return isPeek() || isPreviewAnchor();
    }

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
        return new ActionModifier(SimpleModifiers.GENERAL, action);
    }

    static @NotNull ActionModifier peekAndAction(@NotNull ModifierAction action) {
        return new ActionModifier(SimpleModifiers.PEEK, action);
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

    static @NotNull PreviewAnchorModifier previewAnchor(@NotNull NodeModifier rootModifier) {
        return new PreviewAnchorModifier(rootModifier);
    }

    static @NotNull DependedModifier depended(@NotNull NodeModifier rootModifier) {
        return new DependedModifier(rootModifier);
    }

    static @NotNull DependedModifier dependedActionOrSyntaxFail(
            @NotNull ModifierAction action,
            @NotNull String message
    ) {
        return new DependedModifier(NodeModifier.syntaxFailAndAction(action, message));
    }

    static @NotNull PreviewAnchorModifier previewAnchorGeneral() {
        return new PreviewAnchorModifier(SimpleModifiers.GENERAL);
    }

    static @NotNull SimpleModifiers general() {
        return SimpleModifiers.GENERAL;
    }

    static @NotNull SimpleModifiers peek() {
        return SimpleModifiers.PEEK;
    }
}
