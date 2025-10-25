package cofty.v4.core.parser.ast.value;

import cofty.core.lexer.token.TypedToken;
import cofty.v4.core.parser.ast.AstObject;
import cofty.v4.core.parser.ast.BodyResidentObject;
import org.jetbrains.annotations.NotNull;

public final class ValueExpressionObject implements BodyResidentObject {
    public final ExpressionValue expr;

    public ValueExpressionObject(@NotNull ExpressionValue expr) {
        this.expr = expr;
    }
}
