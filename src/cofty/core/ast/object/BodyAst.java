package cofty.core.ast.object;

import cofty.core.ast.AstObject;
import cofty.core.ast.AstValueList;
import cofty.core.parse.AstParseEntry;
import cofty.core.parse.AstParser;
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

                    if (subresult.isEmpty())
                        break;

                    successfullyParsed = true;
                    astBodyValues.add(Cast.unsafe(subresult.get()));
                }
        }

        return context.isFailed() ? Optional.empty() : Optional.of(result);
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
