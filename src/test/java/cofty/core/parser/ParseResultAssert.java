package cofty.core.parser;

import cofty.core.compiler.diagnostic.DiagnosticCode;
import cofty.core.compiler.diagnostic.DiagnosticEngine;
import cofty.core.lexer.Lexer;
import cofty.type.TextContent;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

public final class ParseResultAssert<T> {
    public final String expr;
    public final ParseContext context;
    public final ParseResult<T> result;

    private ParseResultAssert(@NotNull String expr, @NotNull ParseContext context, @NotNull ParseResult<T> result) {
        this.expr = expr;
        this.context = context;
        this.result = result;
    }

    public @NotNull ParseResultAssert<T> ok() {
        Assertions.assertTrue(result.isOK());
        Assertions.assertNotNull(result.value());
        return this;
    }

    public @NotNull ParseResultAssert<T> failed() {
        Assertions.assertTrue(result.isFailed());
        Assertions.assertNull(result.value());
        return this;
    }

    public @NotNull ParseResultAssert<T> skipped() {
        Assertions.assertTrue(result.isSkipped());
        Assertions.assertNull(result.value());
        return this;
    }

    public @NotNull ParseResultAssert<T> hasNoDiagnosticMessages() {
        Assertions.assertTrue(context.messages.engine.isEmpty());
        return this;
    }

    public @NotNull ParseResultAssert<T> hasNoErrors() {
        Assertions.assertFalse(context.messages.engine.hasErrors());
        return this;
    }

    public @NotNull ParseResultAssert<T> hasRepeatedError(@NotNull DiagnosticCode diagnosticCode, int count) {
        Assertions.assertEquals(count, context.messages.engine.errorsCount());

        for (var i = 0; i < context.messages.engine.errorsCount(); i++)
            Assertions.assertEquals(diagnosticCode, context.messages.engine.getError(i).code);

        return this;
    }

    public @NotNull ParseResultAssert<T> hasErrors(
            @NotNull DiagnosticCode diagnosticCode,
            @NotNull DiagnosticCode @NotNull... diagnosticCodes
    ) {
        Assertions.assertEquals(diagnosticCodes.length + 1, context.messages.engine.errorsCount());
        Assertions.assertEquals(diagnosticCode, context.messages.engine.getFirstError().code);

        for (var i = 0; i < diagnosticCodes.length; i++)
            Assertions.assertEquals(diagnosticCodes[i], context.messages.engine.getError(i + 1).code);

        return this;
    }

    public @NotNull ParseResultAssert<T> hasWarnings(
            @NotNull DiagnosticCode diagnosticCode,
            @NotNull DiagnosticCode @NotNull... diagnosticCodes
    ) {
        Assertions.assertEquals(diagnosticCodes.length + 1, context.messages.engine.warningsCount());
        Assertions.assertEquals(diagnosticCode, context.messages.engine.getFirstWarning().code);

        for (var i = 0; i < diagnosticCodes.length; i++)
            Assertions.assertEquals(diagnosticCodes[i], context.messages.engine.getWarning(i + 1).code);

        return this;
    }

    public <V extends T> @NotNull V value(@NotNull Class<V> valueType) {
        Assertions.assertInstanceOf(valueType, result.valueOrThrow());
        return Cast.quiet(result.valueOrThrow());
    }

    public @NotNull T value() {
        return result.valueOrThrow();
    }

    public static <T> @NotNull ParseResultAssert<T> parse(@NotNull String expr, @NotNull Parser<T> parser) {
        final var textContent = TextContent.ofInput(expr);
        final var messages = new DiagnosticEngine();
        final var tokens = new Lexer(textContent, messages).parse();

        Assertions.assertTrue(messages.isEmpty());

        final var context = new ParseContext(tokens, textContent, messages);
        final var parseResult = parser.parse(context);

        return new ParseResultAssert<>(expr, context, parseResult);
    }
}
