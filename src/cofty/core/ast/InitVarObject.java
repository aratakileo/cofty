package cofty.core.ast;

import cofty.core.ast.value.Expr;
import cofty.core.ast.value.BasicValue;
import cofty.core.parser.LexemeVisitor;
import cofty.core.parser.ProceedResult;
import cofty.core.token.Keyword;
import cofty.core.token.Operator;
import cofty.core.token.Token;
import cofty.core.token.TokenType;
import org.jetbrains.annotations.NotNull;

public class InitVarObject extends AstObject<InitVarObject> {
    private String name = null;
    private Expr<?> value = null;
    private boolean isMutable;

    public void setName(@NotNull Token nameToken) {
        if (frozen()) return;

        this.name = nameToken.content;
    }

    public void setValue(@NotNull ProceedResult<Expr<?>> result) {
        if (frozen()) return;

        value = result.astObject;
    }

    public void setAsMutable() {
        if (frozen()) return;

        isMutable = true;
    }

    @Override
    public String toString() {
        return "InitVarStmt{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", isMutable=" + isMutable +
                '}';
    }

    public static final LexemeVisitor<InitVarObject> VISITOR = LexemeVisitor.byFirst(
            Keyword.LET,
            parserContext -> parserContext.begin(new InitVarObject(), false)
                    .consume(Keyword.LET, "expected `let` keyword")
                    .match(Keyword.MUT)
                    .then(InitVarObject::setAsMutable)
                    .consume(TokenType.ID, "expected variable name")
                    .then(InitVarObject::setName)
                    .consume(Operator.ASSIGN, "expected assign operator")
                    .consume(Expr.VISITOR, InitVarObject::setValue, "expected variable value")
    );
}
