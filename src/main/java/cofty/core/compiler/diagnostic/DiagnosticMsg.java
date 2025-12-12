package cofty.core.compiler.diagnostic;

import cofty.core.lexer.token.TypedToken;
import cofty.type.Representable;
import cofty.type.TextContent;
import cofty.util.Integers;
import cofty.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class DiagnosticMsg {
    private final static int TAB_SIZE = 4;

    private final TextContent textContent;
    public final DiagnosticCode code;
    public final DiagnosticDescriptor descriptor;

    public final int lineStartNumber, start, end, cursorStart, cursorEnd;

    public DiagnosticMsg(
            @NotNull TextContent textContent,
            @NotNull DiagnosticCode code,
            @NotNull DiagnosticDescriptor descriptor,
            int lineStartNumber,
            int start,
            int end,
            int cursorStart,
            int cursorEnd
    ) {
        this.textContent = textContent;
        this.code = code;
        this.descriptor = descriptor;
        this.lineStartNumber = lineStartNumber;
        this.start = start;
        this.end = end;
        this.cursorStart = cursorStart;
        this.cursorEnd = cursorEnd;
    }

    private int lineStart() {
        return textContent.text.lastIndexOf('\n', start) + 1;
    }

    private int lineEnd() {
        final var lineEnd = textContent.text.indexOf('\n', end);
        return lineEnd == -1 ? textContent.text.length() : lineEnd;
    }

    private boolean isCodePreviewMutline() {
        return rawCodePreview().contains("\n");
    }

    private int codePreviewNewLines() {
        return Strings.count(rawCodePreview(), "\n");
    }

    private @NotNull String rawCodePreview() {
        return textContent.text.substring(lineStart(), lineEnd());
    }

    private @NotNull String codePreview() {
        final var rawCodePreview = rawCodePreview();

        if (!isCodePreviewMutline()) return " ".repeat(TAB_SIZE) + rawCodePreview;

        final var maxNumberLength = Integers.digitsCount(lineStartNumber + codePreviewNewLines());

        var lineNumber = lineStartNumber;
        var lastDemonstratedLineNumber = lineStartNumber;
        var offset = 0;

        final var codePreview = new StringBuilder();

        for (final var lineSegment: rawCodePreview.split("\n")) {
            if (lineSegment.isEmpty()) {
                lineNumber++;
                continue;
            }

            if (lastDemonstratedLineNumber != lineNumber) codePreview.append("\n");

            if (lineNumber - lastDemonstratedLineNumber > 1)
                codePreview.append(" ".repeat(TAB_SIZE + maxNumberLength - 1)).append("...\n");

            codePreview.append(" ".repeat(TAB_SIZE + maxNumberLength - Integers.digitsCount(lineNumber)))
                    .append(lineNumber)
                    .append(" | ")
                    .append(" ".repeat(offset))
                    .append(lineSegment);

            lastDemonstratedLineNumber = lineNumber;
            offset += lineSegment.length();
            lineNumber++;
        }

        return codePreview.toString();
    }

    private @NotNull String cursorContent() {
        final var additionalOffset = isCodePreviewMutline() ? Integers.digitsCount(lineStartNumber + codePreviewNewLines()) + 3 : 0;

        return " ".repeat(cursorStart + Math.max(start - lineStart(), 0) + TAB_SIZE + additionalOffset)
                + "^".repeat(cursorEnd - cursorStart - (isCodePreviewMutline() ? codePreviewNewLines() : 0));
    }

    private @NotNull String linesRange() {
        if (!isCodePreviewMutline())
            return "line " + lineStartNumber;

        return String.format(
                "lines %s through %s",
                lineStartNumber,
                lineStartNumber + codePreviewNewLines()
        );
    }

    @Override
    public String toString() {
        return String.format(
                """
                File %s, %s
                %s
                %s
                %s""",
                Representable.repr(textContent.path),
                linesRange(),
                codePreview(),
                cursorContent(),
                descriptor.with(code)
        );
    }

    public static @NotNull DiagnosticMsg create(
            @NotNull TextContent textContent,
            int inTextCursorStart,
            int inTextCursorEnd,
            @NotNull DiagnosticCode diagnosticCode,
            @NotNull Object @NotNull... formatArgs
    ) {
        return new Builder(textContent, diagnosticCode, formatArgs)
                .fillCursor(inTextCursorStart, inTextCursorEnd)
                .build();
    }

    public static @NotNull DiagnosticMsg createAfter(
            @NotNull TextContent textContent,
            @NotNull TypedToken<?> token,
            @NotNull DiagnosticCode diagnosticCode,
            @NotNull Object @NotNull... formatArgs
    ) {
        return new Builder(textContent, diagnosticCode, formatArgs)
                .fillCursor(token)
                .setCursorLengthByRight(1)
                .moveCursor(1)
                .build();
    }

    public static final class Builder {
        private final DiagnosticCode code;
        private final TextContent textContent;
        private DiagnosticDescriptor descriptor = null;
        private int start;
        private int end;
        private int cursorStart;
        private int cursorEnd;

        public Builder(
                @NotNull TextContent textContent,
                @NotNull DiagnosticCode code,
                @NotNull Object @NotNull... formatArgs
        ) {
            this.code = code;
            this.textContent = textContent;
            this.descriptor = code.formatted(formatArgs);
        }

        public @NotNull Builder setRange(int start, int end) {
            if (start < end && end > 0) {
                this.start = start;
                this.end = end;

                return this;
            }

            throw new IllegalArgumentException("Invalid start or/and end value(s)");
        }

        public @NotNull Builder setCursorRange(int cursorStart, int cursorEnd) {
            if (cursorStart < cursorEnd && cursorEnd > 0) {
                this.cursorStart = cursorStart;
                this.cursorEnd = cursorEnd;
                return this;
            }

            throw new IllegalArgumentException("Invalid cursorStart or/and cursorEnd value(s)");
        }

        public @NotNull Builder fillCursor(@NotNull TypedToken<?> token) {
            return fillCursor(token.start, token.end);
        }

        public @NotNull Builder fillCursor(int start, int end) {
            return setRange(start, end).setCursorRange(0, end - start);
        }

        public @NotNull Builder moveCursor(int step) {
            cursorStart = cursorStart + step;
            cursorEnd = Math.max(cursorStart + 1, cursorEnd + step);
            return this;
        }

        public @NotNull Builder setCursorLengthByLeft(int length) {
            cursorEnd = Math.max(cursorStart + 1, cursorStart + length);
            return this;
        }

        public @NotNull Builder setCursorLengthByRight(int length) {
            cursorStart = Math.min(cursorEnd - 1, cursorEnd - length);
            return this;
        }

        private int lineNumberByStart() {
            return Strings.getLineNumber(textContent.text, start);
        }

        private int lineNumberByEnd() {
            return Strings.getLineNumber(textContent.text, end);
        }

        public @NotNull DiagnosticMsg build() {
            if (descriptor == null)
                throw new IllegalStateException("Message content is not set");

            return new DiagnosticMsg(
                    textContent,
                    code,
                    descriptor,
                    lineNumberByStart(),
                    start,
                    end,
                    cursorStart,
                    cursorEnd
            );
        }

        @Override
        public String toString() {
            return "MessageBuilder{" +
                    "messageType=" + code +
                    ", textContent=" + textContent +
                    ", descriptor=" + Representable.repr(descriptor.toString()) +
                    ", start=" + start +
                    ", end=" + end +
                    ", cursorStart=" + cursorStart +
                    ", cursorEnd=" + cursorEnd +
                    '}';
        }
    }

    public enum Severity {
        ERR,
        WARN
    }
}
