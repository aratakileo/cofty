package cofty.core.lexer;

import cofty.core.lexer.token.*;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.message.MessageBuilder;
import cofty.core.message.MessageHandler;
import cofty.type.TextContent;
import cofty.type.exception.SyntaxError;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

            if (prevToken != null && prevToken.type.equals(Simple.MISMATCH)) {
                if (token.type.equals(Simple.MISMATCH)) {
                    prevToken = (AnyToken) prevToken.merge(token);
                    continue;
                }

                showSyntaxError(prevToken);
            }

            if (!tokenType.isIn(Simple.SKIP, Simple.MISMATCH))
                tokens.add(token);

            prevToken = token;
        }

        if (prevToken != null && prevToken.type.equals(Simple.MISMATCH))
            showSyntaxError(prevToken);

        return tokens;
    }

    private void showSyntaxError(@NotNull AnyToken token) {
        messages.CRITICAL.put(MessageBuilder.err(text, token, new SyntaxError("invalid syntax")));
    }

    static {
        var patternTexts = new ArrayList<String>();

        final var patterns = new LinkedHashMap<Simple, String>();
        patterns.put(Simple.STR, "'(?:\\\\.|[^'])*'|\"(?:\\\\.|[^\"])*\"");
        patterns.put(Simple.DOUBLE, "_*\\d+[\\d_]*(?:\\.[\\d_]*|[dD])");
        patterns.put(Simple.INT, "_*\\d+[\\d_]*");
        patterns.put(Simple.WORD, "(?!_*\\d+)[A-Za-z\\d_]+");
        patterns.put(Simple.SEP, ":|\\.|,|->");
        patterns.put(Simple.OP, "<=|>=|\\!=|==|=|<<|>>|>|<|-|\\+|\\*\\*|\\*|\\^|%|/|~|&|\\|");
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
