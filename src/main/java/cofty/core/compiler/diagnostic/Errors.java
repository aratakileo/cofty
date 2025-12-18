package cofty.core.compiler.diagnostic;

import org.jetbrains.annotations.NotNull;

public enum Errors implements DiagnosticCode {
    LEXER_SYNTAX_ERROR(CodePrefix.LEXER_ERR, 1, Type.SYNTAX_ERROR, "invalid syntax"),
    PARSER_INVALID_SYNTAX(CodePrefix.PARSING_ERR, 1, Type.SYNTAX_ERROR, "invalid syntax"),
    NO_OPENED_BRACKETS(
            CodePrefix.PARSING_ERR,
            2,
            Type.SYNTAX_ERROR,
            "expected the start of {0} using the {1} bracket `{2}` here"
    ),
    UNCLOSED_BRACKETS(
            CodePrefix.PARSING_ERR,
            3,
            Type.SYNTAX_ERROR,
            "expected the end of {0} using the {1} bracket `{2}` here"
    ),
    EXPRESSION_WITHOUT_EFFECT(
            CodePrefix.PARSING_ERR,
            4,
            Type.SYNTAX_ERROR,
            "expression has no effect and cannot be used as a statement"
    ),
    NOT_ALLOWED(CodePrefix.PARSING_ERR, 5, Type.SYNTAX_ERROR, "not allowed here"),
    MISSING_SEPARATOR(
            CodePrefix.PARSING_ERR,
            6,
            Type.SYNTAX_ERROR,
            "expected a {0} separator here between the {1}"
    ),
    UNEXPECTED_SEPARATOR(
            CodePrefix.PARSING_ERR,
            7,
            Type.SYNTAX_ERROR,
            "expected {0} here, not the {1} separator"
    ),
    EXPECTED_NAME(CodePrefix.PARSING_ERR, 8, Type.SYNTAX_ERROR, "expected a {0} name here"),
    EXPECTED_FIELD_TYPE(
            CodePrefix.PARSING_ERR,
            9,
            Type.SYNTAX_ERROR,
            "expected a {0} value type here"
    ),
    EXPECTED_FUNC_RETURN_TYPE(
            CodePrefix.PARSING_ERR,
            10,
            Type.SYNTAX_ERROR,
            "expected a function return type here"
    ),
    EXPECTED_FUNC_RETURN_STATEMENT(
            CodePrefix.PARSING_ERR,
            11,
            Type.SYNTAX_ERROR,
            "expected the return statement"
    ),
    EXPECTED_ASSIGNMENT_TARGET(
            CodePrefix.PARSING_ERR,
            12,
            Type.SYNTAX_ERROR,
            "expected specified either the {0} value type or the {0} value itself"
    ),
    EXPECTED_ASSIGNABLE_FIELD(
            CodePrefix.PARSING_ERR,
            13,
            Type.SYNTAX_ERROR,
            "expected an assignable field, not the {0}"
    ),
    EXPECTED_ASSIGNABLE_VALUE(
            CodePrefix.PARSING_ERR,
            14,
            Type.SYNTAX_ERROR,
            "expected an assignable value here"
    ),
    MISSING_EXPLICIT_TYPE(
            CodePrefix.PARSING_ERR,
            15,
            Type.SYNTAX_ERROR,
            "expected an explicit specification of the {0} value type here, " +
                    "since the assigned value is not constant"
    ),
    EXPECTED_MEMBER_ACCESS(
            CodePrefix.PARSING_ERR,
            16,
            Type.SYNTAX_ERROR,
            "expected a field access or a function call here"
    ),
    DISALLOWED_OPERATOR_BEFORE_ASSIGN(
            CodePrefix.PARSING_ERR,
            17,
            Type.SYNTAX_ERROR,
            "operator `{0}` is not allowed here before the assignment"
    ),
    UNCLOSED_PARENTHESIS(
            CodePrefix.PARSING_ERR,
            18,
            Type.SYNTAX_ERROR,
            "parenthesis opened here but never closed"
    ),
    EXPECTED_BINARY_OPERATOR(
            CodePrefix.PARSING_ERR,
            19,
            Type.SYNTAX_ERROR,
            "expected a binary operator, not an opening parenthesis"
    ),
    EXPECTED_OPERAND_NOT_CLOSING_PARENT(
            CodePrefix.PARSING_ERR,
            20,
            Type.SYNTAX_ERROR,
            "expected an operand, not a closing parenthesis"
    ),
    INVALID_OPERATOR_FOR_CONTEXT(
            CodePrefix.PARSING_ERR,
            21,
            Type.SYNTAX_ERROR,
            "expected a contextually appropriate {0} operator, not the {1} operator `{2}`"
    ),
    EXPECTED_OPERAND(
            CodePrefix.PARSING_ERR,
            22,
            Type.SYNTAX_ERROR,
            "expected an operand"
    ),
    REQUIRED_ARGUMENT_FOLLOWS_OPTIONAL(
            CodePrefix.PARSING_ERR,
            23,
            Type.SYNTAX_ERROR,
            "non-default (required) parameter `{0}` cannot follow a parameter with a default value"
    ),
    DUPLICATE_NAME(
            CodePrefix.SEMANTIC_ERR,
            1,
            Type.DECLARATION_ERROR,
            "this name `{0}` is already defined as {1} (see line {2})"
    ),
    DUPLICATE_FUNC_SIGNATURE(
            CodePrefix.SEMANTIC_ERR,
            2,
            Type.DECLARATION_ERROR,
            "the function `{0}` with arguments signature `{1}` has been already defined (see line {2})"
    ),
    NOT_A_TYPE(CodePrefix.SEMANTIC_ERR, 3, Type.TYPE_ERROR, "not a type"),
    NOT_A_VALUE(CodePrefix.SEMANTIC_ERR, 4, Type.VALUE_ERROR, "not a value (expected {0}, got {1})"),
    UNRESOLVED_REFERENCE(CodePrefix.SEMANTIC_ERR, 5, Type.NAME_ERROR, "does not exist"),
    INCOMPATIBLE_TYPES(
            CodePrefix.SEMANTIC_ERR,
            6,
            Type.TYPE_ERROR,
            "expected `{0}` value, but got `{1}` value"
    ),
    IMMUTABLE_REASSIGNMENT(
            CodePrefix.SEMANTIC_ERR,
            7,
            Type.MUTABILITY_ERROR,
            "cannot assign a new value to an immutable {0} `{1}` " +
                    "(consider declaring it with the `mut` keyword at line {2})"
    ),
    UNINITIALIZED_FIELD_ACCESS(
            CodePrefix.SEMANTIC_ERR,
            8,
            Type.VALUE_ERROR,
            "{0} `{1}` might not have been initialized before access"
    ),
    UNEXPECTED_RETURN_VALUE(
            CodePrefix.SEMANTIC_ERR,
            9,
            Type.RETURN_ERROR,
            "the function `{0}` is declared as non-returning (null), " +
                    "but a value expression was found after `return` " +
                    "(use just `return` instead or remove `return` statement)"
    ),
    MISSING_RETURN_VALUE(
            CodePrefix.SEMANTIC_ERR,
            10,
            Type.RETURN_ERROR,
            "the function `{0}` must return a value of type `{1}`, " +
                    "but `return` statement is missing an expression"
    ),
    RETURN_TYPE_MISMATCH(
            CodePrefix.SEMANTIC_ERR,
            11,
            Type.RETURN_ERROR,
            "function `{0}` must return a value of type `{1}`, but the provided value is of type `{2}`"
    ),
    ARGUMENTS_COUNT_MISMATCH(
            CodePrefix.SEMANTIC_ERR,
            12,
            Type.ARGUMENTS_ERROR,
            "function `{0}` expects {1} arguments, but {2} were passed"
    ),
    NOT_ENOUGH_ARGUMENTS(
            CodePrefix.SEMANTIC_ERR,
            13,
            Type.ARGUMENTS_ERROR,
            "function `{0}` expects at least {1} arguments, but {2} were provided"
    ),
    ARGUMENT_TYPE_MISMATCH(
            CodePrefix.SEMANTIC_ERR,
            14,
            Type.ARGUMENTS_ERROR,
            "incompatible type for argument #{0} of function `{1}`: expected `{2}`, but got `{3}`"
    );

    private final CodePrefix prefix;
    private final int codeNum;
    private final Type type;
    private final String placeholder;

    Errors(@NotNull CodePrefix prefix, int codeNum, @NotNull Type type, @NotNull String placeholder) {
        this.prefix = prefix;
        this.codeNum = codeNum;
        this.type = type;
        this.placeholder = placeholder;

        if (prefix.severity != DiagnosticMsg.Severity.ERR)
            throw new IllegalStateException();
    }

    @Override
    public @NotNull CodePrefix prefix() {
        return prefix;
    }

    @Override
    public int num() {
        return codeNum;
    }

    @Override
    public @NotNull String placeholder() {
        return placeholder;
    }

    public @NotNull Type type() {
        return type;
    }

    public enum Type {
        SYNTAX_ERROR,
        NAME_ERROR,
        TYPE_ERROR,
        VALUE_ERROR,
        MUTABILITY_ERROR,
        DECLARATION_ERROR,
        RETURN_ERROR,
        ARGUMENTS_ERROR
    }
}
