package cofty.type;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TextContent {
    public final String text;
    public final String path;

    public TextContent(String text, String path) {
        this.text = text;
        this.path = path.replace('\\', '/');
    }

    public static Result<TextContent, Exception> read(String path) {
        try {
            final var _path = Paths.get(path);
            return Result.ok(new TextContent(Files.readString(_path), _path.toAbsolutePath().toString()));
        } catch (Exception e) {
            return Result.err(e);
        }
    }
}
