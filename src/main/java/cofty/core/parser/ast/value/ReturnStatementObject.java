package cofty.core.parser.ast.value;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.parser.ast.WithDiagnosticFailAnchor;
import cofty.core.parser.ast.body.BodyResidentObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ReturnStatementObject implements BodyResidentObject, WithDiagnosticFailAnchor {
    public final ExpressionValueObject value;

    private final TypedToken<Keyword> firstToken;

    public ReturnStatementObject(@NotNull TypedToken<Keyword> firstToken, @Nullable ExpressionValueObject value) {
        this.value = value;
        this.firstToken = firstToken;
    }

    @Override
    public @NotNull List<TypedToken<?>> failTokensRange() {
        return value == null ? List.of(firstToken) : List.of(firstToken, value.lastFailToken());
    }
}
