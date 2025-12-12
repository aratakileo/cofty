package cofty;

import cofty.core.compiler.diagnostic.DiagnosticCode;
import cofty.core.compiler.diagnostic.DiagnosticEngine;
import cofty.core.lexer.Lexer;
import cofty.core.parser.ParseContext;
import cofty.core.parser.ParseResult;
import cofty.core.parser.Parser;
import cofty.type.TextContent;
import cofty.util.Cast;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;

public class ParseResultAssert<T> {
    public final String expr;
    public final ParseContext context;
    public final ParseResult<T> result;

    private ParseResultAssert(@NonNull String expr, @NonNull ParseContext context, @NonNull ParseResult<T> result) {
        this.expr = expr;
        this.context = context;
        this.result = result;
    }

    public @NonNull ParseResultAssert<T> ok() {
        Assertions.assertTrue(result.isOK());
        Assertions.assertNotNull(result.value());
        return this;
    }

    public @NonNull ParseResultAssert<T> failed() {
        Assertions.assertTrue(result.isFailed());
        Assertions.assertNull(result.value());
        return this;
    }

    public @NonNull ParseResultAssert<T> skipped() {
        Assertions.assertTrue(result.isSkipped());
        Assertions.assertNull(result.value());
        return this;
    }

    public @NonNull ParseResultAssert<T> hasNoDiagnosticMessages() {
        Assertions.assertTrue(context.messages.engine.isEmpty());
        return this;
    }

    public @NonNull ParseResultAssert<T> hasNoErrors() {
        Assertions.assertFalse(context.messages.engine.hasErrors());
        return this;
    }

    public @NonNull ParseResultAssert<T> hasErrors(
            @NonNull DiagnosticCode diagnosticCode,
            @NonNull DiagnosticCode @NonNull... diagnosticCodes
    ) {
        Assertions.assertEquals(diagnosticCodes.length + 1, context.messages.engine.errorsCount());
        Assertions.assertEquals(diagnosticCode, context.messages.engine.getFirstError().code);

        for (var i = 0; i < diagnosticCodes.length; i++)
            Assertions.assertEquals(diagnosticCodes[i], context.messages.engine.getError(i + 1).code);

        return this;
    }

    public @NonNull ParseResultAssert<T> hasWarnings(
            @NonNull DiagnosticCode diagnosticCode,
            @NonNull DiagnosticCode @NonNull... diagnosticCodes
    ) {
        Assertions.assertEquals(diagnosticCodes.length + 1, context.messages.engine.warningsCount());
        Assertions.assertEquals(diagnosticCode, context.messages.engine.getFirstWarning().code);

        for (var i = 0; i < diagnosticCodes.length; i++)
            Assertions.assertEquals(diagnosticCodes[i], context.messages.engine.getWarning(i + 1).code);

        return this;
    }

    public <V extends T> @NonNull V value(@NonNull Class<V> valueType) {
        Assertions.assertInstanceOf(valueType, result.valueOrThrow());
        return Cast.quiet(result.valueOrThrow());
    }

    public @NonNull T value() {
        return result.valueOrThrow();
    }

    public static <T> @NonNull ParseResultAssert<T> parse(@NonNull String expr, @NonNull Parser<T> parser) {
        final var textContent = TextContent.ofInput(expr);
        final var messages = new DiagnosticEngine();
        final var tokens = new Lexer(textContent, messages).parse();

        Assertions.assertTrue(messages.isEmpty());

        final var context = new ParseContext(tokens, textContent, messages);
        final var parseResult = parser.parse(context);

        return new ParseResultAssert<>(expr, context, parseResult);
    }
}
