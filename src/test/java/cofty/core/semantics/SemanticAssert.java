package cofty.core.semantics;

import cofty.core.parser.ParseResultAssert;
import cofty.core.compiler.diagnostic.DiagnosticCode;
import cofty.core.parser.BodyParser;
import cofty.core.semantics.symbol.scope.RootScope;
import cofty.core.semantics.symbol.scope.Scope;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

public final class SemanticAssert {
    private final static String BUILTINS_CODE = """
                       
                       class int {}
                       class null {}
                       class double {}
                       class float {}
                       class bool {}
                       class str {}
                       """;

    public final static int BUILTINS_CLASSES = 6;

    public final ModuleContext context;

    private final boolean scanResult;

    public SemanticAssert(@NotNull ModuleContext context, boolean scanResult) {
        this.context = context;
        this.scanResult = scanResult;
    }

    public @NotNull SemanticAssert ok() {
        Assertions.assertTrue(scanResult);
        return this;
    }

    public @NotNull SemanticAssert failed() {
        Assertions.assertFalse(scanResult);
        return this;
    }

    public @NotNull SemanticAssert hasNoDiagnosticMessages() {
        Assertions.assertTrue(context.messages.engine.isEmpty());
        return this;
    }

    public @NotNull SemanticAssert hasNoErrors() {
        Assertions.assertFalse(context.messages.engine.hasErrors());
        return this;
    }

    public @NotNull SemanticAssert hasErrors(
            @NotNull DiagnosticCode diagnosticCode,
            @NotNull DiagnosticCode @NotNull... diagnosticCodes
    ) {
        Assertions.assertEquals(diagnosticCodes.length + 1, context.messages.engine.errorsCount());
        Assertions.assertEquals(diagnosticCode, context.messages.engine.getFirstError().code);

        for (var i = 0; i < diagnosticCodes.length; i++)
            Assertions.assertEquals(diagnosticCodes[i], context.messages.engine.getError(i + 1).code);

        return this;
    }

    public @NotNull SemanticAssert hasWarnings(
            @NotNull DiagnosticCode diagnosticCode,
            @NotNull DiagnosticCode @NotNull... diagnosticCodes
    ) {
        Assertions.assertEquals(diagnosticCodes.length + 1, context.messages.engine.warningsCount());
        Assertions.assertEquals(diagnosticCode, context.messages.engine.getFirstWarning().code);

        for (var i = 0; i < diagnosticCodes.length; i++)
            Assertions.assertEquals(diagnosticCodes[i], context.messages.engine.getWarning(i + 1).code);

        return this;
    }

    public ScopeAssert<Scope> scope() {
        return new ScopeAssert<>(context.scope);
    }

    public static @NotNull SemanticAssert quickScan(@NotNull String expr) {
        return scan(expr, true);
    }

    public static @NotNull SemanticAssert fullScan(@NotNull String expr) {
        return scan(expr, false);
    }

    private static @NotNull SemanticAssert scan(@NotNull String expr, boolean quickOnly) {
        final var parseResult = ParseResultAssert.parse(
                expr + (quickOnly ? "" : BUILTINS_CODE),
                BodyParser.MODULE_BODY
        ).ok().hasNoDiagnosticMessages();

        final var moduleContext = ModuleContext.create(
                parseResult.context.text,
                parseResult.context.messages.engine,
                RootScope.create(),
                parseResult.value()
        );

        final var quickScannerResult = new ModuleQuickScanner(moduleContext).scan();

        if (quickOnly)
            return new SemanticAssert(moduleContext, quickScannerResult);

        Assertions.assertTrue(quickScannerResult);

        return new SemanticAssert(moduleContext, new ModuleDeepScanner(moduleContext).scan());
    }
}
