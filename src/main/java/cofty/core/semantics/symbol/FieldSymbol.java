package cofty.core.semantics.symbol;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;

public sealed abstract class FieldSymbol<T> extends NamedSymbol implements WithValueType<T>
        permits IncompletedFieldSymbol, CompletedFieldSymbol {
    private final T valueTypePath;

    private final boolean isMutable;
    private boolean isValuePassed;

    protected FieldSymbol(
            @NotNull TypedToken<Simple> name,
            @NotNull T valueTypePath,
            boolean isMutable,
            boolean isValuePassed
    ) {
        super(name);
        this.valueTypePath = valueTypePath;
        this.isMutable = isMutable;
        this.isValuePassed = isValuePassed;
    }

    @Override
    public @NotNull T valueType() {
        return valueTypePath;
    }

    @Override
    public @NotNull String represented() {
        return String.format(
                "%s mutable=%s inited=%s -> %s;",
                representedHeader(),
                isMutable,
                isValuePassed,
                valueTypePath
        );
    }

    public boolean isMutable() {
        return isMutable;
    }

    public boolean isValuePassed() {
        return isValuePassed;
    }

    public boolean passValue() {
        if (this instanceof IncompletedFieldSymbol)
            throw new IllegalCallerException("passing value is not allowed for incomplete field symbol");

        return !isValuePassed && (isValuePassed = true);
    }

}
