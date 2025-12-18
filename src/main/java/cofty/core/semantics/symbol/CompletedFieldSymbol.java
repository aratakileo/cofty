package cofty.core.semantics.symbol;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.FieldDeclarationObject;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import org.jetbrains.annotations.NotNull;

public final class CompletedFieldSymbol extends FieldSymbol<AbsSymbolPath> implements CompletedSymbol {
    private boolean isCompletedInsideOfMainCycle;

    private CompletedFieldSymbol(
            @NotNull TypedToken<Simple> name,
            @NotNull TypeDescriptor<AbsSymbolPath> valueTypePath,
            boolean isMutable,
            boolean isValuePassed,
            boolean isCompletedInsideOfMainCycle
    ) {
        super(name, valueTypePath, isMutable, isValuePassed);
        this.isCompletedInsideOfMainCycle = isCompletedInsideOfMainCycle;
    }

    public static @NotNull CompletedFieldSymbol create(
            @NotNull FieldDeclarationObject basedOn,
            @NotNull TypeDescriptor<AbsSymbolPath> type,
            boolean isCompletedInsideOfMainCycle
    ) {
        return new CompletedFieldSymbol(
                basedOn.nameToken(),
                type,
                basedOn.mutable != null,
                basedOn.value != null,
                isCompletedInsideOfMainCycle
        );
    }

    @Override
    public boolean isCompletedInsideOfMainCycle() {
        return isCompletedInsideOfMainCycle;
    }

    @Override
    public void markAsCompletedInsideOfMainCycle() {
        isCompletedInsideOfMainCycle = true;
    }
}
