package cofty.core.semantics.symbol.scope;

import cofty.core.semantics.symbol.*;
import cofty.core.semantics.symbol.path.SymbolPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public sealed abstract class FuncScope<T extends SymbolPath<T>> extends ChildScope implements WithValueType<T>
        permits IncompletedFuncScope, CompletedFuncScope {

    public final ArgsSignature<T> argsSignature;
    public final TypeDescriptor<T> returnedValueTypePath;

    protected FuncScope(
            @NotNull String name,
            @NotNull ArgsSignature<T> argsSignature,
            @NotNull TypeDescriptor<T> returnedValueTypePath
    ) {
        super(name);

        this.returnedValueTypePath = returnedValueTypePath;
        this.argsSignature = argsSignature;
    }

    @Override
    public void setParent(@NotNull Scope parent) {
        if (this.parent != null)
            throw new IllegalCallerException();

        this.parent = parent;
        this.absPath = parent.absPath().merge(argsSignature.minimumSignature);

        if (this instanceof CompletedFuncScope)
            argsSignature.setParent(this);
    }

    @Override
    public boolean containsLocalName(@NotNull String name) {
        return argsSignature.containsName(name) || super.containsLocalName(name);
    }

    @Override
    public @Nullable Symbol resolve(@NotNull String name) {
        if (argsSignature.containsName(name))
            return argsSignature.getField(name);

        return super.resolve(name);
    }

    @Override
    public @NotNull Set<String> childNames() {
        if (this instanceof CompletedFuncScope)
            return Stream.concat(super.childNames().stream(), argsSignature.names().stream()).collect(Collectors.toSet());

        return super.childNames();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && argsSignature.isEmpty();
    }

    @Override
    public int childrenCount() {
        return super.childrenCount() + argsSignature.size();
    }

    @Override
    public @Nullable TypeDescriptor<T> valueType() {
        return returnedValueTypePath;
    }

    @Override
    public @NotNull String representedHeader() {
        return String.format(
                "%s (%s) -> %s",
                super.representedHeader(),
                IntStream.range(0, argsSignature.size())
                        .mapToObj(i -> argsSignature.getName(i) + ": " + argsSignature.getType(i))
                        .collect(Collectors.joining(", ")),
                returnedValueTypePath
        );
    }
}
