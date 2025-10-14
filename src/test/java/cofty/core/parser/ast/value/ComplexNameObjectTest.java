package cofty.core.parser.ast.value;

import cofty.Utils;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ComplexNameObjectTest {
    @Test
    void validThreeWordsName() {
        final var expression = "one.two.three";
        final var context = Utils.parseContextOf(expression);
        final var astObject = new ComplexNameObject();

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
                astObject.segments().size(),
                "the proceeded name should consist of three name segments"
        );
    }

    @Test
    void validOneWordSimpleName() {
        final var expression = "one";
        final var context = Utils.parseContextOf(expression);
        final var astObject = new ComplexNameObject();

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
                1,
                astObject.segments().size(),
                "the proceeded name should consist of at least one or only one name segments"
        );
    }

    @Test
    void validOneWordSimpleNamePreview() {
        final var expression = "one";
        final var context = Utils.parseContextOf(expression);
        final var astObject = new ComplexNameObject();

        Assertions.assertTrue(
                astObject.parserNode(NodeModifier.generalAndPreview()).proceedQueue(
                        context,
                        NodeModifier.generalAndPreview()
                ),
                String.format("`%s` should be previewed", expression)
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );
    }
}