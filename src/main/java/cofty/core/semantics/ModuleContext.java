package cofty.core.semantics;

import cofty.core.compiler.diagnostic.DiagnosticEngine;
import cofty.core.parser.ast.body.BodyObject;
import cofty.core.parser.ast.body.ModuleBodyObject;
import cofty.core.semantics.symbol.scope.ModuleScope;
import cofty.core.semantics.symbol.scope.RootScope;
import cofty.type.TextContent;
import org.jetbrains.annotations.NotNull;

public final class ModuleContext {
    public final TextContent text;
    public final DiagnosticEngine.TextAssociated messages;

    public final ModuleScope scope;
    public final ModuleBodyObject bodyObject;

    public ModuleContext(
            @NotNull TextContent text,
            @NotNull DiagnosticEngine messages,
            @NotNull ModuleScope scope,
            @NotNull ModuleBodyObject bodyObject
    ) {
        this.text = text;
        this.messages = messages.associateWith(text);
        this.scope = scope;
        this.bodyObject = bodyObject;
    }

    public static @NotNull ModuleContext create(
            @NotNull TextContent text,
            @NotNull DiagnosticEngine messages,
            @NotNull RootScope rootScope,
            @NotNull BodyObject bodyObject
    ) {
        final var moduleScope = new ModuleScope(text.simpleName());
        rootScope.put(moduleScope);

        return new ModuleContext(
                text,
                messages,
                moduleScope,
                new ModuleBodyObject(moduleScope.name(), bodyObject.residents())
        );
    }
}
