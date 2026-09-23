package cofty.core.lexer;

import cofty.core.compiler.diagnostic.Errors;
import cofty.core.lexer.token.*;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.compiler.diagnostic.DiagnosticEngine;
import cofty.type.TextContent;
import cofty.util.Cast;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Lexer {
    private static final Pattern PATTERN;

    public final TextContent text;
    public final DiagnosticEngine.TextAssociated messages;

    private final Matcher matcher;
    private ArrayList<TypedToken<?>> tokens;

    public Lexer(@NotNull TextContent text, @NotNull DiagnosticEngine messages) {
        this.text = text;
        this.messages = messages.associateWith(text);
        this.matcher = PATTERN.matcher(text.text);
    }

    public @NotNull ArrayList<TypedToken<?>> parse() {
        tokens = new ArrayList<>();
        
        var prevToken = (TypedToken<?>)null;

        for (final var matchResult: matcher.results().toList()) {
            final var tokenType = TokenType.valueOf(matchResult);
            final var token = TypedToken.build(matchResult, tokenType);

            if (prevToken != null && prevToken.type.equals(Simple.MISMATCH)) {
                if (token.type.equals(Simple.MISMATCH)) {
                    prevToken = prevToken.merge(Cast.quiet(token));
                    continue;
                }

                messages.report(prevToken, Errors.LEXER_SYNTAX_ERROR);
            }

            if (tokenType.isAny(Simple.SKIP, Simple.MISMATCH, Simple.COMMENT)
                    || (tokenType == Simple.NEWLINE && tokens.isEmpty())) {
                prevToken = token;
                continue;
            }

            prevToken = tokens.isEmpty() ? prevToken : tokens.getLast();

            final var replacePrevToken = prevToken != null
                    && prevToken.type == Simple.NEWLINE
                    && token.type == Simple.NEWLINE;

            tokens.add(replacePrevToken ? tokens.removeLast().strictAs().merge(token) : token);

            prevToken = token;
        }

        if (prevToken != null && prevToken.type.equals(Simple.MISMATCH))
            messages.report(prevToken, Errors.LEXER_SYNTAX_ERROR);

        return tokens;
    }

    public @NotNull String prettyString() {
        if (tokens == null) return "";

        final var textBuilder = new StringBuilder();

        for (final var lexeme: tokens)
            textBuilder.append(lexeme.prettyString()).append("\n");

        return textBuilder.toString();
    }

    static {
        var patternTexts = new ArrayList<String>();

        final var patterns = new LinkedHashMap<Simple, String>();
        patterns.put(Simple.STR, "'(?:\\\\.|[^'\n])*'|\"(?:\\\\.|[^\"\n])*\"");
        patterns.put(Simple.COMMENT, "#[^\n]*");

        // [ \t]* - to avoid NEWLINE token splitting
        patterns.put(Simple.NEWLINE, "([ \t]*\n[ \t]*)+");

        // [ \t] instead of \\s to avoid absorption NEWLINE token by SKIP token
        patterns.put(Simple.SKIP, "[ \t]+");

        patterns.put(Simple.DOUBLE, "_*\\d+[\\d_]*(?:\\.[\\d_]*|[dD])");
        patterns.put(Simple.INT, "_*(?:0_*[xX][\\da-fA-F_]+|\\d+[\\d_]*)");
        patterns.put(Simple.WORD, "(?!_*\\d+)[A-Za-z\\d_]+");
        patterns.put(Simple.OP, "<=|>=|\\!=|==|=|<<|>>|->|>|<|-|\\+|\\*\\*|\\*|\\^|%|/|~|&|:|\\.|,|\\!|\\|");
        patterns.put(Simple.BRACKETS, "\\(|\\)|\\{|\\}");
        patterns.put(Simple.MISMATCH, ".");

        for (var pattern: patterns.entrySet())
            patternTexts.add(String.format("(?<%s>%s)", pattern.getKey().name(), pattern.getValue()));

        PATTERN = Pattern.compile(String.join("|", patternTexts));
    }
}
