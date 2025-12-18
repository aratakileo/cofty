package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithDiagnosticFailAnchor {
    @NotNull List<TypedToken<?>> failTokensRange();

    default @NotNull TypedToken<?> firstFailToken() {
        return failTokensRange().getFirst();
    }

    default @NotNull TypedToken<?> lastFailToken() {
        return failTokensRange().getLast();
    }
}
