package cofty.core.lexer;

import cofty.core.lexer.token.*;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.compiler.message.MessageType;
import cofty.core.compiler.message.CompilationMessage;
import cofty.core.compiler.message.CompilationMessageHandler;
import cofty.type.TextContent;
import cofty.core.compiler.message.CompilationMessageLabel;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private static final Pattern PATTERN;

    private final TextContent text;
    private final CompilationMessageHandler messages;
    private final Matcher matcher;

    public Lexer(@NotNull TextContent text, @NotNull CompilationMessageHandler messages) {
        this.text = text;
        this.messages = messages;
        this.matcher = PATTERN.matcher(text.text);
    }

    public @NotNull ArrayList<TypedToken<?>> parse() {
        final var tokens = new ArrayList<TypedToken<?>>();
        var prevToken = (TypedToken<?>)null;

        for (final var matchResult: matcher.results().toList()) {
            final var tokenType = TokenType.valueOf(matchResult);
            final var token = TypedToken.build(matchResult, tokenType);

            if (prevToken != null && prevToken.type.equals(Simple.MISMATCH)) {
                if (token.type.equals(Simple.MISMATCH)) {
                    prevToken = prevToken.merge(Cast.quiet(token));
                    continue;
                }

                showSyntaxError(prevToken);
            }

            if (!tokenType.isAny(Simple.SKIP, Simple.MISMATCH) && (tokenType != Simple.NEWLINE || !tokens.isEmpty()))
                tokens.add(token);

            prevToken = token;
        }

        if (prevToken != null && prevToken.type.equals(Simple.MISMATCH))
            showSyntaxError(prevToken);

        return tokens;
    }

    private void showSyntaxError(@NotNull TypedToken<?> token) {
        messages.add(CompilationMessage.create(text, MessageType.ERROR, CompilationMessageLabel.INVALID_SYNTAX, token));
    }

    static {
        var patternTexts = new ArrayList<String>();

        final var patterns = new LinkedHashMap<Simple, String>();
        patterns.put(Simple.STR, "'(?:\\\\.|[^'])*'|\"(?:\\\\.|[^\"])*\"");
        patterns.put(Simple.DOUBLE, "_*\\d+[\\d_]*(?:\\.[\\d_]*|[dD])");
        patterns.put(Simple.INT, "_*(?:0_*[xX][\\da-fA-F_]+|\\d+[\\d_]*)");
        patterns.put(Simple.WORD, "(?!_*\\d+)[A-Za-z\\d_]+");
        patterns.put(Simple.OP, "<=|>=|\\!=|==|=|<<|>>|->|>|<|-|\\+|\\*\\*|\\*|\\^|%|/|~|&|:|\\.|,|\\!|\\|");
        patterns.put(Simple.BRACKETS, "\\(|\\)|\\{|\\}");

        // [ \t]* - to avoid NEWLINE token splitting
        patterns.put(Simple.NEWLINE, "([ \t]*\n[ \t]*)+");

        // [ \t] instead of \\s to avoid absorption NEWLINE token by SKIP token
        patterns.put(Simple.SKIP, "[ \t]+");
        patterns.put(Simple.MISMATCH, ".");

        for (var pattern: patterns.entrySet())
            patternTexts.add(String.format("(?<%s>%s)", pattern.getKey().name(), pattern.getValue()));

        PATTERN = Pattern.compile(String.join("|", patternTexts));
    }
}
