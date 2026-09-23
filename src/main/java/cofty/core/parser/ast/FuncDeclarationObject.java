package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.parser.ast.body.BodyObject;
import cofty.core.parser.ast.body.BodyResidentObject;
import cofty.core.parser.ast.value.ExpressionValueObject;
import cofty.core.semantics.symbol.ArgsSignature;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FuncDeclarationObject implements DeclarationObject, WithBody, WithName {
    public final TypedToken<Simple> name;
    public final List<FieldDeclarationObject> args;
    public final BodyObject body;
    public final TypeDescriptorObject returnType;

    private final TypedToken<Bracket> leftBracket, rightBracket;
    private final ArgsSignature<RelativeSymbolPath> argsSignature;

    public FuncDeclarationObject(
            @NotNull TypedToken<Simple> name,
            @NotNull List<FieldDeclarationObject> args,
            @NotNull BodyObject body,
            @Nullable TypeDescriptorObject returnType,
            @NotNull TypedToken<Bracket> leftBracket,
            @NotNull TypedToken<Bracket> rightBracket
    ) {
        this.name = name;
        this.args = args;
        this.body = body;
        this.returnType = returnType;
        this.leftBracket = leftBracket;
        this.rightBracket = rightBracket;

        if (args.isEmpty()) {
            this.argsSignature = ArgsSignature.EMPTY_RELATIVE;
            return;
        }

        final var types = new ArrayList<TypeDescriptor<RelativeSymbolPath>>();
        final var names = new ArrayList<String>();
        final var defaults = new ArrayList<ExpressionValueObject>();

        var lastRequiredArg = 0;

        for (var i = 0; i < args.size(); i++) {
            final var arg = args.get(i);

            types.add(arg.constantValueTypeOrThrow());
            names.add(arg.name());
            defaults.add(arg.value);

            if (arg.value == null)
                lastRequiredArg = i;
        }

        this.argsSignature = ArgsSignature.create(types, names, defaults, lastRequiredArg + 1);
    }

    @Override
    public @NotNull List<BodyResidentObject> residents() {
        return body.residents;
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return name;
    }

    public @NotNull List<TypedToken<?>> argsSignatureRange() {
        return List.of(leftBracket, rightBracket);
    }

    public @NotNull ArgsSignature<RelativeSymbolPath> argsSignature() {
        return argsSignature;
    }

    @Override
    public @NotNull String prettyString(@NotNull String offset, int increase) {
        final var stringifiedArgs = args == null || args.isEmpty()
                ? "no args"
                : (
                '\n' + args.stream()
                        .map(arg -> arg.prettyString(
                                offset.length() + increase,
                                increase
                        )).collect(Collectors.joining("\n")) + '\n' + offset
        );

        return MessageFormat.format(
                "{4}define function [`{0}`; returns {1}] consumes [{2}] body '{'\n{3}{4}\n'}'",
                name(),
                returnType == null ? "nothing" : returnType.prettyString("", increase),
                stringifiedArgs,
                body.prettyString(offset.length() + increase, increase),
                offset
        );
    }
}
