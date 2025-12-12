package cofty.core.parser;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.core.parser.ast.FuncDeclarationObject;
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
        if (!context.goNextIfCurrentIs(Keyword.FUN)) return ParseResult.skipped();

        final var nameToken = context.current(Simple.WORD);

        if (!context.goNextIfCurrentIs(Simple.WORD)) {
            context.messages.report(Errors.EXPECTED_NAME, "function");
            context.goNext();

            return ParseResult.failed();
        }

        if (!context.goNextIfCurrentIs(Bracket.ROUND_OPEN)) {
            context.messages.report(
                    Errors.NO_OPENED_BRACKETS,
                    "description of the arguments",
                    "round",
                    "("
            );

            context.goNext();
            return ParseResult.failed();
        }

        final var argsParseResult = Parser.parseSeparatedQueue(
                context,
                Separator.COMMA,
                FieldDeclarationParser.FUNC_ARG,
                null,
                Errors.MISSING_SEPARATOR,
                Errors.UNEXPECTED_SEPARATOR,
                "comma",
                "argument declarations",
                "an argument",
                "comma"
        );

        var isFailed = argsParseResult.isFailed();

        if (!context.goNextIfCurrentIs(Bracket.ROUND_CLOSE)) {
            context.messages.report(
                    Errors.UNCLOSED_BRACKETS,
                    "description of the arguments",
                    "round",
                    ")"
            );
            context.goNext();

            return ParseResult.failed();
        }

        final var returnTypeParseResult = context.goNextIfCurrentIs(Separator.ARROW)
                ? TypeDescriptionParser.DEFAULT.parse(context) : null;

        if (returnTypeParseResult != null && !returnTypeParseResult.isOK()) {
            if (returnTypeParseResult.isSkipped()) {
                context.messages.report(Errors.EXPECTED_FUNC_RETURN_TYPE);
                context.goNext();
            }

            isFailed = true;
        }

        final var bodyParseResult = BodyParser.createFunctionBodyParser(parentBody).parse(context);

        if (!bodyParseResult.isOK()) return ParseResult.failed();

        final var returnType = returnTypeParseResult == null || !returnTypeParseResult.isOK()
                ? null : returnTypeParseResult.valueOrThrow();

        final var body = bodyParseResult.valueOrThrow();

        if (returnType != null && !body.finishedWithReturnStatement) {
            context.messages.report(context.prevOrThrow(), Errors.EXPECTED_FUNC_RETURN_STATEMENT);
            return ParseResult.failed();
        }

        return isFailed ? ParseResult.failed() : ParseResult.OK(new FuncDeclarationObject(
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
