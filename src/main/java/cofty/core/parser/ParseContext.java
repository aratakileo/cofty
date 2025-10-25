package cofty.core.parser;

import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.message.MessageHandler;
import cofty.core.message.channel.ParserMessagesChannel;
import cofty.type.QueueIterator;
import cofty.type.TextContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
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

    private final ArrayList<Boolean> skipNewLinesStack = new ArrayList<>();

    private boolean skipNewLines = true, isNewLineSkipped = false;

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

    public int index() {
        return index;
    }

    public boolean isSkippingNewLines() {
        return skipNewLines;
    }

    public void startSkippingNewLines() {
        skipNewLinesStack.add(skipNewLines);
        skipNewLines = true;
    }

    public void stopSkippingNewLines() {
        skipNewLinesStack.add(skipNewLines);
        skipNewLines = false;
    }

    public void rollbackSkippingNewLinesState() {
        if (skipNewLinesStack.isEmpty())
            throw new IllegalStateException();

        skipNewLines = skipNewLinesStack.removeLast();
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
        return hasCurrent() && !currentOrThrow().type.equals(Simple.NEWLINE) && !isNewLineSkipped;
    }

    @Override
    public @Nullable TypedToken<?> current() {
        return hasCurrent() ? tokens.get(index) : null;
    }

    public @Nullable TypedToken<?> current(@NotNull TokenType except) {
        if (!hasCurrent()) return null;

        if (skipNewLines && currentOrThrow().type.equals(Simple.NEWLINE) && !except.equals(Simple.NEWLINE))
            skipNewLine();

        return tokens.get(index);
    }

    @Override
    @Deprecated
    public @Nullable TypedToken<?> prev() {
        return tokens.get(index - 1);
    }

    @Override
    @Deprecated
    public @Nullable TypedToken<?> next() {
        return tokens.get(index + 1);
    }

    public void skipNewLine() {
        if (!hasCurrent() || !currentOrThrow().type.equals(Simple.NEWLINE))
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

    public @Nullable TypedToken<?> advance() {
        final var current = current();

        goNext();

        return current;
    }

    public @NotNull TypedToken<?> advanceOrThrow() {
        return Objects.requireNonNull(advance());
    }

    @Deprecated
    public @NotNull TypedToken<?> cursor() {
        return hasCurrent() ? currentOrThrow() : prevOrThrow();
    }

    public @NotNull TypedToken<?> nonNewLineCursorOrPrev() {
        if (isNewLineSkipped) return peekOrThrow(-2);

        return hasNonNewLineCurrent() ? currentOrThrow() : prevOrThrow();
    }

    @Deprecated
    private @Nullable TypedToken<?> peek(int step) {
        return tokens.get(index + step);
    }

    @Deprecated
    private @NotNull TypedToken<?> peekOrThrow(int step) {
        return Objects.requireNonNull(tokens.get(index + step));
    }

    @Deprecated
    public boolean peek(int step, @NotNull Function<TypedToken<?>, Boolean> peeker) {
        return index + step < tokens.size() && peeker.apply(tokens.get(index + step));
    }

    public boolean currentIs(@NotNull TokenType type) {
        if (!hasCurrent()) return false;
        if (canSkipNewLine(type)) skipNewLine();

        return currentOrThrow().type.equals(type);
    }

    public boolean currentIsAny(@NotNull Collection<TokenType> types) {
        if (!hasCurrent()) return false;
        if (canSkipNewLine(types)) skipNewLine();

        return types.contains(currentOrThrow().type);
    }

    public boolean goNextIfCurrentIs(@NotNull TokenType type) {
        if (!currentIs(type)) return false;

        goNext();

        return true;
    }

    public boolean goNextIfCurrentIsAny(@NotNull Collection<TokenType> types) {
        if (!currentIsAny(types)) return false;

        goNext();

        return true;
    }

    private boolean canSkipNewLine(@NotNull TokenType type) {
        return skipNewLines
                && hasNext()
                && currentOrThrow().type.equals(Simple.NEWLINE)
                && !type.equals(Simple.NEWLINE)
                && tokens.get(index + 1).type.equals(type);
    }

    private boolean canSkipNewLine(@NotNull Collection<TokenType> types) {
        return skipNewLines
                && hasNext()
                && currentOrThrow().type.equals(Simple.NEWLINE)
                && !types.contains(Simple.NEWLINE)
                && types.contains(tokens.get(index + 1).type);
    }
}
