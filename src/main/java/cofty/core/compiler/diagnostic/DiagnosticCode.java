package cofty.core.compiler.diagnostic;

import cofty.util.Integers;
import cofty.util.Strings;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;

public sealed interface DiagnosticCode permits Errors, Warnings {
    @NotNull CodePrefix prefix();

    int num();

    default int acceptableArguments() {
        return new MessageFormat(placeholder()).getFormatsByArgumentIndex().length;
    }

    default @NotNull String code() {
        final var prefixParts = prefix().name().split("_+");
        final var codeDigits = Integers.digitsCount(num());

        return String.valueOf(prefixParts[1].charAt(0))
                + prefixParts[0].charAt(0)
                + "0".repeat(3 - codeDigits)
                + num();
    }

    @NotNull String placeholder();

    default @NotNull DiagnosticDescriptor formatted(@NotNull Object @NotNull... args) {
        final var stringifiedArgs = new ArrayList<>();

        for (final var arg: args)
            stringifiedArgs.add(arg instanceof DiagnosticRepresentable representable ? representable.represent() : arg);

        final var descriptorPrefix = this instanceof Errors errors
                ? Strings.capitalize(Strings.snakeToCamel(errors.type().name().toLowerCase())) : null;

        if (stringifiedArgs.size() != acceptableArguments())
            throw new IllegalStateException();

        return DiagnosticDescriptor.create(
                descriptorPrefix,
                MessageFormat.format(placeholder(), stringifiedArgs.toArray())
        );
    }

    enum CodePrefix {
        PARSING_ERR,
        PARSING_WARN,
        SEMANTIC_ERR,
        SEMANTIC_WARN,
        LEXER_ERR;

        public final DiagnosticMsg.Severity severity;

        CodePrefix() {
            this.severity = name().endsWith("ERR") ? DiagnosticMsg.Severity.ERR : DiagnosticMsg.Severity.WARN;
        }
    }
}
