package cofty;

import cofty.core.message.MessageHandler;
import cofty.core.parse.ParseContext;
import cofty.core.token.Keyword;
import cofty.core.token.Lexer;
import cofty.core.token.Operator;
import cofty.core.token.TokenType;
import cofty.type.Representable;
import cofty.type.TextContent;
import cofty.util.Strings;
import cofty.v3.parser.node.ParserNode;
import cofty.v3.parser.node.flag.NodeFlag;

public class Cofty {
    public static void main(String[] args) {
        // v2
//        final var text = TextContent.read("test.cft").unwrap();

        // v3
        final var text = TextContent.read("test_v3.cft").unwrap();

        final var messages = new MessageHandler();
        final var lexer = new Lexer(text, messages);
        final var parsedTokens = lexer.parse();
        final var parseContext = new ParseContext(parsedTokens, text, messages);

        messages.print();

        Strings.println(Representable.repr(parsedTokens));

        // v2
//        final var parseResult = BodyAst.PARSER.parse(parseContext);
//        System.out.println(parseResult.map(BodyAst::toString).orElse(""));

        // v3
        ParserNode parserNode;

        (parserNode = ParserNode.tokenOrSyntaxFail(Keyword.LET))
                .then(ParserNode.token(Keyword.MUT, NodeFlag.peek()))
                .then(ParserNode.token(TokenType.ID, NodeFlag.syntaxFail("expected name")))
                .then(ParserNode.token(Operator.ASSIGN, NodeFlag.syntaxFail("expected assign operator")))
                .then(ParserNode.token(TokenType.INT, NodeFlag.syntaxFail("expected value")));

        Strings.println("Is parsed:", parserNode.proceedQueue(parseContext, NodeFlag.general()));

        messages.print();
    }
}
