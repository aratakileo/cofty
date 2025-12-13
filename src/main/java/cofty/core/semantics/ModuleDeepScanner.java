package cofty.core.semantics;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.parser.ast.ClassDeclarationObject;
import cofty.core.parser.ast.FieldDeclarationObject;
import cofty.core.parser.ast.FuncDeclarationObject;
import cofty.core.semantics.symbol.IncompletedFieldSymbol;
import cofty.core.semantics.symbol.IncompletedSymbol;
import cofty.core.semantics.symbol.NamedSymbol;
import cofty.core.semantics.symbol.scope.ClassScope;
import cofty.core.semantics.symbol.scope.FuncSignaturesScope;
import cofty.core.semantics.symbol.scope.Scope;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ModuleDeepScanner extends ModuleScanner {
    public ModuleDeepScanner(@NotNull ModuleContext context) {
        super(context);
    }

    @Override
    public boolean scan() {
        for (; index < currentBodyObject.residents().size(); index++) {
            final var residentObject = currentBodyObject.residents().get(index);

            switch (residentObject) {
                case ClassDeclarationObject classDeclarationObject -> {
                    final var classScope = (ClassScope)currentScope.resolveOrThrow(classDeclarationObject.name());

                    diveInto(classScope, classDeclarationObject);
                }

                case FuncDeclarationObject funcDeclarationObject -> {
                    final var resolvedFuncSignaturesScope = (FuncSignaturesScope)currentScope.resolveOrThrow(
                            funcDeclarationObject.name()
                    );

                    final var completionResult = resolvedFuncSignaturesScope.tryComplete(
                            funcDeclarationObject.getArgSignatures()
                    );

                    if (completionResult.status != IncompletedSymbol.CompletionResult.Status.OK) {
                        addCompletionFailErr(completionResult);
                        isFailed = true;
                        continue;
                    }

                    resolvedFuncSignaturesScope.put(completionResult.completedSymbolOrThrow());
                    diveInto(completionResult.completedSymbolOrThrow(), funcDeclarationObject);
                }

                case FieldDeclarationObject fieldDeclarationObject -> analyzeField(currentScope, fieldDeclarationObject);

                default -> {}
            }

            tryStepOutScope();
        }

        return !isFailed;
    }

    private void analyzeField(@NotNull Scope parentScope, @NotNull FieldDeclarationObject fieldDeclarationObject) {
        var fieldSymbol = parentScope.resolve(fieldDeclarationObject.name());

        if (fieldSymbol == null) {
            fieldSymbol = IncompletedFieldSymbol.create(fieldDeclarationObject);
            parentScope.put(fieldSymbol);
        }

        if (fieldSymbol instanceof IncompletedFieldSymbol incompletedFieldSymbol) {
            final var completionResult = incompletedFieldSymbol.tryComplete();

            if (completionResult.status != IncompletedSymbol.CompletionResult.Status.OK) {
                addCompletionFailErr(completionResult);
                isFailed = true;
                return;
            }

            parentScope.put(completionResult.completedSymbolOrThrow());

            return;
        }

        addAlreadyDefinedNameError((NamedSymbol)fieldSymbol, fieldDeclarationObject.nameToken());
        isFailed = true;
    }

    private void addCompletionFailErr(@NotNull IncompletedSymbol.CompletionResult<?> completionResult) {
        switch (completionResult.status) {
            case NON_TYPE -> context.messages.reportRange(completionResult.invalidTokens, Errors.NOT_A_TYPE);
            case NON_VALUE -> context.messages.reportRange(
                    completionResult.invalidTokens,
                    Errors.NOT_A_VALUE,
                    completionResult.expected.name().toLowerCase(),
                    completionResult.resolved.name().toLowerCase()
            );

            case UNDEFINED_VALUE, UNDEFINED_TYPE -> context.messages.reportRange(
                    completionResult.invalidTokens,
                    Errors.UNRESOLVED_REFERENCE
            );

            case NOT_SUITABLE_VALUE_TYPE -> context.messages.reportRange(
                    completionResult.invalidTokens,
                    Errors.INCOMPATIBLE_TYPES,
                    completionResult.invalidTokens.getFirst().content,
                    Objects.requireNonNull(completionResult.notSuitableTypePath)
            );
        }
    }
}
