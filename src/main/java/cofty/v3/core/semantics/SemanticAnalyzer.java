package cofty.v3.core.semantics;

import cofty.v3.core.parser.ast.BodyObject;
import cofty.v3.core.semantics.visitor.BodySubsidiaryVisitor;
import org.jetbrains.annotations.NotNull;

public class SemanticAnalyzer {
    public final SemanticsContext context;
    public final BodyObject rootAstObject;

    public SemanticAnalyzer(@NotNull SemanticsContext context, @NotNull BodyObject rootAstObject) {
        this.context = context;
        this.rootAstObject = rootAstObject;
    }

    public boolean analyzePrimary() {
        return BodySubsidiaryVisitor.ROOT_BODY.visitBodyObjects(context, rootAstObject.objectsOrThrow());
    }
}
