package cofty.core.parser.ast;

import cofty.type.Containable;
import org.jetbrains.annotations.NotNull;

public interface AstObject extends Containable<Class<? extends AstObject>> {
    @NotNull String prettyString(@NotNull String offset, int increase);

    default @NotNull String prettyString(int leftOffset, int increase) {
        return prettyString(" ".repeat(leftOffset), increase);
    }

    default @NotNull String prettyString(int offset) {
        return prettyString("", offset);
    }

    default @NotNull String prettyString() {
        return prettyString(3);
    }

    default boolean is(@NotNull Class<? extends AstObject> astObjectClass) {
        return astObjectClass.isInstance(this);
    }

    default boolean isAny(@NotNull Class<? extends AstObject> @NotNull... astObjectClasses) {
        for (final var astObjectClass: astObjectClasses)
            if (astObjectClass.isInstance(this))
                return true;

        return false;
    }
}
