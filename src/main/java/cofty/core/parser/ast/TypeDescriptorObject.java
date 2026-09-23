package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.semantics.symbol.TypeDescriptor;
import cofty.core.semantics.symbol.path.RelativeSymbolPath;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public final class TypeDescriptorObject implements AstObject, WithDiagnosticFailAnchor {
    public final List<TypedToken<Simple>> name;

    public TypeDescriptorObject(@NotNull List<TypedToken<Simple>> name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name.stream().map(token -> token.content).collect(Collectors.joining("."));
    }

    @Override
    public boolean equals(@NotNull Object _other) {
        if (_other instanceof TypeDescriptorObject other) {
            if (name.size() != other.name.size())
                return false;

            for (var i = 0; i < name.size(); i++)
                if (!name.get(i).content.equals(other.name.get(i).content))
                    return false;

            return true;
        }

        return false;
    }

    @Override
    public @NotNull List<TypedToken<?>> failTokensRange() {
        return List.of(name.getFirst(), name.getLast());
    }

    @Override
    public @NotNull String prettyString(@NotNull String offset, int increase) {
        return MessageFormat.format(
                "{1}type [{0}]",
                toString(),
                offset
        );
    }
}
