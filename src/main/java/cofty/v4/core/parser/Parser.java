package cofty.v4.core.parser;

import cofty.core.parser.ParseContext;
import org.jetbrains.annotations.NotNull;

public interface Parser<R> {
    @NotNull ParseResult<R> parse(@NotNull ParseContext context);
}
