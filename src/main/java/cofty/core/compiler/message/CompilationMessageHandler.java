package cofty.core.compiler.message;

import cofty.core.lexer.token.TypedToken;
import cofty.type.TextContent;
import cofty.core.parser.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CompilationMessageHandler {
    private final ArrayList<CompilationMessage> errors = new ArrayList<>(), warnings = new ArrayList<>();

    public int errCount() {
        return errors.size();
    }

    public int warnCount() {
        return warnings.size();
    }

    public boolean isEmpty() {
        return !hasErrs() && !hasWarns();
    }

    public boolean hasWarns() {
        return !warnings.isEmpty();
    }

    public boolean hasErrs() {
        return !errors.isEmpty();
    }

    public void printErrs()  {
        for (final var err: errors) System.out.println(err.toString());
    }

    public void printWarns()  {
        for (final var warn: warnings) System.out.println(warn.toString());
    }

    public void add(@NotNull CompilationMessage msg) {
        (msg.type == MessageType.ERROR ? errors : warnings).add(msg);
    }

    public @NotNull TextAssociated associateWith(@NotNull TextContent text) {
        return new TextAssociated(this, text);
    }

    public @NotNull ParseContextAssociated associateWith(@NotNull ParseContext context) {
        return new ParseContextAssociated(this, context);
    }

    public static class TextAssociated {
        public final TextContent text;
        public final CompilationMessageHandler handler;

        public TextAssociated(@NotNull CompilationMessageHandler handler, @NotNull TextContent text) {
            this.handler = handler;
            this.text = text;
        }

        public void addSyntaxErr(
                @NotNull String label,
                @NotNull TypedToken<?> token
        ) {
            handler.add(CompilationMessage.create(
                    text,
                    MessageType.ERROR,
                    CompilationMessageLabel.syntaxError(label), token)
            );
        }

        public void addSyntaxErrAfterToken(
                @NotNull String label,
                @NotNull TypedToken<?> token
        ) {
            handler.add(CompilationMessage.createAfter(
                    text,
                    MessageType.ERROR,
                    CompilationMessageLabel.syntaxError(label),
                    token
            ));
        }

        public void addSyntaxErrBeforeToken(
                @NotNull String label,
                @NotNull TypedToken<?> token
        ) {
            handler.add(CompilationMessage.createBefore(
                    text,
                    MessageType.ERROR,
                    CompilationMessageLabel.syntaxError(label),
                    token
            ));
        }

        public void addInRangeSyntaxErr(
                @NotNull String label,
                int start,
                int end
        ) {
            handler.add(CompilationMessage.create(
                    text,
                    MessageType.ERROR,
                    CompilationMessageLabel.syntaxError(label),
                    start,
                    end
            ));
        }

        public void addInRangeSyntaxErr(
                @NotNull String label,
                @NotNull TypedToken<?> firstToken,
                @NotNull TypedToken<?> lastToken
        ) {
            handler.add(CompilationMessage.create(
                    text,
                    MessageType.ERROR,
                    CompilationMessageLabel.syntaxError(label),
                    firstToken.start,
                    lastToken.end
            ));
        }

        public void addInRangeErr(
                @NotNull CompilationMessageLabel label,
                @NotNull List<TypedToken<?>> tokens
        ) {
            handler.add(CompilationMessage.create(
                    text,
                    MessageType.ERROR,
                    label,
                    tokens.getFirst().start,
                    tokens.getLast().end
            ));
        }

        public void addInRangeErr(
                @NotNull CompilationMessageLabel label,
                @NotNull TypedToken<?> firstToken,
                @NotNull TypedToken<?> lastToken
        ) {
            handler.add(CompilationMessage.create(
                    text,
                    MessageType.ERROR,
                    label,
                    firstToken.start,
                    lastToken.end
            ));
        }

        public void addErr(
                @NotNull String label,
                @NotNull TypedToken<?> token
        ) {
            handler.add(CompilationMessage.create(
                    text,
                    MessageType.ERROR,
                    CompilationMessageLabel.create(label),
                    token
            ));
        }

        public void addErr(
                @NotNull CompilationMessageLabel label,
                @NotNull TypedToken<?> token
        ) {
            handler.add(CompilationMessage.create(
                    text,
                    MessageType.ERROR,
                    label,
                    token
            ));
        }

        public void addWarn(
                @NotNull String message,
                @NotNull TypedToken<?> token
        ) {
            handler.add(CompilationMessage.create(
                    text,
                    MessageType.WARNING,
                    CompilationMessageLabel.create(message),
                    token
            ));
        }

        public void addWarnAfterToken(
                @NotNull String message,
                @NotNull TypedToken<?> token
        ) {
            handler.add(CompilationMessage.createAfter(
                    text,
                    MessageType.WARNING,
                    CompilationMessageLabel.create(message),
                    token
            ));
        }

        public void addWarnBeforeToken(
                @NotNull String message,
                @NotNull TypedToken<?> token
        ) {
            handler.add(CompilationMessage.createBefore(
                    text,
                    MessageType.WARNING,
                    CompilationMessageLabel.create(message),
                    token
            ));
        }

        public void addInRangeWarn(
                @NotNull String message,
                int start,
                int end
        ) {
            handler.add(CompilationMessage.create(
                    text,
                    MessageType.WARNING,
                    CompilationMessageLabel.create(message),
                    start,
                    end
            ));
        }

        public void addInRangeWarn(
                @NotNull String message,
                @NotNull TypedToken<?> firstToken,
                @NotNull TypedToken<?> lastToken
        ) {
            handler.add(CompilationMessage.create(
                    text,
                    MessageType.WARNING,
                    CompilationMessageLabel.create(message),
                    firstToken.start,
                    lastToken.end
            ));
        }
    }

    public static final class ParseContextAssociated extends TextAssociated {
        public final ParseContext context;

        public ParseContextAssociated(
                @NotNull CompilationMessageHandler handler,
                @NotNull ParseContext context
        ) {
            super(handler, context.text);
            this.context = context;
        }

        public void addSyntaxErr(@NotNull String label) {
            add(MessageType.ERROR, CompilationMessageLabel.syntaxError(label));
        }

        public void addInvalidSyntaxErr() {
            add(MessageType.ERROR, CompilationMessageLabel.INVALID_SYNTAX);
        }

        public void add(@NotNull MessageType type, @NotNull CompilationMessageLabel label) {
            final var token = context.nonNewLineCursorOrPrev();

            final var message = context.hasNonNewLineCurrent()
                    ? CompilationMessage.create(text, type, label, token)
                    : CompilationMessage.createAfter(text, type, label, token);

            handler.add(message);
        }
    }
}
