package cofty.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class Result<OK, ERR> {
    protected final OK ok;
    protected final ERR err;

    protected Result(@Nullable OK ok, @Nullable ERR err) {
        this.ok = ok;
        this.err = err;
    }

    public boolean isOk() {
        return ok != null;
    }

    public boolean isErr() {
        return err != null;
    }

    public @Nullable OK unwrap() {
        return ok;
    }

    public @NotNull OK unwrapOrThrow() {
        if (ok != null)
            return ok;

        throw new IllegalStateException("Called unwrap() on the error value");
    }

    public @NotNull ERR unwrapErrOrThrow() {
        if (err != null)
            return err;

        throw new IllegalStateException("Called unwrapErr() on the ok value");
    }

    @SuppressWarnings("unchecked")
    public <NEW_OK> @NotNull Result<NEW_OK, ERR> map(@NotNull Function<OK, NEW_OK> mapper) {
        if (isErr())
            return (Result<NEW_OK, ERR>)this;

        return new Result<>(mapper.apply(ok), null);
    }

    @SuppressWarnings("unchecked")
    public <NEW_ERR> @NotNull Result<OK, NEW_ERR> mapErr(@NotNull Function<ERR, NEW_ERR> mapper) {
        if (isOk())
            return (Result<OK, NEW_ERR>)this;

        return new Result<>(null, mapper.apply(err));
    }

    public static <OK, ERR> Result<OK, ERR> ok(@NotNull OK ok) {
        return new Result<>(ok, null);
    }

    public static <OK, ERR> Result<OK, ERR> err(@NotNull ERR err) {
        return new Result<>(null, err);
    }
}
