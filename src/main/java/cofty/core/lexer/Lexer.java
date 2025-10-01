package cofty.core.lexer;

import cofty.core.lexer.token.*;
import cofty.core.message.MessageBuilder;
import cofty.core.message.MessageHandler;
import cofty.type.TextContent;
import cofty.type.exception.SyntaxError;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private static final Pattern PATTERN;

    private final TextContent text;
    private final MessageHandler messages;
    private final Matcher matcher;

    public Lexer(@NotNull TextContent text, @NotNull MessageHandler messages) {
        this.text = text;
        this.messages = messages;
        this.matcher = PATTERN.matcher(text.text);
    }

    public @NotNull ArrayList<TypedToken<?>> parse() {
        final var tokens = new ArrayList<TypedToken<?>>();
        var prevToken = (AnyToken) null;

        for (final var matchResult: matcher.results().toList()) {
            final var tokenType = TokenType.valueOf(matchResult);
            final var token = AnyToken.build(matchResult, tokenType);

            if (prevToken != null && prevToken.type.equals(TokenType.MISMATCH)) {
                if (token.type.equals(TokenType.MISMATCH)) {
                    prevToken = (AnyToken) prevToken.merge(token);
                    continue;
                }

                showSyntaxError(prevToken);
            }

            if (!tokenType.isIn(TokenType.SKIP, TokenType.MISMATCH))
                tokens.add(token);

            prevToken = token;
        }

        if (prevToken != null && prevToken.type.equals(TokenType.MISMATCH))
            showSyntaxError(prevToken);

        return tokens;
    }

    private void showSyntaxError(@NotNull AnyToken token) {
        messages.CRITICAL.put(MessageBuilder.err(text, token, new SyntaxError("invalid syntax")));
    }

    static {
        var patternTexts = new ArrayList<String>();

        final var keywordsRegex = String.join(
                "|",
                Arrays.stream(Keyword.values()).map(val -> val.name().toLowerCase()).toList()
        );

        final var modifiersRegex = String.join(
                "|",
                Arrays.stream(Modifier.values()).map(val -> val.name().toLowerCase()).toList()
        );

        final var patterns = new LinkedHashMap<TokenType, String>();
        patterns.put(TokenType.STR, "'(?:\\\\.|[^'])*'|\"(?:\\\\.|[^\"])*\"");
        patterns.put(TokenType.DOUBLE, "_*\\d+[\\d_]*(?:\\.[\\d_]*|[dD])");
        patterns.put(TokenType.INT, "_*\\d+[\\d_]*");
        patterns.put(TokenType.KW, keywordsRegex + '|' + modifiersRegex);
        patterns.put(TokenType.ID, "(?!_*\\d+)[A-Za-z\\d_]+");
        patterns.put(TokenType.OP, "=");
        patterns.put(TokenType.SEP, ":|\\.|,|->");
        patterns.put(TokenType.BRACKETS, "\\(|\\)|\\{|\\}");

        // [ \t]* - to avoid NEWLINE token splitting
        patterns.put(TokenType.NEWLINE, "([ \t]*\n[ \t]*)+");

        // [ \t] instead of \\s to avoid absorption NEWLINE token by SKIP token
        patterns.put(TokenType.SKIP, "[ \t]+");
        patterns.put(TokenType.MISMATCH, ".");

        for (var pattern: patterns.entrySet())
            patternTexts.add(String.format("(?<%s>%s)", pattern.getKey().name(), pattern.getValue()));

        PATTERN = Pattern.compile(String.join("|", patternTexts));
    }
}
