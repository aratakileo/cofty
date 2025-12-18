package cofty.core.semantics.symbol.scope;

import cofty.core.semantics.symbol.ChildSymbol;
import cofty.core.semantics.symbol.Symbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Set;

public abstract class ChildScope extends ChildSymbol implements Scope {
    protected final HashMap<String, Symbol> children = new HashMap<>();

    protected ChildScope(@NotNull String name) {
        super(name);
    }

    @Override
    public @Nullable Symbol resolve(@NotNull String name) {
        if (containsLocalName(name))
            return children.get(name);

        if (parent() != null) return parentOrThrow().resolve(name);

        return null;
    }

    @Override
    public void put(@NotNull String name, @NotNull Symbol symbol) {
        if (symbol instanceof ChildSymbol childSymbol)
            childSymbol.setParent(this);

        children.put(name, symbol);
    }

    @Override
    public boolean containsLocalName(@NotNull String name) {
        return children.containsKey(name);
    }

    @Override
    public @NotNull Set<String> childNames() {
        return children.keySet();
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    public int childrenCount() {
        return children.size();
    }
}
