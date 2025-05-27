package cofty.type;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class TextContent {
    public final String text;
    public final String path;

    public TextContent(String text, String path) {
        // \r\n - Windows way to describe the new line, which can cause problems (EWWWWWWW)
        this.text = text.replace("\r\n", "\n");
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
