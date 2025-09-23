package cofty.core.lexer.token;

import cofty.core.parser.ParseContext;
import cofty.type.Containable;
import cofty.v2.core.parse.AstParseEntry;
import cofty.v2.core.parse.AstParser;
import cofty.v2.core.parse.AstPeeker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public interface ITokenType extends Containable<ITokenType>, AstParser<Token>, AstPeeker {
    @NotNull TokenType type();
    @Nullable String content();

    default boolean equals(@NotNull ITokenType itype) {
        return itype.type().equals(type())
                && (itype.content() == null) == (content() == null)
                && Objects.equals(itype.content(), content());
    }

    @Override
    default @NotNull Optional<Token> parse(@NotNull ParseContext context) {
        return peek(context) ? Optional.of(context.currentOrThrow()) : Optional.empty();
    }

    default @NotNull AstParser<Token> getTransactableParser() {
        return context -> {
            final var result = new AtomicReference<Token>(null);

            context.startTransaction().match(this, result::set).finishTransaction(true);

            return Optional.ofNullable(result.get());
        };
    }

    default boolean safePeek(@NotNull ParseContext context) {
        return equals(context.currentOrThrow().type);
    }

    default @NotNull AstParseEntry<Token> getParseEntry() {
        return AstParseEntry.bind(this, getTransactableParser());
    }
}
