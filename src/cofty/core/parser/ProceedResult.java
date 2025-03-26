package cofty.core.parser;

import cofty.core.message.MessageBuilder;
import cofty.core.ast.IAstObject;
import cofty.core.token.ITokenType;
import cofty.core.token.Token;
import cofty.core.token.TokenType;
import cofty.type.exception.SyntaxError;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ProceedResult<V extends IAstObject<?>> {
    public final ParserContext parserContext;
    public final V astObject;
    public final boolean isOk;
    public boolean strict;
    public final int errors;

    private ProceedResult(
            @NotNull ParserContext parserContext,
            @NotNull V astObject,
            boolean isOk,
            boolean strict,
            int errors
    ) {
        this.parserContext = parserContext;
        this.astObject = astObject;
        this.isOk = isOk;
        this.strict = strict;
        this.errors = errors;
    }

    public @NotNull ProceedResult<V> then(@NotNull BiConsumer<V, Token> consumer) {
        if (isOk)
            consumer.accept(astObject, parserContext.peekPrev());

        return this;
    }

    public @NotNull ProceedResult<V> then(@NotNull Consumer<V> consumer) {
        if (isOk)
            consumer.accept(astObject);

        return this;
    }

    public @NotNull ProceedResult<V> then(@NotNull Runnable runnable) {
        if (isOk)
            runnable.run();

        return this;
    }

    @SuppressWarnings("ConstantConditions")
    public @NotNull ProceedResult<V> consume(@NotNull ITokenType type) {
        if (!parserContext.isOutOfBounds() && parserContext.token().type.equals(type)) {
            parserContext.next();
            return morph(true);
        }

        throw new IllegalStateException(String.format("`%s` is not consumable", parserContext.token()));
    }

    @SuppressWarnings("ConstantConditions")
    public <V1 extends IAstObject<?>> @NotNull ProceedResult<V> consume(
            @NotNull LexemeVisitor<V1> visitor,
            @NotNull BiConsumer<V, ProceedResult<V1>> then,
            @NotNull String syntaxErrorMessage
    ) {
        if (visitor.check(parserContext)) {
            final var result = visitor.fullConsume(parserContext);

            if (result.isOk) {
                then.accept(this.astObject, result);
                return morph(true);
            }

            if (errors == 0)
                return morphWithError(syntaxErrorMessage);
        }

        final var result = morphWithError(syntaxErrorMessage);
        parserContext.next();
        return result;
    }

    @SuppressWarnings("ConstantConditions")
    public @NotNull ProceedResult<V> consume(@NotNull ITokenType type, @NotNull String syntaxErrorMessage) {
        if (!parserContext.isOutOfBounds() && parserContext.token().type.equals(type)) {
            parserContext.next();
            return morph(true);
        }

        final var result = morphWithError(syntaxErrorMessage);
        parserContext.next();
        return result;
    }

    public @NotNull ProceedResult<V> consumeEndOfStatement() {
        if (parserContext.isOutOfBounds())
            return morph(true);

        return consume(TokenType.NEWLINE, "expected end of statement");
    }

    @SuppressWarnings("ConstantConditions")
    public @NotNull ProceedResult<V> match(@NotNull ITokenType... types) {
        for (final var type: types) {
            if (parserContext.token() == null || !type.equals(parserContext.token().type))
                return morph(false);

            parserContext.next();
        }

        return morph(true);
    }

    public @NotNull ProceedResult<V> match(@NotNull LexemeVisitor<V> visitor) {
        return visitor.check(parserContext) ? visitor.consume(parserContext) : morph(false);
    }

    public @NotNull ProceedResult<V> setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    private @NotNull ProceedResult<V> morph(boolean isOk) {
        return new ProceedResult<>(parserContext, astObject, isOk, strict, errors);
    }

    @SuppressWarnings("ConstantConditions")
    private @NotNull ProceedResult<V> morphWithError(@NotNull String syntaxErrorMessage) {
        if (parserContext.isOutOfBounds())
            parserContext.messages.putBuildedMessage(MessageBuilder.errAfter(
                    parserContext.text,
                    parserContext.peekPrev(),
                    new SyntaxError(syntaxErrorMessage)
            ));
        else parserContext.messages.putBuildedMessage(MessageBuilder.err(
                parserContext.text,
                parserContext.token(),
                new SyntaxError(syntaxErrorMessage)
        ));

        return new ProceedResult<>(parserContext, astObject, false, strict, errors + 1);
    }

    public static <V extends IAstObject<?>> @NotNull ProceedResult<V> begin(
            @NotNull ParserContext parserContext,
            @NotNull V astObject,
            boolean strict
    ) {
        return new ProceedResult<>(parserContext, astObject, true, strict, 0);
    }

    public static <V extends IAstObject<?>> @NotNull ProceedResult<V> finish(
            @NotNull ParserContext parserContext,
            @NotNull V astObject
    ) {
        parserContext.next();
        return new ProceedResult<>(parserContext, astObject, true, true, 0);
    }
}
