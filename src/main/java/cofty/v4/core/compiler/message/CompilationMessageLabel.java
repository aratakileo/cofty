package cofty.v4.core.compiler.message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class CompilationMessageLabel {
    public static final CompilationMessageLabel INVALID_SYNTAX = new CompilationMessageLabel(
            "SyntaxError",
            "invalid syntax"
    );

    public final String prefix;
    public final String content;

    private CompilationMessageLabel(@Nullable String prefix, @NotNull String content) {
        this.prefix = prefix;
        this.content = content;
    }

    @Override
    public String toString() {
        return prefix == null ? content : String.format("%s: %s", prefix, content);
    }

    public static @NotNull CompilationMessageLabel create(@NotNull String fullLabel) {
        if (!fullLabel.contains(":")) return new CompilationMessageLabel(null, fullLabel);

        final var splitted = fullLabel.split("\\s+:\\s+");

        return getPredefined(splitted[0], splitted[1]).orElse(new CompilationMessageLabel(splitted[0], splitted[1]));
    }

    public static @NotNull CompilationMessageLabel create(@NotNull String prefix, @NotNull String content) {
        return getPredefined(prefix, content).orElse(new CompilationMessageLabel(prefix, content));
    }

    public static @NotNull CompilationMessageLabel syntaxError(@NotNull String content) {
        return getPredefined("SyntaxError", content)
                .orElse(new CompilationMessageLabel("SyntaxError", content));
    }

    private static @NotNull Optional<CompilationMessageLabel> getPredefined(
            @NotNull String prefix,
            @NotNull String content
    ) {
        if (prefix.equals("SyntaxError") && content.equals("invalid syntax"))
            return Optional.of(INVALID_SYNTAX);

        return Optional.empty();
    }
}
