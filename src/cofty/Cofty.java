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
import cofty.v3.parser.node.flag.NodeModifier;

import java.util.concurrent.atomic.AtomicBoolean;

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
        AtomicBoolean isMut = new AtomicBoolean(false);
        ParserNode parserNode;

        (parserNode = ParserNode.tokenOrSyntaxFail(Keyword.LET))
                .thenToken(Keyword.MUT, NodeModifier.peekSucceed(_ -> isMut.set(true)))
                .thenToken(TokenType.ID, NodeModifier.syntaxFail("expected name"))
                .thenToken(Operator.ASSIGN, NodeModifier.syntaxFail("expected assign operator"))
                .thenToken(TokenType.INT, NodeModifier.syntaxFail("expected value"));

        Strings.println("Is parsed:", parserNode.proceedQueue(parseContext, NodeModifier.general()), "is mut:", isMut);

        messages.print();
    }
}
