package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.TokenType;
import org.jetbrains.annotations.NotNull;

public interface WithAnchor<T extends TokenType> {
    @NotNull TypedToken<T> anchor();
}
