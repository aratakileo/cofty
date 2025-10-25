package cofty.v4.core.parser.ast;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class TypeDescriptionObject implements AstObject {
    public final List<TypedToken<Simple>> name;

    public TypeDescriptionObject(@NotNull List<TypedToken<Simple>> name) {
        this.name = name;
    }
}
