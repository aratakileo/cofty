package cofty.core.semantics.symbol.scope;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.semantics.symbol.WithNameToken;
import org.jetbrains.annotations.NotNull;

public abstract class NamedScope extends ChildScope implements WithNameToken {
    private final TypedToken<Simple> name;

    protected NamedScope(@NotNull TypedToken<Simple> name) {
        super(name.content);

        if (!name.type.equals(Simple.WORD))
            throw new IllegalArgumentException();

        this.name = name;
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return name;
    }
}
