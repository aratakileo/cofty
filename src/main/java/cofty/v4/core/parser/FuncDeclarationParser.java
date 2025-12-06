package cofty.v4.core.parser;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.v4.core.parser.ast.FuncDeclarationObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class FuncDeclarationParser implements Parser<FuncDeclarationObject> {
    public final BodyParser.BodyType parentBody;

    private FuncDeclarationParser(@NotNull BodyParser.BodyType parentBody) {
        this.parentBody = parentBody;
    }

    @Override
    public @NotNull ParseResult<FuncDeclarationObject> parse(@NotNull ParseContext context) {
        if (!context.goNextIfCurrentIs(Keyword.FUN)) return ParseResult.canceled();

        final var nameToken = context.current(Simple.WORD);

        if (!context.goNextIfCurrentIs(Simple.WORD)) {
            context.messages.addSyntaxErr("expected a function name here");
            context.goNext();

            return ParseResult.failed();
        }

        if (!context.goNextIfCurrentIs(Bracket.ROUND_OPEN)) {
            context.messages.addSyntaxErr(
                    "expected the beginning of the description of the arguments here using the round bracket `(`"
            );
            context.goNext();

            return ParseResult.failed();
        }

        final var argsParseResult = Parser.parseSeparatedQueue(
                context,
                Separator.COMMA,
                FieldDeclarationParser.FUNC_ARG,
                null,
                "expected a comma separator here between the argument declarations",
                "expected an argument declaration here, not the comma"
        );

        var isFailed = argsParseResult.isFailed();

        if (!context.goNextIfCurrentIs(Bracket.ROUND_CLOSE)) {
            context.messages.addSyntaxErr(
                    "expected the ending of the description of the arguments here using the round bracket `)`"
            );
            context.goNext();

            return ParseResult.failed();
        }

        final var returnTypeParseResult = context.goNextIfCurrentIs(Separator.ARROW)
                ? TypeDescriptionParser.DEFAULT.parse(context) : null;

        if (returnTypeParseResult != null && !returnTypeParseResult.isSuccessful()) {
            if (returnTypeParseResult.isCanceled()) {
                context.messages.addSyntaxErr("expected a function return type here");
                context.goNext();
            }

            isFailed = true;
        }

        final var bodyParseResult = BodyParser.createFunctionBodyParser(parentBody).parse(context);

        if (!bodyParseResult.isSuccessful()) return ParseResult.failed();

        final var returnType = returnTypeParseResult == null || !returnTypeParseResult.isSuccessful()
                ? null : returnTypeParseResult.valueOrThrow();

        final var body = bodyParseResult.valueOrThrow();

        if (returnType != null && !body.finishedWithReturnStatement) {
            context.messages.addSyntaxErrBeforeToken("expected the return statement", context.prevOrThrow());
            return ParseResult.failed();
        }

        return isFailed ? ParseResult.failed() : ParseResult.successful(new FuncDeclarationObject(
                Objects.requireNonNull(nameToken).strictAs(),
                argsParseResult.valueOrDefault(List.of()),
                body,
                returnType
        ));
    }

    public static @NotNull FuncDeclarationParser create(@NotNull BodyParser.BodyType parentBody) {
        return new FuncDeclarationParser(parentBody);
    }
}
