package cofty;

import cofty.core.lexer.Lexer;
import cofty.core.message.MessageHandler;
import cofty.core.parser.ParseContext;
import cofty.type.Representable;
import cofty.type.TextContent;
import cofty.util.Strings;
import cofty.v3.core.parser.ast.BodyObject;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import cofty.v3.core.semantics.SemanticAnalyzer;
import cofty.v3.core.semantics.SemanticsContext;

public class Cofty {
    public static void main(String[] args) {
        final var text = TextContent.read("test_v3.cft").unwrap();

        final var messages = new MessageHandler();
        final var lexer = new Lexer(text, messages);
        final var parsedTokens = lexer.parse();
        final var parseContext = new ParseContext(parsedTokens, text, messages);

        messages.print();

        Strings.println(Representable.repr(parsedTokens));

        final var bodyObject = new BodyObject();

        var isPreviewSucceed = bodyObject.parserNode().previewQueue(parseContext, NodeModifier.generalAndPreview());
        Strings.println("Is preview succeeded:", isPreviewSucceed);
        Strings.println("Snapshots stack size:", parseContext.snapshotStackSize());
        Strings.println("Cursor after preview:", parseContext.cursor());

        if (isPreviewSucceed) {
            final var parsed = bodyObject.parse(parseContext);

            Strings.println("Is proceeded:", parsed);
            Strings.println("Ast objects after parse:", bodyObject);

            if (parsed) {
                final var analyzerContext = new SemanticsContext(text, messages);
                final var semanticAnalyzer = new SemanticAnalyzer(analyzerContext, bodyObject);

                Strings.println("Is analyzed:", semanticAnalyzer.analyzePrimary());
            }
        }

        messages.print();
    }
}
