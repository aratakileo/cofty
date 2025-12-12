package cofty.core.compiler.diagnostic;

import cofty.core.lexer.token.TypedToken;
import cofty.type.TextContent;
import cofty.core.parser.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class DiagnosticEngine {
    private final ArrayList<DiagnosticMsg> errors = new ArrayList<>(), warnings = new ArrayList<>();

    public @NotNull List<DiagnosticMsg> errors() {
        return errors.stream().toList();
    }

    public @NotNull List<DiagnosticMsg> warnings() {
        return warnings.stream().toList();
    }

    public int errorsCount() {
        return errors.size();
    }

    public int warningsCount() {
        return warnings.size();
    }

    public boolean isEmpty() {
        return !hasErrors() && !hasWarnings();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void printErrors()  {
        for (final var err: errors) System.out.println(err.toString());
    }

    public void printWarnings()  {
        for (final var warn: warnings) System.out.println(warn.toString());
    }

    public void add(@NotNull DiagnosticMsg msg) {
        (msg.code.prefix().severity == DiagnosticMsg.Severity.ERR ? errors : warnings).add(msg);
    }

    public @NotNull TextAssociated associateWith(@NotNull TextContent text) {
        return new TextAssociated(this, text);
    }

    public @NotNull ParseContextAssociated associateWith(@NotNull ParseContext context) {
        return new ParseContextAssociated(this, context);
    }

    public @NotNull DiagnosticMsg getError(int index) {
        return errors.get(index);
    }

    public @NotNull DiagnosticMsg getFirstError() {
        return errors.getFirst();
    }

    public @NotNull DiagnosticMsg getLastError() {
        return errors.getLast();
    }

    public @NotNull DiagnosticMsg getWarning(int index) {
        return warnings.get(index);
    }

    public @NotNull DiagnosticMsg getFirstWarning() {
        return warnings.getFirst();
    }

    public @NotNull DiagnosticMsg getLastWarning() {
        return warnings.getLast();
    }

    public static class TextAssociated {
        public final TextContent text;
        public final DiagnosticEngine engine;

        public TextAssociated(@NotNull DiagnosticEngine engine, @NotNull TextContent text) {
            this.engine = engine;
            this.text = text;
        }

        public void report(
                @NotNull TypedToken<?> token,
                @NotNull DiagnosticCode code,
                @NotNull Object @NotNull... formatArgs
        ) {
            engine.add(DiagnosticMsg.create(
                    text,
                    token.start,
                    token.end,
                    code,
                    formatArgs
            ));
        }

        public void reportAfter(
                @NotNull TypedToken<?> token,
                @NotNull DiagnosticCode code,
                @NotNull Object @NotNull... formatArgs
        ) {
            engine.add(DiagnosticMsg.createAfter(
                    text,
                    token,
                    code,
                    formatArgs
            ));
        }

        public void reportRange(
                @NotNull List<TypedToken<?>> tokensRange,
                @NotNull DiagnosticCode code,
                @NotNull Object @NotNull... formatArgs
        ) {
            engine.add(DiagnosticMsg.create(
                    text,
                    tokensRange.getFirst().start,
                    tokensRange.getLast().end,
                    code,
                    formatArgs
            ));
        }

        public void reportRange(
                @NotNull TypedToken<?> firstToken,
                @NotNull TypedToken<?> lastToken,
                @NotNull DiagnosticCode code,
                @NotNull Object @NotNull... formatArgs
        ) {
            engine.add(DiagnosticMsg.create(
                    text,
                    firstToken.start,
                    lastToken.end,
                    code,
                    formatArgs
            ));
        }
    }

    public static final class ParseContextAssociated extends TextAssociated {
        public final ParseContext context;

        public ParseContextAssociated(
                @NotNull DiagnosticEngine handler,
                @NotNull ParseContext context
        ) {
            super(handler, context.text);
            this.context = context;
        }

        public void report(@NotNull DiagnosticCode code, @NotNull Object @NotNull... formatArgs) {
            final var token = context.nonNewLineCursorOrPrev();

            final var message = context.hasNonNewLineCurrent()
                    ? DiagnosticMsg.create(text, token.start, token.end, code, formatArgs)
                    : DiagnosticMsg.createAfter(text, token, code, formatArgs);

            engine.add(message);
        }
    }
}
