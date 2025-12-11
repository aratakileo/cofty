package cofty.core.semantics;

import cofty.core.compiler.message.CompilationMessageLabel;
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

public final class ModuleDeepAnalyzer extends ModuleAnalyzer {
    public ModuleDeepAnalyzer(@NotNull ModuleContext context) {
        super(context);
    }

    @Override
    public boolean analyze() {
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

                    if (completionResult.status != IncompletedSymbol.CompletionResult.Status.SUCCESSFUL) {
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
            fieldSymbol = new IncompletedFieldSymbol(fieldDeclarationObject);
            parentScope.put(fieldSymbol);
        }

        if (fieldSymbol instanceof IncompletedFieldSymbol incompletedFieldSymbol) {
            final var completionResult = incompletedFieldSymbol.tryComplete();

            if (completionResult.status != IncompletedSymbol.CompletionResult.Status.SUCCESSFUL) {
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
            case NON_TYPE -> context.messages.addInRangeErr(
                    CompilationMessageLabel.create("TypeError: not a type"),
                    completionResult.invalidTokens
            );

            case NON_VALUE -> context.messages.addInRangeErr(
                    CompilationMessageLabel.createFormated(
                            "ValueError: not a value (expected %s, got %s)",
                            completionResult.expected.name().toLowerCase(),
                            completionResult.resolved.name().toLowerCase()
                    ),
                    completionResult.invalidTokens
            );

            case UNDEFINED_VALUE, UNDEFINED_TYPE -> context.messages.addInRangeErr(
                    CompilationMessageLabel.create("NameError: does not exist"),
                    completionResult.invalidTokens
            );

            case NOT_SUITABLE_VALUE_TYPE -> context.messages.addInRangeErr(
                    CompilationMessageLabel.createFormated(
                            "TypeError: expected %s value, got %s value",
                            completionResult.invalidTokens.getFirst().content,
                            Objects.requireNonNull(completionResult.notSuitableTypePath)
                    ),
                    completionResult.invalidTokens
            );
        }
    }
}
