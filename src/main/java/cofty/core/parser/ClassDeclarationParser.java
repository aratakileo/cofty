package cofty.core.parser;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.lexer.token.type.Keyword;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.ClassDeclarationObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ClassDeclarationParser implements Parser<ClassDeclarationObject> {
    public final BodyParser.BodyType parentBody;

    private ClassDeclarationParser(@NotNull BodyParser.BodyType parentBody) {
        this.parentBody = parentBody;
    }

    @Override
    public @NotNull ParseResult<ClassDeclarationObject> parse(@NotNull ParseContext context) {
        if (!context.goNextIfCurrentIs(Keyword.CLASS)) return ParseResult.skipped();

        final var nameToken = context.current(Simple.WORD);

        if (!context.goNextIfCurrentIs(Simple.WORD)) {
            context.messages.report(Errors.EXPECTED_NAME, "class");
            context.goNext();

            return ParseResult.failed();
        }

        final var bodyParseResult = BodyParser.createClassBodyParser(parentBody).parse(context);

        if (!bodyParseResult.isOK()) return ParseResult.failed();
        final var body = bodyParseResult.valueOrThrow();

        return ParseResult.OK(new ClassDeclarationObject(
                Objects.requireNonNull(nameToken).strictAs(),
                body
        ));
    }

    public static @NotNull ClassDeclarationParser create(@NotNull BodyParser.BodyType parentBody) {
        return new ClassDeclarationParser(parentBody);
    }
}
