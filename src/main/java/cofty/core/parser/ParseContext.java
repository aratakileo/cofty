package cofty.core.parser;

import cofty.core.lexer.token.TokenType;
import cofty.core.lexer.token.TypedToken;
import cofty.core.message.MessageHandler;
import cofty.core.message.channel.ParserMessagesChannel;
import cofty.type.QueueIterator;
import cofty.type.TextContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ParseContext implements QueueIterator<TypedToken<?>> {
    public final List<TypedToken<?>> tokens;
    public final TextContent text;
    public final MessageHandler messages;

    public final ParserMessagesChannel CRITICAL_MESSAGES, NON_CRITICAL_MESSAGES;

    private int index = 0;
    private final ArrayList<Integer> indexSnapshotStack = new ArrayList<>();

    private boolean isNewLineSkipped = false;

    public ParseContext(
            @NotNull List<TypedToken<?>> tokens,
            @NotNull TextContent text,
            @NotNull MessageHandler messages
    ) {
        this.tokens = tokens;
        this.text = text;
        this.messages = messages;

        CRITICAL_MESSAGES = messages.CRITICAL.associate(this);
        NON_CRITICAL_MESSAGES = messages.NON_CRITICAL.associate(this);
    }

    public int snapshotStackSize() {
        return indexSnapshotStack.size();
    }

    public void removeIndexSnapshot() {
        indexSnapshotStack.removeLast();
    }

    public void createIndexSnapshot() {
        indexSnapshotStack.add(index);
    }

    public void rollbackIndex() {
        index = indexSnapshotStack.removeLast();
        isNewLineSkipped = false;
    }

    @Override
    public boolean hasNext() {
        return index < tokens.size() - 1;
    }

    @Override
    public boolean hasCurrent() {
        return index < tokens.size();
    }

    public boolean hasNonNewLineCurrent() {
        return hasCurrent() && !currentOrThrow().type.equals(TokenType.NEWLINE) && !isNewLineSkipped;
    }

    @Override
    public @Nullable TypedToken<?> current() {
        return tokens.get(index);
    }

    @Override
    public @Nullable TypedToken<?> prev() {
        return tokens.get(index - 1);
    }

    @Override
    public @Nullable TypedToken<?> next() {
        return tokens.get(index + 1);
    }

    public void skipNewLine() {
        if (!hasCurrent() || !currentOrThrow().type.equals(TokenType.NEWLINE))
            throw new IllegalStateException();

        goNext();

        isNewLineSkipped = true;
    }

    @Override
    public @Nullable TypedToken<?> goNext() {
        if (!hasCurrent())
            return null;

        index++;
        isNewLineSkipped = false;

        return hasCurrent() ? tokens.get(index) : null;
    }

    public @NotNull TypedToken<?> cursor() {
        return hasCurrent() ? currentOrThrow() : prevOrThrow();
    }

    public @NotNull TypedToken<?> nonNewLineCursorOrPrev() {
        if (isNewLineSkipped) return peekOrThrow(-2);

        return hasNonNewLineCurrent() ? currentOrThrow() : prevOrThrow();
    }

    public @Nullable TypedToken<?> peek(int step) {
        return tokens.get(index + step);
    }

    public @NotNull TypedToken<?> peekOrThrow(int step) {
        return Objects.requireNonNull(tokens.get(index + step));
    }

    public boolean peek(int step, @NotNull Function<TypedToken<?>, Boolean> peeker) {
        return index + step < tokens.size() && peeker.apply(tokens.get(index + step));
    }
}
