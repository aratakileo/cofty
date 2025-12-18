package cofty.core.semantics.symbol.scope;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.FuncDeclarationObject;
import cofty.core.semantics.symbol.*;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.TypeDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CompletedFuncScope extends FuncScope<AbsSymbolPath> {
    public CompletedFuncScope(
            @NotNull TypedToken<Simple> name,
            @NotNull ArgsSignature<AbsSymbolPath> argsSignature,
            @NotNull TypeDescriptor<AbsSymbolPath> returnedValueTypePath
    ) {
        super(name, argsSignature, returnedValueTypePath);
    }
}
