package cofty.v3.core.parser.node;

import cofty.core.parser.ParseContext;
import cofty.type.Representable;
import cofty.type.exception.SyntaxError;
import cofty.v3.core.parser.node.modifier.ModifierType;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * TODO: THIS ONLY AN ALPHA VERSION
 */
public class AnyOfNodeQueue extends EmptyNode {
    private final List<@NotNull ParserNode> nodes;
    private final ParserNode separator;

    public AnyOfNodeQueue(
            @NotNull List<@NotNull ParserNode> nodes,
            @NotNull NodeModifier modifier,
            @Nullable ParserNode separator
    ) {
        super(modifier);
        this.nodes = nodes;
        this.separator = separator;
    }

    @Override
    public boolean proceed(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier) {
        var result = true;
        var separatorWasNotProceed = false;

        if (topLevelModifier.is(ModifierType.PREVIEW))
            context.createIndexSnapshot();

        root: while (context.hasCurrent()) {
            var nodeIsTriedToProceed = false;

            for (final var node : nodes)
                if (node.previewQueue(context)) {
                    if (topLevelModifier.is(ModifierType.PREVIEW)) break root;

                    if (separatorWasNotProceed) {
                        context.CRITICAL_MESSAGES.putErr(new SyntaxError("invalid syntax"));
                        result = false;
                        break root;
                    }

                    nodeIsTriedToProceed = true;

                    node.proceedQueue(context, NodeModifier.prioritize(topLevelModifier, modifier));
                    break;
                }

            if (!nodeIsTriedToProceed) {
                if (!topLevelModifier.isAny(ModifierType.PREVIEW, ModifierType.PEEK))
                    context.CRITICAL_MESSAGES.putErr(new SyntaxError("invalid syntax"));

                result = false;
                break;
            }

            if (!context.hasCurrent()) break;

            var isFirstSeparatorScan = true;

            while (separator != null && context.hasCurrent()) {
                if (!separator.previewQueue(context)) {
                    if (isFirstSeparatorScan)
                        separatorWasNotProceed = true;

                    break;
                }

                isFirstSeparatorScan = false;

                if (!separator.proceed(context, NodeModifier.prioritize(topLevelModifier, modifier))) {
                    result = false;
                    break root;
                }
            }
        }

        if (topLevelModifier.is(ModifierType.PREVIEW))
            context.rollbackIndex();

        return result;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format(
                "new %s(%s, %s, %s)",
                getClass().getSimpleName(),
                Representable.repr(nodes),
                Representable.repr(modifier),
                Representable.repr(separator)
        );
    }

    public static class Builder {
        private final ArrayList<@NotNull ParserNode> nodes = new ArrayList<>();
        private ParserNode separator = null;

        private final NodeModifier modifier;
        private final Consumer<AnyOfNodeQueue> onBuild;

        public Builder(@NotNull NodeModifier modifier, @Nullable Consumer<AnyOfNodeQueue> onBuild) {
            this.modifier = modifier;
            this.onBuild = onBuild;
        }

        public @NotNull Builder add(@NotNull ParserNode node) {
            nodes.add(node);
            return this;
        }

        public @NotNull Builder add(@NotNull ParserNode @NotNull... nodes) {
            this.nodes.addAll(List.of(nodes));
            return this;
        }

        public @NotNull Builder setSeparator(@NotNull ParserNode separator) {
            this.separator = separator;
            return this;
        }

        public @NotNull AnyOfNodeQueue build() {
            final var result = new AnyOfNodeQueue(nodes, modifier, separator);

            if (onBuild != null)
                onBuild.accept(result);

            return result;
        }
    }
}
