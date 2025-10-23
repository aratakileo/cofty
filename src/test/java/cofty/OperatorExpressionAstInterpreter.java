package cofty;

import cofty.core.lexer.Lexer;
import cofty.core.message.MessageHandler;
import cofty.core.parser.ParseContext;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.node.OperatorExpressionParser;
import cofty.core.parser.node.modifier.NodeModifier;
import cofty.type.TextContent;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public final class OperatorExpressionAstInterpreter {
    public static void main(String[] args) {
        final var scanner = new Scanner(System.in);

        while (true) {
            System.out.print(">>> ");

            final var input = scanner.nextLine();

            if (input.isEmpty()) break;

            final var inputContent = TextContent.ofInput(input);
            final var messages = new MessageHandler();
            final var lexer = new Lexer(inputContent, messages);
            final var parseContext = new ParseContext(lexer.parse(), inputContent, messages);

            if (messages.isEmpty()) {
                final var result = new AtomicReference<AstObject>(null);
                final var parser = new OperatorExpressionParser(
                        parseContext,
                        NodeModifier.builder().general().astObjectConsumer(result::set).build(), NodeModifier.general(),
                        true
                );

                parser.parse();

                System.out.println("Result: " + result);
            }

            messages.print();
        }
    }
}
