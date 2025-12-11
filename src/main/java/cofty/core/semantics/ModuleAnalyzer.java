package cofty.core.semantics;

import cofty.core.lexer.token.TypedToken;
import cofty.core.parser.ast.*;
import cofty.core.semantics.symbol.*;
import cofty.core.semantics.symbol.scope.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public sealed abstract class ModuleAnalyzer permits ModuleQuickAnalyzer, ModuleDeepAnalyzer {
    private final ArrayList<WithBody> bodyObjectsStack = new ArrayList<>();
    private final ArrayList<Integer> childIndexesStack = new ArrayList<>();
    private final ArrayList<Scope> scopesStack = new ArrayList<>();

    protected int index = 0;
    protected Scope currentScope;
    protected WithBody currentBodyObject;
    protected boolean isFailed = false;

    public final ModuleContext context;

    protected ModuleAnalyzer(@NotNull ModuleContext context) {
        this.context = context;
        this.currentScope = context.scope;
        this.currentBodyObject = context.bodyObject;
    }

    public abstract boolean analyze();

    protected void diveInto(@NotNull Scope scope, @NotNull WithBody bodyObject) {
        if (bodyObject.residents().isEmpty()) return;

        bodyObjectsStack.add(currentBodyObject);
        childIndexesStack.add(index);
        scopesStack.add(currentScope);

        currentScope = scope;
        currentBodyObject = bodyObject;
        index = -1;
    }

    protected void tryStepOutScope() {
        while (index == currentBodyObject.residents().size() - 1 && !scopesStack.isEmpty()) {
            index = childIndexesStack.removeLast();
            currentBodyObject = bodyObjectsStack.removeLast();
            currentScope = scopesStack.removeLast();
        }
    }

    protected void addAlreadyDefinedNameError(@NotNull NamedSymbol problematicSymbol, @NotNull TypedToken<?> problematicName) {
        context.messages.addErr(String.format(
                "NameError: this name has been already defined as the %s earlier at line %s",
                Scope.ResolveTypeResult.ValueType.of(problematicSymbol).name().toLowerCase(),
                problematicSymbol.nameToken().getLineNumber(context.text)
        ), problematicName);
    }
}
