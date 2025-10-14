package cofty.core;

import cofty.core.message.MessageHandler;
import org.jetbrains.annotations.NotNull;

public class CompileResultLogger {
    private final MessageHandler messages;

    private StageState lexerStageState = StageState.CANCELED,
            parserStageState = StageState.CANCELED,
            semanticAnalyzerState = StageState.CANCELED;

    private String lexerStageMessage = null;

    public CompileResultLogger(@NotNull MessageHandler messages) {
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

    public void checkInSemanticAnalyzer(boolean successfully) {
        semanticAnalyzerState = successfully ? StageState.SUCCESSFULLY : StageState.FAILED;
    }

    public void print() {
        System.out.printf(
                "Stages:%n" +
                        " - Splitting into tokens (lexer): %s%s%n" +
                        " - Parsing tokens into AST objects (parser): %s%n" +
                        " - Analyzing AST objects (semantic analyzer): %s%n",
                lexerStageState,
                (lexerStageMessage == null ? "" : " [" + lexerStageMessage + ']'),
                parserStageState,
                semanticAnalyzerState
        );

        if (messages.isEmpty()) return;

        System.out.println("\nError messages:");

        messages.print();
    }

    public enum StageState {
        SUCCESSFULLY,
        FAILED,
        CANCELED
    }
}
