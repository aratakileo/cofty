package cofty.v3.core.parser.node;

import cofty.core.parser.ParseContext;
import cofty.type.QueueIterator;
import cofty.type.exception.InvalidParserNodeStateInQueue;
import cofty.v3.core.parser.node.modifier.ModifierType;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NodeQueueIterator implements QueueIterator<ParserNode> {
    public final ParseContext context;
    public final NodeModifier modifier;
    public final ParserNode first;

    private ParserNode current, prev = null;
    private boolean snapshotMade = false, ignoreDepended = false;

    public NodeQueueIterator(
            @NotNull ParseContext context,
            @NotNull NodeModifier modifier,
            @NotNull ParserNode first
    ) {
        if (modifier.is(ModifierType.ACTION)) throw new IllegalStateException();

        this.context = context;
        this.modifier = modifier;
        this.first = first;
        this.current = first;
    }

    @Override
    public boolean hasNext() {
        return current != null && current.next() != null;
    }

    @Override
    public @Nullable ParserNode current() {
        return current;
    }

    @Override
    public @Nullable ParserNode goNext() {
        if (current == null) return null;
        prev = current;
        current = current.next();
        return current;
    }

    @Override
    public @Nullable ParserNode next() {
        return current == null ? null : current.next();
    }

    @Override
    public @Nullable ParserNode prev() {
        return prev;
    }

    public boolean canMakeContextSnapshot() {
        return (isQueueSnapshotMaker() || current.modifier().is(ModifierType.PEEK)) && !snapshotMade;
    }

    public boolean isIgnoreDepended() {
        if (!ignoreDepended) return false;

        if (currentOrThrow().modifier().is(ModifierType.DEPENDED)) {
            goNext();
            return true;
        }

        return (ignoreDepended = false);
    }

    public boolean isCurrentPeek() {
        return currentOrThrow().modifier().is(ModifierType.PEEK);
    }

    public boolean isQueueSnapshotMaker() {
        return modifier.isAny(ModifierType.PEEK, ModifierType.PREVIEW);
    }

    public void makeContextSnapshot() {
        if (!canMakeContextSnapshot()) throw new IllegalStateException();
        context.createIndexSnapshot();
        snapshotMade = true;
    }

    public void rollbackContextSnapshot() {
        if (!snapshotMade) throw new IllegalStateException();
        context.rollbackIndex();
        snapshotMade = false;
    }

    public void removeContextSnapshot() {
        if (!snapshotMade) throw new IllegalStateException();
        context.removeIndexSnapshot();
        snapshotMade = false;
    }

    private void preprocessDependedStaff() {
        if (
                currentOrThrow().modifier().is(ModifierType.DEPENDED)
                        && !currentOrThrow().modifier().isAny(ModifierType.DEPENDED, ModifierType.PEEK)
        ) throw new InvalidParserNodeStateInQueue();
    }

    public boolean proceed() {
        if (isQueueSnapshotMaker()) makeContextSnapshot();

        while (hasCurrent()) {
            if (isIgnoreDepended()) continue;
            preprocessDependedStaff();

            final var isSnapshotForIteration = isCurrentPeek() && !isQueueSnapshotMaker();

            if (isSnapshotForIteration) makeContextSnapshot();

            final var isNodeProceed = currentOrThrow().proceed(
                    context,
                    modifier.is(ModifierType.PREVIEW) && !modifier.shadowPreview
                            ? NodeModifier.Builder.of(modifier).setShadowPreview(true).build() : modifier
            );

            final var isPreviewFinished = isNodeProceed && modifier.is(ModifierType.PREVIEW)
                    && currentOrThrow().modifier().is(ModifierType.PREVIEW);

            if (isPreviewFinished) {
                if (modifier.shadowPreview && currentOrThrow().modifier().shadowPreview) removeContextSnapshot();
                else rollbackContextSnapshot();

                return true;
            }

            if (isSnapshotForIteration) {
                if (isNodeProceed) removeContextSnapshot();
                else rollbackContextSnapshot();
            }

            if (isCurrentPeek() && !isNodeProceed) ignoreDepended = true;

            if (!isNodeProceed && !isCurrentPeek()) break;

            goNext();
        }

        if (!hasCurrent()) {
            if (snapshotMade) removeContextSnapshot();
            return true;
        }

        if (isQueueSnapshotMaker()) {
            rollbackContextSnapshot();
            return false;
        }

        if (snapshotMade) throw new IllegalStateException();

        return false;
    }
}
