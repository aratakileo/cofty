package cofty.core.parser;

import cofty.core.compiler.diagnostic.DiagnosticRepresentable;
import cofty.core.compiler.diagnostic.Errors;
import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.operator.Assign;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.core.parser.ast.FieldDeclarationObject;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import cofty.util.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class FieldDeclarationParser implements Parser<FieldDeclarationObject> {
    public static final FieldDeclarationParser VARIABLE = new FieldDeclarationParser(FieldType.VARIABLE),
            FUNC_ARG = new FieldDeclarationParser(FieldType.FUNC_ARG),
            CLASS_FIELD = new FieldDeclarationParser(FieldType.CLASS_FIELD);

    private final FieldType fieldType;

    private FieldDeclarationParser(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public @NotNull ParseResult<FieldDeclarationObject> parse(@NotNull ParseContext context) {
        boolean isFailed = false;

        final var varToken = context.current(Keyword.VAR);

        if (fieldType == FieldType.FUNC_ARG && context.currentIs(Keyword.VAR)) {
            context.messages.report(Errors.NOT_ALLOWED);
            context.goNext();

            isFailed = true;
        } else if (fieldType != FieldType.FUNC_ARG && !context.goNextIfCurrentIs(Keyword.VAR))
            return ParseResult.skipped();

        final var mutableToken = context.currentIs(Keyword.MUT) ? context.advanceOrThrow() : null;
        final var nameToken = context.current(Simple.WORD);

        if (!context.goNextIfCurrentIs(Simple.WORD)) {
            if (fieldType == FieldType.FUNC_ARG && mutableToken == null && !isFailed)
                return ParseResult.skipped();

            context.messages.report(Errors.EXPECTED_NAME, fieldType);
            context.goNext();

            return ParseResult.failed();
        }

        final var valueTypeParseResult = context.goNextIfCurrentIs(Separator.COLON)
                ? TypeDescriptionParser.DEFAULT.parse(context) : null;

        if (valueTypeParseResult != null && !valueTypeParseResult.isOK()) {
            if (valueTypeParseResult.isSkipped())
                context.messages.report(Errors.EXPECTED_FIELD_TYPE, fieldType);

            return ParseResult.failed();
        }

        final var valueParseResult = context.goNextIfCurrentIs(Assign.ASSIGN)
                ? ValueExpressionParser.create(fieldType != FieldType.FUNC_ARG).parse(context) : null;

        if (valueParseResult != null && !valueParseResult.isOK()) {
            if (valueParseResult.isSkipped())
                context.messages.report(Errors.EXPECTED_ASSIGNABLE_VALUE);

            return ParseResult.failed();
        }

        if (valueTypeParseResult == null && valueParseResult == null) {
            context.messages.report(Objects.requireNonNull(nameToken), Errors.EXPECTED_ASSIGNMENT_TARGET, fieldType);

            return ParseResult.failed();
        }

        final var explicitTypeSpecificationRequired = fieldType != FieldType.VARIABLE
                && valueTypeParseResult == null
                && !(valueParseResult.valueOrThrow() instanceof SimpleValueObject);

        if (explicitTypeSpecificationRequired) {
            context.messages.report(Objects.requireNonNull(nameToken), Errors.MISSING_EXPLICIT_TYPE, fieldType);
            return ParseResult.failed();
        }

        if (isFailed) return ParseResult.failed();

        if (fieldType == FieldType.FUNC_ARG)
            return ParseResult.OK(FieldDeclarationObject.createArgument(
                    Types.valueAndMapOrNull(mutableToken, TypedToken::strictAs),
                    Types.valueAndMapOrThrow(nameToken, TypedToken::strictAs),
                    Types.valueAndMapOrNull(valueTypeParseResult, ParseResult::value),
                    Types.valueAndMapOrNull(valueParseResult, ParseResult::value)
            ));

        return ParseResult.OK(FieldDeclarationObject.createField(
                Objects.requireNonNull(varToken),
                Types.valueAndMapOrNull(mutableToken, TypedToken::strictAs),
                Types.valueAndMapOrThrow(nameToken, TypedToken::strictAs),
                Types.valueAndMapOrNull(valueTypeParseResult, ParseResult::value),
                Types.valueAndMapOrNull(valueParseResult, ParseResult::value)
        ));
    }

    private enum FieldType implements DiagnosticRepresentable {
        VARIABLE,
        CLASS_FIELD,
        FUNC_ARG;

        @Override
        public @NotNull String represent() {
            return switch (this) {
                case FUNC_ARG -> "function argument";
                case VARIABLE -> "variable";
                case CLASS_FIELD -> "class field";
            };
        }
    }
}
