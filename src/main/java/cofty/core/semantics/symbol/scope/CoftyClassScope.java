package cofty.core.semantics.symbol.scope;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.semantics.symbol.WithNameToken;
import org.jetbrains.annotations.NotNull;

public final class CoftyClassScope extends ClassScope implements WithNameToken  {
    private final TypedToken<Simple> name;

    public CoftyClassScope(TypedToken<Simple> name) {
        super(name.content);
        this.name = name;
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return name;
    }
}
