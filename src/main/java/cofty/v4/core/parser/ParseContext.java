package cofty.v4.core.parser;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.v4.core.compiler.message.CompilationMessageHandler;
import cofty.type.TextContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class ParseContext {
    public final List<TypedToken<?>> tokens;
    public final TextContent text;
    public final CompilationMessageHandler.ParseContextAssociated messages;

    private int index = 0;
    private final ArrayList<Integer> indexSnapshotStack = new ArrayList<>();

    private final ArrayList<Boolean> skipNewLinesStack = new ArrayList<>();

    private boolean skipNewLines = true, isNewLineSkipped = false;

    public ParseContext(
            @NotNull List<TypedToken<?>> tokens,
            @NotNull TextContent text,
            @NotNull CompilationMessageHandler messages
    ) {
        this.tokens = tokens;
        this.text = text;
        this.messages = messages.associateWith(this);
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

    public void rollbackSkippingNewLinesState(int index) {
        if (skipNewLinesStack.isEmpty())
            throw new IllegalStateException();

        skipNewLines = skipNewLinesStack.get(index);

        skipNewLinesStack.subList(index + 1, skipNewLinesStack.size()).clear();
    }

    public int skippingNewLineStatesSize() {
        return skipNewLinesStack.size();
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

    public boolean hasNext() {
        return index < tokens.size() - 1;
    }

    private boolean hasPrev() {
        return index > 0;
    }

    public boolean hasCurrent() {
        return index < tokens.size();
    }

    public boolean hasNonNewLineCurrent() {
        return hasCurrent() && !currentOrThrow().type.equals(Simple.NEWLINE) && !isNewLineSkipped;
    }

    public @Nullable TypedToken<?> current() {
        return hasCurrent() ? tokens.get(index) : null;
    }

    public @NotNull TypedToken<?> currentOrThrow() {
        return Objects.requireNonNull(current());
    }

    public @Nullable TypedToken<?> current(@NotNull TokenType except) {
        if (!hasCurrent()) return null;

        if (canSkipNewLine(except))
            skipNewLine();

        return current();
    }

    public @Nullable TypedToken<?> current(boolean canTrySkipNewLines) {
        if (!hasCurrent()) return null;
        if (!canTrySkipNewLines || !canSkipNewLine()) return current();

        skipNewLine();

        return current();
    }

    public @Nullable TypedToken<?> prev() {
        return hasPrev() ? tokens.get(index - 1) : null;
    }

    public @NotNull TypedToken<?> prevOrThrow() {
        return Objects.requireNonNull(prev());
    }

    public @Nullable TypedToken<?> next() {
        return tokens.get(index + 1);
    }

    public @NotNull TypedToken<?> nextOrThrow() {
        return Objects.requireNonNull(next());
    }

    public void skipNewLine() {
        if (!hasCurrent() || !currentOrThrow().type.equals(Simple.NEWLINE))
            throw new IllegalStateException();

        goNext();

        isNewLineSkipped = true;
    }

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

    public @NotNull TypedToken<?> nonNewLineCursorOrPrev() {
        if (isNewLineSkipped) return tokens.get(index - 2);

        return hasNonNewLineCurrent() ? currentOrThrow() : prevOrThrow();
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

    private boolean canSkipNewLine() {
        return skipNewLines
                && hasNext()
                && currentOrThrow().type.equals(Simple.NEWLINE);
    }

    private boolean canSkipNewLine(@NotNull TokenType type) {
        return canSkipNewLine()
                && !type.equals(Simple.NEWLINE)
                && nextOrThrow().type.equals(type);
    }

    private boolean canSkipNewLine(@NotNull Collection<TokenType> types) {
        return canSkipNewLine()
                && !types.contains(Simple.NEWLINE)
                && types.contains(nextOrThrow().type);
    }
}
