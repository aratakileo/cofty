package cofty.core.semantics;

import cofty.core.parser.ast.BodyObject;
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
