package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.parser.ast.body.BodyObject;
import cofty.core.parser.ast.body.BodyResidentObject;
import cofty.core.parser.ast.value.complex.SimpleValueObject;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import cofty.core.semantics.symbol.path.SymbolPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class FuncDeclarationObject implements DeclarationObject, WithBody, WithName {
    public final TypedToken<Simple> name;
    public final List<FieldDeclarationObject> args;
    public final BodyObject body;
    public final TypeDescriptionObject returnType;

    public FuncDeclarationObject(
            @NotNull TypedToken<Simple> name,
            @NotNull List<FieldDeclarationObject> args,
            @NotNull BodyObject body,
            @Nullable TypeDescriptionObject returnType
    ) {
        this.name = name;
        this.args = args;
        this.body = body;
        this.returnType = returnType;
    }

    @Override
    public @NotNull List<BodyResidentObject> residents() {
        return body.residents;
    }

    @Override
    public @NotNull TypedToken<Simple> nameToken() {
        return name;
    }

    public @NotNull List<RelativeSymbolPath> getArgSignatures() {
        return args.stream()
                .map(
                        arg -> arg.valueType != null
                                ? SymbolPath.rawTokens(arg.valueType.name)
                                : SymbolPath.relative(((SimpleValueObject)arg.valueOrThrow()).valueTypeName())
                ).toList();
    }
}
