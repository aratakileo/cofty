package cofty.v4.core.parser.ast;

import cofty.type.Containable;
import org.jetbrains.annotations.NotNull;

public interface AstObject extends Containable<Class<? extends AstObject>> {
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
