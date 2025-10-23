package cofty.core.parser.ast;

import cofty.Utils;
import cofty.core.lexer.token.type.operator.Bracket;
import cofty.core.parser.node.modifier.NodeModifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BodyObjectTest {
    @Test
    void validBody() {
        final var context = Utils.parseContextOf(
                        """
                        public var num = 10
                        num = 100
                        parent.num = 100
                        private public fun test(num: int,) -> nil {}
                        test()
                        return 45
                        test.child(45, 78,)
                        static {}
                        {}
                        if true {}
                        private class Test {}
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
                11,
                astObject.objects().size(),
                "there should be 11 ast objects of body"
        );
    }

    @Test
    void validBodyWithStopper() {
        final var context = Utils.parseContextOf(
                        """
                        var num = 10
                        num = 100
                        fun test() -> nil {}
                        test()
                        return 45
                        }
                        
                        var value = 345
                        """
        );

        final var astObject = new BodyObject();

        Assertions.assertTrue(
                astObject.parserNode(Bracket.CURVE_CLOSE).proceedQueue(context, NodeModifier.general()),
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
    void invalidTwoExpressionsOnOneLine() {
        final var context = Utils.parseContextOf("var num = 10 fun test() -> nil {}");

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
                "SyntaxError: expected the new expression would starts with a new line",
                context.CRITICAL_MESSAGES.get(0).content
        );
    }
}