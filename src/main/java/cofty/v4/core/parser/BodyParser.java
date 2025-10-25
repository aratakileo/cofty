package cofty.v4.core.parser;

import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ParseContext;
import cofty.util.Cast;
import cofty.v4.core.parser.ast.BodyObject;
import cofty.v4.core.parser.ast.BodyResidentObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public final class BodyParser implements Parser<BodyObject> {
    public static final BodyParser ROOT_BODY = new BodyParser("root");

    private final static Set<? extends Parser<BodyResidentObject>> RESIDENT_PARSERS;

    private final String bodyName;

    private BodyParser(String bodyName) {
        this.bodyName = bodyName;
    }

    @Override
    public @NotNull ParseResult<BodyObject> parse(@NotNull ParseContext context) {
        final var residents = new ArrayList<BodyResidentObject>();

        var parseResult = (ParseResult<BodyResidentObject>)null;
        var lineStartsWithToken = context.current();
        var newLineProceeded = true;

        while (true) {
            parseResult = parseLine(context);

            if (!newLineProceeded && !parseResult.isCanceled()) {
                context.CRITICAL_MESSAGES.putSyntaxErr(
                        "expected a newline separator between expressions",
                        Objects.requireNonNull(lineStartsWithToken)
                );

                return ParseResult.failed();
            }

            if (!parseResult.isSuccessful()) break;

            residents.add(parseResult.valueOrThrow());

            newLineProceeded = context.goNextIfCurrentIs(Simple.NEWLINE);
            lineStartsWithToken = context.current();
        }

        if (parseResult.isFailed()) return ParseResult.failed();

        if (parseResult.isCanceled() && context.hasCurrent()) {
            context.CRITICAL_MESSAGES.putSyntaxErr("invalid syntax");
            return ParseResult.failed();
        }

        if (residents.isEmpty()) return ParseResult.canceled();

        return ParseResult.successful(new BodyObject(residents));
    }

    private @NotNull ParseResult<BodyResidentObject> parseLine(@NotNull ParseContext context) {
        for (final var parser: RESIDENT_PARSERS) {
            final var parseResult = parser.parse(context);

            if (!parseResult.isCanceled()) return parseResult;
        }

        return ParseResult.canceled();
    }

    static {
        RESIDENT_PARSERS = Cast.unsafe(Set.of(
                FieldDeclarationParser.DEFAULT,
                FieldValueAssignmentParser.DEFAULT,
                ValueExpressionParser.DEFAULT
        ));
    }
}
