package cofty.core.parse;

import org.jetbrains.annotations.NotNull;

public interface AstPeeker {
    boolean safePeek(@NotNull ParseContext context);

    default boolean peek(@NotNull ParseContext context) {
        return context.hasCurrent() && safePeek(context);
    }
}
