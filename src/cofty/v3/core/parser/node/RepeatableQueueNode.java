package cofty.v3.core.parser.node;

import cofty.core.parser.ParseContext;
import cofty.type.Representable;
import cofty.type.exception.SyntaxError;
import cofty.v3.core.parser.ast.AstObject;
import cofty.v3.core.parser.node.modifier.ModifierType;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class RepeatableQueueNode extends EmptyNode {
    private final List<@NotNull AstObjectInitializer> astObjectInitializers;
    private final ParserNode separator, stopper;

    public RepeatableQueueNode(
            @NotNull List<@NotNull AstObjectInitializer> astObjectInitializers,
            @NotNull NodeModifier modifier,
            @Nullable ParserNode separator,
            @Nullable ParserNode stopper
    ) {
        super(modifier);
        this.astObjectInitializers = astObjectInitializers;
        this.separator = separator;
        this.stopper = stopper;
    }

    @Override
    public boolean proceed(@NotNull ParseContext context, @NotNull NodeModifier topLevelModifier) {
        final var astObjects = new ArrayList<AstObject>();
        final var astObjectsCache = new HashMap<Integer, AstObject>();

        var result = true;
        var separatorWasNotProceed = false;

        if (topLevelModifier.is(ModifierType.PREVIEW))
            context.createIndexSnapshot();

        root: while (context.hasCurrent()) {
            if (stopper != null && stopper.previewQueue(context)) break;

            var nodeIsTriedToProceed = false;

            for (var i = 0; i < astObjectInitializers.size(); i++) {
                var astObject = (AstObject)null;

                if (astObjectsCache.containsKey(i)) {
                    astObject = astObjectsCache.get(i);
                    astObjectsCache.remove(i);
                } else astObject = astObjectInitializers.get(i).init();

                final var node = astObject.parserNode();

                if (node.previewQueue(context)) {
                    if (topLevelModifier.is(ModifierType.PREVIEW)) break root;

                    if (separatorWasNotProceed) {
                        context.CRITICAL_MESSAGES.putErr(new SyntaxError("invalid syntax"));
                        result = false;
                        break root;
                    }

                    nodeIsTriedToProceed = true;

                    if (!node.proceedQueue(context, NodeModifier.prioritize(topLevelModifier, modifier)))
                        result = false;

                    astObjects.add(astObject);

                    break;
                } else astObjectsCache.put(i, astObject);
            }

            if (!nodeIsTriedToProceed) {
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

        if (result && modifier.is(ModifierType.ACTION) && !topLevelModifier.is(ModifierType.PREVIEW))
            modifier.actionOrThrow().apply(astObjects.stream().toList());

        return result;
    }

    @Override
    public @NotNull String toReprString() {
        return String.format(
                "new %s(%s, %s, %s)",
                getClass().getSimpleName(),
                Representable.repr(astObjectInitializers),
                Representable.repr(modifier),
                Representable.repr(separator)
        );
    }

    public static class Builder {
        private final List<@NotNull AstObjectInitializer> astObjectInitializers = new ArrayList<>();
        private ParserNode separator = null, stopper = null;

        private final NodeModifier modifier;
        private final Consumer<RepeatableQueueNode> onBuild;

        public Builder(@NotNull NodeModifier modifier, @Nullable Consumer<RepeatableQueueNode> onBuild) {
            this.modifier = modifier;
            this.onBuild = onBuild;
        }

        public @NotNull Builder add(@NotNull AstObjectInitializer astObjectInitializer) {
            astObjectInitializers.add(astObjectInitializer);
            return this;
        }

        public @NotNull Builder add(@NotNull AstObjectInitializer @NotNull... astObjectInitializers) {
            this.astObjectInitializers.addAll(List.of(astObjectInitializers));
            return this;
        }

        public @NotNull Builder setSeparator(@NotNull ParserNode separator) {
            this.separator = separator;
            return this;
        }

        public @NotNull Builder setStopper(@NotNull ParserNode stopper) {
            this.stopper = stopper;
            return this;
        }

        public @NotNull RepeatableQueueNode build() {
            final var result = new RepeatableQueueNode(astObjectInitializers, modifier, separator, stopper);

            if (onBuild != null)
                onBuild.accept(result);

            return result;
        }
    }

    @FunctionalInterface
    public interface AstObjectInitializer {
        @NotNull AstObject init();
    }
}
