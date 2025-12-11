package cofty.core.semantics.symbol.scope;

import cofty.core.semantics.symbol.ChildSymbol;
import cofty.core.semantics.symbol.IncompletedSymbol;
import cofty.core.semantics.symbol.Symbol;
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

public final class FuncSignaturesScope extends ChildSymbol implements Scope {
    private final ArrayList<FuncScope<?>> funcs = new ArrayList<>();
//    private boolean isCompleted = true;

    public FuncSignaturesScope(@NotNull String name) {
        super(name);
    }

    @Override
    public void put(@NotNull String name, @NotNull Symbol symbol) {
        if (!name.equals(name()))
            throw new IllegalArgumentException();

        if (symbol instanceof FuncScope<?> newFuncScope) {
            newFuncScope.setParent(this);
            funcs.add(newFuncScope);

//            if (newFuncScope instanceof IncompletedFuncScope)
//                isCompleted = false;

            return;
        }

        throw new IllegalArgumentException();
    }

    @Override
    public boolean containsName(@NotNull String name) {
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

    public @Nullable CompletedFuncScope resolve(@NotNull List<AbsSymbolPath> argSignatures) {
        for (final var funcScope: funcs)
            if (funcScope instanceof CompletedFuncScope completedFuncScope)
                if (completedFuncScope.canReceive(argSignatures))
                    return completedFuncScope;

        return null;
    }

    public @Nullable CompletedFuncScope resolveCompleted(@NotNull List<RelativeSymbolPath> argSignatures) {
        for (final var funcScope: funcs)
            if (funcScope instanceof CompletedFuncScope completedFuncScope)
                if (completedFuncScope.canProbablyReceive(argSignatures))
                    return completedFuncScope;

        return null;
    }

    public @NotNull CompletedFuncScope resolveCompletedOrThrow(@NotNull List<RelativeSymbolPath> argSignatures) {
        return Objects.requireNonNull(resolveCompleted(argSignatures));
    }

    @Override
    public @NotNull Set<String> childNames() {
        return IntStream.range(0, funcs.size()).mapToObj(String::valueOf).collect(Collectors.toSet());
    }

//    public boolean isCompleted() {
//        return isCompleted;
//    }

//    public boolean contains(@NotNull FuncScope<?> funcScope) {
//        return funcs.contains(funcScope);
//    }

    public void remove(@NotNull FuncScope<?> funcScope) {
        if (!funcScope.name().equals(name()))
            throw new IllegalArgumentException();

        funcs.remove(funcScope);
    }

    public boolean containsCompletedSignature(@NotNull List<AbsSymbolPath> argSignatures) {
        for (final var funcScope: funcs)
            if (funcScope instanceof CompletedFuncScope completed && completed.canReceive(argSignatures))
                return true;

        return false;
    }

    public boolean containsIncompletedSignature(@NotNull List<RelativeSymbolPath> argSignatures) {
        for (final var funcScope: funcs)
            if (funcScope instanceof IncompletedFuncScope incompleted && incompleted.canReceive(argSignatures))
                return true;

        return false;
    }

    public @NotNull IncompletedSymbol.CompletionResult<CompletedFuncScope> tryComplete(@NotNull List<RelativeSymbolPath> argSignatures) {
        for (final var funcScope: funcs)
            if (funcScope instanceof IncompletedFuncScope incompleted && incompleted.canReceive(argSignatures))
                return incompleted.tryComplete();

        throw new IllegalArgumentException();
    }

//    public @NotNull IncompletedSymbol.CompletionResult tryCompleteAll() {
//        for (final var func: funcs)
//            if (func instanceof IncompletedFuncScope incompletedFuncScope) {
//                final var completionResult = incompletedFuncScope.tryComplete();
//
//                if (completionResult.status != IncompletedSymbol.CompletionResult.Status.SUCCESSFUL)
//                    return completionResult;
//            }
//
//        isCompleted = true;
//
//        return IncompletedSymbol.CompletionResult.successful();
//    }
}
