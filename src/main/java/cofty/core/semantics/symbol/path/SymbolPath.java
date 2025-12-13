package cofty.core.semantics.symbol.path;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.semantics.symbol.scope.RootScope;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public sealed abstract class SymbolPath<T extends SymbolPath<T>> permits AbsSymbolPath, RelativeSymbolPath {
    public final List<String> parts;

    protected SymbolPath(@NotNull List<String> path) {
        this.parts = path;
    }

    public final boolean isAbs() {
        return parts.getFirst().equals(RootScope.NAME);
    }

    public @NotNull T merge(@NotNull String name) {
        if (name.equals(RootScope.NAME))
            throw new IllegalArgumentException();

        final var newPath = Stream.concat(parts.stream(), Stream.of(name)).toList();

        return isAbs() ? Cast.quiet(new AbsSymbolPath(newPath)) : Cast.quiet(new RelativeSymbolPath(newPath));
    }

    public boolean startsWith(@NotNull SymbolPath<?> otherPath) {
        if (isAbs() != otherPath.isAbs())
            return false;

        if (otherPath.parts.size() > parts.size())
            return false;

        for (var i = 0; i < otherPath.parts.size(); i++)
            if (!parts.get(i).equals(otherPath.parts.get(i)))
                return false;

        return true;
    }

    public boolean endsWith(@NotNull SymbolPath<?> otherPath) {
        if (!isAbs() && otherPath.isAbs())
            return false;

        if (otherPath.parts.size() > parts.size())
            return false;

        for (var i = parts.size() - 1; i > 0; i--)
            if (!parts.get(i).equals(otherPath.parts.get(i)))
                return false;

        return true;
    }

    public @NotNull T sliceUntil(int end) {
        if (end == 0 || end >= parts.size() || end <= -parts.size())
            throw new IllegalArgumentException();

        if (end < 0) end += parts.size();

        return Cast.quiet(raw(parts.subList(0, end)));
    }

    @Override
    public boolean equals(@NotNull Object _other) {
        if (_other instanceof SymbolPath<?> other) {
            if (isAbs() != other.isAbs() || parts.size() != other.parts.size()) return false;

            return parts.equals(other.parts);
        }

        return false;
    }

    @Override
    public String toString() {
        return String.join(".", parts);
    }

    public static @NotNull SymbolPath<?> raw(@NotNull List<String> path) {
        return path.getFirst().equals(RootScope.NAME) ? absolute(path) : relative(path);
    }

    public static @NotNull RelativeSymbolPath rawRelative(@NotNull String path) {
        return relative(Arrays.stream(path.split("\\.")).toList());
    }

    public static @NotNull RelativeSymbolPath rawRelative(@NotNull List<TypedToken<Simple>> path) {
        return new RelativeSymbolPath(path.stream().map(token -> {
            if (!token.type.equals(Simple.WORD))
                throw new IllegalStateException();

            return token.content;
        }).toList());
    }

    public static @NotNull RelativeSymbolPath relative(@NotNull List<String> path) {
        if (path.isEmpty())
            throw new IllegalArgumentException("empty path");

        if (path.getFirst().equals(RootScope.NAME))
            throw new IllegalArgumentException("not a relative path");

        return new RelativeSymbolPath(path);
    }

    public static @NotNull AbsSymbolPath absolute(@NotNull List<String> path) {
        if (path.isEmpty())
            throw new IllegalArgumentException("empty path");

        if (!path.getFirst().equals(RootScope.NAME))
            throw new IllegalArgumentException("not an absolute path");

        if (path.size() == 1)
            return AbsSymbolPath.ROOT;

        return new AbsSymbolPath(path);
    }
}
