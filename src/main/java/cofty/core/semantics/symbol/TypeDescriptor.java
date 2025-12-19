package cofty.core.semantics.symbol;

import cofty.core.parser.ast.TypeDescriptorObject;
import cofty.core.semantics.symbol.path.AbsSymbolPath;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.core.semantics.symbol.path.SymbolPath;
import org.jetbrains.annotations.NotNull;

public final class TypeDescriptor<T extends SymbolPath<?>> {
    public static @NotNull TypeDescriptor<RelativeSymbolPath> RELATIVE_NULL = rawReference("null");

    public final T path;

    private TypeDescriptor(@NotNull T path) {
        this.path = path;
    }

    public boolean isLike(@NotNull TypeDescriptor<? extends SymbolPath<?>> otherType) {
        if (path.isAbs() == otherType.path.isAbs())
            return path.equals(otherType.path);

        return path.endsWith(otherType.path) || otherType.path.endsWith(path);
    }

    @Override
    public boolean equals(@NotNull Object other) {
        if (other instanceof TypeDescriptor<?> typeDescriptor)
            return typeDescriptor.path.equals(path);

        return false;
    }

    @Override
    public String toString() {
        return path.fullName;
    }

    public @NotNull String fullName() {
        return String.join("/", path.parts);
    }

    public static @NotNull TypeDescriptor<AbsSymbolPath> reference(@NotNull AbsSymbolPath path) {
        return new TypeDescriptor<>(path);
    }

    public static @NotNull TypeDescriptor<RelativeSymbolPath> reference(@NotNull RelativeSymbolPath path) {
        return new TypeDescriptor<>(path);
    }

    public static @NotNull TypeDescriptor<RelativeSymbolPath> rawReference(@NotNull String name) {
        return new TypeDescriptor<>(SymbolPath.relative(name));
    }

    public static @NotNull TypeDescriptor<RelativeSymbolPath> rawReference(@NotNull TypeDescriptorObject typeDescriptorObject) {
        return new TypeDescriptor<>(SymbolPath.rawRelative(typeDescriptorObject.name));
    }
}
