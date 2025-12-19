package cofty.core.compiler;

import cofty.core.lexer.Lexer;
import cofty.core.compiler.diagnostic.DiagnosticEngine;
import cofty.core.parser.ParseContext;
import cofty.core.semantics.ModuleContext;
import cofty.core.semantics.ModuleDeepScanner;
import cofty.core.semantics.ModuleQuickScanner;
import cofty.core.semantics.symbol.scope.RootScope;
import cofty.core.transpiler.ModuleTranspiler;
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

        final var moduleContext = ModuleContext.create(text, messages, RootScope.create(), parseResult.valueOrThrow());
        final var quickAnalyzer = new ModuleQuickScanner(moduleContext);

        if (!quickAnalyzer.scan()) {
            resultLogger.checkInQuickSemanticAnalyzer(false);
            return false;
        }

        resultLogger.checkInQuickSemanticAnalyzer(true);

        if (semanticTreeOutput)
            tryWriteScopeTreeSnapshot(moduleContext, true);

        final var deepAnalyzer = new ModuleDeepScanner(moduleContext);

        if (!deepAnalyzer.scan()) {
            resultLogger.checkInDeepSemanticAnalyzer(false);
            return false;
        }

        resultLogger.checkInDeepSemanticAnalyzer(true);

        if (semanticTreeOutput)
            tryWriteScopeTreeSnapshot(moduleContext, false);

        final var transpilator = new ModuleTranspiler(moduleContext, 4);

        if (!tryWriteTranspiledJavaFile(transpilator)) {
            resultLogger.checkInTranspilation(false);
            return false;
        }

        resultLogger.checkInTranspilation(true);

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

    private static boolean tryWriteTranspiledJavaFile(@NotNull ModuleTranspiler transpiler) {
        final var writePath = transpiler.context.text.dirPath() + '/' + transpiler.context.scope.name() + ".java";

        try {
            Files.writeString(Paths.get(writePath), transpiler.compile());
            return true;
        } catch (Exception e) {
            System.out.printf("Failed to write java file \"%s\":%n", writePath);
            e.printStackTrace(System.out);
            System.out.println();
        }

        return false;
    }
}
