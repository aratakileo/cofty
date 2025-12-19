package cofty.core.compiler;

import cofty.core.compiler.diagnostic.DiagnosticEngine;
import org.jetbrains.annotations.NotNull;

public final class CompileResultLogger {
    private final DiagnosticEngine messages;

    private StageState lexerStageState = StageState.SKIPPED,
            parserStageState = StageState.SKIPPED,
            quickSemanticAnalyzerState = StageState.SKIPPED,
            deepSemanticAnalyzerState = StageState.SKIPPED,
            transpilationState = StageState.SKIPPED;

    private String lexerStageMessage = null;

    public CompileResultLogger(@NotNull DiagnosticEngine messages) {
        this.messages = messages;
    }

    public void setLexerStageMessage(@NotNull String lexerStageMessage) {
        this.lexerStageMessage = lexerStageMessage;
    }

    public void checkInLexer(boolean successfully) {
        lexerStageState = successfully ? StageState.OK : StageState.FAILED;
    }

    public void checkInParser(boolean successfully) {
        parserStageState = successfully ? StageState.OK : StageState.FAILED;
    }

    public void checkInQuickSemanticAnalyzer(boolean successfully) {
        quickSemanticAnalyzerState = successfully ? StageState.OK : StageState.FAILED;
    }

    public void checkInDeepSemanticAnalyzer(boolean successfully) {
        deepSemanticAnalyzerState = successfully ? StageState.OK : StageState.FAILED;
    }

    public void checkInTranspilation(boolean successfully) {
        transpilationState = successfully ? StageState.OK : StageState.FAILED;
    }

    public void print() {
        System.out.printf(
                "Stages:%n" +
                        " [1] Lexing: %s%s%n" +
                        " [2] Parsing: %s%n" +
                        " [3] Semantic analysis:%n" +
                        "     - Primary symbol table generation: %s%n" +
                        "     - Full pass: %s%n" +
                        " [4] Transpilation: %s%n",
                lexerStageState,
                (lexerStageMessage == null ? "" : " [" + lexerStageMessage + ']'),
                parserStageState,
                quickSemanticAnalyzerState,
                deepSemanticAnalyzerState,
                transpilationState
        );

        if (messages.isEmpty()) return;

        if (messages.hasErrors()) {
            System.out.printf("%n%s errors:%n", messages.errorsCount());
            messages.printErrors();
        }

        if (messages.hasWarnings()) {
            System.out.printf("%n%s warnings:%n", messages.warningsCount());
            messages.printWarnings();
        }
    }

    public enum StageState {
        OK,
        FAILED,
        SKIPPED
    }
}
