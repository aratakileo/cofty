package cofty.core.parser;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.operator.Separator;
import cofty.util.Lists;
import cofty.core.parser.ast.TypeDescriptionObject;
import org.jetbrains.annotations.NotNull;

public final class TypeDescriptionParser implements Parser<TypeDescriptionObject> {
    public static final TypeDescriptionParser DEFAULT = new TypeDescriptionParser();

    private TypeDescriptionParser() {}

    @Override
    public @NotNull ParseResult<TypeDescriptionObject> parse(@NotNull ParseContext context) {
        if (!context.currentIs(Simple.WORD)) return ParseResult.canceled();

        final var words = Lists.<TypedToken<Simple>>arrayListOf(context.advanceOrThrow().strictAs());

        while (context.goNextIfCurrentIs(Separator.DOT)) {
            if (!context.currentIs(Simple.WORD)) {
                context.messages.addInvalidSyntaxErr();
                context.goNext();

                return ParseResult.failed();
            }

            words.add(context.advanceOrThrow().strictAs());
        }

        return ParseResult.successful(new TypeDescriptionObject(words));
    }
}
