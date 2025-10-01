package cofty.v3.core.semantics.visitor;

import cofty.core.lexer.token.ITokenType;
import cofty.core.lexer.token.Modifier;
import cofty.core.lexer.token.TypedToken;
import cofty.v3.core.parser.ast.AstObject;
import cofty.v3.core.parser.ast.WithBody;
import cofty.v3.core.parser.ast.WithModifiers;
import cofty.v3.core.parser.ast.func.FuncDeclarationObject;
import cofty.v3.core.parser.ast.statement.IfStatementsObject;
import cofty.v3.core.parser.ast.statement.StatementObject;
import cofty.v3.core.semantics.SemanticsContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

public interface BodySubsidiaryVisitor {
    default boolean allowModifiers() {
        return true;
    }

    default boolean allowAccessModifiers() {
        return true;
    }

    default boolean allowFunctionsOrClasses() {
        return true;
    }

    default boolean allowSubBodies() {
        return true;
    }

    default boolean visitModifiers(@NotNull SemanticsContext context, @NotNull WithModifiers astObject) {
        if (astObject.modifiers().modifierTokens().isEmpty()) return true;

        if (!allowModifiers()) {
            context.CRITICAL.putSyntaxErr("not allowed here", astObject.modifiers().modifierTokens().getFirst());
            return false;
        }

        var result = true;
        final var modifiersBuffer = new HashSet<ITokenType>();

        for (final var token: astObject.modifiers().modifierTokens()) {
            final var type = token.type;

            if (modifiersBuffer.contains(type)) {
                context.CRITICAL.putSyntaxErr("this modifier has already been described", token);
                result = false;
                continue;
            }

            if (!checkIfAccessModifiers(context, modifiersBuffer, token)) result = false;

            modifiersBuffer.add(token.type);
        }

        return result;
    }

    default boolean visitIfStatementBodies(@NotNull SemanticsContext context, @NotNull IfStatementsObject astObject) {
        var result = visitBodyObjects(context, astObject.ifStatement().body().objectsOrThrow());

        if (astObject.elseIfStatements() != null)
            for (final var statementObject: astObject.elseIfStatementsOrThrow())
                if (!visitBodyObjects(context, statementObject.body().objectsOrThrow()))
                    result = false;

        if (astObject.elseBody().objects() != null && !visitBodyObjects(context, astObject.elseBody().objectsOrThrow()))
            result = false;

        return result;
    }

    default boolean visitBody(@NotNull SemanticsContext context, @NotNull WithBody astObject) {
        return visitBodyObjects(context, astObject.body().objectsOrThrow());
    }

    default boolean visitBodyObjects(@NotNull SemanticsContext context, @NotNull List<AstObject> objects) {
        if (objects.isEmpty()) return true;

        var result = true;

        for (final var subAstObject: objects) {
            if (subAstObject instanceof WithModifiers withModifiers && !visitModifiers(context, withModifiers))
                result = false;

            if (subAstObject instanceof WithBody withBody) {
                final var subVisitor = by(withBody);

                if (!subVisitor.visitBody(context, withBody)) result = false;

                continue;
            }

            if (subAstObject instanceof IfStatementsObject ifStatementsObject) {
                final var subVisitor = by(ifStatementsObject);

                if (!subVisitor.visitIfStatementBodies(context, ifStatementsObject)) result = false;
            }
        }

        return result;
    }

    default boolean checkIfAccessModifiers(
            @NotNull SemanticsContext context,
            @NotNull HashSet<ITokenType> buffer,
            @NotNull TypedToken<Modifier> token
    ) {
        if (!token.type.isAccessModifier()) return true;

        if (!allowAccessModifiers()) {
            context.CRITICAL.putSyntaxErr("access modifiers are not allowed here", token);
            return false;
        }

        if (buffer.contains(Modifier.PRIV) || buffer.contains(Modifier.PUB)) {
            context.CRITICAL.putSyntaxErr(
                    "the access modifier has already been previously specified earlier",
                    token
            );
            return false;
        }

        return true;
    }

    static BodySubsidiaryVisitor by(@NotNull WithBody astObjectWithBody) {
        if (astObjectWithBody instanceof FuncDeclarationObject)
            return SUB_BODY;

        if (astObjectWithBody instanceof StatementObject)
            return CLASS_BODY;

        return ROOT_BODY;
    }

    static BodySubsidiaryVisitor by(@NotNull IfStatementsObject ifStatementsObject) {
        return SUB_BODY;
    }

    BodySubsidiaryVisitor ROOT_BODY = new BodySubsidiaryVisitor() {};
    SubBodyVisitor SUB_BODY = new SubBodyVisitor();
    ClassVisitor CLASS_BODY = new ClassVisitor();
}
