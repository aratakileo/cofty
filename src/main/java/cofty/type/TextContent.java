package cofty.type;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public final class TextContent {
    public final String text;
    public final String path;

    public TextContent(@NotNull String text, @NotNull String path) {
        // \r\n - Windows way to describe the new line, which can cause problems (EWWWWWWW)
        this.text = text.replace("\r\n", "\n");

        // the unix way to describe path with `/` is better than the windows one with `\`  :O
        this.path = path.replace('\\', '/');
    }

    public @NotNull String simpleName() {
        if (path.equals("<input>")) return "input";

        final var pathSegments = path.split("/");
        final var nameSegments = pathSegments[pathSegments.length - 1].split("\\.");

        if (nameSegments.length != 2)
            throw new IllegalStateException();

        return nameSegments[0];
    }

    public @NotNull String dirPath() {
        final var segments = path.split("/");

        if (segments.length <= 1)
            return ".";

        return String.join("/", Arrays.stream(segments).toList().subList(0, segments.length - 1));
    }

    public static @NotNull Result<@NotNull TextContent, @NotNull Exception> read(@NotNull String path) {
        try {
            final var _path = Paths.get(path);
            return Result.ok(new TextContent(Files.readString(_path), _path.toAbsolutePath().toString()));
        } catch (Exception e) {
            return Result.err(e);
        }
    }

    public static @NotNull TextContent ofInput(@NotNull String text) {
        return new TextContent(text, "<input>");
    }
}
