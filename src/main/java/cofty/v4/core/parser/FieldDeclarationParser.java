package cofty.v4.core.parser;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.operator.Assign;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.v4.core.parser.ast.FieldDeclarationObject;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Objects;

public final class FieldDeclarationParser implements Parser<FieldDeclarationObject> {
    public static final FieldDeclarationParser DEFAULT = new FieldDeclarationParser(false),
            FUNC_ARG = new FieldDeclarationParser(true);

    private final boolean asFuncArgument;

    private FieldDeclarationParser(boolean asFuncArgument) {
        this.asFuncArgument = asFuncArgument;
    }

    @Override
    public @NotNull ParseResult<FieldDeclarationObject> parse(@NotNull ParseContext context) {
        boolean isFailed = false;

        if (asFuncArgument && context.currentIs(Keyword.VAR)) {
            context.messages.addSyntaxErr("not allowed here");
            context.goNext();

            isFailed = true;
        } else if (!asFuncArgument && !context.goNextIfCurrentIs(Keyword.VAR))
            return ParseResult.canceled();

        final var mutableToken = context.currentIs(Keyword.MUT) ? context.advanceOrThrow() : null;
        final var nameToken = context.current(Simple.WORD);

        if (!context.goNextIfCurrentIs(Simple.WORD)) {
            if (asFuncArgument && mutableToken == null && !isFailed)
                return ParseResult.canceled();

            context.messages.addSyntaxErr(String.format("expected a %s name here", declarationName()));
            context.goNext();

            return ParseResult.failed();
        }

        final var valueTypeParseResult = context.goNextIfCurrentIs(Separator.COLON)
                ? TypeDescriptionParser.DEFAULT.parse(context) : null;

        if (valueTypeParseResult != null && !valueTypeParseResult.isSuccessful()) {
            if (valueTypeParseResult.isCanceled())
                context.messages.addSyntaxErr(String.format("expected a %s value type here", declarationName()));

            return ParseResult.failed();
        }

        final var valueParseResult = context.goNextIfCurrentIs(Assign.ASSIGN)
                ? ValueExpressionParser.create(!asFuncArgument).parse(context) : null;

        if (valueParseResult != null && !valueParseResult.isSuccessful()) {
            if (valueParseResult.isCanceled())
                context.messages.addSyntaxErr(String.format("expected a %s value here", declarationName()));

            return ParseResult.failed();
        }

        if (valueTypeParseResult == null && valueParseResult == null) {
            context.messages.addSyntaxErrAfterToken(
                    MessageFormat.format(
                            "expected specified either the {0} value type or the {0} value itself",
                            declarationName()
                    ),
                    Objects.requireNonNull(nameToken)
            );

            return ParseResult.failed();
        }

        if (valueTypeParseResult != null && valueParseResult != null)
            return isFailed ? ParseResult.failed() : ParseResult.successful(FieldDeclarationObject.create(
                    TypedToken.strictAsOrNull(mutableToken),
                    TypedToken.strictAs(nameToken),
                    valueTypeParseResult.valueOrThrow(),
                    valueParseResult.valueOrThrow()
            ));

        if (valueTypeParseResult != null)
            return isFailed ? ParseResult.failed() : ParseResult.successful(FieldDeclarationObject.create(
                    TypedToken.strictAsOrNull(mutableToken),
                    TypedToken.strictAs(nameToken),
                    valueTypeParseResult.valueOrThrow()
            ));

        return isFailed ? ParseResult.failed() : ParseResult.successful(FieldDeclarationObject.create(
                TypedToken.strictAsOrNull(mutableToken),
                TypedToken.strictAs(nameToken),
                valueParseResult.valueOrThrow()
        ));
    }

    private @NotNull String declarationName() {
        return asFuncArgument ? "function argument" : "field";
    }
}
