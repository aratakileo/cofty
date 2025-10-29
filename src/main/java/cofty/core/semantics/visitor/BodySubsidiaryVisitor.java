package cofty.core.semantics.visitor;

import cofty.core.lexer.token.type.TokenType;
import cofty.core.lexer.token.type.Modifier;
import cofty.core.lexer.token.TypedToken;
import cofty.core.parser.ast.*;
import cofty.core.parser.ast.func.FuncDeclarationObject;
import cofty.core.parser.ast.func.ReturnStatementObject;
import cofty.core.parser.ast.statement.IfStatementsObject;
import cofty.core.parser.ast.statement.StatementObject;
import cofty.core.semantics.SemanticsContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

@Deprecated
public interface BodySubsidiaryVisitor {
    default boolean allowModifiers() {
        return true;
    }

    default boolean allowAccessModifiers() {
        return true;
    }

    default boolean allowStaticModifier() {
        return false;
    }

    default boolean allowFunctionsOrClasses() {
        return true;
    }

    default boolean allowReturnStatement() {
        return false;
    }

    default boolean visitModifiers(@NotNull SemanticsContext context, @NotNull WithModifiers astObject) {
        if (astObject.modifiers().modifierTokens().isEmpty()) return true;

        if (!allowModifiers()) {
            context.messages.addSyntaxErr("not allowed here", astObject.modifiers().modifierTokens().getFirst());
            return false;
        }

        var result = true;
        final var modifiersBuffer = new HashSet<TokenType>();

        for (final var token: astObject.modifiers().modifierTokens()) {
            final var type = token.type;

            if (modifiersBuffer.contains(type)) {
                context.messages.addSyntaxErr("this modifier has already been described", token);
                result = false;
                continue;
            }

            if (!checkIfAccessModifiers(context, modifiersBuffer, token)) result = false;
            if (!checkIfStaticModifier(context, modifiersBuffer, token)) result = false;

            modifiersBuffer.add(token.type);
        }

        return result;
    }

    default boolean visitSubsididaryObject(@NotNull SemanticsContext context, @NotNull AstObject astObject) {
        var result = true;

        if (!allowReturnStatement() && astObject instanceof ReturnStatementObject returnStatementObject) {
            context.messages.addSyntaxErr("not allowed here", returnStatementObject.anchor());
            result = false;
        }

        if (allowFunctionsOrClasses()) return result;

        if (!(astObject instanceof ClassDeclarationObject) && !(astObject instanceof FuncDeclarationObject))
            return result;

        context.messages.addSyntaxErr("not allowed here", ((WithAnchor<?>) astObject).anchor());
        return false;

    }

    default boolean visitIfStatementBodies(@NotNull SemanticsContext context, @NotNull IfStatementsObject astObject) {
        var result = SUB_BODY.visitBodyObjects(context, astObject.ifStatement().body().objectsOrThrow());

        if (astObject.elseIfStatements() != null)
            for (final var statementObject: astObject.elseIfStatementsOrThrow())
                if (!SUB_BODY.visitBodyObjects(context, statementObject.body().objectsOrThrow()))
                    result = false;

        if (
                astObject.elseBody().objects() != null
                        && !ELSE_BODY.visitBodyObjects(context, astObject.elseBody().objectsOrThrow())
        ) result = false;

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

            if (!visitSubsididaryObject(context, subAstObject))
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

    default boolean checkIfStaticModifier(
            @NotNull SemanticsContext context,
            @NotNull HashSet<TokenType> buffer,
            @NotNull TypedToken<Modifier> token
    ) {
        if (token.type != Modifier.STATIC) return true;

        if (!allowStaticModifier()) {
            context.messages.addSyntaxErr("static modifier are not allowed here", token);
            return false;
        }

        return true;
    }

    default boolean checkIfAccessModifiers(
            @NotNull SemanticsContext context,
            @NotNull HashSet<TokenType> buffer,
            @NotNull TypedToken<Modifier> token
    ) {
        if (!token.type.isAccessModifier()) return true;

        if (!allowAccessModifiers()) {
            context.messages.addSyntaxErr("access modifiers are not allowed here", token);
            return false;
        }

        if (buffer.contains(Modifier.PRIVATE) || buffer.contains(Modifier.PUBLIC)) {
            context.messages.addSyntaxErr(
                    "the access modifier has already been previously specified earlier",
                    token
            );
            return false;
        }

        return true;
    }

    static BodySubsidiaryVisitor by(@NotNull WithBody astObjectWithBody) {
        if (astObjectWithBody instanceof FuncDeclarationObject)
            return FUNCTION_BODY;

        if (astObjectWithBody instanceof ClassDeclarationObject)
            return CLASS_BODY;

        if (astObjectWithBody instanceof StatementObject || astObjectWithBody instanceof SubBodyObject)
            return SUB_BODY;

        return ROOT_BODY;
    }

    static BodySubsidiaryVisitor by(@NotNull IfStatementsObject ifStatementsObject) {
        return SUB_BODY;
    }

    BodySubsidiaryVisitor ROOT_BODY = new BodySubsidiaryVisitor() {};
    SubBodyVisitor SUB_BODY = new SubBodyVisitor();
    ElseBodyVisitor ELSE_BODY = new ElseBodyVisitor();
    FunctionBodyVisitor FUNCTION_BODY = new FunctionBodyVisitor();
    ClassBodyVisitor CLASS_BODY = new ClassBodyVisitor();
}
