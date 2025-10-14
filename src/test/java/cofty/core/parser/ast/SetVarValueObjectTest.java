package cofty.core.parser.ast;

import cofty.Utils;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

class SetVarValueObjectTest {
    @Test
    void validGeneral() {
        final var expression = "parent.variable = _3_000_000";
        final var context = Utils.parseContextOf(expression);
        final var astObject = new SetVarValueObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                String.format("`%s` should be proceeded", expression)
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertEquals(
                2,
                astObject.name().segments().size(),
                "the proceeded variable name should consist of two segments"
        );

        Assertions.assertEquals(
                "parent",
                astObject.name().segments().getFirst().content,
                "the first segment of variable name should be `parent`"
        );

        Assertions.assertEquals(
                "variable",
                astObject.name().segments().getLast().content,
                "the last segment of variable name should be `variable`"
        );

        Assertions.assertNotNull(astObject.value(), "the proceeded variable value shouldn't be null");
        Assertions.assertNotNull(astObject.value().value().value(), "the variable value shouldn't be null");

        Assertions.assertEquals(
                "_3_000_000",
                Objects.requireNonNull(astObject.value().value().value()).content,
                "the variable value should be `_3_000_000`"
        );
    }

    @Test
    void validPreview() {
        final var context = Utils.parseContextOf("paren.child =");
        final var astObject = new SetVarValueObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.generalAndPreview()),
                "`num =` should be proceeded with preview modifier"
        );
    }

    @Test
    void invalidSetVariableNonValue() {
        final var context = Utils.parseContextOf("num = ->");
        final var astObject = new SetVarValueObject();

        Assertions.assertFalse(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "`num = ->` shouldn't be proceeded"
        );

        Assertions.assertEquals(
                1,
                context.messages.count(),
                "there should be an error message"
        );

        Assertions.assertEquals(
                "SyntaxError: expected a variable value",
                context.CRITICAL_MESSAGES.get(0).content
        );
    }
}