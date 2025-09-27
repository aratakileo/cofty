package cofty.core.parser.ast;

import cofty.Utils;
import cofty.v3.core.parser.ast.SetVarValueObject;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SetVarValueObjectTest {
    @Test
    void validGeneral() {
        final var context = Utils.parseContextOf("num = _3_000_000");
        final var astObject = new SetVarValueObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "`num = _3_000_000` should be proceeded"
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertNotNull(astObject.name(), "the proceeded variable name shouldn't be null");
        Assertions.assertNotNull(astObject.value(), "the proceeded variable value shouldn't be null");

        Assertions.assertEquals("num", astObject.name().content, "the variable name should be `num`");

        Assertions.assertNotNull(astObject.value().value().value(), "the variable value shouldn't be null");

        Assertions.assertEquals(
                "_3_000_000",
                astObject.value().value().value().content,
                "the variable value should be `_3_000_000`"
        );
    }

    @Test
    void validPreview() {
        final var context = Utils.parseContextOf("num =");
        final var astObject = new SetVarValueObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.generalAndPreview()),
                "`num =` should be proceeded with preview modifier"
        );
    }

    @Test
    void setVariableNonValue() {
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