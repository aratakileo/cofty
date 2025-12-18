package cofty.core.semantics;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.parser.ast.ClassDeclarationObject;
import cofty.core.parser.ast.FieldDeclarationObject;
import cofty.core.parser.ast.FieldValueAssignmentObject;
import cofty.core.parser.ast.FuncDeclarationObject;
import cofty.core.parser.ast.value.ExpressionValueObject;
import cofty.core.parser.ast.value.ReturnStatementObject;
import cofty.core.semantics.symbol.CompletedFieldSymbol;
import cofty.core.semantics.symbol.SymbolType;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.scope.CompletedFuncScope;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ModuleDeepScanner extends ModuleScanner {
    private CompletedFuncScope funcContext;

    public ModuleDeepScanner(@NotNull ModuleContext context) {
        super(context);
    }

    @Override
    public boolean scan() {
        for (; index < currentBodyObject.residents().size(); index++) {
            final var residentObject = currentBodyObject.residents().get(index);

            switch (residentObject) {
                case ClassDeclarationObject classDeclarationObject -> {
                    final var classScope = scanClass(classDeclarationObject, false);

                    if (classScope == null)
                        throw new IllegalStateException();

                    diveInto(classScope, classDeclarationObject);
                }

                case FuncDeclarationObject funcDeclarationObject -> {
                    final var funcScope = scanFunc(funcDeclarationObject, false);

                    if (funcScope != null) {
                        funcContext = (CompletedFuncScope)funcScope;
                        diveInto(funcScope, funcDeclarationObject);
                    }
                }

                case FieldValueAssignmentObject fieldValueAssignmentObject -> scanValueAssignment(
                        fieldValueAssignmentObject
                );

                case ReturnStatementObject returnStatementObject -> scanReturnStatement(returnStatementObject);
                case ExpressionValueObject expressionValueObject -> scanExpressionStatement(expressionValueObject);
                case FieldDeclarationObject fieldDeclarationObject -> deepScanFieldOrVariable(
                        fieldDeclarationObject,
                        true
                );

                default -> {}
            }

            tryStepOutScope();
        }

        return !isFailed;
    }

    private void scanReturnStatement(@NotNull ReturnStatementObject returnStatementObject) {
        final var functionReturnsNothing = Objects.equals(
                funcContext.valueTypeOrThrow(),
                TypeDescriptor.reference(currentScope.resolveTypePath(TypeDescriptor.RELATIVE_NULL.path).unwrap())
        );

        if (functionReturnsNothing && returnStatementObject.value == null) return;

        if (functionReturnsNothing) {
            context.messages.reportRange(
                    returnStatementObject.value.failTokensRange(),
                    Errors.UNEXPECTED_RETURN_VALUE,
                    funcContext.name()
            );
            return;
        }

        if (returnStatementObject.value == null) {
            context.messages.reportRange(
                    returnStatementObject.failTokensRange(),
                    Errors.MISSING_RETURN_VALUE,
                    funcContext.name(),
                    funcContext.valueTypeOrThrow()
            );
            return;
        }

        final var returningValueType = resolveType(returnStatementObject.value);

        if (returningValueType == null || funcContext.valueTypeOrThrow().equals(returningValueType)) return;

        context.messages.reportRange(
                returnStatementObject.value.failTokensRange(),
                Errors.RETURN_TYPE_MISMATCH,
                funcContext.name(),
                funcContext.valueTypeOrThrow(),
                returningValueType
        );
    }

    private void scanExpressionStatement(@NotNull ExpressionValueObject expressionValueObject) {
        final var resolvedFuncCall = resolveValue(expressionValueObject);

        if (resolvedFuncCall == null) isFailed = true;
    }

    private void scanValueAssignment(@NotNull FieldValueAssignmentObject fieldValueAssignmentObject) {
        final var resolvedValue = resolveValue(fieldValueAssignmentObject.fieldView);

        if (resolvedValue == null) {
            isFailed = true;
            return;
        }

        final var assignableFieldSymbol = (CompletedFieldSymbol)resolvedValue;

        if (!assignableFieldSymbol.isMutable() && assignableFieldSymbol.isValuePassed()) {
            context.messages.report(
                    fieldValueAssignmentObject.actualField().nameToken(),
                    Errors.IMMUTABLE_REASSIGNMENT,
                    SymbolType.of(assignableFieldSymbol),
                    assignableFieldSymbol.name(),
                    assignableFieldSymbol.nameToken().getLineNumber(context.text)
            );

            isFailed = true;
            return;
        }

        final var newValueType = resolveType(fieldValueAssignmentObject.value);

        if (newValueType == null) {
            isFailed = true;
            return;
        }

        if (!assignableFieldSymbol.valueTypeOrThrow().equals(newValueType)) {
            context.messages.reportRange(
                    fieldValueAssignmentObject.value.failTokensRange(),
                    Errors.INCOMPATIBLE_TYPES,
                    assignableFieldSymbol.valueTypeOrThrow(),
                    newValueType
            );
            isFailed = true;
            return;
        }

        assignableFieldSymbol.passValue();
    }

    @Override
    protected boolean tryStepOutScope() {
        final var scopeBeforeStepOut = currentScope;
        final var steppedOutScope = super.tryStepOutScope();

        if (steppedOutScope && scopeBeforeStepOut instanceof CompletedFuncScope)
            funcContext = null;

        return steppedOutScope;
    }
}
