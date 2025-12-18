package cofty.core.semantics.symbol;

import cofty.core.parser.ast.body.BodyResidentObject;
import org.jetbrains.annotations.NotNull;

public interface IncompletedSymbol<T extends BodyResidentObject, R extends Symbol> extends Symbol {
    @NotNull T basedOn();
}
