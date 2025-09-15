package cofty.core.parse;

import cofty.core.ast.AstValue;
import cofty.core.message.MessageBuilder;
import cofty.core.message.MessageHandler;
import cofty.core.token.Token;
import cofty.core.token.TokenType;
import cofty.type.TextContent;
import cofty.type.exception.SyntaxError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ParseContext {
    private final ParseContext parent;

    public final List<Token> tokens;
    public final TextContent text;
    public final MessageHandler messages;

    private int index = 0, successfulCases = 0, indexSnapshot = -1;
    protected boolean failed = false, elseProcessed = false;

    private ParseContext(@NotNull ParseContext parent) {
        this.tokens = parent.tokens;
        this.text = parent.text;
        this.messages = new MessageHandler();
        this.parent = parent;
        this.index = parent.index;
        this.failed = parent.failed;

        if (parent.failed)
            this.elseProcessed = true;
    }

    public ParseContext(
            @NotNull List<Token> tokens,
            @NotNull TextContent text,
            @NotNull MessageHandler messages
    ) {
        this.tokens = tokens;
        this.text = text;
        this.messages = messages;
        this.parent = null;
    }

    public boolean isFailed() {
        return failed;
    }

    public <T extends AstValue, V extends AstParser<T>, P extends AstPeeker> @NotNull ParseContext matchIf(
            @NotNull P peeker,
            @NotNull V value,
            @NotNull Consumer<T> consumer
    ) {
        if (peeker.peek(this))
            _match(value).ifPresent(consumer);

        return this;
    }

    public <T extends AstValue, V extends AstParser<T>> @NotNull ParseContext match(
            @NotNull V value,
            @NotNull BiConsumer<V, T> consumer
    ) {
        _match(value).ifPresent(_value -> consumer.accept(value, _value));
        return this;
    }

    public <T extends AstValue, V extends AstParser<T>> @NotNull ParseContext match(
            @NotNull V value,
            @NotNull Consumer<T> consumer
    ) {
        _match(value).ifPresent(consumer);
        return this;
    }

    public @NotNull ParseContext match(@NotNull AstParser<? extends AstValue> value) {
        _match(value);
        return this;
    }

    private <T extends AstValue, V extends AstParser<T>> @NotNull Optional<T> _match(@NotNull V value) {
        if (failed) return Optional.empty();

        var result = value.parse(this);

        if (result.isEmpty()) {
            failed = true;
            return Optional.empty();
        }

        next();
        successfulCases++;

        return result;
    }

    public @NotNull ParseContext syntaxErrorOnFail_v2(@NotNull String errorMessage) {
        return errorOnFail_v2(new SyntaxError(errorMessage));
    }

    public @NotNull ParseContext errorOnFail_v2(@NotNull Exception err) {
        if (!failed || elseProcessed) return this;

        elseProcessed = true;

        messages.putBuildedMessage(
                hasCurrent() && !currentOrThrow().type.equals(TokenType.NEWLINE)
                        ? MessageBuilder.err(text, currentOrThrow(), err)
                        : MessageBuilder.errAfter(text, strictPeekPrev(), err)
        );
        return this;
    }

    public @NotNull ParseContext errorOnFail(@NotNull Exception err) {
        messages.putBuildedMessage(
                hasCurrent() && !currentOrThrow().type.equals(TokenType.NEWLINE)
                        ? MessageBuilder.err(text, currentOrThrow(), err)
                        : MessageBuilder.errAfter(text, strictPeekPrev(), err)
        );
        return this;
    }

    public void resetIndexSnapshot() {
        indexSnapshot = -1;
    }

    public void createIndexSnapshot() {
        if (indexSnapshot != -1)
            throw new IllegalStateException();

        indexSnapshot = index;
    }

    public void rollbackIndex() {
        if (indexSnapshot == -1)
            throw new IllegalStateException();

        index = indexSnapshot;

        resetIndexSnapshot();
    }

    public int cursorStart() {
        return hasCurrent() ? tokens.get(index).start : tokens.get(index - 1).end;
    }

    public int cursorEnd() {
        return hasCurrent() ? tokens.get(index).end : tokens.get(index - 1).end;
    }

    public @NotNull ParseContext finishTransaction(boolean mayIgnoreFail) {
        if (parent == null)
            return this;

        if (!mayIgnoreFail || successfulCases > 0) {
            parent.messages.putMessages(messages);
            parent.failed = failed;
        }

        if (failed)
            return parent;

        parent.index = index;
        parent.successfulCases += successfulCases;

        return parent;
    }

    public @NotNull ParseContext finishTransaction() {
        return finishTransaction(false);
    }

    public boolean hasNext() {
        return index < tokens.size() - 1;
    }

    public boolean hasCurrent() {
        return index < tokens.size();
    }

    public @NotNull Token currentOrThrow() {
        if (!hasCurrent())
            throw new RuntimeException("has no current token");

        return tokens.get(index);
    }

    public @NotNull Token nextOrThrow() {
        if (!hasNext())
            throw new RuntimeException("has no next token");

        return tokens.get(++index);
    }

    public @Nullable Token next() {
        if (!hasCurrent())
            return null;

        index++;

        return hasCurrent() ? tokens.get(index) : null;
    }

    public boolean peek(int step, @NotNull Function<Token, Boolean> peeker) {
        return index + step < tokens.size() && peeker.apply(tokens.get(index + step));
    }

    public @NotNull Token strictPeekPrev() {
        if (index == 0)
            throw new RuntimeException("has no prev token");

        return tokens.get(index - 1);
    }

    public @NotNull ParseContext startTransaction() {
        return new ParseContext(this);
    }
}
