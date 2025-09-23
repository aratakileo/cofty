package cofty.v3.core.parser.node.modifier;

import cofty.core.lexer.token.Token;
import cofty.type.Representable;
import cofty.type.exception.SyntaxError;
import cofty.util.Lists;
import cofty.v3.core.parser.ast.AstObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class NodeModifier {
    private final HashSet<@NotNull ModifierType> types;
    
    public final Exception failMessage;
    public final ModifierConsumer modifierConsumer;

    private NodeModifier(
            @NotNull HashSet<@NotNull ModifierType> types, 
            @Nullable Exception failMessage, 
            @Nullable ModifierConsumer modifierConsumer
    ) {
        this.types = new HashSet<>(types);
        this.failMessage = failMessage;
        this.modifierConsumer = modifierConsumer;
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

    public @NotNull ModifierConsumer actionOrThrow() {
        return Objects.requireNonNull(modifierConsumer);
    }

    public static @NotNull NodeModifier fail(@NotNull Exception message) {
        return new NodeModifier(Lists.hashSetOf(ModifierType.FAIL), message, null);
    }

    public static @NotNull NodeModifier syntaxFail(@NotNull String message) {
        return fail(new SyntaxError(message));
    }

    public static @NotNull NodeModifier previewAndGeneral() {
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
        if (
                !topLevelModifier.isAny(ModifierType.GENERAL, ModifierType.PREVIEW)
                        || currentLevelModifier.isAny(ModifierType.ACTION)
        )
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
                Representable.repr(modifierConsumer)
        );
    }

    public static class Builder {
        private final HashSet<@NotNull ModifierType> types = new HashSet<>();
        private Exception failMessage = null;
        private ModifierConsumer modifierConsumer = null;

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

        public @NotNull Builder tokenConsumer(@NotNull Consumer<Token> action) {
            return action(new ModifierConsumer() {
                @Override
                public void consume(@NotNull Token token) {
                    action.accept(token);
                }
            });
        }

        public @NotNull Builder astObjectConsumer(@NotNull Consumer<AstObject> action) {
            return action(new ModifierConsumer() {
                @Override
                public void consume(@NotNull AstObject astObject) {
                    action.accept(astObject);
                }
            });
        }

        public @NotNull Builder astObjectsConsumer(@NotNull Consumer<List<AstObject>> action) {
            return action(new ModifierConsumer() {
                @Override
                public void consume(@NotNull List<AstObject> astObjects) {
                    action.accept(astObjects);
                }
            });
        }

        public @NotNull Builder remove(@NotNull ModifierType type) {
            if (!types.contains(type))
                throw new IllegalStateException();

            types.remove(type);

            if (type == ModifierType.ACTION)
                modifierConsumer = null;

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

            return new NodeModifier(types, failMessage, modifierConsumer);
        }

        private @NotNull Builder action(@NotNull ModifierConsumer action) {
            if (this.modifierConsumer != null)
                throw new IllegalStateException();

            types.add(ModifierType.ACTION);
            this.modifierConsumer = action;

            return this;
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
            builder.modifierConsumer = nodeModifier.modifierConsumer;
            builder.failMessage = nodeModifier.failMessage;

            return builder;
        }
    }
}
