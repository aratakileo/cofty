package cofty.core.compiler.diagnostic;

import org.jetbrains.annotations.NotNull;

public enum Warnings implements DiagnosticCode {
    LONG_POSTFIX_CHAIN(
            CodePrefix.PARSING_WARN,
            1,
            "more than three consecutive postfix calls may reduce code readability"
    ),
    POSTFIX_FUNC_CALL_WITH_NO_ARGS(
            CodePrefix.PARSING_WARN,
            2,
            "a postfix function call without arguments, but with round brackets"
    );

    private final CodePrefix prefix;
    private final int codeNum;
    private final String placeholder;

    Warnings(@NotNull CodePrefix prefix, int codeNum, @NotNull String placeholder) {
        this.prefix = prefix;
        this.codeNum = codeNum;
        this.placeholder = placeholder;

        if (prefix.severity != DiagnosticMsg.Severity.WARN)
            throw new IllegalStateException();
    }

    @Override
    public @NotNull CodePrefix prefix() {
        return prefix;
    }

    @Override
    public int num() {
        return codeNum;
    }

    @Override
    public @NotNull String placeholder() {
        return placeholder;
    }
}
