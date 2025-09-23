package cofty.core.message;

import cofty.type.Representable;
import cofty.type.TextContent;
import org.jetbrains.annotations.NotNull;

public class Message {
    private final TextContent textContent;
    public final MessageType type;
    public final String content;
    public final int lineNumber, start, end, cursorStart, cursorEnd;

    public Message(
            @NotNull TextContent textContent,
            @NotNull MessageType type,
            @NotNull String content,
            int lineNumber,
            int start,
            int end,
            int cursorStart,
            int cursorEnd
    ) {
        this.textContent = textContent;
        this.type = type;
        this.content = content;
        this.lineNumber = lineNumber;
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

    private @NotNull String line() {
        return textContent.text.substring(lineStart(), lineEnd());
    }

    private @NotNull String cursorContent() {
        return " ".repeat(cursorStart + Math.max(start - lineStart(), 0)) + "^".repeat(cursorEnd - cursorStart);
    }

    @Override
    public String toString() {
        return String.format(
                """
                %s: File %s, line %s
                    %s
                    %s
                %s""",
                type,
                Representable.repr(textContent.path),
                lineNumber,
                line(),
                cursorContent(),
                content
        );
    }
}
