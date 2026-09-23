package cofty.core.semantics.symbol.path;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.semantics.symbol.scope.RootScope;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public sealed abstract class SymbolPath<T extends SymbolPath<T>> permits AbsSymbolPath, RelativeSymbolPath {
    public final String fullName;
    public final List<String> parts;

    protected SymbolPath(@NotNull String path) {
        this.parts = Arrays.stream(path.split("\\.")).toList();
        this.fullName = path;
    }

    protected SymbolPath(@NotNull List<String> path) {
        this.parts = path;
        this.fullName = String.join(".", parts);
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

        final var offset = parts.size() - otherPath.parts.size();

        for (var i = parts.size() - 1; i >= offset; i--)
            if (!parts.get(i).equals(otherPath.parts.get(i - offset)))
                return false;

        return true;
    }

    public @NotNull RelativeSymbolPath sliceParts(int startPart) {
        return relative(parts.subList(startPart, parts.size()));
    }

    public @NotNull T sliceUntilPart(int endPart) {
        if (endPart == 0 || endPart >= parts.size() || endPart <= -parts.size())
            throw new IllegalArgumentException();

        if (endPart < 0) endPart += parts.size();

        return Cast.quiet(raw(parts.subList(0, endPart)));
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
        return fullName;
    }

    public static @NotNull SymbolPath<?> raw(@NotNull String path) {
        return path.startsWith(RootScope.NAME) ? absolute(path) : relative(path);
    }

    public static @NotNull SymbolPath<?> raw(@NotNull List<String> path) {
        return path.getFirst().equals(RootScope.NAME) ? absolute(path) : relative(path);
    }

    public static @NotNull RelativeSymbolPath rawRelative(@NotNull List<TypedToken<Simple>> path) {
        return new RelativeSymbolPath(path.stream().map(token -> {
            if (!token.type.equals(Simple.WORD))
                throw new IllegalStateException();

            return token.content;
        }).toList());
    }

    public static @NotNull AbsSymbolPath asAbsolute(@NotNull String path) {
        final var symbolPath = raw(path);

        if (symbolPath.isAbs())
            throw new IllegalArgumentException();

        return absolute(AbsSymbolPath.ROOT.fullName + '.' + path);
    }

    public static @NotNull AbsSymbolPath absolute(@NotNull String path) {
        return absolute(null, path);
    }

    public static @NotNull AbsSymbolPath absolute(@NotNull List<String> path) {
        return absolute(path, null);
    }

    public static @NotNull RelativeSymbolPath relative(@NotNull String path) {
        return relative(null, path);
    }

    public static @NotNull RelativeSymbolPath relative(@NotNull List<String> path) {
        return relative(path, null);
    }

    private static @NotNull RelativeSymbolPath relative(@Nullable List<String> slicedPath, @Nullable String path) {
        checkValidity(slicedPath, path);

        if (path != null) path = path.strip();

        if (startsWithRootName(slicedPath, path))
            throw new IllegalArgumentException("not a relative path");

        return slicedPath == null ? new RelativeSymbolPath(path) : new RelativeSymbolPath(slicedPath);
    }

    private static @NotNull AbsSymbolPath absolute(@Nullable List<String> slicedPath, @Nullable String path) {
        checkValidity(slicedPath, path);

        if (!startsWithRootName(slicedPath, path))
            throw new IllegalArgumentException(String.format(
                    "not an absolute path `%s`",
                    String.join(".", slicedPath)
            ));

        return slicedPath == null ? new AbsSymbolPath(path) : new AbsSymbolPath(slicedPath);
    }

    private static void checkValidity(@Nullable List<String> slicedPath, @Nullable String path) {
        if (slicedPath == null && path == null)
            throw new IllegalArgumentException();

        if (slicedPath != null && slicedPath.isEmpty() || path != null && path.isBlank())
            throw new IllegalArgumentException("empty path");
    }

    private static boolean startsWithRootName(@Nullable List<String> slicedPath, @Nullable String path) {
        return slicedPath != null && slicedPath.getFirst().equals(RootScope.NAME)
                || path != null && path.startsWith(RootScope.NAME);
    }
}
