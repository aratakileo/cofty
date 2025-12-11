package cofty.core.compiler;

import cofty.core.compiler.message.CompilationMessageHandler;
import org.jetbrains.annotations.NotNull;

public class CompileResultLogger {
    private final CompilationMessageHandler messages;

    private StageState lexerStageState = StageState.CANCELED,
            parserStageState = StageState.CANCELED,
            quickSemanticAnalyzerState = StageState.CANCELED,
            deepSemanticAnalyzerState = StageState.CANCELED;

    private String lexerStageMessage = null;

    public CompileResultLogger(@NotNull CompilationMessageHandler messages) {
        this.messages = messages;
    }

    public void setLexerStageMessage(@NotNull String lexerStageMessage) {
        this.lexerStageMessage = lexerStageMessage;
    }

    public void checkInLexer(boolean successfully) {
        lexerStageState = successfully ? StageState.SUCCESSFULLY : StageState.FAILED;
    }

    public void checkInParser(boolean successfully) {
        parserStageState = successfully ? StageState.SUCCESSFULLY : StageState.FAILED;
    }

    public void checkInQuickSemanticAnalyzer(boolean successfully) {
        quickSemanticAnalyzerState = successfully ? StageState.SUCCESSFULLY : StageState.FAILED;
    }

    public void checkInDeepSemanticAnalyzer(boolean successfully) {
        deepSemanticAnalyzerState = successfully ? StageState.SUCCESSFULLY : StageState.FAILED;
    }

    public void print() {
        System.out.printf(
                "Stages:%n" +
                        " - Splitting into tokens (lexer): %s%s%n" +
                        " - Parsing tokens into AST objects (parser): %s%n" +
                        " - Analyzing AST objects (semantic analyzer):%n" +
                        "   - Quick analyzing: %s%n" +
                        "   - Deep analyzing: %s%n",
                lexerStageState,
                (lexerStageMessage == null ? "" : " [" + lexerStageMessage + ']'),
                parserStageState,
                quickSemanticAnalyzerState,
                deepSemanticAnalyzerState
        );

        if (messages.isEmpty()) return;

        if (messages.hasErrs()) {
            System.out.printf("%n%s errors:%n", messages.errCount());
            messages.printErrs();
        }

        if (messages.hasWarns()) {
            System.out.printf("%n%s warnings:%n", messages.warnCount());
            messages.printWarns();
        }
    }

    public enum StageState {
        SUCCESSFULLY,
        FAILED,
        CANCELED
    }
}
