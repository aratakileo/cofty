package cofty.core.semantics.symbol.scope;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.semantics.symbol.*;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class FuncSignaturesScope extends NamedSymbol implements Scope {
    private final ArrayList<FuncScope<?>> funcs = new ArrayList<>();

    public FuncSignaturesScope(@NotNull TypedToken<Simple> name) {
        super(name);
    }

    @Override
    public void put(@NotNull String name, @NotNull Symbol symbol) {
        if (!name.equals(name()))
            throw new IllegalArgumentException();

        if (symbol instanceof FuncScope<?> newFuncScope) {
            newFuncScope.setParent(this);
            funcs.add(newFuncScope);
            return;
        }

        throw new IllegalArgumentException();
    }

    @Override
    public boolean containsLocalName(@NotNull String name) {
        try {
            return Integer.parseInt(name) < funcs.size();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @Nullable Symbol resolve(@NotNull String name) {
        try {
            final var funcIndex = Integer.parseInt(name);

            if (funcIndex < funcs.size())
                return funcs.get(funcIndex);

            return parentOrThrow().resolve(name);
        } catch (Exception e) {
            return parentOrThrow().resolve(name);
        }
    }

    public @Nullable CompletedFuncScope resolveBySignature(@NotNull List<TypeDescriptor<AbsSymbolPath>> argSignatures) {
        for (final var funcScope: funcs)
            if (funcScope instanceof CompletedFuncScope completedFuncScope)
                if (completedFuncScope.canReceive(argSignatures))
                    return completedFuncScope;

        return null;
    }

    public @Nullable IncompletedFuncScope resolveIncompleted(@NotNull List<TypeDescriptor<RelativeSymbolPath>> argSignatures) {
        for (final var funcScope: funcs)
            if (funcScope instanceof IncompletedFuncScope incompletedFuncScope)
                if (incompletedFuncScope.canReceive(argSignatures))
                    return incompletedFuncScope;

        return null;
    }

    public @NotNull IncompletedFuncScope resolveIncompletedOrThrow(@NotNull List<TypeDescriptor<RelativeSymbolPath>> argSignatures) {
        return Objects.requireNonNull(resolveIncompleted(argSignatures));
    }

    public @Nullable CompletedFuncScope resolveCompleted(@NotNull List<TypeDescriptor<RelativeSymbolPath>> argSignatures) {
        for (final var funcScope: funcs)
            if (funcScope instanceof CompletedFuncScope completedFuncScope)
                if (completedFuncScope.canProbablyReceive(argSignatures))
                    return completedFuncScope;

        return null;
    }

    @Override
    public @NotNull Set<String> childNames() {
        return IntStream.range(0, funcs.size()).mapToObj(String::valueOf).collect(Collectors.toSet());
    }

    @Override
    public boolean isEmpty() {
        return funcs.isEmpty();
    }

    @Override
    public int childrenCount() {
        return funcs.size();
    }

    public void remove(@NotNull FuncScope<?> funcScope) {
        if (!funcScope.name().equals(name()))
            throw new IllegalArgumentException();

        funcs.remove(funcScope);
    }

    public boolean containsCompletedSignature(@NotNull List<TypeDescriptor<AbsSymbolPath>> argSignatures) {
        for (final var funcScope: funcs)
            if (funcScope instanceof CompletedFuncScope completed && completed.canReceive(argSignatures))
                return true;

        return false;
    }

    public boolean containsIncompletedSignature(@NotNull List<TypeDescriptor<RelativeSymbolPath>> argSignatures) {
        for (final var funcScope: funcs)
            if (funcScope instanceof IncompletedFuncScope incompleted && incompleted.canReceive(argSignatures))
                return true;

        return false;
    }

    public @NotNull IncompletedSymbol.CompletionResult<CompletedFuncScope> tryComplete(@NotNull List<TypeDescriptor<RelativeSymbolPath>> argSignatures) {
        for (final var funcScope: funcs)
            if (funcScope instanceof IncompletedFuncScope incompleted && incompleted.canReceive(argSignatures))
                return incompleted.tryComplete();

        throw new IllegalArgumentException();
    }
}
