package cofty.core.parser.ast.func;

import cofty.Utils;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CallFuncObjectTest {
    @Test
    void validThreeWordsName() {
        final var expression = "one.two.three()";
        final var context = Utils.parseContextOf(expression);
        final var astObject = new CallFuncObject();

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
                3,
                astObject.name().segments().size(),
                "the proceeded name should consist of three name segments"
        );
    }

    @Test
    void validOneWordSimpleNameWithTwoArguments() {
        final var expression = "one(23, 56,)";
        final var context = Utils.parseContextOf(expression);
        final var astObject = new CallFuncObject();

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
                astObject.args().size(),
                "the function call should contain two arguments"
        );
    }

    @Test
    void validThreeWordsNamePreview() {
        final var expression = "one.two.three(";
        final var context = Utils.parseContextOf(expression);
        final var astObject = new CallFuncObject();

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.generalAndPreview()),
                String.format("`%s` should be previewed", expression)
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );
    }
}