package cofty.core.parser.ast.value.expr.op;

import cofty.core.lexer.token.TypedToken;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Deprecated
public interface Operator {
    @NotNull List<TypedToken<?>> operator();
}
