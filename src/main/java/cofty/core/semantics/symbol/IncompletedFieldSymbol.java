package cofty.core.semantics.symbol;

import cofty.core.parser.ast.FieldDeclarationObject;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.core.semantics.symbol.scope.Scope;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class IncompletedFieldSymbol
        extends FieldSymbol<RelativeSymbolPath>
        implements IncompletedSymbol<FieldDeclarationObject, CompletedFieldSymbol> {
    private final FieldDeclarationObject basedOn;

    private IncompletedFieldSymbol(
            @NotNull FieldDeclarationObject basedOn,
            @Nullable TypeDescriptor<RelativeSymbolPath> valueTypePath
    ) {
        super(basedOn.nameToken(), valueTypePath, basedOn.mutable != null, basedOn.value != null);
        this.basedOn = basedOn;
    }

    @Override
    public @NotNull FieldDeclarationObject basedOn() {
        return basedOn;
    }

    public static @NotNull IncompletedFieldSymbol create(@NotNull FieldDeclarationObject basedOn) {
        final var valueType = basedOn.valueType != null ? TypeDescriptor.rawReference(basedOn.valueType) : (
                basedOn.value instanceof SimpleValueObject simpleValueObject
                        ? TypeDescriptor.rawReference(simpleValueObject.valueTypeName()) : null
        );

        return new IncompletedFieldSymbol(basedOn, valueType);
    }
}
