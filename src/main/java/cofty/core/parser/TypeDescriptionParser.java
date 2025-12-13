package cofty.core.parser;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.util.Lists;
import cofty.core.parser.ast.TypeDescriptorObject;
import org.jetbrains.annotations.NotNull;

public final class TypeDescriptionParser implements Parser<TypeDescriptorObject> {
    public static final TypeDescriptionParser DEFAULT = new TypeDescriptionParser();

    private TypeDescriptionParser() {}

    @Override
    public @NotNull ParseResult<TypeDescriptorObject> parse(@NotNull ParseContext context) {
        if (!context.currentIs(Simple.WORD)) return ParseResult.skipped();

        final var words = Lists.<TypedToken<Simple>>arrayListOf(context.advanceOrThrow().strictAs());

        while (context.goNextIfCurrentIs(Separator.DOT)) {
            if (!context.currentIs(Simple.WORD)) {
                context.messages.report(Errors.PARSER_INVALID_SYNTAX);
                context.goNext();

                return ParseResult.failed();
            }

            words.add(context.advanceOrThrow().strictAs());
        }

        return ParseResult.OK(new TypeDescriptorObject(words));
    }
}
