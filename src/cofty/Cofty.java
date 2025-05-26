package cofty;

import cofty.core.ast.object.BodyAst;
import cofty.core.parse.ParseContext;
import cofty.core.message.MessageHandler;
import cofty.core.token.Lexer;
import cofty.type.Representable;
import cofty.type.TextContent;

public class Cofty {
    public static void main(String[] args) {
        final var text = TextContent.read("test.cft").unwrap();
        final var messages = new MessageHandler();
        final var lexer = new Lexer(text, messages);
        final var parsedTokens = lexer.parse();
        final var parseContext = new ParseContext(parsedTokens, text, messages);

        messages.print();

        System.out.println(Representable.repr(parsedTokens));
        final var parseResult = BodyAst.PARSER.parse(parseContext);
        System.out.println(parseResult.map(BodyAst::toString).orElse(""));

        messages.print();
    }
}
