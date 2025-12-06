package cofty.v2.core.semantics;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Path {
    public static Path ROOT = new Path(List.of("$"));

    public final List<String> segments;

    private Path(@NotNull List<String> segments) {
        this.segments = segments.stream().toList();
    }

    public @NotNull Path merge(@NotNull String segment) {
        final var newSegments = new ArrayList<>(segments);
        newSegments.add(segment);
        return of(newSegments);
    }

    public @NotNull Path merge(@NotNull Path path) {
        final var newSegments = new ArrayList<>(segments);
        newSegments.addAll(path.segments);
        return of(newSegments);
    }

    public boolean equals(@Nullable Path path) {
        return path != null && path.segments.equals(segments);
    }

    public static @NotNull Path of(@NotNull List<String> segments) {
        return segments.equals(ROOT.segments) ? ROOT : new Path(segments);
    }

    public static @NotNull Path ofRaw(@NotNull List<TypedToken<?>> rawSegments) {
        return of(rawSegments.stream()
                .filter(token -> token.type.equals(Simple.WORD))
                .map(token -> token.content)
                .toList());
    }
}
