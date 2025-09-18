package cofty.core.message;

import cofty.core.lexer.token.Token;
import cofty.type.Representable;
import cofty.type.TextContent;
import cofty.util.Strings;
import org.jetbrains.annotations.NotNull;

public class MessageBuilder {
    private final MessageType messageType;
    private final TextContent textContent;
    private String content = null;
    private int start;
    private int end;
    private int cursorStart;
    private int cursorEnd;

    public MessageBuilder(
            @NotNull MessageType messageType,
            @NotNull TextContent textContent
    ) {
        this.messageType = messageType;
        this.textContent = textContent;
    }

    public @NotNull MessageBuilder setRange(int start, int end) {
        if (start < end && end > 0) {
            this.start = start;
            this.end = end;

            return this;
        }

        throw new IllegalArgumentException("Invalid start or/and end value(s)");
    }

    public @NotNull MessageBuilder setCursorRange(int cursorStart, int cursorEnd) {
        if (cursorStart < cursorEnd && cursorEnd > 0) {
            this.cursorStart = cursorStart;
            this.cursorEnd = cursorEnd;
            return this;
        }

        throw new IllegalArgumentException("Invalid cursorStart or/and cursorEnd value(s)");
    }

    public @NotNull MessageBuilder fillCursor(@NotNull Token token) {
        return fillCursor(token.start, token.end);
    }

    public @NotNull MessageBuilder fillCursor(int start, int end) {
        return setRange(start, end).setCursorRange(0, end - start);
    }

    public @NotNull MessageBuilder moveCursor(int step) {
        cursorStart = Math.max(0, cursorStart + step);
        cursorEnd = Math.max(cursorStart + 1, cursorEnd + step);
        return this;
    }

    public @NotNull MessageBuilder setCursorLengthByLeft(int length) {
        cursorEnd = Math.max(cursorStart + 1, cursorStart + length);
        return this;
    }

    public @NotNull MessageBuilder setCursorLengthByRight(int length) {
        cursorStart = Math.min(cursorEnd - 1, cursorEnd - length);
        return this;
    }

    public @NotNull MessageBuilder setContent(@NotNull String content) {
        this.content = content;
        return this;
    }

    public @NotNull MessageBuilder setContent(@NotNull Exception exception) {
        this.content = String.format("%s: %s", exception.getClass().getSimpleName(), exception.getMessage());
        return this;
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

    private int lineNumberByStart() {
        return Strings.getLineNumber(textContent.text, start);
    }

    private int lineNumberByEnd() {
        return Strings.getLineNumber(textContent.text, end);
    }

    private @NotNull String cursorContent() {
        return " ".repeat(cursorStart + Math.max(start - lineStart(), 0)) + "^".repeat(cursorEnd - cursorStart);
    }

    public @NotNull String build() {
        if (content == null)
            throw new IllegalStateException("Message content is not set");

        return String.format(
                """
                %s: File %s, line %s
                    %s
                    %s
                %s""",
                messageType,
                Representable.repr(textContent.path),
                lineNumberByStart(),
                line(),
                cursorContent(),
                content
        );
    }

    @Override
    public String toString() {
        return "MessageBuilder{" +
                "messageType=" + messageType +
                ", textContent=" + textContent +
                ", content=" + Representable.repr(content) +
                ", start=" + start +
                ", end=" + end +
                ", cursorStart=" + cursorStart +
                ", cursorEnd=" + cursorEnd +
                '}';
    }

    public static @NotNull String err(
            @NotNull TextContent textContent,
            @NotNull Token token,
            @NotNull Exception err
    ) {
        return err(textContent, token.start, token.end, err);
    }

    public static @NotNull String err(
            @NotNull TextContent textContent,
            int cursorStart,
            int cursorEnd,
            @NotNull Exception err
    ) {
        return new MessageBuilder(MessageType.ERROR, textContent)
                .setContent(err)
                .fillCursor(cursorStart, cursorEnd)
                .build();
    }

    public static @NotNull String errAfter(
            @NotNull TextContent textContent,
            @NotNull Token token,
            @NotNull Exception err
    ) {
        return new MessageBuilder(MessageType.ERROR, textContent)
                .setContent(err)
                .fillCursor(token)
                .setCursorLengthByRight(1)
                .moveCursor(1)
                .build();
    }

    public enum MessageType {
        ERROR,
        WARNING
    }
}
