package cofty;

import cofty.core.lexer.token.TokenType;
import cofty.core.message.MessageHandler;
import cofty.core.parser.ParseContext;
import cofty.core.lexer.Lexer;
import cofty.type.Representable;
import cofty.type.TextContent;
import cofty.util.Strings;
import cofty.v3.core.parser.ast.InitVarObject;
import cofty.v3.core.parser.node.ParserNode;
import cofty.v3.core.parser.node.modifier.NodeModifier;

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
        var astObject = new InitVarObject();
//        var isPreviewSucceed = astObject.preview(parseContext);
//
//        Strings.println("Is preview succeeded:", isPreviewSucceed);
//        Strings.println("Ast object after preview:", astObject);
//
//        if (isPreviewSucceed) {
//            Strings.println("Is parsed:", astObject.parse(parseContext));
//            Strings.println("Ast object after parse:", astObject);
//        }

        var rootNode = ParserNode.anyOfBuilder(NodeModifier.general())
                .add(astObject.parserNode())
                .setSeparator(ParserNode.token(TokenType.NEWLINE, NodeModifier.previewAnchorAndGeneral()))
                .build();

        var isPreviewSucceed = rootNode.proceed(parseContext, NodeModifier.previewAnchorAndGeneral());
        Strings.println("Is preview succeeded:", isPreviewSucceed);
        Strings.println("Snapshots stack size:", parseContext.snapshotStackSize());
        Strings.println("Cursor after preview:", parseContext.cursor());

        if (isPreviewSucceed) {
            Strings.println("Is proceeded:", rootNode.proceed(parseContext, NodeModifier.general()));
            Strings.println("Ast object after parse:", astObject);
        }

        messages.print();
    }
}
