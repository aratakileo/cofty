package cofty.core.semantics.symbol.scope;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.FuncDeclarationObject;
import cofty.core.parser.ast.TypeDescriptionObject;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import cofty.core.semantics.symbol.FieldSymbol;
import cofty.core.semantics.symbol.Symbol;
import cofty.core.semantics.symbol.WithValueType;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed abstract class FuncScope<T> extends NamedScope implements WithValueType<T> permits IncompletedFuncScope, CompletedFuncScope {
    protected final LinkedHashMap<String, FieldSymbol<T>> args = new LinkedHashMap<>();

    private final T returnedValueTypePath;

    protected FuncScope(
            @NotNull TypedToken<Simple> name,
            @NotNull T returnedValueTypePath,
            @NotNull List<? extends FieldSymbol<T>> args
    ) {
        super(name);
        this.returnedValueTypePath = returnedValueTypePath;

        for (final var arg: args) {
            this.args.put(arg.name(), arg);
        }
    }

    @Override
    public void setParent(@NotNull Scope parent) {
        super.setParent(parent);

        for (final var arg: args.sequencedValues())
            arg.setParent(this);
    }

    @Override
    public boolean containsName(@NotNull String name) {
        return args.containsKey(name) || super.containsName(name);
    }

    @Override
    public @Nullable Symbol resolve(@NotNull String name) {
        if (args.containsKey(name))
            return args.get(name);

        return super.resolve(name);
    }

    @Override
    public @NotNull Set<String> childNames() {
        return Stream.concat(super.childNames().stream(), args.keySet().stream()).collect(Collectors.toSet());
    }

    @Override
    public @NotNull T valueType() {
        return returnedValueTypePath;
    }

    @Override
    public @NotNull String representedHeader() {
        return String.format(
                "%s (%s) -> %s",
                super.representedHeader(),
                args.entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + ": " + entry.getValue().valueType())
                        .collect(Collectors.joining(", ")),
                returnedValueTypePath
        );
    }

    @Override
    public boolean equals(@NotNull Object _other) {
        if (_other instanceof FuncScope<?> other) {
            if (!name().equals(other.name()) || args.size() != other.args.size() || !getClass().isInstance(other))
                return false;

            if (args.isEmpty())
                return true;

            for (final var arg : args.values().stream().toList())
                if (!other.args.containsKey(arg.name()) || !arg.valueType().equals(other.args.get(arg.name()).valueType()))
                    return false;

            return true;
        }

        return false;
    }
}
