package cofty.core.parser.ast;

import cofty.Utils;
import cofty.v3.core.parser.ast.BodyObject;
import cofty.v3.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BodyObjectTest {
    @Test
    void validRootBody() {
        final var context = Utils.parseContextOf(
                        """
                        let num = 10
                        num = 100
                        fn test() -> nil {}
                        test()
                        return 45
                        """
        );

        final var astObject = new BodyObject(true);

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "root body expressions should be proceeded"
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertEquals(
                5,
                astObject.objects().size(),
                "there should be 5 ast objects of body"
        );
    }

    @Test
    void validNonRootBody() {
        final var context = Utils.parseContextOf(
                        """
                        let num = 10
                        num = 100
                        fn test() -> nil {}
                        test()
                        return 45
                        }
                        
                        let value = 345
                        """
        );

        final var astObject = new BodyObject(false);

        Assertions.assertTrue(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "non-root body expressions should be proceeded"
        );

        Assertions.assertEquals(
                0,
                context.messages.count(),
                "there shouldn't be any error or warning messages"
        );

        Assertions.assertEquals(
                5,
                astObject.objects().size(),
                "there should be 5 ast objects of body"
        );
    }

    @Test
    void twoExpressionsOnOneLineFail() {
        final var context = Utils.parseContextOf("let num = 10 fn test() -> nil {}");

        final var astObject = new BodyObject(true);

        Assertions.assertFalse(
                astObject.parserNode().proceedQueue(context, NodeModifier.general()),
                "root body expressions shouldn't be proceeded"
        );

        Assertions.assertEquals(
                1,
                context.messages.count(),
                "there should be an error message"
        );

        Assertions.assertEquals(
                "SyntaxError: expected the new expression would start on a new line",
                context.CRITICAL_MESSAGES.get(0).content
        );
    }
}