package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;

public interface WithName {
    @NotNull TypedToken<Simple> nameToken();
    default @NotNull String name() {
        return nameToken().content;
    }
}
