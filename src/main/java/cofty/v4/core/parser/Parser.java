package cofty.v4.core.parser;

import org.jetbrains.annotations.NotNull;

public interface Parser<R> {
    @NotNull ParseResult<R> parse(@NotNull ParseContext context);
}
