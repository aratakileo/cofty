package cofty.core.compiler.diagnostic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DiagnosticDescriptor {
    public final String prefix;
    public final String content;

    private DiagnosticDescriptor(@Nullable String prefix, @NotNull String content) {
        this.prefix = prefix;
        this.content = content;
    }

    public @NotNull String with(@NotNull DiagnosticCode code) {
        final var formattedCode = "[%s]".formatted(code.code());

        return "%s: %s".formatted(
                prefix == null ? formattedCode : "%s %s".formatted(formattedCode, prefix),
                content
        );
    }

    @Override
    public @NotNull String toString() {
        return prefix == null ? content : "%s: %s".formatted(prefix, content);
    }

    public static @NotNull DiagnosticDescriptor create(@NotNull String fullDecription) {
        if (!fullDecription.contains(":")) return new DiagnosticDescriptor(null, fullDecription);

        final var splitted = fullDecription.split("\\s*:\\s*");

        return new DiagnosticDescriptor(splitted[0], splitted[1]);
    }

    public static @NotNull DiagnosticDescriptor create(@Nullable String prefix, @NotNull String content) {
        return new DiagnosticDescriptor(prefix, content);
    }
}
