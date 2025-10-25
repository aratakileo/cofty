package cofty.core.semantics;

import cofty.core.parser.ast.BodyObject;
import cofty.core.semantics.visitor.BodySubsidiaryVisitor;
import org.jetbrains.annotations.NotNull;

@Deprecated
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

    public boolean analyze() {
        return analyzePrimary();
    }
}
