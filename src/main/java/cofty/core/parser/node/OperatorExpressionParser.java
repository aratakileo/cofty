package cofty.core.parser.node;

import cofty.core.lexer.token.TypedToken;
import cofty.core.lexer.token.type.Simple;
import cofty.core.lexer.token.type.TokenType;
import cofty.core.lexer.token.type.operator.*;
import cofty.core.parser.ParseContext;
import cofty.core.parser.ast.AstObject;
import cofty.core.parser.ast.value.expr.ValueExpression;
import cofty.core.parser.ast.value.expr.ValueExpressionObject;
import cofty.core.parser.ast.value.expr.op.BinaryExpressionObject;
import cofty.core.parser.ast.value.expr.op.UnaryExpressionObject;
import cofty.core.parser.node.modifier.ModifierType;
import cofty.core.parser.node.modifier.NodeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class OperatorExpressionParser {
    private final ArrayList<TokenType> operatorStack = new ArrayList<>();
    private final ArrayList<List<TypedToken<?>>> operatorTokensStack = new ArrayList<>();
    private final ArrayList<ValueExpression> operandStack = new ArrayList<>();

    private final ParseContext context;
    private final NodeModifier modifier, topLevelModifier;
    private final boolean breakExpressionByNewLine;

    private boolean expectOperand = true;
    private ValueExpressionObject _emptyValueBuffer = null;
    private int nestingLevel = 0;

    public OperatorExpressionParser(
            @NotNull ParseContext context,
            @NotNull NodeModifier modifier,
            @NotNull NodeModifier topLevelModifier,
            boolean breakExpressionByNewLine
    ) {
        this.context = context;
        this.modifier = modifier;
        this.topLevelModifier = topLevelModifier;
        this.breakExpressionByNewLine = breakExpressionByNewLine;
    }

    public boolean parse() {
        tryCreateSnapshot();

        while (context.hasCurrent()) {
            final var currentTokenType = context.currentOrThrow().type;

            if (shouldBreakExpression()) return checkParsingResult();

            if (currentTokenType.equals(Bracket.ROUND_OPEN)) {
                if (!isValidContextedOperand(true)) {
                    tryRollbackSnapshot();
                    return false;
                }

                if (isInPreviewMode()) {
                    tryRollbackSnapshot();
                    return true;
                }

                expectOperand = true;
                nestingLevel++;

                addOperatorToStack(context.currentOrThrow().type);
                context.goNext();
                continue;
            }

            if (currentTokenType.equals(Bracket.ROUND_CLOSE)) {
                if (!proceedCloseRoundBracket()) {
                    tryRollbackSnapshot();
                    return false;
                }
                continue;
            }

            if (currentTokenType.type().equals(Simple.OP)) {
                if (!proceedOperator()) {
                    tryRollbackSnapshot();
                    return false;
                }

                if (isInPreviewMode()) {
                    tryRollbackSnapshot();
                    return true;
                }

                continue;
            }

            if (getEmptyBufferedValue().simpleParserNode().previewQueue(context, topLevelModifier)) {
                if (!isValidContextedOperand(true)) {
                    tryRollbackSnapshot();

                    if (!expectOperand) return checkParsingResult();

                    return false;
                }

                final var value = getEmptyBufferedValue();
                removeEmptyBufferValue();

                value.simpleParserNode().proceedQueue(context, NodeModifier.prioritize(
                        topLevelModifier,
                        modifier
                ));

                expectOperand = false;
                operandStack.add(value);

                continue;
            }

            break;
        }

        if (isInPreviewMode()) {
            tryRollbackSnapshot();
            return false;
        }

        return checkParsingResult();
    }

    private boolean proceedCloseRoundBracket() {
        if (!isValidContextedOperand(false)) return false;

        var openBracketHasBeenDefined = false;

        while (!operatorStack.isEmpty()) {
            if (operatorStack.getLast().equals(Bracket.ROUND_OPEN)) {
                openBracketHasBeenDefined = true;

                operatorTokensStack.removeLast();
                operatorStack.removeLast();

                break;
            }

            applyOperator();
        }

        if (!openBracketHasBeenDefined) {
            if (!topLevelModifier.isAny(ModifierType.PREVIEW, ModifierType.PEEK))
                context.CRITICAL_MESSAGES.putSyntaxErr("inconsistent ending of round brackets");

            return false;
        }

        nestingLevel--;
        expectOperand = false;
        context.goNext();

        return true;
    }

    private void applyOperator() {
        final var operator = operatorStack.removeLast();

        if (operator instanceof Binary) {
            if (operandStack.size() < 2) throw new IllegalStateException("less than two operands for binary operator");

            final var right = operandStack.removeLast();
            final var left = operandStack.removeLast();

            operandStack.add(new BinaryExpressionObject(operatorTokensStack.removeLast(), left, right));
            return;
        }

        if (operator instanceof Unary) {
            if (operandStack.isEmpty()) throw new IllegalStateException("no operands for unary operator");

            operandStack.add(new UnaryExpressionObject(operatorTokensStack.removeLast(), operandStack.removeLast()));

            return;
        }

        throw new IllegalStateException("not applicable operator");
    }

    private boolean proceedOperator() {
        final var operator = _proceedOperator();

        if (operator == null) return false;

        while (!operatorStack.isEmpty() && !operatorStack.getLast().equals(Bracket.ROUND_OPEN)) {
            final var topOperator = (Associative) operatorStack.getLast();

            if (topOperator instanceof Unary) {
                applyOperator();
                continue;
            }

            final var isOperatorHigherByRight = operator.isRightAssociative()
                    && operator.priorityLevel() < topOperator.priorityLevel();

            final var isOperatorHigherByLeft = operator.isLeftAssociative()
                    && operator.priorityLevel() <= topOperator.priorityLevel();

            if (isOperatorHigherByRight || isOperatorHigherByLeft)
                applyOperator();
            else break;
        }

        expectOperand = true;
        addOperatorToStack(operator);
        context.goNext();

        return true;
    }

    private @Nullable Associative _proceedOperator() {
        final var currentOperatorType = context.currentOrThrow().type;

        if (currentOperatorType.equals(Binary.IS) && context.peek(
                1,
                token -> token.type.equals(Unary.NOT)
        )) return checkContextedOperator(Binary.IS_NOT, true);

        if (currentOperatorType.equals(Unary.NOT) && context.peek(
                1,
                token -> token.type.equals(Binary.IN)
        )) return checkContextedOperator(Binary.NOT_IN, true);

        if (expectOperand) {
            final var currentUnaryOperatorType = currentOperatorType instanceof ContextSensitive contextSensitive
                    ? contextSensitive.unary() : currentOperatorType;

            return checkContextedOperator(
                    (Associative)currentUnaryOperatorType,
                    currentUnaryOperatorType instanceof Binary
            );
        }

        final var currentBinaryOperatorType = currentOperatorType instanceof ContextSensitive contextSensitive
                ? contextSensitive.binary() : currentOperatorType;

        return checkContextedOperator(
                (Associative)currentBinaryOperatorType,
                currentBinaryOperatorType instanceof Binary
        );
    }

    private @Nullable Associative checkContextedOperator(
            @NotNull Associative operator,
            boolean gotBinary
    ) {
        final var expectBinary = !expectOperand;

        if (expectBinary == gotBinary) return operator;

        if (!topLevelModifier.isAny(ModifierType.PREVIEW, ModifierType.PEEK))
            context.CRITICAL_MESSAGES.putSyntaxErr(String.format(
                    "expected any %s operator, but got %s operator `%s`",
                    getOperatorTypeName(expectBinary),
                    getOperatorTypeName(gotBinary),
                    operator.content()
            ));

        return null;
    }

    private void addOperatorToStack(@NotNull TokenType tokenType) {
        operatorStack.add(tokenType);

        if (tokenType.isIn(Binary.IS_NOT, Binary.NOT_IN)) {
            operatorTokensStack.add(List.of(context.currentOrThrow(), context.goNextOrThrow()));
            return;
        }

        operatorTokensStack.add(List.of(context.currentOrThrow()));
    }

    private boolean isValidContextedOperand(boolean gotOperand) {
        if (expectOperand == gotOperand) return true;

        if (!topLevelModifier.isAny(ModifierType.PREVIEW, ModifierType.PEEK))
            context.CRITICAL_MESSAGES.putSyntaxErr("expected any binary operator");

        return false;
    }

    private @NotNull ValueExpressionObject getEmptyBufferedValue() {
        if (_emptyValueBuffer == null) _emptyValueBuffer = new ValueExpressionObject();
        return _emptyValueBuffer;
    }

    private void removeEmptyBufferValue() {
        _emptyValueBuffer = null;
    }

    private boolean isInPreviewMode() {
        return topLevelModifier.is(ModifierType.PREVIEW);
    }

    private void tryCreateSnapshot() {
        if (isInPreviewMode()) context.createIndexSnapshot();
    }

    private void tryRollbackSnapshot() {
        if (isInPreviewMode()) context.rollbackIndex();
    }

    private boolean checkParsingResult() {
        while (!operatorStack.isEmpty()) {
            if (operatorStack.getLast().equals(Bracket.ROUND_OPEN)) {
                if (!topLevelModifier.isAny(ModifierType.PREVIEW, ModifierType.PEEK))
                    context.CRITICAL_MESSAGES.putSyntaxErr("inconsistent starting of round brackets", operatorTokensStack.getLast().getFirst());

                return false;
            }

            applyOperator();
        }

        if (operandStack.size() != 1) return false;

        if (isInPreviewMode()) {
            tryRollbackSnapshot();
            return true;
        }

        if (!modifier.is(ModifierType.CONSUME))
            throw new IllegalStateException("these modifiers should contain an ast object consumer");

        modifier.consumerOrThrow().consume((AstObject) operandStack.getFirst());

        return true;
    }
    
    private boolean shouldBreakExpression() {
        if (!breakExpressionByNewLine || nestingLevel != 0) return false;
        if (context.hasCurrent() && context.currentOrThrow().type.equals(Simple.NEWLINE)) return false;

        return true;
    }

    private static @NotNull String getOperatorTypeName(boolean isOperatorBinary) {
        return isOperatorBinary ? "binary" : "unary";
    }
}
