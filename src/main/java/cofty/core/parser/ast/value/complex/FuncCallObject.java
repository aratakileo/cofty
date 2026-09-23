package cofty.core.parser.ast.value.complex;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.WithName;
import cofty.core.parser.ast.value.ExpressionValueObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public final class FuncCallObject implements ValueSegmentObject, WithName {
    public final TypedToken<Simple> name;
    public final List<ExpressionValueObject> args;
    public final boolean isPostfix;

    private final TypedToken<?> lastToken;

    public FuncCallObject(
            @NotNull TypedToken<Simple> name,
            @NotNull List<ExpressionValueObject> args,
            @Nullable TypedToken<?> lastToken,
            boolean isPostfix
    ) {
        this.name = name;
        this.args = args;
        this.isPostfix = isPostfix;
        this.lastToken = lastToken;
    }

    @Override
    public @NotNull String represent() {
        return isPostfix ? "postfix function call" : "function call";
    }

    @Override
    public @NotNull List<TypedToken<?>> failTokensRange() {
        return lastToken == null ? List.of(name) : List.of(name, lastToken);
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return name;
    }

    public static @NotNull FuncCallObject createPostfixFuncCall(@NotNull TypedToken<Simple> name) {
        return new FuncCallObject(name, List.of(), null, true);
    }

    @Override
    public @NotNull String prettyString(@NotNull String offset, int increase) {
        return MessageFormat.format(
                "{3}call {0} function `{1}` pass [{2}]",
                isPostfix ? "postfix" : "normal",
                name.content,
                args.isEmpty()
                        ? "no args"
                        : args.stream().map(value -> value.prettyString("", increase))
                            .collect(Collectors.joining("; ")),
                offset
        );
    }
}
