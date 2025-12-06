package cofty.core.parser;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.parser.ast.AstObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface Parser<R> {
    @NotNull ParseResult<R> parse(@NotNull ParseContext context);

    static <_R extends AstObject> ParseResult<List<_R>> parseSeparatedQueue(
            @NotNull ParseContext context,
            @NotNull TokenType separator,
            @NotNull Parser<_R> separatedObjectParser,
            @Nullable AstObjectFilter<_R> separatedObjectFilter,
            @NotNull String noSeparatorFailMessage,
            @Nullable String duplicatedSeparatorFailMessage
    ) {
        final var separatedObjects = new ArrayList<_R>();

        var separatedObjectParseResult = (ParseResult<? extends _R>)null;
        var separatedObjectStartsWithToken = (TypedToken<?>)null;
        var isAlreadySeparated = true;
        var isFailed = false;

        while ((separatedObjectStartsWithToken = context.current()) != null) {
            if (isAlreadySeparated && context.currentIs(separator)) {
                if (duplicatedSeparatorFailMessage != null)
                    context.messages.addSyntaxErr(duplicatedSeparatorFailMessage);

                context.goNext();

                return ParseResult.failed();
            }

            separatedObjectParseResult = separatedObjectParser.parse(context);

            if (!isAlreadySeparated && !separatedObjectParseResult.isCanceled()) {
                context.messages.addSyntaxErrBeforeToken(
                        noSeparatorFailMessage,
                        Objects.requireNonNull(separatedObjectStartsWithToken)
                );

                isFailed = true;
            }

            if (separatedObjectParseResult.isCanceled()) break;

            isAlreadySeparated = context.goNextIfCurrentIs(separator);

            if (separatedObjectParseResult.isFailed()) {
                isFailed = true;
                continue;
            }

            final var separatedObject = separatedObjectParseResult.valueOrThrow();

            if (separatedObjectFilter == null || separatedObjectFilter.applyFilter(separatedObject)) {
                separatedObjects.add(separatedObject);
                continue;
            }

            context.messages.addSyntaxErr("not allowed here", separatedObjectStartsWithToken);
            isFailed = true;
        }

        if (isFailed)
            return ParseResult.failed();

        if (separatedObjects.isEmpty())
            return ParseResult.canceled();

        return ParseResult.successful(separatedObjects.stream().toList());
    }
}
