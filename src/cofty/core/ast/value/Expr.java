package cofty.core.ast.value;

import cofty.core.ast.AstObject;
import cofty.core.ast.IAstObject;
import cofty.core.parser.LexemeVisitor;
import cofty.core.parser.ParserContext;
import cofty.core.parser.ProceedResult;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;

public abstract class Expr<T extends IAstObject<?>> extends AstObject<T> {
    protected String computedType;

    public void setComputedType(@NotNull String type) {
        computedType = type;
    }

    public final static LexemeVisitor<Expr<?>> VISITOR = new LexemeVisitor<>() {
        @Override
        public boolean check(@NotNull ParserContext parserContext) {
            return BasicValue.VISITOR.check(parserContext);
        }

        @Override
        public @NotNull ProceedResult<Expr<?>> consume(@NotNull ParserContext parserContext) {
            return Cast.unsafe(BasicValue.VISITOR.consume(parserContext));
        }
    };
}
