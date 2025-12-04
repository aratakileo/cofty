package cofty.v4.core.parser;

import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.v4.core.parser.ast.ClassDeclarationObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ClassDeclarationParser implements Parser<ClassDeclarationObject> {
    public final BodyParser.BodyType parentBody;

    private ClassDeclarationParser(BodyParser.BodyType parentBody) {
        this.parentBody = parentBody;
    }

    @Override
    public @NotNull ParseResult<ClassDeclarationObject> parse(@NotNull ParseContext context) {
        if (!context.goNextIfCurrentIs(Keyword.CLASS)) return ParseResult.canceled();

        final var nameToken = context.current(Simple.WORD);

        if (!context.goNextIfCurrentIs(Simple.WORD)) {
            context.messages.addSyntaxErr("expected a class name here");
            context.goNext();

            return ParseResult.failed();
        }

        final var bodyParseResult = BodyParser.createClassBodyParser(parentBody).parse(context);

        if (!bodyParseResult.isSuccessful()) return ParseResult.failed();
        final var body = bodyParseResult.valueOrThrow();

        return ParseResult.successful(new ClassDeclarationObject(
                Objects.requireNonNull(nameToken).strictAs(),
                body
        ));
    }

    public static @NotNull ClassDeclarationParser create(@NotNull BodyParser.BodyType parentBody) {
        return new ClassDeclarationParser(parentBody);
    }
}
