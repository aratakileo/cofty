package cofty.type;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TextContent {
    public final String text;
    public final String path;

    public TextContent(@NotNull String text, @NotNull String path) {
        // \r\n - Windows way to describe the new line, which can cause problems (EWWWWWWW)
        this.text = text.replace("\r\n", "\n");
        this.path = path.replace('\\', '/');
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
