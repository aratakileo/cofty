package cofty.core.semantics.symbol;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.FieldDeclarationObject;
import cofty.core.parser.ast.WithName;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import org.jetbrains.annotations.NotNull;

public final class CoftyFieldSymbol extends CompletedFieldSymbol implements WithNameToken, CompletedSymbol {
    private boolean isCompletedInsideOfMainCycle;
    private final TypedToken<Simple> name;

    private CoftyFieldSymbol(
            @NotNull TypedToken<Simple> name,
            @NotNull TypeDescriptor<AbsSymbolPath> valueTypePath,
            boolean isMutable,
            boolean isValuePassed,
            boolean isCompletedInsideOfMainCycle
    ) {
        super(name.content, valueTypePath, isMutable, isValuePassed);

        this.isCompletedInsideOfMainCycle = isCompletedInsideOfMainCycle;
        this.name = name;
    }

    @Override
    public boolean isCompletedInsideOfMainCycle() {
        return isCompletedInsideOfMainCycle;
    }

    @Override
    public void markAsCompletedInsideOfMainCycle() {
        isCompletedInsideOfMainCycle = true;
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return name;
    }

    public static @NotNull CoftyFieldSymbol create(
            @NotNull FieldDeclarationObject basedOn,
            @NotNull TypeDescriptor<AbsSymbolPath> type,
            boolean isCompletedInsideOfMainCycle
    ) {
        return new CoftyFieldSymbol(
                basedOn.nameToken(),
                type,
                basedOn.mutable != null,
                basedOn.value != null,
                isCompletedInsideOfMainCycle
        );
    }
}
