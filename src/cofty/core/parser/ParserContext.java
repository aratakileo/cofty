package cofty.core.parser;

import cofty.core.message.MessageHandler;
import cofty.core.ast.IAstObject;
import cofty.core.ast.InitVarObject;
import cofty.core.token.*;
import cofty.type.TextContent;
import cofty.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class ParserContext {
    public final List<Token> tokens;
    public final TextContent text;
    public final MessageHandler messages;

    public int index = 0;

    public ParserContext(@NotNull List<Token> tokens, @NotNull TextContent text, @NotNull MessageHandler messages) {
        this.tokens = List.copyOf(tokens);
        this.text = text;
        this.messages = messages;
    }

    public boolean hasNext() {
        return hasNext(1);
    }

    public boolean hasNext(int step) {
        return index + step < tokens.size();
    }

    public boolean isOutOfBounds() {
        return index >= tokens.size();
    }

    public @Nullable Token get(int index) {
        return index < 0 ? null : Lists.get(tokens, index);
    }

    public @Nullable Token token() {
        return get(index);
    }

    @SuppressWarnings("UnusedReturnValue")
    public @Nullable Token next() {
        return next(1);
    }

    public @Nullable Token next(int step) {
        return get(index = Math.min(index + step, tokens.size()));
    }

    public @Nullable Token peekPrev() {
        return peek(-1);
    }

    public @Nullable Token peek() {
        return peek(1);
    }

    public @Nullable Token peek(int step) {
        return get(index + step);
    }

    public boolean peek(@NotNull Function<Token, Boolean> peeker) {
        return peek(peeker, 1);
    }

    public boolean peek(@NotNull Function<Token, Boolean> peeker, int step) {
        return hasNext(step) ? peeker.apply(get(index + step)) : false;
    }

    public void parse() {
        System.out.println(InitVarObject.VISITOR.fullConsume(this).astObject);
    }

    public <T extends IAstObject<?>> @NotNull ProceedResult<T> begin(@NotNull T astObject, boolean strict) {
        return ProceedResult.begin(this, astObject, strict);
    }
}
