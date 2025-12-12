package cofty.core.compiler.diagnostic;

import org.jetbrains.annotations.NotNull;

public interface DiagnosticRepresentable {
    @NotNull String represent();
}
