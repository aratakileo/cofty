package cofty.v2.core.ast.object;

import cofty.v2.core.ast.AstObject;
import cofty.v2.core.ast.AstValueList;
import cofty.v2.core.parse.AstParseEntry;
import cofty.v2.core.parse.AstParser;
import cofty.core.token.TokenType;
import cofty.util.Cast;

import java.util.ArrayList;
import java.util.Optional;

public class BodyAst implements AstObject {
    private final static ArrayList<AstParseEntry<?>> PARSE_ENTRIES = new ArrayList<>();

    public final static AstParser<BodyAst> PARSER = context -> {
        final var result = new BodyAst();
        final var astBodyValues = result.body;

        var successfullyParsed = true;

        while (successfullyParsed) {
            successfullyParsed = false;

            for (final var parseEntry : PARSE_ENTRIES)
                if (parseEntry.peek(context)) {
                    final var subresult = parseEntry.parse(context);

                    successfullyParsed = subresult.isPresent();

                    if (subresult.isEmpty())
                        break;

                    astBodyValues.add(Cast.unsafe(subresult.get()));
                }
        }

        return context.isFailed_v2() ? Optional.empty() : Optional.of(result);
    };

    public AstValueList<?> body = new AstValueList<>();

    @Override
    public String toString() {
        return "BodyAst{" +
                "body=" + body +
                '}';
    }

    static {
        PARSE_ENTRIES.add(InitVarAst.PARSE_ENTRY);
        PARSE_ENTRIES.add(SetVarAst.PARSE_ENTRY);
        PARSE_ENTRIES.add(TokenType.NEWLINE.getParseEntry());
    }
}
