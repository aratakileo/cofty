package cofty.core.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ParseResult<R> {
    private final R value;
    private final boolean isSkipped;

    private ParseResult(@Nullable R value, boolean isSkipped) {
        this.value = value;
        this.isSkipped = isSkipped;
    }

    public boolean isSkipped() {
        return isSkipped;
    }

    public boolean isOK() {
        return value != null;
    }

    public boolean isFailed() {
        return value == null && !isSkipped;
    }

    public @Nullable R value() {
        return value;
    }

    public @NotNull R valueOrThrow() {
        return Objects.requireNonNull(value);
    }

    public @NotNull R valueOrDefault(@NotNull R defaultValue) {
        return isOK() ? valueOrThrow() : defaultValue;
    }

    public static <R> @NotNull ParseResult<R> OK(@NotNull R result) {
        return new ParseResult<>(result, false);
    }

    public static <R> @NotNull ParseResult<R> failed() {
        return new ParseResult<>(null, false);
    }

    public static <R> @NotNull ParseResult<R> skipped() {
        return new ParseResult<>(null, true);
    }
}
