package cofty.core.transpiler;

import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.*;
import cofty.core.parser.ast.value.ExpressionValueObject;
import cofty.core.parser.ast.value.ReturnStatementObject;
import cofty.core.parser.ast.value.complex.ComplexValueObject;
import cofty.core.parser.ast.value.complex.FieldAccessObject;
import cofty.core.parser.ast.value.complex.FuncCallObject;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import cofty.core.semantics.ModuleContext;
import cofty.core.semantics.symbol.ArgsSignature;
import cofty.core.semantics.symbol.CompletedFieldSymbol;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.core.semantics.symbol.scope.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public final class ModuleTranspiler {
    private final ArrayList<WithBody> bodyObjectsStack = new ArrayList<>();
    private final ArrayList<Integer> childIndexesStack = new ArrayList<>();
    private final ArrayList<Scope> scopesStack = new ArrayList<>();
    private final int nestedLineOffset;

    private final StringBuilder moduleContent = new StringBuilder(),
            moduleStaticContent = new StringBuilder();

    private int currentLineOffset = 0;
    private int index = 0;
    private Scope currentScope;
    private WithBody currentBodyObject;
    private boolean emmitingStaticContent = false;

    public final ModuleContext context;

    public ModuleTranspiler(@NotNull ModuleContext context, int nestedLineOffset) {
        this.context = context;
        this.currentScope = context.scope;
        this.currentBodyObject = context.bodyObject;
        this.nestedLineOffset = nestedLineOffset;
    }

    public @NotNull String compile() {
        emit("import java.util.Scanner;\n\n");
        emit("public final class", context.text.simpleName());
        startEmittingNestedBody();

        for (; index < currentBodyObject.residents().size(); index++) {
            final var residentObject = currentBodyObject.residents().get(index);

            switch (residentObject) {
                case ClassDeclarationObject classDeclarationObject -> {
                    final var className = classDeclarationObject.name();

                    if (isCoftyBuiltinType(className)) continue;

                    final var classScope = currentScope.resolveOrThrow(className);

                    emmitingStaticContent = false;
                    emitLine("public final static class", className);

                    diveInto((Scope)classScope, classDeclarationObject);
                }

                case FieldDeclarationObject fieldDeclarationObject -> {
                    emmitingStaticContent = false;

                    final var fieldName = fieldDeclarationObject.name();
                    final var fieldSymbol = (CompletedFieldSymbol)currentScope.resolveOrThrow(fieldName);

                    emitLine();

                    if (currentScope instanceof ModuleScope || currentScope instanceof ClassScope)
                        emit("public static ");

                    if (!fieldSymbol.isMutable())
                        emit("final ");

                    emit(
                            normalizeType(
                                    Objects.requireNonNull(fieldSymbol.valueType()),
                                    fieldDeclarationObject.value == null
                            ),
                            fieldName,
                            "= "
                    );

                    emitValue(fieldDeclarationObject.value);
                    emit(";");
                }

                case FuncDeclarationObject funcDeclarationObject -> {
                    emmitingStaticContent = false;

                    final var funcName = funcDeclarationObject.name();

                    if (isCoftyBuiltinFunc(funcName)) continue;

                    final var funcSignaturesScope = (FuncSignaturesScope)currentScope.resolveOrThrow(funcName);
                    final var funcScope = (CompletedFuncScope)(
                            funcDeclarationObject.args.isEmpty()
                                    ? funcSignaturesScope.resolveOrThrow(ArgsSignature.SIGNATURES_PREFIX)
                                    : funcSignaturesScope
                                    .resolveByArgsSignature(funcDeclarationObject.argsSignature().types())
                                    .unwrapOrThrow()
                    );

                    final var maxLimitOffset = funcScope.argsSignature.size() - funcScope.argsSignature.requiredArgsCount;

                    for (var limitOffset = 0; limitOffset < maxLimitOffset; limitOffset++)
                        emitFuncDeclaration(funcScope, funcScope.argsSignature.size() - limitOffset - 1);

                    emitFuncDeclaration(funcScope, funcScope.argsSignature.size());
                    diveInto(funcScope, funcDeclarationObject);
                }

                case FieldValueAssignmentObject fieldValueAssignmentObject -> {
                    tryToSwitchToStaticContent();
                    emitLine();
                    emitValue(fieldValueAssignmentObject.fieldView);
                    emit(" = ");
                    emitValue(fieldValueAssignmentObject.value);
                    emit(";");
                }

                case ReturnStatementObject returnStatementObject -> {
                    emmitingStaticContent = false;
                    emitLine("return");
                    emit(" ");
                    emitValue(returnStatementObject.value);
                    emit(";");
                }

                case ExpressionValueObject expressionValueObject -> {
                    tryToSwitchToStaticContent();
                    emitLine();
                    emitValue(expressionValueObject);
                    emit(";");
                }

                default -> {}
            }

            tryStepOutScope();
        }

        emmitingStaticContent = false;
        emitLine();
        emitLine("static");
        startEmittingNestedBody();
        emit(moduleStaticContent.toString());
        finishEmittingNestedBody();
        emitLine();
        emitLine("public static void main(String[] args) {}");

        finishEmittingNestedBody();

        return moduleContent.toString();
    }

    private void tryToSwitchToStaticContent() {
        emmitingStaticContent = currentScope instanceof ModuleScope;
    }

    private void emitFuncDeclaration(@NotNull CompletedFuncScope funcScope, int argsLimit) {
        emitLine("public static", normalizeType(funcScope.valueTypeOrThrow(), false), funcScope.name());
        emit("(");

        for (var i = 0; i < argsLimit; i++) {
            if (!funcScope.argsSignature.getField(i).isMutable())
                emit("final ");

            emit(normalizeType(funcScope.argsSignature.getType(i), false), funcScope.argsSignature.getName(i));

            if (i != argsLimit - 1)
                emit(", ");
        }

        emit(")");

        if (funcScope.argsSignature.size() == argsLimit) return;

        startEmittingNestedBody();

        if (!normalizeType(funcScope.valueTypeOrThrow(), false).equals("void"))
            emitLine("return", funcScope.name());
        else emitLine(funcScope.name());

        emit("(");
        for (var i = 0; i < argsLimit + 1; i++) {
            if (i == argsLimit) emitValue(funcScope.argsSignature.getDefaultValue(i));
            else emit(funcScope.argsSignature.getName(i));

            if (i != argsLimit)
                emit(", ");
        }
        emit(");");

        finishEmittingNestedBody();
    }

    private void emitValue(@Nullable ExpressionValueObject expressionValueObject) {
        switch (expressionValueObject) {
            case SimpleValueObject simpleValueObject -> emit(
                    simpleValueObject.value.type.equals(Simple.STR)
                            ? normalizeStringValue(simpleValueObject.value.content)
                            : simpleValueObject.value.content
            );

            case FuncCallObject funcCallObject -> {
                if (currentScope.resolve(funcCallObject.name()) instanceof ClassScope)
                    emit("new ");

                emit(normalizeFuncCallName(funcCallObject.name()));
                emit("(");

                for (final var arg: funcCallObject.args) {
                    emitValue(arg);

                    if (arg != funcCallObject.args.getLast()) emit(", ");
                }

                emit(")");
            }

            case ComplexValueObject complexValueObject -> {
                for (final var segment: complexValueObject.segments) {
                    emitValue(segment);

                    if (segment != complexValueObject.segments.getLast())
                        emit(".");
                }
            }

            case FieldAccessObject fieldAccessObject -> emit(fieldAccessObject.name());

            case null -> emit("null");

            default -> {}
        }
    }

    private void diveInto(@NotNull Scope scope, @NotNull WithBody bodyObject) {
        if (bodyObject.residents().isEmpty()) {
            emit(" {}");
            return;
        }

        startEmittingNestedBody();

        bodyObjectsStack.add(currentBodyObject);
        childIndexesStack.add(index);
        scopesStack.add(currentScope);

        currentScope = scope;
        currentBodyObject = bodyObject;
        index = -1;
    }

    private void tryStepOutScope() {
        while (index == currentBodyObject.residents().size() - 1 && !scopesStack.isEmpty()) {
            index = childIndexesStack.removeLast();
            currentBodyObject = bodyObjectsStack.removeLast();
            currentScope = scopesStack.removeLast();

            finishEmittingNestedBody();
        }
    }

    private void emit(@NotNull String value, @NotNull String @NotNull... otherValues) {
        final var currentContent = emmitingStaticContent ? moduleStaticContent : moduleContent;

        currentContent.append(value);

        if (otherValues.length == 0) return;

        currentContent.append(' ').append(String.join(" ", otherValues));
    }

    private void emitLine() {
        (emmitingStaticContent ? moduleStaticContent : moduleContent).append("\n")
                .append(" ".repeat(emmitingStaticContent ? nestedLineOffset * 2 : currentLineOffset));
    }

    private void emitLine(@NotNull String value, @NotNull String @NotNull... otherValues) {
        emitLine();
        emit(value, otherValues);
    }

    private void startEmittingNestedBody() {
        emit(" {");
        currentLineOffset += nestedLineOffset;
    }

    private void finishEmittingNestedBody() {
        currentLineOffset -= nestedLineOffset;
        emitLine("}");
    }

    private static boolean isCoftyBuiltinType(@NotNull String typeName) {
        return switch (typeName) {
            case "int", "float", "double", "bool", "str", "null" -> true;
            default -> false;
        };
    }

    private static boolean isCoftyBuiltinFunc(@NotNull String funcName) {
        return switch (funcName) {
            case "input" -> true;
            default -> false;
        };
    }

    private static @NotNull String normalizeType(@NotNull TypeDescriptor<AbsSymbolPath> type, boolean nullable) {
        if (!isCoftyBuiltinType(type.path.parts.getLast()))
            return type.path.sliceParts(2).fullName;

        return switch (type.path.parts.getLast()) {
            case "str" -> "String";
            case "bool" -> nullable ? "Boolean" : "boolean";
            case "null" -> "void";
            case "int" -> nullable ? "Integer" : "int";
            case "float" -> nullable ? "Float" : "float";
            case "double" -> nullable ? "Double" : "double";
            default -> throw new IllegalStateException();
        };
    }

    private static @NotNull String normalizeFuncCallName(@NotNull String funcName) {
        return switch (funcName) {
            case "input" -> "new Scanner(System.in).nextLine";
            default -> funcName;
        };
    }

    private static @NotNull String normalizeStringValue(@NotNull String string) {
        if (string.startsWith("'")) return "\"%s\"".formatted(string.substring(1, string.length() - 1));
        return string;
    }
}
