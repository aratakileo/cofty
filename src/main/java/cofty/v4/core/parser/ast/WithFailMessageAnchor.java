package cofty.v4.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import org.jetbrains.annotations.NotNull;

public interface WithFailMessageAnchor {
    @NotNull TypedToken<?> failAnchor();
}
