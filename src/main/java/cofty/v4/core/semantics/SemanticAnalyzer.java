package cofty.v4.core.semantics;

import cofty.core.semantics.SemanticsContext;
import cofty.v4.core.parser.ast.BodyObject;
import org.jetbrains.annotations.NotNull;

public final class SemanticAnalyzer {
    public final SemanticsContext context;
    public final BodyObject rootAstObject;

    public SemanticAnalyzer(@NotNull SemanticsContext context, @NotNull BodyObject rootBodyObject) {
        this.context = context;
        this.rootAstObject = rootBodyObject;
    }

    public boolean analyze() {
        return false;
    }
}
