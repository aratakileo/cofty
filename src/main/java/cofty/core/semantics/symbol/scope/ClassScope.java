package cofty.core.semantics.symbol.scope;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;

public final class ClassScope extends NamedScope {
    public ClassScope(@NotNull TypedToken<Simple> name) {
        super(name);
    }
}
