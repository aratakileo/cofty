package cofty.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public final class TypeDescriptionObject implements AstObject {
    public final List<TypedToken<Simple>> name;

    public TypeDescriptionObject(@NotNull List<TypedToken<Simple>> name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name.stream().map(token -> token.content).collect(Collectors.joining("."));
    }

    @Override
    public boolean equals(@NotNull Object _other) {
        if (_other instanceof TypeDescriptionObject other) {
            if (name.size() != other.name.size())
                return false;

            for (var i = 0; i < name.size(); i++)
                if (!name.get(i).content.equals(other.name.get(i).content))
                    return false;

            return true;
        }

        return false;
    }
}
