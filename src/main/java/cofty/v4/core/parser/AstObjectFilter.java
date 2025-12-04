package cofty.v4.core.parser;

import cofty.v4.core.parser.ast.AstObject;
import org.jetbrains.annotations.NotNull;

public interface AstObjectFilter<R extends AstObject> {
    boolean applyFilter(@NotNull R astObject);
}
