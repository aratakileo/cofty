package cofty.v4.core.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ParseResult<R> {
    private final R value;
    private final boolean isCanceled;

    private ParseResult(@Nullable R value, boolean isCanceled) {
        this.value = value;
        this.isCanceled = isCanceled;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public boolean isSuccessful() {
        return value != null;
    }

    public boolean isFailed() {
        return value == null && !isCanceled;
    }

    public @Nullable R value() {
        return value;
    }

    public @NotNull R valueOrThrow() {
        return Objects.requireNonNull(value);
    }

    public @NotNull R valueOrDefault(@NotNull R defaultValue) {
        return isSuccessful() ? valueOrThrow() : defaultValue;
    }

    public static <R> @NotNull ParseResult<R> successful(@NotNull R result) {
        return new ParseResult<>(result, false);
    }

    public static <R> @NotNull ParseResult<R> failed() {
        return new ParseResult<>(null, false);
    }

    public static <R> @NotNull ParseResult<R> canceled() {
        return new ParseResult<>(null, true);
    }
}
