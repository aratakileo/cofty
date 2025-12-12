package cofty.core.semantics;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.parser.ast.*;
import cofty.core.semantics.symbol.IncompletedFieldSymbol;
import cofty.core.semantics.symbol.NamedSymbol;
import cofty.core.semantics.symbol.scope.*;
import org.jetbrains.annotations.NotNull;

public final class ModuleQuickAnalyzer extends ModuleAnalyzer {
    public ModuleQuickAnalyzer(@NotNull ModuleContext context) {
        super(context);
    }

    @Override
    public boolean analyze() {
        for (; index < currentBodyObject.residents().size(); index++) {
            final var residentObject = currentBodyObject.residents().get(index);

            var isFunc = residentObject.is(FuncDeclarationObject.class);

            if (residentObject instanceof WithName named && currentScope.containsName(named.name()) && !isFunc) {
                addAlreadyDefinedNameError((NamedSymbol) currentScope.resolveOrThrow(named.name()), named.nameToken());
                isFailed = true;
                continue;
            }

            switch (residentObject) {
                case ClassDeclarationObject classDeclarationObject -> {
                    final var newScope = new ClassScope(classDeclarationObject.name);
                    currentScope.put(newScope);

                    diveInto(newScope, classDeclarationObject);
                }

                case FuncDeclarationObject funcDeclarationObject -> {
                    var _newScope = (FuncSignaturesScope)null;

                    if (currentScope.containsName(funcDeclarationObject.name.content)) {
                        final var resolvedSymbol = currentScope.resolveOrThrow(funcDeclarationObject.name.content);

                        if (resolvedSymbol instanceof FuncSignaturesScope funcSignaturesScope) _newScope = funcSignaturesScope;
                        else {
                            addAlreadyDefinedNameError((NamedSymbol) resolvedSymbol, funcDeclarationObject.name);
                            isFailed = true;
                            continue;
                        }
                    } else {
                        _newScope = new FuncSignaturesScope(funcDeclarationObject.name);
                        currentScope.put(_newScope);
                    }

                    final var newScope = new IncompletedFuncScope(funcDeclarationObject);

                    if (_newScope.containsIncompletedSignature(newScope.getArgSignatures())) {
                        context.messages.report(
                                funcDeclarationObject.nameToken(),
                                Errors.DUPLICATE_FUNC_SIGNATURE,
                                _newScope.resolveIncompletedOrThrow(newScope.getArgSignatures())
                                        .nameToken()
                                        .getLineNumber(context.text)
                        );
                        isFailed = true;
                        continue;
                    }

                    _newScope.put(newScope);
                }

                case FieldDeclarationObject fieldDeclarationObject -> currentScope.put(
                        new IncompletedFieldSymbol(fieldDeclarationObject)
                );

                default -> {}
            }

            tryStepOutScope();
        }

        return !isFailed;
    }
}
