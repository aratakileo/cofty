package cofty.core.semantics.symbol;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import org.jetbrains.annotations.NotNull;

public final class CompletedFieldSymbol extends FieldSymbol<AbsSymbolPath> {
    public CompletedFieldSymbol(
            @NotNull TypedToken<Simple> name,
            @NotNull TypeDescriptor<AbsSymbolPath> valueTypePath,
            boolean isMutable,
            boolean isValuePassed
    ) {
        super(name, valueTypePath, isMutable, isValuePassed);
    }
}
