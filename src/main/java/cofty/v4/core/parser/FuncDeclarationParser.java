package cofty.v4.core.parser;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.v4.core.parser.ast.FieldDeclarationObject;
import cofty.v4.core.parser.ast.FuncDeclarationObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public final class FuncDeclarationParser implements Parser<FuncDeclarationObject> {
    public static final FuncDeclarationParser DEFAULT = new FuncDeclarationParser();

    private FuncDeclarationParser() {}

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

        final var args = new ArrayList<FieldDeclarationObject>();

        var argParseResult = (ParseResult<FieldDeclarationObject>)null;
        var lineStartsWithToken = context.current();
        var commaSeparatorProceed = true;
        var isFailed = false;

        while (true) {
            argParseResult = FieldDeclarationParser.FUNC_ARG.parse(context);

            if (!commaSeparatorProceed && !argParseResult.isCanceled()) {
                context.messages.addSyntaxErrBeforeToken(
                        "expected a comma separator here between the argument declarations",
                        Objects.requireNonNull(lineStartsWithToken)
                );

                isFailed = true;
            }

            if (argParseResult.isCanceled()) break;

            if (argParseResult.isFailed()) isFailed = true;
            else args.add(argParseResult.valueOrThrow());

            commaSeparatorProceed = context.goNextIfCurrentIs(Separator.COMMA);
            lineStartsWithToken = context.current();
        }

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

        final var bodyParseResult = BodyParser.FUNC_BODY.parse(context);

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
                args.stream().toList(),
                body,
                returnType
        ));
    }
}
