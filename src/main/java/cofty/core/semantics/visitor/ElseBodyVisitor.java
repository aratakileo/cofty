package cofty.core.semantics.visitor;

import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.statement.IfStatementsObject;
import cofty.core.semantics.SemanticsContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ElseBodyVisitor extends SubBodyVisitor {
    @Override
    public boolean visitBodyObjects(@NotNull SemanticsContext context, @NotNull List<AstObject> objects) {
        var result = super.visitBodyObjects(context, objects);

        if (objects.getFirst() instanceof IfStatementsObject ifStatementsObject) {
            context.CRITICAL.putSyntaxErr("not allowed here", ifStatementsObject.anchor());
            return false;
        }

        return result;
    }
}
