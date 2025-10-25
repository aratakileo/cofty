package cofty.v4.core;

import cofty.core.CompileResultLogger;
import cofty.core.lexer.Lexer;
import cofty.core.message.MessageHandler;
import cofty.core.parser.ParseContext;
import cofty.core.semantics.SemanticsContext;
import cofty.type.TextContent;
import cofty.v4.core.parser.BodyParser;
import cofty.v4.core.semantics.SemanticAnalyzer;
import org.jetbrains.annotations.NotNull;

public final class Compiler {
    public final TextContent text;
    public final MessageHandler messages = new MessageHandler();
    public final CompileResultLogger resultLogger = new CompileResultLogger(messages);

    public Compiler(@NotNull TextContent text) {
        this.text = text;
    }

    public boolean compile() {
        if (text.text.isEmpty()) {
            resultLogger.setLexerStageMessage("the input file is empty");
            return true;
        }

        final var lexer = new Lexer(text, messages);
        final var parsedTokens = lexer.parse();

        if (parsedTokens.isEmpty()) {
            resultLogger.setLexerStageMessage("there are no suitable tokens for parsing");
            resultLogger.checkInLexer(messages.isEmpty());

            return messages.isEmpty();
        }

        resultLogger.checkInLexer(true);

        final var parseResult = BodyParser.ROOT_BODY.parse(new ParseContext(parsedTokens, text, messages));

        if (parseResult.isFailed()) {
            resultLogger.checkInParser(false);
            return false;
        }

        resultLogger.checkInParser(true);

        final var semanticAnalyzer = new SemanticAnalyzer(
                new SemanticsContext(text, messages),
                parseResult.valueOrThrow()
        );

        return true;

//        if (!semanticAnalyzer.analyze()) {
//            resultLogger.checkInSemanticAnalyzer(false);
//            return false;
//        }
//
//        resultLogger.checkInSemanticAnalyzer(true);
//
//        return true;
    }
}
