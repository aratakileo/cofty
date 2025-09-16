package cofty.v2.core.ast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AstValueList<E extends AstValue> extends ArrayList<E> implements AstValue {
    @Override
    public @NotNull E set(int index, @NotNull E element) {
        return super.set(index, Objects.requireNonNull(element));
    }

    @Override
    public boolean add(@NotNull E e) {
        return super.add(Objects.requireNonNull(e));
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return super.addAll(c.stream().map(Objects::requireNonNull).toList());
    }

    public @NotNull AstValueList<E> add(@NotNull E value, E... values) {
        add(value);
        addAll(List.of(values));
        return this;
    }

    public static <E extends AstValue> AstValueList<E> of(@NotNull E value) {
        final var list = new AstValueList<E>();
        list.add(value);
        return list;
    }

    @SafeVarargs
    public static <E extends AstValue> AstValueList<E> of(@NotNull E value, @NotNull E... values) {
        final var list = new AstValueList<E>();

        list.add(value);
        list.addAll(List.of(values));

        return list;
    }

    public static <E extends AstValue> AstValueList<E> of(@NotNull Collection<E> collection) {
        final var list = new AstValueList<E>();
        list.addAll(collection);
        return list;
    }
}
