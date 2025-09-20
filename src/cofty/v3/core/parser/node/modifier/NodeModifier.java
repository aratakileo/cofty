package cofty.v3.core.parser.node.modifier;

import cofty.type.Representable;
import cofty.type.exception.SyntaxError;
import cofty.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class NodeModifier {
    private final HashSet<@NotNull ModifierType> types;
    
    public final Exception failMessage;
    public final ModifierAction action;

    private NodeModifier(
            @NotNull HashSet<@NotNull ModifierType> types, 
            @Nullable Exception failMessage, 
            @Nullable ModifierAction action
    ) {
        this.types = new HashSet<>(types);
        this.failMessage = failMessage;
        this.action = action;
    }
    
    public boolean is(@NotNull ModifierType type) {
        return types.contains(type);
    }
    
    public boolean is(@NotNull ModifierType @NotNull... types) {
        return this.types.containsAll(List.of(types));
    }
    
    public boolean isAny(@NotNull ModifierType type) {
        return is(type);
    }
    
    public boolean isAny(@NotNull ModifierType type1, @NotNull ModifierType type2) {
        return is(type1) || is(type2);
    }

    public boolean isAny(@NotNull ModifierType @NotNull... types) {
        return Lists.containsAny(this.types, types);
    }

    public @NotNull Exception failMessageOrThrow() {
        return Objects.requireNonNull(failMessage);
    }

    public @NotNull ModifierAction actionOrThrow() {
        return Objects.requireNonNull(action);
    }

    public static @NotNull NodeModifier fail(@NotNull Exception message) {
        return new NodeModifier(Lists.hashSetOf(ModifierType.FAIL), message, null);
    }

    public static @NotNull NodeModifier syntaxFail(@NotNull String message) {
        return fail(new SyntaxError(message));
    }

    public static @NotNull NodeModifier previewAnchorAndGeneral() {
        return new NodeModifier(
                Lists.hashSetOf(ModifierType.GENERAL, ModifierType.PREVIEW),
                null,
                null
        );
    }

    public static @NotNull NodeModifier general() {
        return new NodeModifier(Lists.hashSetOf(ModifierType.GENERAL), null, null);
    }

    public static @NotNull NodeModifier peek() {
        return new NodeModifier(Lists.hashSetOf(ModifierType.PEEK), null, null);
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static @NotNull NodeModifier prioritize(
            @NotNull NodeModifier topLevelModifier,
            @NotNull NodeModifier currentLevelModifier
    ) {
        if (!topLevelModifier.isAny(ModifierType.GENERAL, ModifierType.PREVIEW))
            return topLevelModifier;

        return currentLevelModifier;
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s, %s, %s)",
                getClass().getSimpleName(),
                Representable.repr(types),
                Representable.repr(failMessage),
                Representable.repr(action)
        );
    }

    public static class Builder {
        private final HashSet<@NotNull ModifierType> types = new HashSet<>();
        private Exception failMessage = null;
        private ModifierAction action = null;

        public Builder() {}
        
        public @NotNull Builder general() {
            checkBasicTypes();
            
            types.add(ModifierType.GENERAL);
            
            return this;
        }
        
        public @NotNull Builder peek() {
            checkBasicTypes();
            
            types.add(ModifierType.PEEK);
            
            return this;
        }
        
        public @NotNull Builder fail(@NotNull Exception message) {
            checkBasicTypes();
            
            types.add(ModifierType.FAIL);
            failMessage = message;
            
            return this;
        }

        public @NotNull Builder syntaxFail(@NotNull String message) {
            return fail(new SyntaxError(message));
        }

        public @NotNull Builder preview() {
            if (types.contains(ModifierType.PREVIEW))
                throw new IllegalStateException();

            types.add(ModifierType.PREVIEW);

            return this;
        }

        public @NotNull Builder depended() {
            if (types.contains(ModifierType.DEPENDED))
                throw new IllegalStateException();

            types.add(ModifierType.DEPENDED);

            return this;
        }

        public @NotNull Builder action(@NotNull ModifierAction action) {
            if (this.action != null)
                throw new IllegalStateException();

            types.add(ModifierType.ACTION);
            this.action = action;

            return this;
        }

        public @NotNull Builder remove(@NotNull ModifierType type) {
            if (!types.contains(type))
                throw new IllegalStateException();

            types.remove(type);

            if (type == ModifierType.ACTION)
                action = null;

            if (type == ModifierType.FAIL)
                failMessage = null;

            return this;
        }

        public @NotNull Builder removeBasicType() {
            types.remove(ModifierType.PEEK);
            types.remove(ModifierType.GENERAL);
            types.remove(ModifierType.FAIL);
            failMessage = null;

            return this;
        }

        public @NotNull NodeModifier build() {
            if (!hasAtLeastOneBasicType())
                throw new IllegalStateException();

            if (types.contains(ModifierType.PEEK) && types.contains(ModifierType.PREVIEW))
                throw new IllegalStateException();

            return new NodeModifier(types, failMessage, action);
        }

        private boolean hasAtLeastOneBasicType() {
            return Lists.containsAny(types, ModifierType.GENERAL, ModifierType.PEEK, ModifierType.FAIL);
        }
        
        private void checkBasicTypes() {
            if (hasAtLeastOneBasicType())
                throw new IllegalStateException();
        }

        public static @NotNull Builder of(@NotNull NodeModifier nodeModifier) {
            final var builder = new Builder();
            builder.types.addAll(nodeModifier.types);
            builder.action = nodeModifier.action;
            builder.failMessage = nodeModifier.failMessage;

            return builder;
        }
    }
}
