package cofty.core.semantics.symbol.scope;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.semantics.symbol.CompletedFieldSymbol;
import cofty.core.semantics.symbol.FieldSymbol;
import cofty.core.semantics.symbol.IncompletedSymbol;
import cofty.core.semantics.symbol.Symbol;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.core.semantics.symbol.path.SymbolPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CompletedFuncScope extends FuncScope<AbsSymbolPath> {
    public CompletedFuncScope(
            @NotNull TypedToken<Simple> name,
            @NotNull AbsSymbolPath returnedValueTypePath,
            @NotNull List<CompletedFieldSymbol> args
    ) {
        super(name, returnedValueTypePath, args);
    }

    public boolean canReceive(@NotNull List<AbsSymbolPath> argSignatures) {
        if (args.size() != argSignatures.size()) return false;

        final var functionArgSymbols = args.values().stream().toList();

        for (var i = 0; i < args.size(); i++)
            if (!functionArgSymbols.get(i).valueType().equals(argSignatures.get(i)))
                return false;

        return true;
    }

    public boolean canProbablyReceive(@NotNull List<RelativeSymbolPath> argSignatures) {
        if (args.size() != argSignatures.size()) return false;

        final var functionArgSymbols = args.values().stream().toList();

        for (var i = 0; i < args.size(); i++)
            if (!functionArgSymbols.get(i).valueType().endsWith(argSignatures.get(i)))
                return false;

        return true;
    }

    public @NotNull List<AbsSymbolPath> getArgSignatures() {
        return args.sequencedValues().stream().map(FieldSymbol::valueType).toList();
    }
}
