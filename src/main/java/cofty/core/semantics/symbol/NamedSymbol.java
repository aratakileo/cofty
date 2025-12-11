package cofty.core.semantics.symbol;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;

public abstract class NamedSymbol extends ChildSymbol implements WithNameToken {
    private final TypedToken<Simple> name;

    protected NamedSymbol(@NotNull TypedToken<Simple> name) {
        super(name.content);
        this.name = name;
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return name;
    }
}
