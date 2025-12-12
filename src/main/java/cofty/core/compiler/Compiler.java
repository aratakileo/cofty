package cofty.core.compiler;

import cofty.core.lexer.Lexer;
import cofty.core.compiler.diagnostic.DiagnosticEngine;
import cofty.core.parser.ParseContext;
import cofty.core.semantics.ModuleContext;
import cofty.core.semantics.ModuleDeepAnalyzer;
import cofty.core.semantics.ModuleQuickAnalyzer;
import cofty.core.semantics.symbol.scope.RootScope;
import cofty.type.TextContent;
import cofty.core.parser.BodyParser;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Paths;

public final class Compiler {
    public final TextContent text;
    public final DiagnosticEngine messages = new DiagnosticEngine();
    public final CompileResultLogger resultLogger = new CompileResultLogger(messages);

    public Compiler(@NotNull TextContent text) {
        this.text = text;
    }

    public boolean compile(boolean semanticTreeOutput) {
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

        final var parseResult = BodyParser.MODULE_BODY.parse(new ParseContext(parsedTokens, text, messages));

        if (parseResult.isFailed()) {
            resultLogger.checkInParser(false);
            return false;
        }

        resultLogger.checkInParser(true);

        final var moduleContext = ModuleContext.create(text, messages, new RootScope(), parseResult.valueOrThrow());
        final var quickAnalyzer = new ModuleQuickAnalyzer(moduleContext);

        if (!quickAnalyzer.analyze()) {
            resultLogger.checkInQuickSemanticAnalyzer(false);
            return false;
        }

        resultLogger.checkInQuickSemanticAnalyzer(true);

        if (semanticTreeOutput)
            tryWriteScopeTreeSnapshot(moduleContext, true);

        final var deepAnalyzer = new ModuleDeepAnalyzer(moduleContext);

        if (!deepAnalyzer.analyze()) {
            resultLogger.checkInDeepSemanticAnalyzer(false);
            return false;
        }

        resultLogger.checkInDeepSemanticAnalyzer(true);

        if (semanticTreeOutput)
            tryWriteScopeTreeSnapshot(moduleContext, false);

        return true;
    }

    private static void tryWriteScopeTreeSnapshot(@NotNull ModuleContext context, boolean isQuick) {
        final var writePath = context.text.dirPath() + '/' + context.scope.name() + (isQuick ? ".qscope" : ".scope");

        try {
            Files.writeString(Paths.get(writePath), context.scope.represented());
        } catch (Exception e) {
            System.out.printf("Failed to write scope file \"%s\":%n", writePath);
            e.printStackTrace(System.out);
            System.out.println();
        }
    }
}
