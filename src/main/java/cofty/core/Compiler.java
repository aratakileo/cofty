package cofty.core;

import cofty.core.lexer.Lexer;
import cofty.core.message.MessageHandler;
import cofty.core.parser.ParseContext;
import cofty.core.parser.Parser;
import cofty.core.semantics.SemanticAnalyzer;
import cofty.core.semantics.SemanticsContext;
import cofty.type.TextContent;
import org.jetbrains.annotations.NotNull;

public class Compiler {
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

        final var parser = new Parser(new ParseContext(parsedTokens, text, messages));

        if (!parser.parse()) {
            resultLogger.checkInParser(false);
            return false;
        }

        resultLogger.checkInParser(true);

        final var semanticAnalyzer = new SemanticAnalyzer(new SemanticsContext(text, messages), parser.bodyObject);

        if (!semanticAnalyzer.analyze()) {
            resultLogger.checkInSemanticAnalyzer(false);
            return false;
        }

        resultLogger.checkInSemanticAnalyzer(true);

        return true;
    }
}
