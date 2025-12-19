package cofty.core.semantics.symbol.scope;

import cofty.core.semantics.symbol.*;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.type.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class FuncSignaturesScope extends ChildScope {
    private final HashMap<String, FuncScope<?>> funcs = new HashMap<>();

    private int maxArgumentsCount = 0, minArgumentsCount = Integer.MAX_VALUE;
    private FuncScope<?> firstDeclaredSignature = null;

    public FuncSignaturesScope(@NotNull String name) {
        super(name);
    }

    @Override
    public void put(@NotNull String name, @NotNull Symbol symbol) {
        if (!name.equals(name()))
            throw new IllegalArgumentException();

        if (symbol instanceof FuncScope<?> newFuncScope) {
            newFuncScope.setParent(this);

            funcs.put(newFuncScope.argsSignature.minimumSignature, newFuncScope);

            if (firstDeclaredSignature == null)
                firstDeclaredSignature = newFuncScope;

            maxArgumentsCount = Math.max(maxArgumentsCount, newFuncScope.argsSignature.size());
            minArgumentsCount = Math.min(minArgumentsCount, newFuncScope.argsSignature.requiredArgsCount);

            return;
        }

        throw new IllegalArgumentException();
    }

    @Override
    public boolean containsLocalName(@NotNull String minimumArgsSignature) {
        return funcs.containsKey(minimumArgsSignature);
    }

    @Override
    public @Nullable Symbol resolve(@NotNull String nameOrMinimumArgsSignature) {
        if (funcs.containsKey(nameOrMinimumArgsSignature))
            return funcs.get(nameOrMinimumArgsSignature);

        return parentOrThrow().resolve(nameOrMinimumArgsSignature);
    }

    @Override
    public @NotNull Set<String> childNames() {
        return funcs.keySet();
    }

    @Override
    public boolean isEmpty() {
        return funcs.isEmpty();
    }

    @Override
    public int childrenCount() {
        return funcs.size();
    }

    public @Nullable FuncScope<?> firstDeclaredSignature() {
        return firstDeclaredSignature;
    }

    public @NotNull FuncScope<?> firstDeclaredSignatureOrThrow() {
        return Objects.requireNonNull(firstDeclaredSignature);
    }

    public int maxArgumentsCount() {
        return maxArgumentsCount;
    }

    public int minArgumentsCount() {
        return minArgumentsCount;
    }

    public @Nullable FuncScope<?> resolveByEmptyArgsSignature() {
        return funcs.getOrDefault(ArgsSignature.SIGNATURES_PREFIX, null);
    }

    public void removeSignature(@NotNull ArgsSignature<RelativeSymbolPath> argsSignature) {
        if (firstDeclaredSignature == funcs.get(argsSignature.minimumSignature))
            firstDeclaredSignature = null;

        funcs.remove(argsSignature.minimumSignature);
    }

    public @NotNull Result<FuncScope<?>, IncompatibleArgumentInfo> resolveByArgsSignature(
            @NotNull List<? extends TypeDescriptor<?>> argSignatures
    ) {
        if (argSignatures.isEmpty())
            throw new IllegalArgumentException("list of args signature should not be empty");

        var bestMatchIndex = -1;
        var bestFunc = (FuncScope<?>)null;

        ROOT: for (final var func: funcs.values()) {
            // this is necessary in case the search for matches does not find any,
            // even for the first argument of the function
            if (bestFunc == null && !func.argsSignature.isEmpty()) bestFunc = func;
            if (func.argsSignature.size() < argSignatures.size()) continue;

            for (var i = 0; i < argSignatures.size(); i++) {
                if (!func.argsSignature.getType(i).isLike(argSignatures.get(i))) continue ROOT;
                if (i <= bestMatchIndex) continue;

                bestMatchIndex = i;
                bestFunc = func;
            }

            if (argSignatures.size() < bestFunc.argsSignature.requiredArgsCount) break;

            return Result.ok(func);
        }

        return Result.err(new IncompatibleArgumentInfo(
                Objects.requireNonNull(bestFunc),
                bestMatchIndex + 1
        ));
    }

    public static class IncompatibleArgumentInfo {
        public final int invalidArgIndex;
        public final FuncScope<?> func;

        public IncompatibleArgumentInfo(@NotNull FuncScope<?> func, int invalidArgIndex) {
            this.invalidArgIndex = invalidArgIndex;
            this.func = func;
        }
    }
}
