package cofty.core.semantics.symbol;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;

public interface WithNameToken {
    @NotNull TypedToken<Simple> nameToken();
}
