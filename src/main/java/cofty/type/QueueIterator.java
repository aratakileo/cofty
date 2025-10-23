package cofty.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface QueueIterator<T> {
    boolean hasNext();

    @Nullable T current();
    @Nullable T goNext();

    default @NotNull T goNextOrThrow() {
        return Objects.requireNonNull(goNext());
    }

    @Nullable T next();
    @Nullable T prev();

    default boolean hasCurrent() {
        return current() != null;
    }

    default boolean hasPrev() {
        return prev() != null;
    }

    default @NotNull T currentOrThrow() {
        return Objects.requireNonNull(current());
    }

    default @NotNull T nextOrThrow() {
        return Objects.requireNonNull(next());
    }

    default @NotNull T prevOrThrow() {
        return Objects.requireNonNull(prev());
    }
}
