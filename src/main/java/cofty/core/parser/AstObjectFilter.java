package cofty.core.parser;

import cofty.core.parser.ast.AstObject;
import org.jetbrains.annotations.NotNull;

public interface AstObjectFilter<R extends AstObject> {
    boolean applyFilter(@NotNull R astObject);
}
