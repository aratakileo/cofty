package cofty.core.parser;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.lexer.token.type.operator.*;
import cofty.core.parser.ast.value.BinaryExpressionObject;
import cofty.core.parser.ast.value.ExpressionValueObject;
import cofty.core.parser.ast.value.UnaryExpressionObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ValueExpressionParser implements Parser<ExpressionValueObject> {
    public final boolean newlinesSensitive;

    private final ArrayList<TokenType> operatorStack = new ArrayList<>();
    private final ArrayList<List<TypedToken<?>>> operatorTokensStack = new ArrayList<>();
    private final ArrayList<ExpressionValueObject> operandStack = new ArrayList<>();

    /**
     * from the perspective of the middle (binary) operator:
     * an operand is a value or a unary operator to the left or right of it
     */
    private boolean expectsOperand;

    private int nestingLevel;

    private ValueExpressionParser(boolean newlinesSensitive) {
        this.newlinesSensitive = newlinesSensitive;
    }

    @Override
    public @NotNull ParseResult<ExpressionValueObject> parse(@NotNull ParseContext context) {
        // IMPORTANT to reset buffered data
        nestingLevel = 0;
        expectsOperand = true;

        operandStack.clear();
        operatorStack.clear();
        operatorTokensStack.clear();

        // main parsing logic
        final var initSkippingNewLinesStatesBufferIndex = Math.max(context.skippingNewLineStatesSize() - 1, 0);

        if (newlinesSensitive)
            context.stopSkippingNewLines();
        else context.startSkippingNewLines();

        while (context.hasCurrent()) {
            var parseResult = parseOpenRoundBracket(context);

            if (parseResult.isFailed()) {
                context.rollbackSkippingNewLinesState(initSkippingNewLinesStatesBufferIndex);
                return ParseResult.failed();
            }

            var nothingParsed = parseResult.isCanceled();
            parseResult = parseCloseRoundBracket(context);

            if (parseResult.isFailed()) {
                context.rollbackSkippingNewLinesState(initSkippingNewLinesStatesBufferIndex);
                return ParseResult.failed();
            }

            nothingParsed = nothingParsed && parseResult.isCanceled();

            parseResult = parseOperator(context);

            if (parseResult.isFailed()) {
                context.rollbackSkippingNewLinesState(initSkippingNewLinesStatesBufferIndex);
                return ParseResult.failed();
            }

            nothingParsed = nothingParsed && parseResult.isCanceled();

            parseResult = parseOperand(context);

            if (parseResult.isFailed()) {
                context.rollbackSkippingNewLinesState(initSkippingNewLinesStatesBufferIndex);
                return ParseResult.failed();
            }

            nothingParsed = nothingParsed && parseResult.isCanceled();

            if (nothingParsed) break;
        }

        context.rollbackSkippingNewLinesState();

        if (operandStack.isEmpty() && operatorStack.isEmpty())
            return ParseResult.canceled();

        while (!operatorStack.isEmpty()) {
            if (operatorStack.getLast().equals(Bracket.ROUND_OPEN)) {
                context.messages.addSyntaxErr(
                        "the round brackets are opened here but never closed",
                        operatorTokensStack.getLast().getFirst()
                );
                return ParseResult.failed();
            }

            if (!applyOperator(context)) return ParseResult.failed();
        }

        if (operandStack.size() > 1)
            throw new IllegalStateException();

        return ParseResult.successful(operandStack.getFirst());
    }

    private @NotNull StaticParseResult parseOpenRoundBracket(@NotNull ParseContext context) {
        if (!context.currentIs(Bracket.ROUND_OPEN))
            return StaticParseResult.CANCELED;

        final var token = context.advanceOrThrow();

        if (!expectsOperand) {
            context.messages.addSyntaxErr(
                    "expected any binary operator here, not the opening round bracket `(`",
                    token
            );
            return StaticParseResult.FAILED;
        }

        addOperatorToStack(token);
        context.startSkippingNewLines();
        nestingLevel++;

        return StaticParseResult.SUCCESSFUL;
    }

    private @NotNull StaticParseResult parseCloseRoundBracket(@NotNull ParseContext context) {
        /*
         *
         * `|| nestingLevel == 0` is really necessary to avoid triggering on the non value based brackets
         * like the function argument brackets:
         *      textCall('my value')
         *                         ^
         *                         this one bracket can trigger that parser part if remove `|| nestingLevel == 0`
         *
         */
        if (!context.currentIs(Bracket.ROUND_CLOSE) || nestingLevel == 0)
            return StaticParseResult.CANCELED;

        final var token = context.advanceOrThrow();

        if (expectsOperand) {
            context.messages.addSyntaxErr("expected any operand here, not the closing round bracket `)`", token);
            return StaticParseResult.FAILED;
        }

        while (!operatorStack.isEmpty()) {
            if (operatorStack.getLast().equals(Bracket.ROUND_OPEN)) {
                operatorTokensStack.removeLast();
                operatorStack.removeLast();
                break;
            }

            if (!applyOperator(context)) return StaticParseResult.FAILED;
        }

        context.rollbackSkippingNewLinesState();
        nestingLevel--;

        return StaticParseResult.SUCCESSFUL;
    }

    private @NotNull StaticParseResult parseOperator(@NotNull ParseContext context) {
        final var token = context.current(true);

        if (token == null || !token.type.isValueOperator())
            return StaticParseResult.CANCELED;

        var operatorType = token.type;
        var operatorTokens = (List<TypedToken<?>>)null;

        if (context.hasNext()) {
            if (context.currentIs(Binary.IS) && context.nextOrThrow().type.equals(Unary.NOT)) {
                operatorTokens = List.of(token, context.nextOrThrow());
                operatorType = Binary.IS_NOT;
            }

            if (context.currentIs(Unary.NOT) && context.nextOrThrow().type.equals(Binary.IN)) {
                operatorTokens = List.of(token, context.nextOrThrow());
                operatorType = Binary.NOT_IN;
            }
        }

        if (operatorTokens == null) {
            if (operatorType instanceof ContextSensitive sensitive)
                operatorType = expectsOperand ? sensitive.unary() : sensitive.binary();

            operatorTokens = List.of(token);
        }

        final var isOperatorUnary = operatorType instanceof Unary;

        if (isOperatorUnary != expectsOperand) {
            final var errorMessage = String.format(
                    "expected any contextually appropriate %s operator here, not the %s operator `%s`",
                    expectsOperand ? "unary" : "binary",
                    isOperatorUnary ? "unary" : "binary",
                    operatorType.content()
            );

            if (operatorTokens.size() == 2)
                context.messages.addInRangeSyntaxErr(
                        errorMessage,
                        context.advanceOrThrow(),
                        context.advanceOrThrow()
                );

            context.messages.addSyntaxErr(errorMessage, context.advanceOrThrow());
            return StaticParseResult.FAILED;
        }

        while (!operatorStack.isEmpty() && !operatorStack.getLast().equals(Bracket.ROUND_OPEN)) {
            final var lastPrioritizedOperator = (Associative) operatorStack.getLast();

            if (lastPrioritizedOperator instanceof Unary) {
                if (!applyOperator(context)) return StaticParseResult.FAILED;
                continue;
            }

            final var prioritizedOperator = (Associative) operatorType;

            final var isOperatorHigherByRight = prioritizedOperator.isRightAssociative()
                    && prioritizedOperator.priorityLevel() < lastPrioritizedOperator.priorityLevel();

            final var isOperatorHigherByLeft = prioritizedOperator.isLeftAssociative()
                    && prioritizedOperator.priorityLevel() <= lastPrioritizedOperator.priorityLevel();

            if (isOperatorHigherByRight || isOperatorHigherByLeft) {
                if (!applyOperator(context)) return StaticParseResult.FAILED;
            }
            else break;
        }

        addOperatorToStack(operatorType, operatorTokens);

        context.goNext();

        if (operatorTokens.size() == 2)
            context.goNext();

        expectsOperand = true;

        return StaticParseResult.SUCCESSFUL;
    }

    @NotNull StaticParseResult parseOperand(@NotNull ParseContext context) {
        /*
         *
         * this condition helps to avoid reacting to "incorrectly positioned operands"
         * that are potentially unrelated to this expression,
         * which also allows not to intercept error handling of "incorrectly positioned operands"
         * from other parsers where it is really necessary.
         *
         * example:
         * functionCall(one two)
         *                  ^^^
         *                  this wrong operand will be processed by ComplexValueParser because of that condition
         *
         */
        if (!expectsOperand) return StaticParseResult.CANCELED;

        final var parseResult = ComplexValueParser.DEFAULT.parse(context);

        if (!parseResult.isSuccessful())
            return StaticParseResult.of(parseResult);

        expectsOperand = false;
        operandStack.add(parseResult.valueOrThrow());

        return StaticParseResult.SUCCESSFUL;
    }

    private boolean applyOperator(@NotNull ParseContext context) {
        final var operator = operatorStack.removeLast();

        if (operator instanceof Binary) {
            if (operandStack.size() < 2) {
                context.messages.addSyntaxErrAfterToken(
                        "expected any second operand here",
                        operatorTokensStack.getLast().getLast()
                );
                return false;
            }

            final var right = operandStack.removeLast();
            final var left = operandStack.removeLast();

            operandStack.add(new BinaryExpressionObject(operatorTokensStack.removeLast(), left, right));
            return true;
        }

        if (operator instanceof Unary) {
            if (operandStack.isEmpty()) {
                context.messages.addSyntaxErrAfterToken(
                        "expected any operand here",
                        operatorTokensStack.getLast().getLast()
                );
                return false;
            }

            operandStack.add(new UnaryExpressionObject(
                    operatorTokensStack.removeLast().getLast(),
                    operandStack.removeLast()
            ));

            return true;
        }

        throw new IllegalStateException("not applicable operator");
    }

    private void addOperatorToStack(@NotNull TypedToken<?> token) {
        addOperatorToStack(token.type, List.of(token));
    }

    private void addOperatorToStack(@NotNull TokenType operatorType, @NotNull List<TypedToken<?>> tokens) {
        operatorStack.add(operatorType);

        if (operatorType.isAny(Binary.IS_NOT, Binary.NOT_IN) && tokens.size() != 2)
            throw new IllegalStateException();

        operatorTokensStack.add(tokens);
    }

    public static @NotNull ValueExpressionParser create(boolean newlinesSensitive) {
        return new ValueExpressionParser(newlinesSensitive);
    }

    private enum StaticParseResult {
        SUCCESSFUL,
        FAILED,
        CANCELED;

        public boolean isFailed() {
            return this == FAILED;
        }

        public boolean isCanceled() {
            return this == CANCELED;
        }

        public static @NotNull StaticParseResult of(@NotNull ParseResult<?> result) {
            if (result.isCanceled())
                return CANCELED;

            if (result.isSuccessful())
                return SUCCESSFUL;

            return FAILED;
        }
    }
}
