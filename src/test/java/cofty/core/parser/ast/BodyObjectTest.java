package cofty.core.parser.ast;

import cofty.Utils;
import cofty.core.lexer.token.Brackets;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BodyObjectTest {
    @Test
    void validBody() {
        final var context = Utils.parseContextOf(
                        """
                        pub let num = 10
                        num = 100
                        priv pub fn test() -> nil {}
                        test()
                        return 45
                        if true {}
                        priv cls Test {}
                        """
        );

        final var astObject = new BodyObject();

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
                7,
                astObject.objects().size(),
                "there should be 5 ast objects of body"
        );
    }

    @Test
    void validBodyWithStopper() {
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

        final var astObject = new BodyObject();

        Assertions.assertTrue(
                astObject.parserNode(Brackets.CURVE_CLOSE).proceedQueue(context, NodeModifier.general()),
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

        final var astObject = new BodyObject();

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