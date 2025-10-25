package cofty.v4.core.parser;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.operator.Assign;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.core.parser.ParseContext;
import cofty.v4.core.parser.ast.FieldDeclarationObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class FieldDeclarationParser implements Parser<FieldDeclarationObject> {
    public static final FieldDeclarationParser DEFAULT = new FieldDeclarationParser();

    private FieldDeclarationParser() {}

    @Override
    public @NotNull ParseResult<FieldDeclarationObject> parse(@NotNull ParseContext context) {
        if (!context.goNextIfCurrentIs(Keyword.VAR)) return ParseResult.canceled();

        final var mutableToken = context.currentIs(Keyword.MUT) ? context.advanceOrThrow() : null;
        final var nameToken = context.current(Simple.WORD);

        if (!context.goNextIfCurrentIs(Simple.WORD)) {
            context.CRITICAL_MESSAGES.putSyntaxErr("expected a field name");
            return ParseResult.failed();
        }

        final var valueTypeParseResult = context.goNextIfCurrentIs(Separator.COLON)
                ? TypeDescriptionParser.DEFAULT.parse(context) : null;

        if (valueTypeParseResult != null && valueTypeParseResult.isCanceled()) {
            context.CRITICAL_MESSAGES.putSyntaxErr("expected a field value type");
            return ParseResult.failed();
        }

        final var valueParseResult = context.goNextIfCurrentIs(Assign.ASSIGN)
                ? ValueExpressionParser.DEFAULT.parse(context) : null;

        if (valueParseResult != null && valueParseResult.isCanceled()) {
            context.CRITICAL_MESSAGES.putSyntaxErr("expected a field value");
            return ParseResult.failed();
        }

        if (valueTypeParseResult == null && valueParseResult == null) {
            context.CRITICAL_MESSAGES.putSyntaxErrAfterToken(
                    "expected specified either the field value type or the field value itself",
                    Objects.requireNonNull(nameToken)
            );

            return ParseResult.failed();
        }

        if (valueTypeParseResult != null && valueParseResult != null)
            return ParseResult.successful(FieldDeclarationObject.create(
                    TypedToken.strictAsOrNull(mutableToken),
                    TypedToken.strictAs(nameToken),
                    valueTypeParseResult.valueOrThrow(),
                    valueParseResult.valueOrThrow()
            ));

        if (valueTypeParseResult != null)
            return ParseResult.successful(FieldDeclarationObject.create(
                    TypedToken.strictAsOrNull(mutableToken),
                    TypedToken.strictAs(nameToken),
                    valueTypeParseResult.valueOrThrow()
            ));

        return ParseResult.successful(FieldDeclarationObject.create(
                TypedToken.strictAsOrNull(mutableToken),
                TypedToken.strictAs(nameToken),
                valueParseResult.valueOrThrow()
        ));
    }
}
